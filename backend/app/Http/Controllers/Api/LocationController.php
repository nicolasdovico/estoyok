<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Jobs\ProcessGeofencing;
use App\Jobs\SendBatteryAlertJob;
use App\Models\CurrentLocation;
use App\Models\LocationHistory;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;

class LocationController extends Controller
{
    /**
     * @OA\Post(
     *     path="/api/locations/update",
     *     summary="Update user's current location",
     *     tags={"Location"},
     *     security={{"sanctum":{}}},
     *
     *     @OA\RequestBody(
     *         required=true,
     *
     *         @OA\JsonContent(
     *             required={"latitude", "longitude"},
     *
     *             @OA\Property(property="latitude", type="number", format="float", example=-34.6037),
     *             @OA\Property(property="longitude", type="number", format="float", example=-58.3816),
     *             @OA\Property(property="accuracy", type="number", format="float", example=15.5),
     *             @OA\Property(property="battery_level", type="number", format="float", example=0.85, nullable=true)
     *         )
     *     ),
     *
     *     @OA\Response(response=200, description="Location updated successfully"),
     *     @OA\Response(response=401, description="Unauthenticated")
     * )
     */
    public function update(Request $request)
    {
        $request->validate([
            'latitude' => 'required|numeric',
            'longitude' => 'required|numeric',
            'accuracy' => 'nullable|numeric',
            'battery_level' => 'nullable|numeric|between:0,1',
        ]);

        $user = $request->user();
        $lat = $request->latitude;
        $lng = $request->longitude;
        $accuracy = $request->accuracy;
        $batteryLevel = $request->battery_level;
        $recordedAt = now();

        $previousIsBatteryLow = false;
        $currentLocation = CurrentLocation::where('user_id', $user->id)->first();
        if ($currentLocation) {
            $previousIsBatteryLow = (bool) $currentLocation->is_battery_low;
        }

        $isBatteryLow = ($batteryLevel !== null && $batteryLevel < 0.15);

        try {
            DB::transaction(function () use ($user, $lat, $lng, $accuracy, $recordedAt, $batteryLevel, $isBatteryLow, $previousIsBatteryLow) {
                // 1. Update Current Location (Point PostGIS)
                $updateData = [
                    'accuracy' => $accuracy,
                    'recorded_at' => $recordedAt,
                    'location' => DB::raw("ST_GeomFromText('POINT($lng $lat)', 4326)"),
                ];

                if ($batteryLevel !== null) {
                    $updateData['battery_level'] = $batteryLevel;
                    $updateData['is_battery_low'] = $isBatteryLow;
                }

                CurrentLocation::updateOrCreate(
                    ['user_id' => $user->id],
                    $updateData
                );

                // 2. Add to History
                LocationHistory::create([
                    'user_id' => $user->id,
                    'accuracy' => $accuracy,
                    'recorded_at' => $recordedAt,
                    'location' => DB::raw("ST_GeomFromText('POINT($lng $lat)', 4326)"),
                ]);

                // 3. Dispatch low battery alert if transitioning to low battery
                if ($batteryLevel !== null && $isBatteryLow && !$previousIsBatteryLow && $user->low_battery_alerts_enabled) {
                    $oneHourAgo = now()->subHour();
                    if (is_null($user->last_battery_alert_sent_at) || $user->last_battery_alert_sent_at->lt($oneHourAgo)) {
                        SendBatteryAlertJob::dispatch($user, $batteryLevel);
                        $user->update(['last_battery_alert_sent_at' => now()]);
                    }
                }

                // 4. Dispatch Geofencing processing
                ProcessGeofencing::dispatch($user, $lat, $lng);
            });

            return response()->json(['message' => 'Location updated']);
        } catch (\Exception $e) {
            Log::error("Failed to update location for user {$user->id}: ".$e->getMessage());

            return response()->json(['error' => 'Failed to update location'], 500);
        }
    }
}

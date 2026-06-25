<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Jobs\ProcessGeofencing;
use App\Jobs\ProcessDynamicGeofencing;
use App\Jobs\SendBatteryAlertJob;
use App\Jobs\SendSpeedingAlertJob;
use App\Models\CurrentLocation;
use App\Models\LocationHistory;
use App\Models\DriveEvent;
use App\Models\User;
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
     *             @OA\Property(property="battery_level", type="number", format="float", example=0.85, nullable=true),
     *             @OA\Property(property="speed", type="number", format="float", example=8.5, nullable=true),
     *             @OA\Property(property="is_driving", type="boolean", example=false, nullable=true)
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
            'is_tracking_active' => 'nullable|boolean',
            'gps_enabled' => 'nullable|boolean',
            'recorded_at' => 'nullable|date',
            'speed' => 'nullable|numeric',
            'is_driving' => 'nullable|boolean',
        ]);

        $user = $request->user();
        $lat = $request->latitude;
        $lng = $request->longitude;
        $accuracy = $request->accuracy;
        $batteryLevel = $request->battery_level;
        $recordedAt = $request->has('recorded_at') ? \Carbon\Carbon::parse($request->recorded_at) : now();

        $previousIsBatteryLow = false;
        $currentLocation = CurrentLocation::where('user_id', $user->id)->first();
        if ($currentLocation) {
            $previousIsBatteryLow = (bool) $currentLocation->is_battery_low;
        }

        $isBatteryLow = ($batteryLevel !== null && $batteryLevel < 0.15);

        try {
            DB::transaction(function () use ($user, $lat, $lng, $accuracy, $recordedAt, $batteryLevel, $isBatteryLow, $previousIsBatteryLow, $request) {
                $speedMps = $request->speed;
                $speedKmh = $speedMps !== null ? $speedMps * 3.6 : null;
                $isDriving = $request->has('is_driving') ? (bool) $request->is_driving : false;

                // 1. Update Current Location (Point PostGIS)
                $updateData = [
                    'accuracy' => $accuracy,
                    'recorded_at' => $recordedAt,
                    'location' => DB::raw("ST_GeomFromText('POINT($lng $lat)', 4326)"),
                    'last_seen_at' => $recordedAt,
                    'speed' => $speedKmh,
                    'is_driving' => $isDriving,
                ];

                if ($batteryLevel !== null) {
                    $updateData['battery_level'] = $batteryLevel;
                    $updateData['is_battery_low'] = $isBatteryLow;
                }

                if ($request->has('is_tracking_active')) {
                    $updateData['is_tracking_active'] = (bool) $request->is_tracking_active;
                }
                if ($request->has('gps_enabled')) {
                    $updateData['gps_enabled'] = (bool) $request->gps_enabled;
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
                    'speed' => $speedKmh,
                    'is_driving' => $isDriving,
                ]);

                // 3. Dispatch low battery alert if transitioning to low battery
                if ($batteryLevel !== null && $isBatteryLow && !$previousIsBatteryLow && $user->low_battery_alerts_enabled) {
                    $oneHourAgo = now()->subHour();
                    if (is_null($user->last_battery_alert_sent_at) || $user->last_battery_alert_sent_at->lt($oneHourAgo)) {
                        SendBatteryAlertJob::dispatch($user, $batteryLevel);
                        $user->update(['last_battery_alert_sent_at' => now()]);
                    }
                }

                // 4. Drive Event Processing
                if ($isDriving) {
                    $activeDriveEvent = DriveEvent::where('user_id', $user->id)
                        ->whereNull('end_time')
                        ->first();

                    $exceeded = false;
                    $minLimit = $user->circles()->min('speed_limit') ?? 120;
                    if ($speedKmh !== null && $speedKmh > $minLimit) {
                        $exceeded = true;

                        // Notificar a los dueños de los círculos
                        $user->load('circles');
                        foreach ($user->circles as $circle) {
                            $limit = $circle->speed_limit ?? 120;
                            if ($speedKmh > $limit && $circle->owner_id) {
                                $cacheKey = "speeding_alert_{$user->id}_{$circle->id}";
                                if (!cache()->has($cacheKey)) {
                                    $circleOwner = User::find($circle->owner_id);
                                    if ($circleOwner && $circleOwner->id !== $user->id) {
                                        SendSpeedingAlertJob::dispatch($circleOwner, $user, $speedKmh, $limit);
                                    }
                                    cache()->put($cacheKey, true, now()->addMinutes(15));
                                }
                            }
                        }
                    }

                    if (!$activeDriveEvent) {
                        DriveEvent::create([
                            'user_id' => $user->id,
                            'start_time' => $recordedAt,
                            'max_speed' => $speedKmh ?? 0.0,
                            'exceeded_speed_limit' => $exceeded,
                        ]);
                    } else {
                        $updatePayload = [];
                        if ($speedKmh !== null && $speedKmh > $activeDriveEvent->max_speed) {
                            $updatePayload['max_speed'] = $speedKmh;
                        }
                        if ($exceeded) {
                            $updatePayload['exceeded_speed_limit'] = true;
                        }

                        if (!empty($updatePayload)) {
                            $activeDriveEvent->update($updatePayload);
                        }
                    }
                } else {
                    // Cerrar el trayecto activo si existe
                    $activeDriveEvent = DriveEvent::where('user_id', $user->id)
                        ->whereNull('end_time')
                        ->first();
                    if ($activeDriveEvent) {
                        $activeDriveEvent->update([
                            'end_time' => $recordedAt,
                        ]);
                    }
                }

                // 5. Dispatch Geofencing processing
                ProcessGeofencing::dispatch($user, $lat, $lng);
                ProcessDynamicGeofencing::dispatch($user, $lat, $lng);
            });

            $activeGeofenceExists = \App\Models\DynamicGeofence::active()
                ->where(function ($query) use ($user) {
                    $query->where('initiator_id', $user->id)
                          ->orWhere('target_id', $user->id);
                })
                ->exists();

            return response()->json([
                'message' => 'Location updated',
                'active_dynamic_geofence' => $activeGeofenceExists,
            ]);
        } catch (\Exception $e) {
            Log::error("Failed to update location for user {$user->id}: ".$e->getMessage());

            return response()->json(['error' => 'Failed to update location'], 500);
        }
    }

    /**
     * @OA\Put(
     *     path="/api/locations/sensor-status",
     *     summary="Actualizar estado de sensores (GPS, rastreo activo)",
     *     tags={"Location"},
     *     security={{"sanctum":{}}},
     *     @OA\RequestBody(
     *         required=true,
     *         @OA\JsonContent(
     *             properties={
     *                 @OA\Property(property="is_tracking_active", type="boolean", example=true),
     *                 @OA\Property(property="gps_enabled", type="boolean", example=true)
     *             }
     *         )
     *     ),
     *     @OA\Response(response=200, description="Estado de sensores actualizado exitosamente"),
     *     @OA\Response(response=401, description="Unauthenticated")
     * )
     */
    public function updateSensorStatus(Request $request)
    {
        $validated = $request->validate([
            'is_tracking_active' => 'sometimes|boolean',
            'gps_enabled' => 'sometimes|boolean',
        ]);

        $user = $request->user();
        $recordedAt = now();

        $updateData = [
            'last_seen_at' => $recordedAt,
            'recorded_at' => $recordedAt,
        ];

        if ($request->has('is_tracking_active')) {
            $updateData['is_tracking_active'] = (bool) $request->is_tracking_active;
        }

        if ($request->has('gps_enabled')) {
            $updateData['gps_enabled'] = (bool) $request->gps_enabled;
        }

        $currentLocation = CurrentLocation::updateOrCreate(
            ['user_id' => $user->id],
            $updateData
        );

        return response()->json([
            'message' => 'Sensor status updated successfully',
            'current_location' => $currentLocation,
        ]);
    }
}

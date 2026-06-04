<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\CurrentLocation;
use App\Models\LocationHistory;
use App\Jobs\ProcessGeofencing;
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
     *     @OA\RequestBody(
     *         required=true,
     *         @OA\JsonContent(
     *             required={"latitude", "longitude"},
     *             @OA\Property(property="latitude", type="number", format="float", example=-34.6037),
     *             @OA\Property(property="longitude", type="number", format="float", example=-58.3816),
     *             @OA\Property(property="accuracy", type="number", format="float", example=15.5)
     *         )
     *     ),
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
        ]);

        $user = $request->user();
        $lat = $request->latitude;
        $lng = $request->longitude;
        $accuracy = $request->accuracy;
        $recordedAt = now();

        try {
            DB::transaction(function () use ($user, $lat, $lng, $accuracy, $recordedAt) {
                // 1. Update Current Location (Point PostGIS)
                CurrentLocation::updateOrCreate(
                    ['user_id' => $user->id],
                    [
                        'accuracy' => $accuracy,
                        'recorded_at' => $recordedAt,
                        'location' => DB::raw("ST_GeomFromText('POINT($lng $lat)', 4326)")
                    ]
                );

                // 2. Add to History
                LocationHistory::create([
                    'user_id' => $user->id,
                    'accuracy' => $accuracy,
                    'recorded_at' => $recordedAt,
                    'location' => DB::raw("ST_GeomFromText('POINT($lng $lat)', 4326)")
                ]);
                
                // 3. Dispatch Geofencing processing
                ProcessGeofencing::dispatch($user, $lat, $lng);
            });

            return response()->json(['message' => 'Location updated']);
        } catch (\Exception $e) {
            Log::error("Failed to update location for user {$user->id}: " . $e->getMessage());
            return response()->json(['error' => 'Failed to update location'], 500);
        }
    }
}

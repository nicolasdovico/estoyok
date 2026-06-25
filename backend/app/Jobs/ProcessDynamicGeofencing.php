<?php

namespace App\Jobs;

use App\Models\DynamicGeofence;
use App\Models\User;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class ProcessDynamicGeofencing implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    /**
     * Create a new job instance.
     */
    public function __construct(
        public User $user,
        public float $latitude,
        public float $longitude
    ) {}

    /**
     * Execute the job.
     */
    public function handle(): void
    {
        // Find active dynamic geofences where this user is initiator or target
        $activeGeofences = DynamicGeofence::active()
            ->where(function ($query) {
                $query->where('initiator_id', $this->user->id)
                      ->orWhere('target_id', $this->user->id);
            })
            ->get();

        if ($activeGeofences->isEmpty()) {
            return;
        }

        foreach ($activeGeofences as $geofence) {
            // Check if both users have current location records
            $initiatorLocExists = DB::table('current_locations')->where('user_id', $geofence->initiator_id)->exists();
            $targetLocExists = DB::table('current_locations')->where('user_id', $geofence->target_id)->exists();

            if (!$initiatorLocExists || !$targetLocExists) {
                continue;
            }

            // Calculate geodetic distance in meters using PostGIS
            $distanceResult = DB::selectOne(
                'SELECT ST_Distance(l1.location::geography, l2.location::geography) as dist 
                 FROM current_locations l1, current_locations l2 
                 WHERE l1.user_id = ? AND l2.user_id = ?',
                [$geofence->initiator_id, $geofence->target_id]
            );

            if (!$distanceResult || $distanceResult->dist === null) {
                continue;
            }

            $dist = (float) $distanceResult->dist;
            $cacheKey = "dynamic_geofence_breached_{$geofence->id}";
            $initiator = User::find($geofence->initiator_id);
            $target = User::find($geofence->target_id);

            if (!$initiator || !$target) {
                continue;
            }

            if ($dist > $geofence->safe_radius_meters) {
                // Breached state
                if (!cache()->has($cacheKey)) {
                    Log::info("Dynamic geofence breached: {$target->name} is {$dist}m away from {$initiator->name} (Limit: {$geofence->safe_radius_meters}m)");
                    
                    if ($initiator->expo_push_token) {
                        try {
                            Http::post('https://exp.host/--/api/v2/push/send', [
                                'to' => $initiator->expo_push_token,
                                'title' => '🚨 Alerta de Proximidad',
                                'body' => "¡Atención! {$target->name} se ha alejado demasiado (" . round($dist) . "m)",
                                'sound' => 'default',
                                'priority' => 'high',
                                'channelId' => 'emergency',
                                'data' => [
                                    'type' => 'dynamic_geofence_breached',
                                    'geofence_id' => $geofence->id,
                                    'distance' => $dist,
                                    'target_name' => $target->name,
                                ],
                            ]);
                        } catch (\Exception $e) {
                            Log::error("Failed to send dynamic geofence push: " . $e->getMessage());
                        }
                    }
                    // Keep breached state in cache for rate limiting (15 minutes)
                    cache()->put($cacheKey, true, now()->addMinutes(15));
                }
            } else {
                // Restored state
                if (cache()->has($cacheKey)) {
                    Log::info("Dynamic geofence restored: {$target->name} returned near {$initiator->name} ({$dist}m)");

                    if ($initiator->expo_push_token) {
                        try {
                            Http::post('https://exp.host/--/api/v2/push/send', [
                                'to' => $initiator->expo_push_token,
                                'title' => '🛡️ Proximidad Restablecida',
                                'body' => "{$target->name} ha regresado a la zona de seguridad (" . round($dist) . "m)",
                                'sound' => 'default',
                                'priority' => 'high',
                                'data' => [
                                    'type' => 'dynamic_geofence_restored',
                                    'geofence_id' => $geofence->id,
                                    'distance' => $dist,
                                ],
                            ]);
                        } catch (\Exception $e) {
                            Log::error("Failed to send dynamic geofence restored push: " . $e->getMessage());
                        }
                    }
                    cache()->forget($cacheKey);
                }
            }
        }
    }
}

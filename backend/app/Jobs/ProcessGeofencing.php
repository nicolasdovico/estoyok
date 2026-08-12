<?php

namespace App\Jobs;

use App\Models\Geofence;
use App\Models\GeofenceEvent;
use App\Models\User;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class ProcessGeofencing implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    /**
     * Create a new job instance.
     */
    /**
     * Create a new job instance.
     */
    public function __construct(
        public User $user,
        public float $latitude,
        public float $longitude,
        public ?float $accuracy = null,
        public ?string $currentWifiSsid = null,
        public ?float $speedKmh = null
    ) {}

    /**
     * Execute the job.
     */
    public function handle(): void
    {
        $circleIds = $this->user->circles()->pluck('circles.id');

        if ($circleIds->isEmpty()) {
            return;
        }

        $allGeofences = Geofence::whereIn('circle_id', $circleIds)
            ->where('is_active', true)
            ->where(function ($query) {
                $query->whereNull('user_id')
                      ->orWhere('user_id', $this->user->id);
            })
            ->get();

        foreach ($allGeofences as $geofence) {
            $distResult = DB::selectOne(
                'SELECT ST_Distance(center, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography) as distance FROM geofences WHERE id = ?',
                [$this->longitude, $this->latitude, $geofence->id]
            );

            $distanceMeters = $distResult ? (float) $distResult->distance : 9999999.0;

            $lastEvent = GeofenceEvent::where('user_id', $this->user->id)
                ->where('geofence_id', $geofence->id)
                ->latest('occurred_at')
                ->first();

            $lastType = $lastEvent ? $lastEvent->type : 'exit';

            // Safe Wi-Fi Suppression: If user is connected to their designated safe Wi-Fi SSID, any exit is suppressed
            $isSafeWifiConnected = !empty($this->user->safe_wifi_ssid) && 
                !empty($this->currentWifiSsid) && 
                strcasecmp(trim($this->user->safe_wifi_ssid), trim($this->currentWifiSsid)) === 0;

            $pendingExitKey = "geofence_pending_exit_{$this->user->id}_{$geofence->id}";

            if ($isSafeWifiConnected) {
                // If safe wifi is connected, clear any pending exit and ignore exit attempts
                if (cache()->has($pendingExitKey)) {
                    cache()->forget($pendingExitKey);
                }
                if ($lastType === 'exit') {
                    // Record entry if last type was exit
                    $this->recordEvent($geofence, 'entry');
                    $this->sendGeofenceAlert($geofence, 'ingresado a');
                }
                continue;
            }

            // Cooldown check: Require at least 3 minutes between state transition alerts for the same geofence
            $cooldownActive = $lastEvent && $lastEvent->occurred_at && $lastEvent->occurred_at->diffInMinutes(now()) < 3;
            if ($cooldownActive) {
                continue;
            }

            // Life360 Hysteresis Dual-Threshold Buffer with Accuracy Padding:
            $radius = (float) $geofence->radius;
            $accuracyPadding = max(0.0, (float) ($this->accuracy ?? 0.0) - 15.0);
            $hysteresisBuffer = max(35.0, $radius * 0.25) + $accuracyPadding;
            $exitThreshold = $radius + $hysteresisBuffer;

            if ($distanceMeters <= $radius) {
                // User is INSIDE geofence
                if (cache()->has($pendingExitKey)) {
                    // Cancel pending exit on GPS rebound inside
                    Log::info("Geofence exit pending cancelled for user {$this->user->name} (GPS rebound inside {$geofence->name})");
                    cache()->forget($pendingExitKey);
                }

                if ($lastType === 'exit') {
                    $this->recordEvent($geofence, 'entry');
                    $this->sendGeofenceAlert($geofence, 'ingresado a');
                }
            } elseif ($distanceMeters > $exitThreshold && ($lastType === 'entry' || $lastEvent === null)) {
                // User is OUTSIDE geofence
                // If high-speed driving (>= 15 km/h) or very large distance (> 5x radius or > 10km), exit is confirmed immediately
                $isFastMoving = ($this->speedKmh !== null && $this->speedKmh >= 15.0) || $distanceMeters > max(2000.0, $radius * 5);

                if ($isFastMoving) {
                    cache()->forget($pendingExitKey);
                    $this->recordEvent($geofence, 'exit');
                    $this->sendGeofenceAlert($geofence, 'salido de');
                } else {
                    // Dwell Time Confirmation Window (Pending Exit)
                    if (!cache()->has($pendingExitKey)) {
                        cache()->put($pendingExitKey, now()->timestamp, now()->addMinutes(10));
                        Log::info("Geofence exit pending for user {$this->user->name} at geofence {$geofence->name} ({$distanceMeters}m)");
                    } else {
                        $firstSeenTimestamp = (int) cache()->get($pendingExitKey);
                        $elapsedSeconds = now()->timestamp - $firstSeenTimestamp;

                        // Confirm exit after 2 minutes (120 seconds) of sustained coordinates outside
                        if ($elapsedSeconds >= 120) {
                            cache()->forget($pendingExitKey);
                            $this->recordEvent($geofence, 'exit');
                            $this->sendGeofenceAlert($geofence, 'salido de');
                        }
                    }
                }
            }
        }
    }

    protected function recordEvent(Geofence $geofence, string $type)
    {
        GeofenceEvent::create([
            'user_id' => $this->user->id,
            'geofence_id' => $geofence->id,
            'type' => $type,
            'occurred_at' => now(),
        ]);
    }

    protected function sendGeofenceAlert(Geofence $geofence, string $action)
    {
        Log::info("User {$this->user->name} {$action} geofence: {$geofence->name}");

        $userPushToken = $this->user->expo_push_token;
        if (!empty($userPushToken)) {
            User::where('expo_push_token', $userPushToken)
                ->where('id', '!=', $this->user->id)
                ->update(['expo_push_token' => null]);
        }

        $sentTokens = [];
        if (!empty($userPushToken)) {
            $sentTokens[$userPushToken] = true;
        }

        $members = $geofence->circle->users()->where('users.id', '!=', $this->user->id)->get();

        foreach ($members as $member) {
            $tokens = [];
            if (!empty($member->expo_push_token)) {
                $tokens[] = $member->expo_push_token;
            }
            $deviceTokens = DB::table('user_devices')
                ->where('user_id', $member->id)
                ->where('is_active', true)
                ->whereNotNull('push_token')
                ->pluck('push_token')
                ->toArray();
            $tokens = array_unique(array_merge($tokens, $deviceTokens));

            foreach ($tokens as $token) {
                if (!empty($token) && !isset($sentTokens[$token])) {
                    $sentTokens[$token] = true;
                    app(\App\Services\PushNotificationService::class)->sendPush(
                        $token,
                        'Alerta de Perímetro',
                        "{$this->user->name} ha {$action}: {$geofence->name}",
                        [
                            'type' => 'geofence_alert',
                            'geofence_id' => (string) $geofence->id,
                            'event' => $action == 'ingresado a' ? 'entry' : 'exit',
                        ],
                        true
                    );
                }
            }
        }
    }
}

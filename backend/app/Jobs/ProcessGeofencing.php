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

        $pointWkt = "POINT({$this->longitude} {$this->latitude})";

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

            // Cooldown check: Require at least 3 minutes between state transition alerts for the same geofence
            $cooldownActive = $lastEvent && $lastEvent->occurred_at && $lastEvent->occurred_at->diffInMinutes(now()) < 3;
            if ($cooldownActive) {
                continue;
            }

            // Life360 Hysteresis Dual-Threshold Buffer:
            // Entry threshold: distance <= radius
            // Exit threshold: distance > (radius + hysteresisBuffer)
            $radius = (float) $geofence->radius;
            $hysteresisBuffer = max(30.0, $radius * 0.3); // 30 meters or 30% of radius

            if ($distanceMeters <= $radius && $lastType === 'exit') {
                $this->recordEvent($geofence, 'entry');
                $this->sendGeofenceAlert($geofence, 'ingresado a');
            } elseif ($distanceMeters > ($radius + $hysteresisBuffer) && $lastType === 'entry') {
                $this->recordEvent($geofence, 'exit');
                $this->sendGeofenceAlert($geofence, 'salido de');
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

        $members = $geofence->circle->users()->where('users.id', '!=', $this->user->id)->get();

        foreach ($members as $member) {
            if ($member->expo_push_token) {
                app(\App\Services\PushNotificationService::class)->sendPush(
                    $member->expo_push_token,
                    'Alerta de Perímetro',
                    "{$this->user->name} ha {$action}: {$geofence->name}",
                    [
                        'type' => 'geofence_alert',
                        'geofence_id' => (string) $geofence->id,
                        'event' => $action == 'ingresado a' ? 'entry' : 'exit',
                    ]
                );
            }
        }
    }
}

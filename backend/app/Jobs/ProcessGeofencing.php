<?php

namespace App\Jobs;

use App\Models\User;
use App\Models\Geofence;
use App\Models\GeofenceEvent;
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
            ->get();

        $pointWkt = "POINT({$this->longitude} {$this->latitude})";

        foreach ($allGeofences as $geofence) {
            $isInside = DB::selectOne(
                "SELECT ST_DWithin(center, ST_GeomFromText(?, 4326), ?) as inside FROM geofences WHERE id = ?",
                [$pointWkt, $geofence->radius, $geofence->id]
            )->inside;

            $lastEvent = GeofenceEvent::where('user_id', $this->user->id)
                ->where('geofence_id', $geofence->id)
                ->latest('occurred_at')
                ->first();

            $lastType = $lastEvent ? $lastEvent->type : 'exit';

            if ($isInside && $lastType === 'exit') {
                $this->recordEvent($geofence, 'entry');
                $this->sendGeofenceAlert($geofence, 'ingresado a');
            } elseif (!$isInside && $lastType === 'entry') {
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
                Http::post('https://exp.host/--/api/v2/push/send', [
                    'to' => $member->expo_push_token,
                    'title' => 'Alerta de Perímetro',
                    'body' => "{$this->user->name} ha {$action}: {$geofence->name}",
                    'data' => [
                        'type' => 'geofence_alert', 
                        'geofence_id' => $geofence->id,
                        'event' => $action == 'ingresado a' ? 'entry' : 'exit'
                    ],
                ]);
            }
        }
    }
}

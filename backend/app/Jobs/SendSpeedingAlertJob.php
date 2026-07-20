<?php

namespace App\Jobs;

use App\Models\User;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class SendSpeedingAlertJob implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    public $owner;
    public $user;
    public $speed;
    public $limit;

    /**
     * Create a new job instance.
     */
    public function __construct(User $owner, User $user, float $speed, int $limit)
    {
        $this->owner = $owner;
        $this->user = $user;
        $this->speed = $speed;
        $this->limit = $limit;
    }

    /**
     * Execute the job.
     */
    public function handle(): void
    {
        Log::info("Processing speeding alert for user {$this->user->name} ({$this->user->id}) to owner {$this->owner->name} ({$this->owner->id}), speed: {$this->speed} km/h (limit: {$this->limit} km/h)");

        if ($this->owner->expo_push_token) {
            $roundedSpeed = round($this->speed);
            app(\App\Services\PushNotificationService::class)->sendPush(
                $this->owner->expo_push_token,
                '🚨 Exceso de velocidad detectado',
                "{$this->user->name} está viajando en coche a {$roundedSpeed} km/h, superando el límite de {$this->limit} km/h.",
                [
                    'type' => 'speeding_alert',
                    'user_id' => (string) $this->user->id,
                    'speed' => (string) $this->speed,
                    'limit' => (string) $this->limit,
                ]
            );
        }
    }
}

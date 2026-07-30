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

class SendBatteryAlertJob implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    public $user;
    public $batteryLevel;

    /**
     * Create a new job instance.
     */
    public function __construct(User $user, float $batteryLevel)
    {
        $this->user = $user;
        $this->batteryLevel = $batteryLevel;
    }

    /**
     * Execute the job.
     */
    public function handle(): void
    {
        $this->user->load('circles.users');

        Log::info("Processing low battery alert for user {$this->user->name} ({$this->user->id}), battery level: " . ($this->batteryLevel * 100) . "%");

        // Get unique members of all circles that this user is in (excluding themselves)
        $memberIds = [];
        foreach ($this->user->circles as $circle) {
            foreach ($circle->users as $member) {
                if ($member->id !== $this->user->id) {
                    $memberIds[$member->id] = $member;
                }
            }
        }

        $percentage = round($this->batteryLevel * 100);

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

        foreach ($memberIds as $member) {
            $memberToken = $member->expo_push_token;
            if (!empty($memberToken) && !isset($sentTokens[$memberToken])) {
                $sentTokens[$memberToken] = true;
                app(\App\Services\PushNotificationService::class)->sendPush(
                    $memberToken,
                    '🔋 Batería baja de un miembro',
                    "La batería de {$this->user->name} está baja ({$percentage}%).",
                    [
                        'type' => 'low_battery_alert',
                        'user_id' => (string) $this->user->id,
                        'battery_level' => (string) $this->batteryLevel,
                    ]
                );
            }
        }
    }
}

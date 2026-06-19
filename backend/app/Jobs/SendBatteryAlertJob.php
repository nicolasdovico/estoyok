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

        foreach ($memberIds as $member) {
            if ($member->expo_push_token) {
                try {
                    $response = Http::post('https://exp.host/--/api/v2/push/send', [
                        'to' => $member->expo_push_token,
                        'title' => '🔋 Batería baja de un miembro',
                        'body' => "La batería de {$this->user->name} está baja ({$percentage}%).",
                        'data' => [
                            'type' => 'low_battery_alert',
                            'user_id' => $this->user->id,
                            'battery_level' => $this->batteryLevel,
                        ],
                    ]);

                    if ($response->failed()) {
                        Log::warning("Expo Push Notification failed for low battery alert to user {$member->id}: " . $response->body());
                    }
                } catch (\Exception $e) {
                    Log::error("Failed to send low battery push notification to user {$member->id}: " . $e->getMessage());
                }
            }
        }
    }
}

<?php

namespace App\Jobs;

use App\Models\CrashEvent;
use App\Models\EmergencyAlert;
use App\Services\WhatsAppServiceInterface;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class SendCrashAlertJob implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    /**
     * Create a new job instance.
     */
    public function __construct(
        public CrashEvent $crashEvent,
        public EmergencyAlert $emergencyAlert
    ) {}

    /**
     * Execute the job.
     */
    public function handle(WhatsAppServiceInterface $whatsAppService): void
    {
        $user = $this->crashEvent->user;
        if (!$user) {
            Log::error("Crash alert job failed: user not found for crash event {$this->crashEvent->id}");
            return;
        }

        $user->load('circles.users');
        $emergencyUrl = config('app.frontend_url', env('FRONTEND_URL', 'http://localhost:3000'))."/emergencia/{$this->emergencyAlert->id}";

        Log::info("Processing crash alert for user {$user->name} ({$user->id}), force G: {$this->crashEvent->g_force}");

        // Get unique members of all circles that this user is in (excluding themselves)
        $members = [];
        foreach ($user->circles as $circle) {
            foreach ($circle->users as $member) {
                if ($member->id !== $user->id) {
                    $members[$member->id] = $member;
                }
            }
        }

        // Send Push Notifications to Nucleus Members
        foreach ($members as $member) {
            if ($member->expo_push_token) {
                try {
                    $gForceStr = $this->crashEvent->g_force ? " de {$this->crashEvent->g_force} G" : "";
                    $response = Http::post('https://exp.host/--/api/v2/push/send', [
                        'to' => $member->expo_push_token,
                        'title' => '🚨 ¡ALERTA DE ACCIDENTE!',
                        'body' => "Se ha detectado un fuerte impacto vehicular{$gForceStr} en el dispositivo de {$user->name}.",
                        'sound' => 'default',
                        'priority' => 'high',
                        'channelId' => 'emergency',
                        'data' => [
                            'type' => 'crash_alert',
                            'alert_id' => $this->emergencyAlert->id,
                            'crash_event_id' => $this->crashEvent->id,
                            'user_id' => $user->id,
                            'user_name' => $user->name,
                        ],
                    ]);

                    if ($response->failed()) {
                        Log::warning("Expo Push Notification failed for crash alert to user {$member->id}: " . $response->body());
                    }
                } catch (\Exception $e) {
                    Log::error("Failed to send crash push notification to user {$member->id}: " . $e->getMessage());
                }
            }
        }

        // Send Twilio SMS and/or WhatsApp to Emergency Contacts
        $contacts = $user->emergencyContacts()->where('is_active', true)->get();
        $gForceText = $this->crashEvent->g_force ? " de {$this->crashEvent->g_force} G" : "";
        $message = "🚨 ¡ALERTA DE ACCIDENTE! Detectamos un fuerte impacto vehicular{$gForceText} en la ubicación de {$user->name}. Ver mapa en vivo: {$emergencyUrl}";

        foreach ($contacts as $contact) {
            if ($contact->phone) {
                try {
                    // Send basic SMS
                    $whatsAppService->sendSMS($contact->phone, $message);

                    // Send WhatsApp if premium
                    if ($user->hasPremiumAccess()) {
                        $whatsAppService->sendWhatsApp($contact->phone, $message);
                    }
                    Log::info("Crash alert SMS/WhatsApp sent to contact {$contact->phone} for user {$user->id}");
                } catch (\Exception $e) {
                    Log::error("Failed to send Twilio alert to contact {$contact->id}: " . $e->getMessage());
                }
            }
        }
    }
}

<?php

namespace App\Jobs;

use App\Mail\InactivityAlertMail;
use App\Models\EmergencyAlert;
use App\Models\User;
use App\Services\WhatsAppServiceInterface;
use Exception;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Mail;

class SendInactivityAlerts implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    /**
     * Create a new job instance.
     */
    public function __construct(
        public User $user,
        public ?string $emergencyAlertId = null,
        public int $contactIndex = 0
    ) {}

    /**
     * Execute the job.
     */
    public function handle(WhatsAppServiceInterface $whatsAppService): void
    {
        try {
            // Find or create emergency alert
            if ($this->emergencyAlertId) {
                $alert = EmergencyAlert::find($this->emergencyAlertId);
                if (! $alert || $alert->status !== 'active') {
                    Log::info("Inactivity alert escalation cancelled or alert not active for user {$this->user->id}");
                    return;
                }
            } else {
                // Initial run: create the alert
                $alert = EmergencyAlert::create([
                    'user_id' => $this->user->id,
                    'type' => 'inactivity',
                    'status' => 'active',
                    'expires_at' => now()->addHours(48),
                ]);
            }

            $emergencyUrl = config('app.frontend_url', env('FRONTEND_URL', 'http://localhost:3000'))."/emergencia/{$alert->id}";

            // If index is 0, send push notification to the user themselves
            if ($this->contactIndex === 0) {
                $this->sendPushNotification();
            }

            // Check if escalation is enabled
            if ($this->user->escalation_enabled) {
                $contacts = $this->user->emergencyContacts()
                    ->where('is_active', true)
                    ->orderBy('priority', 'asc')
                    ->get();

                if ($contacts->isEmpty()) {
                    Log::info("No active emergency contacts for user {$this->user->id}");
                    return;
                }

                $contact = $contacts->get($this->contactIndex);
                if ($contact) {
                    // Send alert to this contact
                    $this->sendEmailAlertToContact($contact, $emergencyUrl);
                    if ($this->user->hasPremiumAccess()) {
                        $this->sendPremiumAlertToContact($whatsAppService, $contact, $emergencyUrl);
                    }

                    // Enqueue next contact if available
                    $nextContact = $contacts->get($this->contactIndex + 1);
                    if ($nextContact) {
                        $delayMinutes = $this->user->escalation_interval_minutes ?? 15;
                        self::dispatch($this->user, $alert->id, $this->contactIndex + 1)
                            ->delay(now()->addMinutes($delayMinutes));
                        Log::info("Enqueued next escalation alert for user {$this->user->id}, contact index " . ($this->contactIndex + 1) . " in {$delayMinutes} minutes.");
                    }
                }
            } else {
                // Traditional behavior: alert all contacts at once immediately
                $this->sendEmailAlert($emergencyUrl);

                if ($this->user->hasPremiumAccess()) {
                    $this->sendPremiumAlerts($whatsAppService, $emergencyUrl);
                }
            }

            // Log success
            Log::info("Inactivity alerts process step {$this->contactIndex} completed for user: {$this->user->id} with alert ID: {$alert->id}");
        } catch (Exception $e) {
            Log::error("Failed to execute inactivity alerts step {$this->contactIndex} for user {$this->user->id}: ".$e->getMessage());
            throw $e; // Retry if configured
        }
    }

    protected function sendPushNotification()
    {
        if (! $this->user->expo_push_token) {
            return;
        }

        app(\App\Services\PushNotificationService::class)->sendPush(
            $this->user->expo_push_token,
            '¡Alerta de Seguridad!',
            'No has realizado tu check-in diario. Tus contactos de emergencia han sido notificados.',
            ['type' => 'inactivity_alert']
        );
    }

    protected function sendEmailAlert(string $emergencyUrl)
    {
        $contacts = $this->user->emergencyContacts()->where('is_active', true)->get();

        if ($contacts->isEmpty()) {
            Log::info("No active emergency contacts for user {$this->user->id}");

            return;
        }

        foreach ($contacts as $contact) {
            $this->sendEmailAlertToContact($contact, $emergencyUrl);
        }
    }

    protected function sendEmailAlertToContact($contact, string $emergencyUrl)
    {
        if ($contact->email) {
            Mail::to($contact->email)->send(new InactivityAlertMail(
                $this->user,
                $emergencyUrl,
                $contact->relationship
            ));
            Log::info("Email alert sent to contact ID {$contact->id} for user {$this->user->id}");
        }
    }

    protected function sendPremiumAlerts(WhatsAppServiceInterface $whatsAppService, string $emergencyUrl)
    {
        $contacts = $this->user->emergencyContacts()->where('is_active', true)->get();

        foreach ($contacts as $contact) {
            $this->sendPremiumAlertToContact($whatsAppService, $contact, $emergencyUrl);
        }
    }

    protected function sendPremiumAlertToContact(WhatsAppServiceInterface $whatsAppService, $contact, string $emergencyUrl)
    {
        if ($contact->phone) {
            $relationshipText = $contact->relationship ? " (Usted figura como '{$contact->relationship}')" : '';
            $hours = $this->user->checkin_interval_hours;

            $message = "Aviso de seguridad: Su contacto '{$this->user->name}' no ha reportado actividad en la aplicación 'Estoy Ok' durante las últimas {$hours} horas.{$relationshipText} Ver última ubicación y estado aquí: {$emergencyUrl}";

            $success = $whatsAppService->sendWhatsApp($contact->phone, $message);

            if (! $success) {
                Log::warning("WhatsApp failed for contact {$contact->phone}, falling back to SMS.");
                $whatsAppService->sendSMS($contact->phone, $message);
            }
            Log::info("Premium alert sent to contact ID {$contact->id} for user {$this->user->id}");
        }
    }
}

<?php

namespace App\Jobs;

use App\Models\User;
use App\Models\EmergencyAlert;
use App\Services\WhatsAppServiceInterface;
use App\Mail\InactivityAlertMail;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Mail;
use Exception;

class SendInactivityAlerts implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    /**
     * Create a new job instance.
     */
    public function __construct(public User $user)
    {
    }

    /**
     * Execute the job.
     */
    public function handle(WhatsAppServiceInterface $whatsAppService): void
    {
        try {
            $alert = EmergencyAlert::create([
                'user_id' => $this->user->id,
                'type' => 'inactivity',
                'status' => 'active',
                'expires_at' => now()->addHours(48),
            ]);

            $emergencyUrl = config('app.frontend_url', env('FRONTEND_URL', 'http://localhost:3000')) . "/emergencia/{$alert->id}";

            $this->sendPushNotification();
            $this->sendEmailAlert($emergencyUrl);

            if ($this->user->hasPremiumAccess()) {
                $this->sendPremiumAlerts($whatsAppService, $emergencyUrl);
            }
            
            // Log success
            Log::info("Inactivity alerts sent for user: {$this->user->id} with alert ID: {$alert->id}");
        } catch (Exception $e) {
            Log::error("Failed to send inactivity alerts for user {$this->user->id}: " . $e->getMessage());
            throw $e; // Retry if configured
        }
    }

    protected function sendPushNotification()
    {
        if (!$this->user->expo_push_token) {
            return;
        }

        $response = Http::post('https://exp.host/--/api/v2/push/send', [
            'to' => $this->user->expo_push_token,
            'title' => '¡Alerta de Seguridad!',
            'body' => 'No has realizado tu check-in diario. Tus contactos de emergencia han sido notificados.',
            'data' => ['type' => 'inactivity_alert'],
        ]);

        if ($response->failed()) {
            Log::warning("Expo Push Notification failed for user {$this->user->id}: " . $response->body());
        }
    }

    protected function sendEmailAlert(string $emergencyUrl)
    {
        $contacts = $this->user->emergencyContacts()->where('is_active', true)->get();

        if ($contacts->isEmpty()) {
            Log::info("No active emergency contacts for user {$this->user->id}");
            return;
        }

        foreach ($contacts as $contact) {
            if ($contact->email) {
                Mail::to($contact->email)->send(new InactivityAlertMail(
                    $this->user,
                    $emergencyUrl,
                    $contact->relationship
                ));
            }
        }
    }

    protected function sendPremiumAlerts(WhatsAppServiceInterface $whatsAppService, string $emergencyUrl)
    {
        $contacts = $this->user->emergencyContacts()->where('is_active', true)->get();

        foreach ($contacts as $contact) {
            if ($contact->phone) {
                $relationshipText = $contact->relationship ? " (Usted figura como '{$contact->relationship}')" : "";
                $hours = $this->user->checkin_interval_hours;
                
                $message = "Aviso de seguridad: Su contacto '{$this->user->name}' no ha reportado actividad en la aplicación 'Estoy Ok' durante las últimas {$hours} horas.{$relationshipText} Ver última ubicación y estado aquí: {$emergencyUrl}";
                
                $success = $whatsAppService->sendWhatsApp($contact->phone, $message);

                if (!$success) {
                    Log::warning("WhatsApp failed for contact {$contact->phone}, falling back to SMS.");
                    $whatsAppService->sendSMS($contact->phone, $message);
                }
            }
        }
    }
}

<?php

namespace App\Jobs;

use App\Models\User;
use App\Services\WhatsAppServiceInterface;
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
            $this->sendPushNotification();
            $this->sendEmailAlert();

            if ($this->user->hasPremiumAccess()) {
                $this->sendPremiumAlerts($whatsAppService);
            }
            
            // Log success
            Log::info("Inactivity alerts sent for user: {$this->user->id}");
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

    protected function sendEmailAlert()
    {
        $contacts = $this->user->emergencyContacts;

        if ($contacts->isEmpty()) {
            Log::info("No emergency contacts for user {$this->user->id}");
            return;
        }

        foreach ($contacts as $contact) {
            if ($contact->email) {
                // In a real app, we would send a real email
                Log::info("Simulating email to emergency contact {$contact->email} for user {$this->user->name}");
            }
        }
    }

    protected function sendPremiumAlerts(WhatsAppServiceInterface $whatsAppService)
    {
        $contacts = $this->user->emergencyContacts;

        foreach ($contacts as $contact) {
            if ($contact->phone) {
                $message = "Aviso de seguridad: {$this->user->name} no ha reportado actividad en la aplicación 'Estoy Ok' durante las últimas 24 horas. Por favor, intente contactarlo.";
                
                $success = $whatsAppService->sendWhatsApp($contact->phone, $message);

                if (!$success) {
                    Log::warning("WhatsApp failed for contact {$contact->phone}, falling back to SMS.");
                    $whatsAppService->sendSMS($contact->phone, $message);
                }
            }
        }
    }
}

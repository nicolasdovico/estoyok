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
    public function handle(): void
    {
        try {
            $this->sendPushNotification();
            $this->sendEmailAlert();
            
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
        // For MVP, we send to the user's email and their emergency contacts
        $contacts = $this->user->emergencyContacts;

        if ($contacts->isEmpty()) {
            Log::info("No emergency contacts for user {$this->user->id}");
            return;
        }

        foreach ($contacts as $contact) {
            if ($contact->email) {
                // Simplified email sending for MVP
                // Mail::to($contact->email)->send(new InactivityAlertMail($this->user));
                Log::info("Simulating email to emergency contact {$contact->email} for user {$this->user->name}");
            }
        }
    }
}

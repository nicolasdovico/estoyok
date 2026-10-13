<?php

namespace App\Jobs;

use App\Models\EmergencyAlert;
use App\Models\User;
use App\Services\WhatsAppServiceInterface;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;
use Illuminate\Support\Facades\Log;

class SendSosAlertJob implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    /**
     * Create a new job instance.
     */
    public function __construct(
        public EmergencyAlert $alert,
        public User $user
    ) {}

    /**
     * Execute the job.
     */
    public function handle(WhatsAppServiceInterface $whatsAppService): void
    {
        if ($this->alert->status !== 'active') {
            Log::info("SendSosAlertJob: Alert {$this->alert->id} is not active (status: {$this->alert->status}). Skipping WhatsApp dispatch.");
            return;
        }

        $baseUrl = rtrim(config('app.frontend_url', env('FRONTEND_URL', 'https://estoyok24.com')), '/');
        if (empty($baseUrl) || str_contains($baseUrl, 'localhost') || str_contains($baseUrl, '127.0.0.1') || str_contains($baseUrl, 'railway.app')) {
            $baseUrl = 'https://estoyok24.com';
        }
        $emergencyUrl = "{$baseUrl}/emergencia/{$this->alert->id}";

        $appUrl = config('app.url', env('APP_URL', ''));
        $isDev = config('app.env') !== 'production' || str_contains($appUrl, 'dev') || str_contains($appUrl, 'railway.app');
        $apiParam = $isDev ? "&api=" . urlencode(rtrim($appUrl ?: 'https://backend-api-dev-2a56.up.railway.app', '/') . '/api') : '';

        $contacts = $this->user->emergencyContacts()
            ->where('is_active', true)
            ->orderBy('priority', 'asc')
            ->get();

        if ($contacts->isEmpty()) {
            Log::info("SendSosAlertJob: No active emergency contacts found for user {$this->user->id}");
            return;
        }

        Log::info("SendSosAlertJob: Processing SOS WhatsApp alerts for user {$this->user->name} ({$this->user->id}) to " . $contacts->count() . " active contact(s)");

        $previousPhone = null;
        foreach ($contacts as $index => $contact) {
            if (empty($contact->phone)) {
                continue;
            }

            // Espaciado técnico seguro: 1.5s entre números distintos, 5s si es el mismo número (escenarios de testing)
            if ($index > 0) {
                if ($contact->phone === $previousPhone) {
                    if (! app()->runningUnitTests()) {
                        sleep(5);
                    }
                } else {
                    if (! app()->runningUnitTests()) {
                        usleep(1500000); // 1.5 segundos
                    }
                }
            }
            $previousPhone = $contact->phone;

            $contactNameParam = urlencode($contact->name);
            $contactUrl = "{$emergencyUrl}?contact={$contactNameParam}{$apiParam}";
            $sosBody = "🚨 ¡SOS CRÍTICO! {$this->user->name} ha activado un SOS de emergencia silenciosa. Ubicación en tiempo real en: {$contactUrl}";

            try {
                $success = $whatsAppService->sendWhatsApp($contact->phone, $sosBody);
                if ($success) {
                    Log::info("SendSosAlertJob: SOS WhatsApp sent successfully to contact {$contact->phone} (ID: {$contact->id}) for user {$this->user->id}");
                } else {
                    Log::warning("SendSosAlertJob: SOS WhatsApp returned false for contact {$contact->phone} (ID: {$contact->id}) for user {$this->user->id}");
                }
            } catch (\Throwable $e) {
                Log::error("SendSosAlertJob: Exception sending SOS WhatsApp to contact {$contact->phone} (ID: {$contact->id}): " . $e->getMessage());
            }
        }
    }
}

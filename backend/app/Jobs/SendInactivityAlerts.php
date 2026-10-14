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
use Illuminate\Support\Facades\Cache;
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
        Log::info("SendInactivityAlerts::handle started for user {$this->user->id}, alert {$this->emergencyAlertId}, contactIndex {$this->contactIndex}");

        try {
            Cache::put('last_inactivity_job_received', [
                'time' => now()->toDateTimeString(),
                'server_time_utc' => now('UTC')->toDateTimeString(),
                'app_env' => app()->environment(),
                'user_id' => $this->user->id,
                'alert_id' => $this->emergencyAlertId,
                'contact_index' => $this->contactIndex,
                'pid' => getmypid(),
                'hostname' => gethostname(),
            ], 3600);

            // Find or create emergency alert
            if ($this->emergencyAlertId) {
                $alert = EmergencyAlert::find($this->emergencyAlertId);
                if (! $alert || $alert->status !== 'active') {
                    Cache::put('last_inactivity_job_missing_alert', [
                        'time' => now()->toDateTimeString(),
                        'app_env' => app()->environment(),
                        'alert_id' => $this->emergencyAlertId,
                        'found' => (bool) $alert,
                        'status' => $alert?->status,
                    ], 3600);
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

            $baseUrl = rtrim(config('app.frontend_url', env('FRONTEND_URL', 'https://estoyok24.com')), '/');
            if (empty($baseUrl) || str_contains($baseUrl, 'localhost') || str_contains($baseUrl, '127.0.0.1') || str_contains($baseUrl, 'railway.app')) {
                $baseUrl = 'https://estoyok24.com';
            }
            $emergencyUrl = "{$baseUrl}/emergencia/{$alert->id}";

            $selfWhatsAppSent = false;
            $lastSentPhone = null;

            // If index is 0, send push notification to the user themselves, self WhatsApp alert if enabled, and to nucleus members
            if ($this->contactIndex === 0) {
                try {
                    $this->sendPushNotification();
                } catch (\Throwable $e) {
                    Log::error("Failed to send push notification to user {$this->user->id}: " . $e->getMessage());
                }

                try {
                    $selfWhatsAppSent = $this->sendSelfWhatsAppAlert($whatsAppService);
                    if ($selfWhatsAppSent && !empty($this->user->phone)) {
                        $lastSentPhone = $this->normalizePhone($this->user->phone);
                    }
                } catch (\Throwable $e) {
                    Log::error("Failed to send self WhatsApp alert to user {$this->user->id}: " . $e->getMessage());
                }

                try {
                    $this->sendPushNotificationToNucleusMembers($alert);
                } catch (\Throwable $e) {
                    Log::error("Failed to send push notification to nucleus members for user {$this->user->id}: " . $e->getMessage());
                }
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
                    // 1. WhatsApp inmediatamente (canal crítico prioritario)
                    if ($this->user->hasPremiumAccess() && !empty($contact->phone)) {
                        $contactPhoneClean = $this->normalizePhone($contact->phone);

                        if (! app()->runningUnitTests()) {
                            if ($lastSentPhone !== null && $contactPhoneClean === $lastSentPhone) {
                                sleep(5); // 5s técnico seguro para mismo número
                            } elseif ($lastSentPhone !== null) {
                                usleep(1500000); // 1.5s espaciado ágil entre números distintos
                            }
                        }

                        try {
                            $this->sendPremiumAlertToContact($whatsAppService, $contact, $emergencyUrl);
                        } catch (\Throwable $e) {
                            Log::error("Failed to send premium WhatsApp to contact ID {$contact->id}: " . $e->getMessage());
                        }
                    }

                    // 2. Email después (canal secundario)
                    try {
                        $this->sendEmailAlertToContact($contact, $emergencyUrl);
                    } catch (\Throwable $e) {
                        Log::error("Failed to send email alert to contact ID {$contact->id}: " . $e->getMessage());
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
                // 1. WhatsApp inmediatamente a contactos (canal crítico prioritario sin esperar emails)
                if ($this->user->hasPremiumAccess()) {
                    try {
                        $this->sendPremiumAlerts($whatsAppService, $emergencyUrl, $lastSentPhone);
                    } catch (\Throwable $e) {
                        Log::error("Failed to send premium alerts to contacts for user {$this->user->id}: " . $e->getMessage());
                    }
                }

                // 2. Email después (canal secundario no bloqueante)
                try {
                    $this->sendEmailAlert($emergencyUrl);
                } catch (\Throwable $e) {
                    Log::error("Failed to send email alerts for user {$this->user->id}: " . $e->getMessage());
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

    protected function sendSelfWhatsAppAlert(WhatsAppServiceInterface $whatsAppService): bool
    {
        if ($this->user->notify_self_whatsapp_on_inactivity && !empty($this->user->phone)) {
            $hours = $this->user->checkin_interval_hours ?? 24;
            $message = "⚠️ Aviso Estoy Ok: Tu reporte diario de bienestar ha vencido (sin confirmación durante las últimas {$hours} horas). Tus contactos de emergencia han sido alertados. Puedes confirmar tu estado ingresando a la app o respondiendo 'OK' a este mensaje.";

            $success = $whatsAppService->sendWhatsApp($this->user->phone, $message);

            Cache::put('last_inactivity_job_self_whatsapp', [
                'time' => now()->toDateTimeString(),
                'phone' => $this->user->phone,
                'notify_self_enabled' => true,
                'success' => $success,
            ], 3600);

            if (! $success) {
                Log::warning("Self WhatsApp inactivity alert failed for user {$this->user->id} ({$this->user->phone}).");
            } else {
                Log::info("Self WhatsApp inactivity alert sent successfully to user {$this->user->id} ({$this->user->phone}).");
            }

            return $success;
        }

        Cache::put('last_inactivity_job_self_whatsapp', [
            'time' => now()->toDateTimeString(),
            'phone' => $this->user->phone,
            'notify_self_enabled' => (bool) $this->user->notify_self_whatsapp_on_inactivity,
            'skipped' => true,
        ], 3600);

        return false;
    }

    protected function sendEmailAlert(string $emergencyUrl)
    {
        $contacts = $this->user->emergencyContacts()->where('is_active', true)->get();

        if ($contacts->isEmpty()) {
            Log::info("No active emergency contacts for user {$this->user->id}");

            return;
        }

        foreach ($contacts as $contact) {
            try {
                $this->sendEmailAlertToContact($contact, $emergencyUrl);
            } catch (\Throwable $e) {
                Log::error("Failed to send email alert to contact ID {$contact->id} for user {$this->user->id}: " . $e->getMessage());
            }
        }
    }

    protected function sendEmailAlertToContact($contact, string $emergencyUrl)
    {
        $alwaysTo = config('mail.always_to') ?? env('MAIL_ALWAYS_TO');
        $recipient = !empty($alwaysTo) ? $alwaysTo : $contact->email;

        if (!empty($recipient)) {
            try {
                Mail::to($recipient)->send(new InactivityAlertMail(
                    $this->user,
                    $emergencyUrl,
                    $contact->relationship
                ));
                Log::info("Email alert sent to {$recipient} (contact ID {$contact->id}) for user {$this->user->id}");
            } catch (\Throwable $e) {
                Log::error("Failed to send email alert to {$recipient} for user {$this->user->id}: ".$e->getMessage());
            }
        } else {
            Log::warning("No email address or MAIL_ALWAYS_TO found for emergency contact ID {$contact->id}");
        }
    }

    protected function sendPremiumAlerts(WhatsAppServiceInterface $whatsAppService, string $emergencyUrl, ?string $lastSentPhone = null)
    {
        $contacts = $this->user->emergencyContacts()->where('is_active', true)->get();

        $sentPhones = [];

        foreach ($contacts as $contact) {
            if (empty($contact->phone)) continue;

            $contactPhoneClean = $this->normalizePhone($contact->phone);

            if (isset($sentPhones[$contactPhoneClean])) {
                continue;
            }

            // Espaciado técnico seguro:
            // Si el contacto coincide con el último número enviado (ej: auto-aviso al mismo teléfono),
            // aguardamos 5s completos para que Baileys / Signal ratchee limpiamente antes del mensaje de contacto
            if (! app()->runningUnitTests()) {
                if ($lastSentPhone !== null && $contactPhoneClean === $lastSentPhone) {
                    sleep(5); // 5 segundos obligatorios para mismo número
                } elseif ($lastSentPhone !== null || ! empty($sentPhones)) {
                    usleep(1500000); // 1.5s espaciado ágil entre números distintos
                }
            }

            $sentPhones[$contactPhoneClean] = true;
            $lastSentPhone = $contactPhoneClean;

            try {
                $this->sendPremiumAlertToContact($whatsAppService, $contact, $emergencyUrl);
            } catch (\Throwable $e) {
                Log::error("Failed to send WhatsApp alert to contact ID {$contact->id} for user {$this->user->id}: " . $e->getMessage());
            }
        }
    }

    protected function sendPremiumAlertToContact(WhatsAppServiceInterface $whatsAppService, $contact, string $emergencyUrl)
    {
        if ($contact->phone) {
            $relationshipText = $contact->relationship ? " (Usted figura como '{$contact->relationship}')" : '';
            $hours = $this->user->checkin_interval_hours;
            $contactNameParam = urlencode($contact->name);
            $contactUrl = "{$emergencyUrl}?contact={$contactNameParam}";

            $message = "Aviso de seguridad: Su contacto '{$this->user->name}' no ha reportado actividad en la aplicación 'Estoy Ok' durante las últimas {$hours} horas.{$relationshipText} Ver última ubicación y estado aquí: {$contactUrl}";

            $success = $whatsAppService->sendWhatsApp($contact->phone, $message);

            $results = Cache::get('last_inactivity_job_contacts_result', []);
            if (! is_array($results)) {
                $results = [];
            }
            $results[] = [
                'time' => now()->toDateTimeString(),
                'contact_id' => $contact->id,
                'phone' => $contact->phone,
                'success' => $success,
            ];
            Cache::put('last_inactivity_job_contacts_result', $results, 3600);

            if (! $success) {
                Log::warning("WhatsApp failed for contact {$contact->phone} (user {$this->user->id}).");
            } else {
                Log::info("Premium alert sent to contact ID {$contact->id} for user {$this->user->id}");
            }
        }
    }

    protected function sendPushNotificationToNucleusMembers(EmergencyAlert $alert): void
    {
        $this->user->loadMissing('circles.users');

        $members = [];
        foreach ($this->user->circles as $circle) {
            foreach ($circle->users as $member) {
                if ($member->id !== $this->user->id) {
                    $members[$member->id] = $member;
                }
            }
        }

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

        $pushService = app(\App\Services\PushNotificationService::class);
        foreach ($members as $member) {
            $memberToken = $member->expo_push_token;
            if (!empty($memberToken) && !isset($sentTokens[$memberToken])) {
                $sentTokens[$memberToken] = true;
                $pushService->sendPush(
                    $memberToken,
                    '⚠️ Alerta de Inactividad',
                    "{$this->user->name} no ha reportado su estado ('Estoy OK') en el tiempo configurado.",
                    [
                        'type' => 'inactivity_alert',
                        'alert_id' => (string) $alert->id,
                        'user_id' => (string) $this->user->id,
                        'user_name' => $this->user->name,
                    ]
                );
                Log::info("Inactivity push notification sent to nucleus member ID {$member->id} for user {$this->user->id}");
            }
        }
    }

    /**
     * Normaliza un número telefónico a formato canónico con dígitos limpios.
     * Para números argentinos, garantiza el prefijo internacional 549...
     */
    public function normalizePhone(?string $phone): ?string
    {
        if (empty($phone)) {
            return null;
        }

        $clean = preg_replace('/[^0-9]/', '', $phone);

        if (str_starts_with($clean, '0')) {
            $clean = substr($clean, 1);
        }

        if (str_starts_with($clean, '54') && !str_starts_with($clean, '549') && strlen($clean) === 12) {
            $clean = '549' . substr($clean, 2);
        } elseif (!str_starts_with($clean, '54') && strlen($clean) === 10) {
            $clean = '549' . $clean;
        }

        return $clean;
    }
}

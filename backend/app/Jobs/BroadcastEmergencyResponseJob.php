<?php

namespace App\Jobs;

use App\Models\EmergencyAlert;
use App\Models\EmergencyResponse;
use App\Models\User;
use App\Services\PushNotificationService;
use App\Services\WhatsAppServiceInterface;
use Exception;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;
use Illuminate\Support\Facades\Log;

class BroadcastEmergencyResponseJob implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    public function __construct(
        public string $alertId,
        public int $responseId
    ) {}

    public function handle(WhatsAppServiceInterface $whatsAppService, PushNotificationService $pushService): void
    {
        try {
            $alert = EmergencyAlert::with(['user.emergencyContacts', 'user.circles.users', 'user.devices'])->find($this->alertId);
            if (! $alert || ! $alert->user) {
                Log::warning("BroadcastEmergencyResponseJob: Alert or user not found for ID {$this->alertId}");
                return;
            }

            $user = $alert->user;
            if ($user->share_contact_responses === false) {
                Log::info("BroadcastEmergencyResponseJob: User {$user->id} has disabled share_contact_responses");
                return;
            }

            $response = EmergencyResponse::find($this->responseId);
            if (! $response) {
                Log::warning("BroadcastEmergencyResponseJob: Response not found for ID {$this->responseId}");
                return;
            }

            $contactName = $response->contact_name ?: 'Un contacto de emergencia';
            $status = $response->status;

            $baseUrl = rtrim(config('app.frontend_url', env('FRONTEND_URL', 'https://estoyok24.com')), '/');
            if (empty($baseUrl) || str_contains($baseUrl, 'localhost') || str_contains($baseUrl, '127.0.0.1') || str_contains($baseUrl, 'railway.app')) {
                $baseUrl = 'https://estoyok24.com';
            }
            $emergencyUrl = "{$baseUrl}/emergencia/{$alert->id}";

            $pushTitle = '🚨 Actualización de Emergencia';
            $userPushBody = '';
            $userWhatsAppMsg = '';
            $contactWhatsAppMsg = '';
            $circlePushBody = '';

            if ($status === 'on_my_way') {
                $pushTitle = '🚗 ¡Ayuda en Camino!';
                $userPushBody = "{$contactName} confirmó que VA EN CAMINO a socorrerte.";
                $userWhatsAppMsg = "🚗 *{$contactName}* confirmó que VA EN CAMINO a socorrerte en tu alerta de emergencia.";
                $contactWhatsAppMsg = "🚗 *{$contactName}* confirmó que VA EN CAMINO a asistir a {$user->name}. Seguimiento en vivo: {$emergencyUrl}";
                $circlePushBody = "{$contactName} va en camino a socorrer a {$user->name}.";
            } elseif ($status === 'acknowledged') {
                $pushTitle = '✅ Alerta Confirmada';
                $userPushBody = "{$contactName} confirmó que recibió y está enterado de tu alerta.";
                $userWhatsAppMsg = "✅ *{$contactName}* confirmó que RECIBIÓ y está enterado de tu alerta de emergencia.";
                $contactWhatsAppMsg = "✅ *{$contactName}* ya está enterado y tomó conocimiento de la alerta de {$user->name}. Seguimiento en vivo: {$emergencyUrl}";
                $circlePushBody = "{$contactName} tomó conocimiento de la alerta de {$user->name}.";
            } elseif ($status === 'read') {
                $pushTitle = '👁️ Alerta Vista';
                $userPushBody = "{$contactName} abrió y visualizó tu enlace de emergencia.";
                $circlePushBody = "{$contactName} visualizó el mapa de emergencia de {$user->name}.";
            } else {
                return;
            }

            // 1. Collect all Push tokens for the alert initiator (User)
            $userTokens = [];
            if (! empty($user->expo_push_token)) {
                $userTokens[] = $user->expo_push_token;
            }
            $activeDeviceTokens = $user->devices()->where('is_active', true)->whereNotNull('push_token')->pluck('push_token')->toArray();
            foreach ($activeDeviceTokens as $token) {
                if (! empty($token) && ! in_array($token, $userTokens)) {
                    $userTokens[] = $token;
                }
            }

            Log::info("BroadcastEmergencyResponseJob: Found " . count($userTokens) . " push token(s) for user {$user->id}");

            foreach ($userTokens as $token) {
                $pushService->sendPush(
                    $token,
                    $pushTitle,
                    $userPushBody,
                    [
                        'type' => 'emergency_response',
                        'alert_id' => (string) $alert->id,
                        'status' => $status,
                        'contact_name' => $contactName,
                        'title' => $pushTitle,
                        'body' => $userPushBody,
                    ],
                    true
                );
            }

            // 2. Send WhatsApp to the alert initiator (User) if phone is registered and status is critical
            if (! empty($user->phone) && ! empty($userWhatsAppMsg) && in_array($status, ['on_my_way', 'acknowledged'])) {
                try {
                    $whatsAppService->sendWhatsApp($user->phone, $userWhatsAppMsg);
                } catch (Exception $e) {
                    Log::warning("BroadcastEmergencyResponseJob: Failed sending WhatsApp to user {$user->id}: {$e->getMessage()}");
                }
            }

            // 3. Send WhatsApp broadcast to other emergency contacts
            if (! empty($contactWhatsAppMsg) && in_array($status, ['on_my_way', 'acknowledged'])) {
                $contacts = $user->emergencyContacts()->where('is_active', true)->get();
                foreach ($contacts as $contact) {
                    if (! empty($contact->phone)) {
                        try {
                            $whatsAppService->sendWhatsApp($contact->phone, $contactWhatsAppMsg);
                        } catch (Exception $e) {
                            Log::warning("BroadcastEmergencyResponseJob: Failed sending WhatsApp to contact {$contact->id}: {$e->getMessage()}");
                        }
                    }
                }
            }

            // 4. Send Push notification to circle members
            if (! empty($circlePushBody)) {
                $members = [];
                foreach ($user->circles as $circle) {
                    foreach ($circle->users as $member) {
                        if ($member->id !== $user->id) {
                            $members[$member->id] = $member;
                        }
                    }
                }

                foreach ($members as $member) {
                    $memberTokens = [];
                    if (! empty($member->expo_push_token)) {
                        $memberTokens[] = $member->expo_push_token;
                    }
                    $memberDeviceTokens = $member->devices()->where('is_active', true)->whereNotNull('push_token')->pluck('push_token')->toArray();
                    foreach ($memberDeviceTokens as $token) {
                        if (! empty($token) && ! in_array($token, $memberTokens)) {
                            $memberTokens[] = $token;
                        }
                    }

                    foreach ($memberTokens as $mt) {
                        $pushService->sendPush(
                            $mt,
                            $pushTitle,
                            $circlePushBody,
                            [
                                'type' => 'emergency_response',
                                'alert_id' => (string) $alert->id,
                                'status' => $status,
                                'contact_name' => $contactName,
                                'user_id' => (string) $user->id,
                                'title' => $pushTitle,
                                'body' => $circlePushBody,
                            ],
                            true
                        );
                    }
                }
            }

            Log::info("BroadcastEmergencyResponseJob: Successfully broadcasted response '{$status}' from '{$contactName}' for alert {$alert->id}");
        } catch (Exception $e) {
            Log::error("BroadcastEmergencyResponseJob error: {$e->getMessage()}", [
                'trace' => $e->getTraceAsString(),
            ]);
        }
    }
}


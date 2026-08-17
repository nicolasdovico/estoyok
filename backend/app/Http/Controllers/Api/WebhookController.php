<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;

class WebhookController extends Controller
{
    public function mercadopago(Request $request)
    {
        Log::info('Mercado Pago Webhook Received', $request->all());

        $type = $request->input('type') ?? $request->input('topic');
        $id = $request->input('data.id') ?? $request->input('id');

        if ($type === 'subscription_preapproval' || $type === 'preapproval') {
            // In a real scenario, you'd fetch the preapproval details from MP API using the ID
            // and find the user by external_reference or payer_email.
            // For this MVP, we'll assume we can identify the user somehow or it's a simplified flow.

            // Example:
            // $mpUser = User::where('mp_subscription_id', $id)->first();
            // if ($mpUser) { $mpUser->update(['is_premium' => true, 'mp_status' => 'authorized']); }
        }

        return response()->json(['status' => 'ok']);
    }

    public function paypal(Request $request)
    {
        Log::info('PayPal Webhook Received', $request->all());

        $eventType = $request->input('event_type');
        $resource = $request->input('resource');

        if ($eventType === 'BILLING.SUBSCRIPTION.ACTIVATED' || $eventType === 'BILLING.SUBSCRIPTION.CREATED') {
            $subscriptionId = $resource['id'];
            $user = User::where('paypal_subscription_id', $subscriptionId)->first();
            if ($user) {
                $user->update([
                    'is_premium' => true,
                    'paypal_status' => 'active',
                ]);
            }
        }

        if ($eventType === 'BILLING.SUBSCRIPTION.CANCELLED' || $eventType === 'BILLING.SUBSCRIPTION.EXPIRED') {
            $subscriptionId = $resource['id'];
            $user = User::where('paypal_subscription_id', $subscriptionId)->first();
            if ($user) {
                $user->update([
                    'is_premium' => false,
                    'paypal_status' => 'cancelled',
                ]);
            }
        }

        return response()->json(['status' => 'ok']);
    }

    public function evolutionMessage(Request $request)
    {
        Log::info('Evolution API Message Webhook Received', [
            'payload' => $request->all(),
            'headers' => $request->headers->all(),
        ]);

        $payload = $request->all();
        $data = $request->input('data', $payload);

        // Unwrap if data is a list of messages (e.g. data: [ { key: ... } ])
        if (is_array($data) && isset($data[0]) && is_array($data[0])) {
            $item = $data[0];
        } elseif (is_array($data) && isset($data['messages'][0]) && is_array($data['messages'][0])) {
            $item = $data['messages'][0];
        } elseif (is_array($data)) {
            $item = $data;
        } else {
            $item = $payload;
        }

        // Ignore messages sent by ourselves
        $fromMe = data_get($item, 'key.fromMe') 
            ?? data_get($item, 'fromMe') 
            ?? data_get($payload, 'data.key.fromMe') 
            ?? data_get($payload, 'key.fromMe') 
            ?? false;

        if ($fromMe === true) {
            $this->recordWebhookCall($payload, 'ignored_from_me');
            return response()->json(['status' => 'ignored', 'reason' => 'from_me']);
        }

        $from = data_get($item, 'key.remoteJid') 
            ?? data_get($item, 'remoteJid') 
            ?? data_get($item, 'sender') 
            ?? data_get($item, 'from') 
            ?? data_get($item, 'participant') 
            ?? data_get($payload, 'sender') 
            ?? data_get($payload, 'from')
            ?? data_get($payload, 'remoteJid');

        if (! $from) {
            Log::info("Evolution Webhook: Missing sender/from in payload");
            $this->recordWebhookCall($payload, 'ignored_missing_from');
            return response()->json(['status' => 'ignored', 'reason' => 'missing_from']);
        }

        // Ignore group messages
        if (str_contains($from, '@g.us') || str_contains($from, '@broadcast')) {
            $this->recordWebhookCall($payload, 'ignored_group_or_broadcast');
            return response()->json(['status' => 'ignored', 'reason' => 'group_or_broadcast_message']);
        }

        // Clean @s.whatsapp.net or @c.us or whatsapp: prefix
        if (str_contains($from, '@')) {
            $from = explode('@', $from)[0];
        }
        if (str_starts_with($from, 'whatsapp:')) {
            $from = substr($from, 9);
        }

        // Clean phone format (keep numbers only)
        $fromCleaned = preg_replace('/[^0-9]/', '', $from);

        // Find user by phone (flexible matching against verified active users)
        $user = User::whereNotNull('email_verified_at')
            ->get()
            ->first(function ($u) use ($fromCleaned) {
                if (empty($u->phone)) return false;
                $userPhoneClean = preg_replace('/[^0-9]/', '', $u->phone);
                if (empty($userPhoneClean)) return false;

                if ($userPhoneClean === $fromCleaned) return true;
                if (str_ends_with($fromCleaned, $userPhoneClean) || str_ends_with($userPhoneClean, $fromCleaned)) return true;

                // Compare last 10 digits (national number)
                $from10 = substr($fromCleaned, -10);
                $user10 = substr($userPhoneClean, -10);
                if (strlen($from10) >= 8 && strlen($user10) >= 8 && $from10 === $user10) {
                    return true;
                }

                // Compare last 8 digits (subscriber number)
                $from8 = substr($fromCleaned, -8);
                $user8 = substr($userPhoneClean, -8);
                if (strlen($from8) >= 8 && strlen($user8) >= 8 && $from8 === $user8) {
                    return true;
                }

                return false;
            });

        if (! $user) {
            Log::info("Evolution Webhook: User not found for phone {$fromCleaned}");
            $this->recordWebhookCall($payload, "user_not_found_for_phone_{$fromCleaned}");
            return response()->json(['status' => 'user_not_found']);
        }

        // Check if configuration is enabled (defaults to true if null)
        if ($user->allow_sms_whatsapp_checkin === false) {
            Log::info("Evolution Webhook: Check-in disabled for user {$user->id}");
            $this->recordWebhookCall($payload, 'checkin_disabled_for_user', $user);
            return response()->json(['status' => 'checkin_disabled']);
        }

        $rawBody = data_get($item, 'message.conversation')
            ?? data_get($item, 'message.extendedTextMessage.text')
            ?? data_get($item, 'message.buttonsResponseMessage.selectedDisplayText')
            ?? data_get($item, 'message.templateButtonReplyMessage.selectedDisplayText')
            ?? data_get($item, 'message.listResponseMessage.title')
            ?? data_get($item, 'messageText')
            ?? data_get($item, 'body')
            ?? data_get($item, 'text')
            ?? data_get($payload, 'data.message.conversation')
            ?? data_get($payload, 'data.message.extendedTextMessage.text')
            ?? data_get($payload, 'body')
            ?? '';

        $body = mb_strtolower(trim((string) $rawBody), 'UTF-8');
        // Replace accents
        $body = strtr($body, [
            'á' => 'a', 'é' => 'e', 'í' => 'i', 'ó' => 'o', 'ú' => 'u', 'ü' => 'u',
        ]);
        // Remove non-alphanumeric characters except spaces
        $bodyClean = trim(preg_replace('/[^a-z0-9\s]/', '', $body));
        $bodyClean = preg_replace('/\s+/', ' ', $bodyClean);

        $accepted = [
            'ok', 'estoy ok', 'estoyok', '1', 'bien', 'estoy bien', 'estoybien', 'reporte', 'si', 'estoy a salvo', 'a salvo'
        ];

        $isValidPattern = in_array($bodyClean, $accepted, true) 
            || str_starts_with($bodyClean, 'ok') 
            || str_contains($bodyClean, 'estoy ok')
            || str_contains($bodyClean, 'estoy bien');

        if (! $isValidPattern) {
            Log::info("Evolution Webhook: Unrecognized body '{$rawBody}' (normalized: '{$bodyClean}') from user {$user->id}");
            $this->recordWebhookCall($payload, "unrecognized_body_{$bodyClean}", $user);
            return response()->json(['status' => 'unrecognized_body']);
        }

        // Process check-in
        $user->update([
            'last_check_in_at' => \Illuminate\Support\Carbon::now(),
        ]);
        $user->checkIns()->create(['source' => 'whatsapp']);
        $user->emergencyAlerts()->where('status', 'active')->update([
            'status' => 'resolved',
        ]);

        Log::info("Evolution Webhook: Check-in successfully registered via WhatsApp for user {$user->id} ({$user->name})");
        $this->recordWebhookCall($payload, 'SUCCESS_CHECKIN_PROCESSED', $user);

        // Send Silent Push / Refresh event to the user's mobile app if token exists
        if (! empty($user->expo_push_token)) {
            try {
                app(\App\Services\PushNotificationService::class)->sendPush(
                    $user->expo_push_token,
                    'Bienestar Actualizado',
                    'Tu bienestar se ha confirmado vía WhatsApp.',
                    [
                        'type' => 'check_in_update',
                        'source' => 'whatsapp',
                    ],
                    true
                );
            } catch (\Exception $e) {
                Log::warning("Evolution Webhook: Failed to send push refresh: " . $e->getMessage());
            }
        }

        // Confirmation reply via WhatsApp
        try {
            $whatsAppService = app(\App\Services\WhatsAppServiceInterface::class);
            $whatsAppService->sendWhatsApp($user->phone, '✅ Bienestar verificado con éxito en Estoy Ok. ¡Gracias!');
        } catch (\Exception $e) {
            Log::warning("Evolution Webhook: Failed to send WhatsApp confirmation to {$user->phone}: " . $e->getMessage());
        }

        return response()->json(['status' => 'success', 'message' => 'Check-in processed successfully']);
    }

    protected function recordWebhookCall(array $payload, string $decision, ?User $user = null): void
    {
        try {
            $history = \Illuminate\Support\Facades\Cache::get('evolution_webhook_history', []);
            array_unshift($history, [
                'time' => now()->toIso8601String(),
                'decision' => $decision,
                'user_id' => $user?->id,
                'user_name' => $user?->name,
                'payload_summary' => [
                    'event' => $payload['event'] ?? null,
                    'instance' => $payload['instance'] ?? null,
                    'keys' => array_keys($payload),
                ],
                'full_payload' => $payload,
            ]);
            \Illuminate\Support\Facades\Cache::put('evolution_webhook_history', array_slice($history, 0, 10), now()->addHours(24));
        } catch (\Throwable $e) {}
    }
}

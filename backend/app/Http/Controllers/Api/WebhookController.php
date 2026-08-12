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

    public function ultramsgMessage(Request $request)
    {
        Log::info('UltraMsg Message Webhook Received', $request->all());

        $from = $request->input('data.from') ?? $request->input('from');
        if (! $from) {
            return response()->json(['status' => 'ignored', 'reason' => 'missing_from']);
        }

        // Clean @c.us or whatsapp: prefix if present
        if (str_contains($from, '@')) {
            $from = explode('@', $from)[0];
        }
        if (str_starts_with($from, 'whatsapp:')) {
            $from = substr($from, 9);
        }

        // Clean phone format (keep numbers)
        $fromCleaned = preg_replace('/[^0-9]/', '', $from);

        // Find user by phone who is active (email verified)
        $user = User::whereNotNull('email_verified_at')
            ->get()
            ->first(function ($u) use ($fromCleaned) {
                if (empty($u->phone)) return false;
                $userPhoneClean = preg_replace('/[^0-9]/', '', $u->phone);
                return $userPhoneClean === $fromCleaned || str_ends_with($fromCleaned, $userPhoneClean) || str_ends_with($userPhoneClean, $fromCleaned);
            });

        if (! $user) {
            Log::info("UltraMsg Webhook: User not found for phone {$fromCleaned}");
            return response()->json(['status' => 'user_not_found']);
        }

        // Check if configuration is enabled
        if (! $user->allow_sms_whatsapp_checkin) {
            Log::info("UltraMsg Webhook: Check-in disabled for user {$user->id}");
            return response()->json(['status' => 'checkin_disabled']);
        }

        $body = trim(strtolower($request->input('data.body') ?? $request->input('body', '')));
        $acceptedPatterns = ['ok', 'estoy ok', '1', 'bien'];

        if (! in_array($body, $acceptedPatterns)) {
            Log::info("UltraMsg Webhook: Unrecognized body '{$body}' from user {$user->id}");
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

        // Optional confirmation reply via WhatsApp
        $whatsAppService = app(\App\Services\WhatsAppServiceInterface::class);
        $whatsAppService->sendWhatsApp($user->phone, 'Bienestar verificado con éxito en Estoy Ok. ¡Gracias!');

        return response()->json(['status' => 'success', 'message' => 'Check-in processed successfully']);
    }
}

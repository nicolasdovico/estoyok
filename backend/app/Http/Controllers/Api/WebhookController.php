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

    public function twilioMessage(Request $request)
    {
        Log::info('Twilio Message Webhook Received', $request->all());

        $from = $request->input('From');
        if (! $from) {
            return $this->twimlResponse('');
        }

        // Clean "whatsapp:" prefix if present
        if (str_starts_with($from, 'whatsapp:')) {
            $from = substr($from, 9);
        }

        // Clean E.164 phone format
        $fromCleaned = preg_replace('/[\s\-\(\)]+/', '', $from);

        // Find user by phone who is active (email verified)
        $user = User::where('phone', $fromCleaned)
            ->whereNotNull('email_verified_at')
            ->first();

        if (! $user) {
            return $this->twimlResponse('');
        }

        // Check if configuration is enabled
        if (! $user->allow_sms_whatsapp_checkin) {
            return $this->twimlResponse('La opción de Check-in por SMS/WhatsApp no está activa en tu cuenta. Por favor, actívala en tus Ajustes de Estoy Ok.');
        }

        $body = trim(strtolower($request->input('Body', '')));
        $acceptedPatterns = ['ok', 'estoy ok', '1', 'bien'];

        if (! in_array($body, $acceptedPatterns)) {
            return $this->twimlResponse('No reconocimos tu respuesta. Para reportar que estás bien, responde con: OK, ESTOY OK, 1 o BIEN.');
        }

        // Check time limit
        $isLocal = app()->environment('local');
        $unit = $isLocal ? 'minutes' : 'hours';
        $lastCheckIn = $user->last_check_in_at ?? $user->created_at;
        $expiresAt = $lastCheckIn->copy()->add($user->checkin_interval_hours, $unit);

        // Process check-in
        $user->update([
            'last_check_in_at' => \Illuminate\Support\Carbon::now(),
        ]);
        $user->checkIns()->create();
        $user->emergencyAlerts()->where('status', 'active')->update([
            'status' => 'resolved',
        ]);

        return $this->twimlResponse('Bienestar verificado con éxito en Estoy Ok. ¡Gracias!');
    }

    private function twimlResponse(string $message)
    {
        $xml = '<?xml version="1.0" encoding="UTF-8"?>';
        $xml .= '<Response>';
        if (! empty($message)) {
            $xml .= '<Message>' . e($message) . '</Message>';
        }
        $xml .= '</Response>';

        return response($xml, 200)->header('Content-Type', 'text/xml');
    }
}

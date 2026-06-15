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
}

<?php

namespace App\Services;

use Srmklive\PayPal\Services\PayPal as PayPalClient;

class PayPalService
{
    protected $provider;

    public function __construct()
    {
        $this->provider = new PayPalClient;
        $this->provider->setApiCredentials(config('paypal'));
        $token = $this->provider->getAccessToken();
        $this->provider->setAccessToken($token);
    }

    public function createSubscriptionLink($user, $planId = 'premium')
    {
        $successUrl = route('subscription.callback', ['provider' => 'paypal', 'status' => 'success', 'user_id' => $user->id]);
        $cancelUrl = route('subscription.callback', ['provider' => 'paypal', 'status' => 'cancel', 'user_id' => $user->id]);

        // Fallback back_urls if localhost/127.0.0.1
        if (str_contains($successUrl, 'localhost') || str_contains($successUrl, '127.0.0.1')) {
            $successUrl = 'https://frontend-web-production-f4f0.up.railway.app/api/subscriptions/callback/paypal?status=success&user_id=' . $user->id;
            $cancelUrl = 'https://frontend-web-production-f4f0.up.railway.app/api/subscriptions/callback/paypal?status=cancel&user_id=' . $user->id;
        }

        $data = [
            'intent' => 'CAPTURE',
            'purchase_units' => [
                [
                    'amount' => [
                        'currency_code' => 'USD',
                        'value' => '4.99',
                    ],
                    'description' => 'Suscripción Estoy Ok PRO (Mensual)',
                ],
            ],
            'application_context' => [
                'brand_name' => 'Estoy Ok PRO',
                'locale' => 'es-ES',
                'shipping_preference' => 'NO_SHIPPING',
                'user_action' => 'PAY_NOW',
                'return_url' => $successUrl,
                'cancel_url' => $cancelUrl,
            ],
        ];

        try {
            $response = $this->provider->createOrder($data);

            if (isset($response['links'])) {
                foreach ($response['links'] as $link) {
                    if ($link['rel'] === 'approve') {
                        return $link['href'];
                    }
                }
            }

            return null;
        } catch (\Exception $e) {
            \Log::error('PayPal Subscription Error: ' . $e->getMessage());

            throw $e;
        }
    }
}


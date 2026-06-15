<?php

namespace App\Services;

use Srmklive\PayPal\Services\PayPal as PayPalClient;

class PayPalService
{
    protected $provider;

    public function __construct()
    {
        if (config('paypal.sandbox.client_id') || config('paypal.live.client_id')) {
            $this->provider = new PayPalClient;
            $this->provider->getAccessToken();
        }
    }

    public function createSubscriptionLink($user, $planId = 'P-PREMIUM-PLAN-ID')
    {
        // $planId should be the ID created in PayPal dashboard

        $data = [
            'plan_id' => $planId,
            'quantity' => '1',
            'subscriber' => [
                'name' => [
                    'given_name' => $user->name,
                ],
                'email_address' => $user->email,
            ],
            'application_context' => [
                'brand_name' => config('app.name'),
                'locale' => 'en-US',
                'shipping_preference' => 'NO_SHIPPING',
                'user_action' => 'SUBSCRIBE_NOW',
                'return_url' => route('subscription.callback', ['provider' => 'paypal', 'status' => 'success']),
                'cancel_url' => route('subscription.callback', ['provider' => 'paypal', 'status' => 'cancel']),
            ],
        ];

        try {
            $response = $this->provider->createSubscription($data);

            if (isset($response['links'])) {
                foreach ($response['links'] as $link) {
                    if ($link['rel'] == 'approve') {
                        return $link['href'];
                    }
                }
            }

            return null;
        } catch (\Exception $e) {
            \Log::error('PayPal Subscription Error: '.$e->getMessage());

            return null;
        }
    }
}

<?php

namespace App\Services;

use MercadoPago\Client\Preapproval\PreapprovalClient;
use MercadoPago\MercadoPagoConfig;

class MercadoPagoService
{
    public function __construct()
    {
        $accessToken = config('services.mercadopago.access_token');
        if ($accessToken) {
            MercadoPagoConfig::setAccessToken($accessToken);
        }
    }

    public function createSubscriptionLink($user, $planId = 'premium')
    {
        $client = new PreapprovalClient;

        // This is a simplified example. In a real scenario, you'd have plan IDs in MP.
        // For now, we'll use a generic request structure.
        $request = [
            'back_url' => route('subscription.callback', ['provider' => 'mercadopago']),
            'reason' => 'Estoy Ok Premium Subscription',
            'auto_recurring' => [
                'frequency' => 1,
                'frequency_type' => 'months',
                'transaction_amount' => 1000, // Amount in ARS
                'currency_id' => 'ARS',
            ],
            'payer_email' => $user->email,
            'status' => 'pending',
        ];

        try {
            $preapproval = $client->create($request);

            return $preapproval->init_point;
        } catch (\Exception $e) {
            \Log::error('Mercado Pago Subscription Error: '.$e->getMessage());

            return null;
        }
    }
}

<?php

namespace App\Services;

use MercadoPago\Client\Preference\PreferenceClient;
use MercadoPago\Exceptions\MPApiException;
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
        $accessToken = config('services.mercadopago.access_token');
        if (! $accessToken) {
            throw new \Exception('Falta la variable MERCADOPAGO_ACCESS_TOKEN en el servidor.');
        }

        MercadoPagoConfig::setAccessToken($accessToken);
        $client = new PreferenceClient();

        $successUrl = route('subscription.callback', ['provider' => 'mercadopago', 'status' => 'success', 'user_id' => $user->id]);
        $cancelUrl = route('subscription.callback', ['provider' => 'mercadopago', 'status' => 'cancel', 'user_id' => $user->id]);

        // Fallback back_urls if localhost/127.0.0.1 (Mercado Pago rejects localhost back_urls)
        if (str_contains($successUrl, 'localhost') || str_contains($successUrl, '127.0.0.1')) {
            $successUrl = 'https://estoyok24.com/api/subscriptions/callback/mercadopago?status=success&user_id=' . $user->id;
            $cancelUrl = 'https://estoyok24.com/api/subscriptions/callback/mercadopago?status=cancel&user_id=' . $user->id;
        }

        $request = [
            'items' => [
                [
                    'title' => 'Suscripción Estoy Ok PRO (Mensual)',
                    'quantity' => 1,
                    'unit_price' => 4990.0,
                    'currency_id' => 'ARS',
                ]
            ],
            'back_urls' => [
                'success' => $successUrl,
                'failure' => $cancelUrl,
                'pending' => $successUrl,
            ],
            'auto_return' => 'approved',
            'binary_mode' => true,
            'external_reference' => (string) $user->id,
        ];

        try {
            $preference = $client->create($request);

            return $preference->init_point ?? $preference->sandbox_init_point;
        } catch (MPApiException $e) {
            $apiResponse = $e->getApiResponse();
            $content = $apiResponse ? json_encode($apiResponse->getContent()) : $e->getMessage();
            \Log::error('Mercado Pago API Exception: ' . $content);
            throw new \Exception('Mercado Pago API error: ' . $content);
        } catch (\Exception $e) {
            \Log::error('Mercado Pago Subscription Error: ' . $e->getMessage());
            throw $e;
        }
    }
}




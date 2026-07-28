<?php

namespace App\Services;

use MercadoPago\Client\PreApproval\PreApprovalClient;
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
        $client = new PreApprovalClient();

        $backUrl = route('subscription.callback', ['provider' => 'mercadopago', 'user_id' => $user->id]);

        // Fallback back_url if localhost/127.0.0.1 (Mercado Pago rejects localhost back_urls)
        if (str_contains($backUrl, 'localhost') || str_contains($backUrl, '127.0.0.1')) {
            $backUrl = 'https://frontend-web-production-f4f0.up.railway.app/api/subscriptions/callback/mercadopago';
        }

        $request = [
            'back_url' => $backUrl,
            'reason' => 'Suscripción Estoy Ok PRO',
            'auto_recurring' => [
                'frequency' => 1,
                'frequency_type' => 'months',
                'transaction_amount' => 4990, // Monto en ARS
                'currency_id' => 'ARS',
            ],
            'payer_email' => $user->email,
        ];

        try {
            $preapproval = $client->create($request);

            return $preapproval->init_point ?? $preapproval->sandbox_init_point;
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


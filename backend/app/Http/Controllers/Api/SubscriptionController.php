<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\MercadoPagoService;
use App\Services\PayPalService;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class SubscriptionController extends Controller
{
    /**
     * @OA\Post(
     *     path="/api/subscriptions/checkout",
     *     summary="Generate a checkout URL for a subscription",
     *     tags={"Subscriptions"},
     *     security={{"sanctum":{}}},
     *
     *     @OA\RequestBody(
     *         required=true,
     *
     *         @OA\JsonContent(
     *
     *             @OA\Property(property="provider", type="string", enum={"stripe", "mercadopago", "paypal"}),
     *             @OA\Property(property="plan", type="string", default="premium")
     *         )
     *     ),
     *
     *     @OA\Response(
     *         response=200,
     *         description="Checkout URL generated",
     *
     *         @OA\JsonContent(
     *
     *             @OA\Property(property="checkout_url", type="string")
     *         )
     *     )
     * )
     */
    public function checkout(Request $request, MercadoPagoService $mpService, PayPalService $paypalService)
    {
        $request->validate([
            'provider' => 'required|in:stripe,mercadopago,paypal',
            'plan' => 'string',
            'billing_cycle' => 'nullable|in:monthly,annual',
        ]);

        $user = Auth::user();
        $provider = $request->provider;
        $plan = $request->plan ?? 'premium';
        $billingCycle = $request->input('billing_cycle', 'monthly');

        $checkoutUrl = null;

        switch ($provider) {
            case 'stripe':
                try {
                    $secretKey = config('services.stripe.secret') ?? config('cashier.secret');
                    if ($secretKey) {
                        $priceId = ($billingCycle === 'annual')
                            ? (config('services.stripe.premium_price_id_annual') ?? env('STRIPE_PRICE_ID_ANNUAL'))
                            : (config('services.stripe.premium_price_id') ?? env('STRIPE_PRICE_ID_MONTHLY'));

                        if ($priceId) {
                            $checkoutUrl = $user->newSubscription('default', $priceId)
                                ->trialDays(7)
                                ->checkout([
                                    'success_url' => route('subscription.callback', ['provider' => 'stripe', 'status' => 'success', 'user_id' => $user->id]),
                                    'cancel_url' => route('subscription.callback', ['provider' => 'stripe', 'status' => 'cancel', 'user_id' => $user->id]),
                                ])->url;
                        }
                    }
                } catch (\Exception $e) {
                    \Log::error('Stripe Checkout Error: ' . $e->getMessage());
                    return response()->json([
                        'message' => 'Error de conexión con Stripe: ' . $e->getMessage()
                    ], 422);
                }
                break;

            case 'mercadopago':
                $checkoutUrl = $mpService->createSubscriptionLink($user, $plan);
                break;

            case 'paypal':
                $checkoutUrl = $paypalService->createSubscriptionLink($user, $plan);
                break;
        }

        if (! $checkoutUrl) {
            return response()->json([
                'message' => 'No se pudo generar el enlace de pago. Verifica que las claves de la pasarela (' . $provider . ') y el Price ID estén configurados en el servidor.'
            ], 422);
        }

        return response()->json(['checkout_url' => $checkoutUrl]);
    }

    /**
     * @OA\Post(
     *     path="/api/subscriptions/start-trial",
     *     summary="Start 7-day free trial for authenticated user via payment gateway",
     *     tags={"Subscriptions"},
     *     security={{"sanctum":{}}},
     *     @OA\Response(response=200, description="Redirects to payment gateway checkout")
     * )
     */
    public function startTrial(Request $request, MercadoPagoService $mpService, PayPalService $paypalService)
    {
        $user = Auth::user();

        if ($user->subscription_status === 'active' && $user->subscribed('default')) {
            return response()->json([
                'message' => 'Ya cuentas con una suscripción Premium activa.'
            ], 422);
        }

        return $this->checkout($request, $mpService, $paypalService);
    }

    /**
     * @OA\Get(
     *     path="/api/subscriptions/callback/{provider}",
     *     summary="Callback for subscription redirects",
     *     tags={"Subscriptions"},
     *
     *     @OA\Parameter(name="provider", in="path", required=true, @OA\Schema(type="string")),
     *
     *     @OA\Response(response=200, description="Redirects to app")
     * )
     */
    public function callback(Request $request, $provider)
    {
        $status = $request->query('status', 'unknown');
        $userId = $request->query('user_id');
        $user = ($userId ? User::find($userId) : null) ?? Auth::user();

        if ($status === 'success' && $user) {
            $user->update([
                'trial_ends_at' => now()->addDays(7),
                'subscription_status' => 'trialing',
                'subscription_provider' => $provider,
                'is_premium' => true,
            ]);
        }

        if ($request->wantsJson()) {
            return response()->json([
                'message' => 'Subscription process finished',
                'provider' => $provider,
                'status' => $status,
            ]);
        }

        $isSuccess = $status === 'success';
        $title = $isSuccess ? '¡Prueba Gratuita Activada!' : 'Proceso de Suscripción';
        $message = $isSuccess
            ? 'Tu prueba de 7 días de <strong>Estoy Ok PRO</strong> se ha registrado correctamente. Tu familia ya cuenta con la máxima protección.'
            : 'El proceso de suscripción se ha completado. Puedes volver a la aplicación.';

        $html = "
<!DOCTYPE html>
<html lang='es'>
<head>
    <meta charset='UTF-8'>
    <meta name='viewport' content='width=device-width, initial-scale=1.0'>
    <title>Estoy Ok — Suscripción</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background-color: #0F172A;
            color: #FFFFFF;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            margin: 0;
            padding: 20px;
            box-sizing: border-box;
        }
        .card {
            background-color: #1E293B;
            border: 1px solid #00E5D9;
            border-radius: 16px;
            padding: 32px;
            max-width: 440px;
            width: 100%;
            text-align: center;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5);
        }
        .icon { font-size: 48px; margin-bottom: 16px; }
        h1 { color: #00E5D9; font-size: 22px; margin-bottom: 8px; margin-top: 0; }
        p { color: #94A3B8; font-size: 14px; line-height: 1.5; margin-bottom: 24px; }
        .btn {
            display: inline-block;
            background-color: #00E5D9;
            color: #0F172A;
            font-weight: bold;
            text-decoration: none;
            padding: 14px 28px;
            border-radius: 10px;
            font-size: 15px;
            width: 100%;
            box-sizing: border-box;
        }
    </style>
</head>
<body>
    <div class='card'>
        <div class='icon'>👑</div>
        <h1>{$title}</h1>
        <p>{$message}</p>
        <a href='javascript:history.back()' class='btn' onclick='window.close()'>Volver a Estoy Ok</a>
    </div>
</body>
</html>
        ";

        return response($html, 200)->header('Content-Type', 'text/html');
    }

    /**
     * @OA\Post(
     *     path="/api/subscriptions/cancel",
     *     summary="Cancel subscription or free trial for authenticated user",
     *     tags={"Subscriptions"},
     *     security={{"sanctum":{}}},
     *     @OA\Response(response=200, description="Subscription canceled successfully")
     * )
     */
    public function cancelSubscription(Request $request)
    {
        $user = Auth::user();

        if ($user->subscribed('default')) {
            try {
                $user->subscription('default')->cancel();
            } catch (\Exception $e) {
                // Log and continue local cancellation
            }
        }

        $user->update([
            'subscription_status' => 'canceled',
            'is_premium' => false,
        ]);

        return response()->json([
            'message' => 'Tu prueba gratuita o suscripción ha sido cancelada sin costo alguno.',
            'user' => $user->fresh()
        ]);
    }
}

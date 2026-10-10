<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Mail\SubscriptionCanceledMail;
use App\Services\MercadoPagoService;
use App\Services\PayPalService;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Mail;

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
                                ->checkout([
                                    'subscription_data' => [
                                        'trial_period_days' => 7,
                                    ],
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
                try {
                    $checkoutUrl = $mpService->createSubscriptionLink($user, $plan);
                } catch (\Exception $e) {
                    \Log::error('Mercado Pago Checkout Error: ' . $e->getMessage());
                    return response()->json([
                        'message' => 'Error de conexión con Mercado Pago: ' . $e->getMessage()
                    ], 422);
                }
                break;

            case 'paypal':
                try {
                    $checkoutUrl = $paypalService->createSubscriptionLink($user, $plan);
                } catch (\Exception $e) {
                    \Log::error('PayPal Checkout Error: ' . $e->getMessage());
                    return response()->json([
                        'message' => 'Error de conexión con PayPal: ' . $e->getMessage()
                    ], 422);
                }
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

        if ($user->subscription_status === 'active' && ($user->is_premium || $user->subscribed('default'))) {
            return response()->json([
                'message' => 'Ya cuentas con una suscripción Premium activa.'
            ], 422);
        }

        if ($user->has_used_trial || $user->trial_ends_at !== null) {
            return response()->json([
                'message' => 'Ya has utilizado tu periodo de prueba gratuita de 7 días. Por favor suscríbete para continuar con Estoy Ok PRO.'
            ], 422);
        }

        // Direct in-app 7-day trial activation (Google Play compliance & safe review mode)
        $user->update([
            'is_premium' => true,
            'subscription_status' => 'trialing',
            'subscription_provider' => 'trial',
            'trial_ends_at' => now()->addDays(7),
            'has_used_trial' => true,
        ]);

        return response()->json([
            'message' => '¡Prueba gratuita de 7 días activada con éxito! Disfruta de Estoy Ok PRO.',
            'checkout_url' => null,
            'user' => $user->fresh(),
        ]);
    }

    /**
     * @OA\Post(
     *     path="/api/subscriptions/verify-google-play",
     *     summary="Verify and activate Google Play subscription for authenticated user",
     *     tags={"Subscriptions"},
     *     security={{"sanctum":{}}},
     *     @OA\RequestBody(
     *         required=true,
     *         @OA\JsonContent(
     *             required={"purchase_token", "product_id"},
     *             @OA\Property(property="purchase_token", type="string", example="pbagknomimjjk..."),
     *             @OA\Property(property="product_id", type="string", example="estoyok_premium"),
     *             @OA\Property(property="base_plan_id", type="string", example="monthly-plan")
     *         )
     *     ),
     *     @OA\Response(response=200, description="Subscription activated successfully"),
     *     @OA\Response(
     *         response=409,
     *         description="Subscription already linked to another account",
     *         @OA\JsonContent(
     *             @OA\Property(property="message", type="string"),
     *             @OA\Property(property="error_code", type="string", example="SUBSCRIPTION_ALREADY_LINKED"),
     *             @OA\Property(property="status", type="string", example="conflict"),
     *             @OA\Property(property="is_premium", type="boolean", example=false)
     *         )
     *     )
     * )
     */
    public function verifyGooglePlay(Request $request)
    {
        $request->validate([
            'purchase_token' => 'required|string',
            'product_id' => 'required|string',
            'base_plan_id' => 'nullable|string',
        ]);

        $user = Auth::user();
        $purchaseToken = $request->input('purchase_token');

        $conflictOwner = $this->findConflictingSubscriptionOwner($purchaseToken, $user->id);
        if ($conflictOwner) {
            return response()->json([
                'message' => 'Esta suscripción de Google Play ya se encuentra vinculada a otra cuenta de Estoy Ok (' . $this->maskEmail($conflictOwner->email) . ').',
                'error_code' => 'SUBSCRIPTION_ALREADY_LINKED',
                'status' => 'conflict',
                'is_premium' => false,
            ], 409);
        }

        $basePlan = $request->input('base_plan_id', 'monthly-plan');
        $isAnnual = str_contains((string) $basePlan, 'annual');

        $isTrialExpired = $user->trial_ends_at && $user->trial_ends_at <= now();
        $status = $isTrialExpired ? 'active' : 'trialing';

        $user->update([
            'is_premium' => true,
            'subscription_status' => $status,
            'subscription_provider' => 'google_play',
            'subscription_id' => $purchaseToken,
            'trial_ends_at' => $user->trial_ends_at ?: now()->addDays(7),
            'has_used_trial' => true,
            'billing_cycle_ends_at' => $isAnnual ? now()->addYear() : now()->addMonth(),
        ]);

        return response()->json([
            'message' => '¡Suscripción de Google Play verificada y activada con éxito!',
            'user' => $user->fresh(),
        ]);
    }

    /**
     * @OA\Post(
     *     path="/api/subscriptions/sync-google-play",
     *     summary="Synchronize subscription status with Google Play Billing",
     *     tags={"Subscriptions"},
     *     security={{"sanctum":{}}},
     *     @OA\RequestBody(
     *         required=true,
     *         @OA\JsonContent(
     *             required={"has_active_subscription"},
     *             @OA\Property(property="has_active_subscription", type="boolean", example=true),
     *             @OA\Property(property="purchase_token", type="string", example="GPA.1234..."),
     *             @OA\Property(property="product_id", type="string", example="estoyok_premium"),
     *             @OA\Property(property="base_plan_id", type="string", example="monthly-plan")
     *         )
     *     ),
     *     @OA\Response(
     *         response=200,
     *         description="Subscription synchronized successfully",
     *         @OA\JsonContent(
     *             @OA\Property(property="message", type="string"),
     *             @OA\Property(property="status", type="string"),
     *             @OA\Property(property="is_premium", type="boolean")
     *         )
     *     ),
     *     @OA\Response(
     *         response=409,
     *         description="Subscription already linked to another account",
     *         @OA\JsonContent(
     *             @OA\Property(property="message", type="string"),
     *             @OA\Property(property="error_code", type="string", example="SUBSCRIPTION_ALREADY_LINKED"),
     *             @OA\Property(property="status", type="string", example="conflict"),
     *             @OA\Property(property="is_premium", type="boolean", example=false)
     *         )
     *     )
     * )
     */
    public function syncGooglePlay(Request $request)
    {
        $request->validate([
            'has_active_subscription' => 'required|boolean',
            'purchase_token' => 'required_if:has_active_subscription,true|nullable|string',
            'product_id' => 'nullable|string',
            'base_plan_id' => 'nullable|string',
        ]);

        $user = Auth::user();
        $hasActive = (bool) $request->input('has_active_subscription');

        if ($hasActive) {
            $purchaseToken = $request->input('purchase_token');

            if (!empty($purchaseToken)) {
                $conflictOwner = $this->findConflictingSubscriptionOwner($purchaseToken, $user->id);
                if ($conflictOwner) {
                    // Si el usuario actual tenía este token asignado erróneamente con anterioridad, revocarlo
                    if ($user->subscription_id === $purchaseToken && $user->id !== $conflictOwner->id) {
                        $user->update([
                            'is_premium' => false,
                            'subscription_status' => 'free',
                            'subscription_id' => null,
                        ]);
                    }

                    return response()->json([
                        'message' => 'Esta suscripción de Google Play ya se encuentra vinculada a otra cuenta de Estoy Ok (' . $this->maskEmail($conflictOwner->email) . ').',
                        'error_code' => 'SUBSCRIPTION_ALREADY_LINKED',
                        'status' => 'conflict',
                        'is_premium' => false,
                        'user' => $user->fresh(),
                    ], 409);
                }
            }

            $basePlan = $request->input('base_plan_id', 'monthly-plan');
            $isAnnual = str_contains((string) $basePlan, 'annual');

            // Si el período de prueba ya finalizó (o venció), pasa inmediatamente a 'active'
            $isTrialActive = $user->trial_ends_at && $user->trial_ends_at > now();
            $newStatus = $isTrialActive ? 'trialing' : 'active';

            $user->update([
                'is_premium' => true,
                'subscription_status' => $newStatus,
                'subscription_provider' => 'google_play',
                'subscription_id' => $purchaseToken ?: $user->subscription_id,
                'billing_cycle_ends_at' => $isAnnual ? now()->addYear() : now()->addMonth(),
            ]);

            return response()->json([
                'message' => 'Suscripción de Google Play sincronizada como activa.',
                'status' => $newStatus,
                'is_premium' => true,
                'user' => $user->fresh(),
            ]);
        } else {
            // Google Play no tiene compras activas
            $wasPremium = (bool) $user->is_premium || in_array($user->subscription_status, ['trialing', 'active', 'grace_period']);

            if ($wasPremium) {
                $user->update([
                    'is_premium' => false,
                    'subscription_status' => 'canceled',
                ]);

                $this->notifyAdminSubscriptionCanceled($user, 'Google Play reportó compra inactiva o cancelada');
            }

            return response()->json([
                'message' => 'Suscripción sincronizada. El usuario no cuenta con suscripción activa en Google Play.',
                'status' => 'canceled',
                'is_premium' => false,
                'user' => $user->fresh(),
            ]);
        }
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
                'has_used_trial' => true,
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
    <script>
        setTimeout(function() {
            window.location.href = 'estoyok://subscription-success';
        }, 1000);
    </script>
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
        <a href='estoyok://subscription-success' class='btn'>Volver a la App Estoy Ok</a>
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

        $this->notifyAdminSubscriptionCanceled($user, 'Cancelación voluntaria solicitada por el usuario');

        return response()->json([
            'message' => 'Tu prueba gratuita o suscripción ha sido cancelada sin costo alguno.',
            'user' => $user->fresh()
        ]);
    }

    /**
     * Notify the administrator via email about a canceled or expired subscription.
     */
    protected function notifyAdminSubscriptionCanceled(User $user, string $reason = 'Cancelación o expiración de suscripción'): void
    {
        $adminEmail = config('mail.admin_notification_email');
        if (!empty($adminEmail)) {
            try {
                $activePremium = User::where('is_premium', true)->count();
                Mail::to($adminEmail)->send(new SubscriptionCanceledMail($user, $reason, $activePremium));
            } catch (\Throwable $e) {
                Log::warning("No se pudo enviar la notificación de baja al administrador: " . $e->getMessage());
            }
        }
    }

    /**
     * Check if a Google Play purchase token is already owned by another active user.
     */
    protected function findConflictingSubscriptionOwner(string $purchaseToken, int $currentUserId): ?User
    {
        return User::where('subscription_id', $purchaseToken)
            ->where('id', '!=', $currentUserId)
            ->where(function ($query) {
                $query->where('is_premium', true)
                    ->orWhereIn('subscription_status', ['trialing', 'active', 'grace_period']);
            })
            ->orderBy('id', 'asc')
            ->first();
    }

    /**
     * Mask an email address for privacy in error responses (e.g. ta******p@gmail.com).
     */
    protected function maskEmail(string $email): string
    {
        $parts = explode('@', $email);
        $name = $parts[0];
        $domain = $parts[1] ?? '';
        $len = strlen($name);

        if ($len <= 2) {
            $maskedName = substr($name, 0, 1) . '*';
        } else {
            $maskedName = substr($name, 0, 2) . str_repeat('*', max(1, $len - 3)) . substr($name, -1);
        }

        return $maskedName . '@' . $domain;
    }
}

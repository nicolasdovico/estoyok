<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\MercadoPagoService;
use App\Services\PayPalService;
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
        ]);

        $user = Auth::user();
        $provider = $request->provider;
        $plan = $request->plan ?? 'premium';

        $checkoutUrl = null;

        switch ($provider) {
            case 'stripe':
                $checkoutUrl = $user->newSubscription('default', config('services.stripe.premium_price_id'))
                    ->trialDays(7)
                    ->checkout([
                        'success_url' => route('subscription.callback', ['provider' => 'stripe', 'status' => 'success']),
                        'cancel_url' => route('subscription.callback', ['provider' => 'stripe', 'status' => 'cancel']),
                    ])->url;
                break;

            case 'mercadopago':
                $checkoutUrl = $mpService->createSubscriptionLink($user, $plan);
                break;

            case 'paypal':
                $checkoutUrl = $paypalService->createSubscriptionLink($user, $plan);
                break;
        }

        if (! $checkoutUrl) {
            return response()->json(['message' => 'Could not generate checkout URL'], 500);
        }

        return response()->json(['checkout_url' => $checkoutUrl]);
    }

    /**
     * @OA\Post(
     *     path="/api/subscriptions/start-trial",
     *     summary="Start 7-day free trial for authenticated user",
     *     tags={"Subscriptions"},
     *     security={{"sanctum":{}}},
     *     @OA\Response(response=200, description="Trial started successfully"),
     *     @OA\Response(response=422, description="Trial already consumed")
     * )
     */
    public function startTrial(Request $request)
    {
        $user = Auth::user();

        if ($user->trial_ends_at !== null || $user->subscription_status === 'trialing') {
            return response()->json([
                'message' => 'Ya has utilizado o tienes activa la prueba gratuita de 7 días.'
            ], 422);
        }

        $user->update([
            'trial_ends_at' => now()->addDays(7),
            'subscription_status' => 'trialing',
            'subscription_provider' => $request->input('provider', 'stripe'),
        ]);

        return response()->json([
            'message' => 'Prueba gratuita de 7 días activada con éxito.',
            'user' => $user->fresh()
        ]);
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

        if ($status === 'success') {
            $user = Auth::user();
            if ($user && $user->trial_ends_at === null) {
                $user->update([
                    'trial_ends_at' => now()->addDays(7),
                    'subscription_status' => 'trialing',
                    'subscription_provider' => $provider,
                ]);
            }
        }

        return response()->json([
            'message' => 'Subscription process finished',
            'provider' => $provider,
            'status' => $status,
        ]);
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

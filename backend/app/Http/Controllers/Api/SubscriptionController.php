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
     *     @OA\RequestBody(
     *         required=true,
     *         @OA\JsonContent(
     *             @OA\Property(property="provider", type="string", enum={"stripe", "mercadopago", "paypal"}),
     *             @OA\Property(property="plan", type="string", default="premium")
     *         )
     *     ),
     *     @OA\Response(
     *         response=200,
     *         description="Checkout URL generated",
     *         @OA\JsonContent(
     *             @OA\Property(property="checkout_url", type="string")
     *         )
     *     )
     * )
     */
    public function checkout(Request $request, MercadoPagoService $mpService, PayPalService $paypalService)
    {
        $request->validate([
            'provider' => 'required|in:stripe,mercadopago,paypal',
            'plan' => 'string'
        ]);

        $user = Auth::user();
        $provider = $request->provider;
        $plan = $request->plan ?? 'premium';

        $checkoutUrl = null;

        switch ($provider) {
            case 'stripe':
                $checkoutUrl = $user->newSubscription('default', config('services.stripe.premium_price_id'))
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

        if (!$checkoutUrl) {
            return response()->json(['message' => 'Could not generate checkout URL'], 500);
        }

        return response()->json(['checkout_url' => $checkoutUrl]);
    }

    /**
     * @OA\Get(
     *     path="/api/subscriptions/callback/{provider}",
     *     summary="Callback for subscription redirects",
     *     tags={"Subscriptions"},
     *     @OA\Parameter(name="provider", in="path", required=true, @OA\Schema(type="string")),
     *     @OA\Response(response=200, description="Redirects to app")
     * )
     */
    public function callback(Request $request, $provider)
    {
        // Here you would normally redirect to the frontend with a success/error message
        // For now, we'll return a JSON or a simple view
        return response()->json([
            'message' => 'Subscription process finished',
            'provider' => $provider,
            'status' => $request->query('status', 'unknown')
        ]);
    }
}

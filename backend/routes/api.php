<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\CheckInController;
use App\Http\Controllers\Api\SubscriptionController;
use App\Http\Controllers\Api\WebhookController;
use App\Http\Controllers\Api\LocationController;
use App\Http\Controllers\EmergencyAlertController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

Route::post('/register', [AuthController::class, 'register']);
Route::post('/login', [AuthController::class, 'login']);
Route::post('/verify-email', [AuthController::class, 'verifyEmail']);
Route::post('/resend-otp', [AuthController::class, 'resendOtp']);

// Webhooks
Route::post('/webhooks/mercadopago', [WebhookController::class, 'mercadopago']);
Route::post('/webhooks/paypal', [WebhookController::class, 'paypal']);
Route::post('/webhooks/stripe', [\Laravel\Cashier\Http\Controllers\WebhookController::class, 'handleWebhook']);

// Emergency Routes (Public)
Route::get('/emergency-alerts/{id}', [EmergencyAlertController::class, 'show']);

Route::middleware('auth:sanctum')->group(function () {
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::post('/check-in', [CheckInController::class, 'store']);
    
    // Subscriptions
    Route::post('/subscriptions/checkout', [SubscriptionController::class, 'checkout']);
    Route::get('/subscriptions/callback/{provider}', [SubscriptionController::class, 'callback'])->name('subscription.callback');

    // Tracking
    Route::post('/locations/update', [LocationController::class, 'update']);

    Route::get('/user', function (Request $request) {
        return $request->user()->load(['currentLocation', 'circles']);
    });
});

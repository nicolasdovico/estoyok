<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\CheckInController;
use App\Http\Controllers\Api\CircleController;
use App\Http\Controllers\Api\EmergencyContactController;
use App\Http\Controllers\Api\GeofenceController;
use App\Http\Controllers\Api\LocationController;
use App\Http\Controllers\Api\SettingsController;
use App\Http\Controllers\Api\SubscriptionController;
use App\Http\Controllers\Api\WebhookController;
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
Route::post('/webhooks/stripe', [Laravel\Cashier\Http\Controllers\WebhookController::class, 'handleWebhook']);
Route::post('/webhooks/twilio/message', [WebhookController::class, 'twilioMessage']);

// Emergency Routes (Public)
Route::get('/emergency-alerts/{id}', [EmergencyAlertController::class, 'show']);
Route::post('/emergency-alerts/{id}/respond', [EmergencyAlertController::class, 'respond']);

Route::middleware('auth:sanctum')->group(function () {
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::post('/check-in', [CheckInController::class, 'store']);
    Route::get('/check-ins', [CheckInController::class, 'index']);

    // Subscriptions
    Route::post('/subscriptions/checkout', [SubscriptionController::class, 'checkout']);
    Route::get('/subscriptions/callback/{provider}', [SubscriptionController::class, 'callback'])->name('subscription.callback');

    // Tracking
    Route::post('/locations/update', [LocationController::class, 'update']);

    // Circles
    Route::get('/circles', [CircleController::class, 'index']);
    Route::post('/circles', [CircleController::class, 'store']);
    Route::post('/circles/join', [CircleController::class, 'join']);
    Route::delete('/circles/{circle}/members/{member}', [CircleController::class, 'removeMember']);
    Route::delete('/circles/{circle}', [CircleController::class, 'destroy']);

    // Geofences
    Route::get('/circles/{circle}/geofences', [GeofenceController::class, 'index']);
    Route::post('/circles/{circle}/geofences', [GeofenceController::class, 'store']);
    Route::put('/geofences/{geofence}', [GeofenceController::class, 'update']);
    Route::delete('/geofences/{geofence}', [GeofenceController::class, 'destroy']);

    // Emergency Contacts
    Route::post('/emergency-contacts/reorder', [EmergencyContactController::class, 'reorder']);
    Route::apiResource('emergency-contacts', EmergencyContactController::class);

    // Settings
    Route::put('/settings/checkin-interval', [SettingsController::class, 'updateCheckinInterval']);
    Route::put('/settings/quiet-hours', [SettingsController::class, 'updateQuietHours']);
    Route::put('/settings/sms-whatsapp-checkin', [SettingsController::class, 'updateSmsWhatsappCheckin']);
    Route::put('/settings/escalation', [SettingsController::class, 'updateEscalation']);
    Route::put('/settings/privacy', [SettingsController::class, 'updatePrivacy']);
    Route::put('/settings/automation', [SettingsController::class, 'updateAutomation']);

    Route::get('/user', function (Request $request) {
        return $request->user()->load(['currentLocation', 'circles', 'emergencyContacts']);
    });
});

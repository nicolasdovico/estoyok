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
use App\Http\Controllers\Api\HistoryController;
use App\Http\Controllers\Api\DynamicGeofenceController;
use App\Http\Controllers\Api\DriveController;
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

// Temporary Debug Route for Geofence Diagnostics on Railway
Route::get('/debug/geofence-test', function (Illuminate\Http\Request $request) {
    $email = $request->query('email', 'analia@gmail.com');
    $user = \App\Models\User::where('email', $email)->first();
    if (!$user) {
        return response()->json(['error' => "User $email not found"], 404);
    }

    $circleIds = $user->circles->pluck('id');
    $geofences = \App\Models\Geofence::whereIn('circle_id', $circleIds)->get();

    $geofencesData = [];
    foreach ($geofences as $g) {
        $lastEvent = \App\Models\GeofenceEvent::where('user_id', $user->id)
            ->where('geofence_id', $g->id)
            ->latest('occurred_at')
            ->first();

        $members = $g->circle->users()->where('users.id', '!=', $user->id)->get()->map(function ($m) {
            return [
                'email' => $m->email,
                'has_push_token' => !empty($m->expo_push_token),
                'push_token_preview' => $m->expo_push_token ? substr($m->expo_push_token, 0, 30) . '...' : null,
            ];
        });

        $geofencesData[] = [
            'geofence_name' => $g->name,
            'is_active' => $g->is_active,
            'user_id' => $g->user_id,
            'last_event' => $lastEvent ? [
                'type' => $lastEvent->type,
                'occurred_at' => $lastEvent->occurred_at
            ] : null,
            'members_to_notify' => $members
        ];
    }

    $triggered = false;
    $syncMode = false;
    if ($request->query('trigger') == '1' && $geofences->isNotEmpty()) {
        $firstGeofence = $geofences->first();
        \App\Models\GeofenceEvent::create([
            'user_id' => $user->id,
            'geofence_id' => $firstGeofence->id,
            'type' => 'entry',
            'occurred_at' => now()->subMinutes(5)
        ]);
        
        if ($request->query('sync') == '1') {
            \App\Jobs\ProcessGeofencing::dispatchSync($user, 0.0, 0.0);
            $syncMode = true;
        } else {
            \App\Jobs\ProcessGeofencing::dispatch($user, 0.0, 0.0);
        }
        $triggered = true;
    }

    return response()->json([
        'user' => [
            'id' => $user->id,
            'email' => $user->email,
            'has_push_token' => !empty($user->expo_push_token),
            'push_token_preview' => $user->expo_push_token ? substr($user->expo_push_token, 0, 30) . '...' : null,
        ],
        'circles' => $user->circles->pluck('name', 'id'),
        'geofences' => $geofencesData,
        'triggered_simulation' => $triggered,
        'triggered_sync_mode' => $syncMode,
        'queue_connection' => config('queue.default'),
        'app_env' => config('app.env')
    ]);
});

Route::middleware('auth:sanctum')->group(function () {
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::post('/check-in', [CheckInController::class, 'store']);
    Route::get('/check-ins', [CheckInController::class, 'index']);
    Route::post('/emergency-alerts/sos', [EmergencyAlertController::class, 'storeSos']);
    Route::post('/emergency-alerts/{id}/audio', [EmergencyAlertController::class, 'uploadAudio']);
    Route::post('/alerts/crash', [EmergencyAlertController::class, 'storeCrash']);
    Route::post('/alerts/crash/{id}/false-alarm', [EmergencyAlertController::class, 'falseAlarm']);

    // Subscriptions
    Route::post('/subscriptions/checkout', [SubscriptionController::class, 'checkout']);
    Route::get('/subscriptions/callback/{provider}', [SubscriptionController::class, 'callback'])->name('subscription.callback');

    // Tracking
    Route::post('/locations/update', [LocationController::class, 'update']);
    Route::put('/locations/sensor-status', [LocationController::class, 'updateSensorStatus']);

    // Circles
    Route::get('/circles', [CircleController::class, 'index']);
    Route::post('/circles', [CircleController::class, 'store']);
    Route::post('/circles/join', [CircleController::class, 'join']);
    Route::delete('/circles/{circle}/members/{member}', [CircleController::class, 'removeMember']);
    Route::delete('/circles/{circle}', [CircleController::class, 'destroy']);
    Route::put('/circles/{circle}/speed-limit', [CircleController::class, 'updateSpeedLimit']);
    Route::get('/circles/{circle}/members/{member}/history', [HistoryController::class, 'getHistory']);
    Route::get('/circles/{circle}/members/{member}/drives', [DriveController::class, 'getDrives']);

    // Geofences
    Route::get('/circles/{circle}/geofences', [GeofenceController::class, 'index']);
    Route::post('/circles/{circle}/geofences', [GeofenceController::class, 'store']);
    Route::put('/geofences/{geofence}', [GeofenceController::class, 'update']);
    Route::delete('/geofences/{geofence}', [GeofenceController::class, 'destroy']);

    // Dynamic Geofences
    Route::get('/dynamic-geofences/active', [DynamicGeofenceController::class, 'active']);
    Route::post('/dynamic-geofences', [DynamicGeofenceController::class, 'store']);
    Route::post('/dynamic-geofences/{id}/deactivate', [DynamicGeofenceController::class, 'deactivate']);

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
    Route::put('/settings/proximity-alerts', [SettingsController::class, 'updateProximityAlerts']);
    Route::put('/settings/push-token', [SettingsController::class, 'updatePushToken']);
    Route::post('/settings/avatar', [SettingsController::class, 'updateAvatar']);

    Route::get('/user', function (Request $request) {
        return $request->user()->load(['currentLocation', 'circles', 'emergencyContacts']);
    });
});

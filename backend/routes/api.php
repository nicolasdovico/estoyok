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

Route::post('/maintenance/purge-noise-drives', function () {
    // 1. Purgar eventos de viaje duplicados por peticiones concurrentes (mismo usuario dentro de 10s)
    $count1 = \Illuminate\Support\Facades\DB::delete("
        DELETE FROM drive_events
        WHERE id IN (
            SELECT d1.id
            FROM drive_events d1
            JOIN drive_events d2 
              ON d1.user_id = d2.user_id 
             AND d1.id < d2.id
             AND ABS(EXTRACT(EPOCH FROM (d1.start_time - d2.start_time))) <= 10
        )
    ");

    // 2. Purgar eventos de viaje cortos menores a 60 segundos
    $count2 = \Illuminate\Support\Facades\DB::delete("
        DELETE FROM drive_events
        WHERE end_time IS NOT NULL
          AND EXTRACT(EPOCH FROM (end_time - start_time)) < 60
    ");

    // 3. Purgar eventos de viaje donde la velocidad máxima fue menor a 3 km/h o tienen menos de 4 puntos reales (estacionado/estático)
    $count3 = \Illuminate\Support\Facades\DB::delete("
        DELETE FROM drive_events
        WHERE id IN (
            SELECT d.id
            FROM drive_events d
            LEFT JOIN location_histories lh 
               ON lh.user_id = d.user_id 
              AND lh.recorded_at BETWEEN d.start_time AND d.end_time
            WHERE d.end_time IS NOT NULL
            GROUP BY d.id
            HAVING COUNT(lh.id) < 4 OR COALESCE(MAX(lh.speed), 0) < 3.0
        )
    ");

    $total = $count1 + $count2 + $count3;
    return response()->json(['message' => "Purged {$total} ghost noise drives."]);
});

Route::post('/maintenance/purge-all-drives', function () {
    try {
        \Illuminate\Support\Facades\DB::statement("DELETE FROM drive_events;");
        \Illuminate\Support\Facades\DB::statement("DELETE FROM location_histories;");
        \Illuminate\Support\Facades\DB::table('current_locations')->update([
            'is_driving' => false,
            'speed' => 0.0
        ]);
        return response()->json(['message' => 'All drive events and location histories purged successfully. Clean slate restored.']);
    } catch (\Throwable $e) {
        return response()->json(['error' => $e->getMessage()], 500);
    }
});

Route::get('/maintenance/debug-wifi-user', function (Request $request) {
    $email = $request->query('email', 'nicolas@gmail.com');
    $user = \App\Models\User::where('email', $email)->first();
    if (!$user) {
        $allUsers = \App\Models\User::select('id', 'email', 'name')->get();
        return response()->json(['error' => 'User not found', 'searched_email' => $email, 'all_users' => $allUsers], 404);
    }
    
    $loc = \App\Models\CurrentLocation::where('user_id', $user->id)->first();
    $checkIns = \App\Models\CheckIn::where('user_id', $user->id)->orderBy('created_at', 'desc')->take(10)->get();
    $cacheKey = 'auto_checkin_wifi_' . $user->id;
    $inCache = cache()->has($cacheKey);
    
    return response()->json([
        'user' => [
            'id' => $user->id,
            'email' => $user->email,
            'name' => $user->name,
            'wifi_checkin_enabled' => (bool)$user->wifi_checkin_enabled,
            'safe_wifi_ssid' => $user->safe_wifi_ssid,
            'checkin_interval_hours' => $user->checkin_interval_hours,
            'last_check_in_at' => $user->last_check_in_at ? $user->last_check_in_at->toIso8601String() : null,
            'hours_since_last_checkin' => $user->last_check_in_at ? round(now()->diffInMinutes($user->last_check_in_at) / 60, 2) : null,
        ],
        'current_location' => $loc ? [
            'recorded_at' => $loc->recorded_at ? $loc->recorded_at->toIso8601String() : null,
            'last_seen_at' => $loc->last_seen_at ? $loc->last_seen_at->toIso8601String() : null,
            'is_tracking_active' => $loc->is_tracking_active,
            'gps_enabled' => $loc->gps_enabled,
        ] : null,
        'recent_checkins' => $checkIns->map(fn($c) => [
            'id' => $c->id,
            'source' => $c->source,
            'created_at' => $c->created_at->toIso8601String(),
        ]),
        'cache' => [
            'key' => $cacheKey,
            'is_locked' => $inCache,
        ]
    ]);
});

// Webhooks
Route::post('/webhooks/mercadopago', [WebhookController::class, 'mercadopago']);
Route::post('/webhooks/paypal', [WebhookController::class, 'paypal']);
Route::post('/webhooks/stripe', [Laravel\Cashier\Http\Controllers\WebhookController::class, 'handleWebhook']);
Route::post('/webhooks/twilio/message', [WebhookController::class, 'twilioMessage']);

// Emergency Routes (Public)
Route::get('/emergency-alerts/{id}', [EmergencyAlertController::class, 'show']);
Route::post('/emergency-alerts/{id}/respond', [EmergencyAlertController::class, 'respond']);

// Public Subscription Callbacks
Route::get('/subscriptions/callback/{provider}', [SubscriptionController::class, 'callback'])->name('subscription.callback');

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
    Route::post('/subscriptions/start-trial', [SubscriptionController::class, 'startTrial']);
    Route::post('/subscriptions/cancel', [SubscriptionController::class, 'cancelSubscription']);

    // Tracking
    Route::post('/locations/update', [LocationController::class, 'update']);
    Route::put('/locations/sensor-status', [LocationController::class, 'updateSensorStatus']);
    Route::post('/locations/cleanup-history', [LocationController::class, 'cleanupHistory']);

    // Circles
    Route::get('/circles', [CircleController::class, 'index']);
    Route::post('/circles', [CircleController::class, 'store']);
    Route::post('/circles/join', [CircleController::class, 'join']);
    Route::delete('/circles/{circle}/members/{member}', [CircleController::class, 'removeMember']);
    Route::delete('/circles/{circle}', [CircleController::class, 'destroy']);
    Route::put('/circles/{circle}/speed-limit', [CircleController::class, 'updateSpeedLimit']);
    Route::post('/circles/{circle}/members/{member}/remind', [CircleController::class, 'remindMember']);
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
    Route::post('/settings/accept-disclaimer', [SettingsController::class, 'acceptDisclaimer']);

    Route::get('/user', function (Request $request) {
        return $request->user()->load(['currentLocation', 'circles', 'emergencyContacts']);
    });
});

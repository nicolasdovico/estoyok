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

Route::post('/maintenance/delete-user', function (Request $request) {
    $email = $request->input('email');
    if (!$email) {
        return response()->json(['error' => 'Email required'], 400);
    }
    $user = \App\Models\User::where('email', $email)->first();
    if (!$user) {
        return response()->json(['message' => 'User not found']);
    }
    \App\Models\EmergencyContact::where('user_id', $user->id)->delete();
    \App\Models\CheckIn::where('user_id', $user->id)->delete();
    \App\Models\CurrentLocation::where('user_id', $user->id)->delete();
    \App\Models\LocationHistory::where('user_id', $user->id)->delete();
    \Illuminate\Support\Facades\DB::table('circle_user')->where('user_id', $user->id)->delete();
    $user->delete();

    return response()->json(['message' => "User {$email} deleted successfully from Railway DB."]);
});

Route::post('/maintenance/send-test-email', function (Request $request) {
    $to = $request->input('email', 'nicolasdovico@gmail.com');
    $user = \App\Models\User::first() ?? new \App\Models\User(['name' => 'Usuario Pruebas']);

    try {
        \Illuminate\Support\Facades\Mail::to($to)->send(
            new \App\Mail\InactivityAlertMail(
                $user,
                'https://estoyok24.com/emergencia/test-email-verification',
                'Contacto de Pruebas'
            )
        );
        return response()->json([
            'success' => true,
            'message' => "Test email successfully dispatched to {$to} via Railway production mailer.",
        ]);
    } catch (\Throwable $e) {
        return response()->json([
            'success' => false,
            'error' => $e->getMessage(),
        ], 500);
    }
});

Route::post('/maintenance/send-test-whatsapp', function (Request $request) {
    $to = $request->input('phone', '+5491112345678');
    $message = $request->input('message', '🔔 Prueba oficial de WhatsApp de Estoy Ok enviada con UltraMsg.');

    $instanceId = config('services.ultramsg.instance_id');
    $token = config('services.ultramsg.token');

    if (!$instanceId || !$token) {
        return response()->json([
            'success' => false,
            'message' => 'UltraMsg no está configurado en Railway. Falta ULTRAMSG_INSTANCE_ID o ULTRAMSG_TOKEN.',
            'values' => [
                'instance_id' => $instanceId ? substr($instanceId, 0, 6) . '...' : null,
            ]
        ], 400);
    }

    try {
        $ultraMsg = new \App\Services\UltraMsgService();
        $ok = $ultraMsg->sendWhatsApp($to, $message);

        return response()->json([
            'success' => $ok,
            'target_phone' => $to,
            'instance_id' => $instanceId,
            'message' => $ok ? 'Petición procesada exitosamente por UltraMsg.' : 'UltraMsg devolvió error o no pudo entregar el mensaje. Revisa los logs.',
        ]);
    } catch (\Throwable $e) {
        return response()->json([
            'success' => false,
            'error' => $e->getMessage(),
        ], 500);
    }
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

Route::get('/maintenance/diagnose-push', function (Request $request) {
    $report = [];

    // 1. Firebase SDK Configuration Audit
    $envCreds = env('FIREBASE_CREDENTIALS') ?? env('GOOGLE_APPLICATION_CREDENTIALS');
    $configCreds = config('firebase.projects.app.credentials');

    $firebaseStatus = [
        'env_firebase_credentials_set' => !empty($envCreds),
        'config_firebase_credentials_set' => !empty($configCreds),
        'sdk_status' => 'unknown',
        'sdk_error' => null,
    ];

    try {
        $messaging = app(\Kreait\Firebase\Contract\Messaging::class);
        $firebaseStatus['sdk_status'] = 'operational';
    } catch (\Throwable $e) {
        $firebaseStatus['sdk_status'] = 'failed';
        $firebaseStatus['sdk_error'] = $e->getMessage();
    }
    $report['firebase_sdk'] = $firebaseStatus;

    // 2. Push Tokens Audit in Database
    $users = \App\Models\User::all();
    $tokenTypes = [
        'expo_token' => 0,
        'fcm_native_token' => 0,
        'null_or_empty' => 0,
    ];

    $usersSummary = [];
    foreach ($users as $user) {
        $token = $user->expo_push_token;
        if (empty($token)) {
            $type = 'null_or_empty';
        } elseif (str_starts_with($token, 'ExponentPushToken[') || str_starts_with($token, 'ExpoPushToken[')) {
            $type = 'expo_token';
        } else {
            $type = 'fcm_native_token';
        }
        $tokenTypes[$type]++;

        $usersSummary[] = [
            'id' => $user->id,
            'name' => $user->name,
            'email' => $user->email,
            'token_type' => $type,
            'token_snippet' => $token ? (substr($token, 0, 25) . '...') : null,
        ];
    }

    $report['tokens_summary'] = [
        'total_users' => $users->count(),
        'breakdown' => $tokenTypes,
        'users' => $usersSummary,
    ];

    // 3. User Devices Table Audit
    $devices = \Illuminate\Support\Facades\DB::table('user_devices')->get();
    $report['user_devices'] = [
        'total_registered_devices' => $devices->count(),
        'active_devices_with_token' => $devices->where('is_active', true)->whereNotNull('push_token')->count(),
        'devices' => $devices->map(function ($dev) {
            return [
                'user_id' => $dev->user_id,
                'device_uuid' => $dev->device_uuid,
                'platform' => $dev->platform,
                'is_active' => $dev->is_active,
                'token_snippet' => $dev->push_token ? (substr($dev->push_token, 0, 25) . '...') : null,
                'last_active_at' => $dev->last_active_at,
            ];
        }),
    ];

    // 4. Geofences & Events Audit
    $geofences = \App\Models\Geofence::where('is_active', true)->get();
    $recentEvents = \App\Models\GeofenceEvent::where('occurred_at', '>=', now()->subHours(24))->get();
    $report['geofencing_audit'] = [
        'active_geofences_count' => $geofences->count(),
        'geofences' => $geofences->map(fn($g) => ['id' => $g->id, 'name' => $g->name, 'circle_id' => $g->circle_id, 'radius' => $g->radius]),
        'recent_events_24h_count' => $recentEvents->count(),
    ];

    // 5. Failed Jobs Audit
    try {
        $failedCount = \Illuminate\Support\Facades\DB::table('failed_jobs')->count();
        $report['failed_jobs_count'] = $failedCount;
    } catch (\Throwable $e) {
        $report['failed_jobs_count'] = 'N/A';
    }

    // 6. Test Push Dispatch if requested
    $testUserId = $request->query('test_user_id');
    if (!empty($testUserId)) {
        $targetUser = \App\Models\User::find($testUserId);
        if ($targetUser && !empty($targetUser->expo_push_token)) {
            $pushService = app(\App\Services\PushNotificationService::class);
            $sendSuccess = $pushService->sendPush(
                $targetUser->expo_push_token,
                '🔔 Prueba de Notificación Estoy OK',
                "Hola {$targetUser->name}, esta es una prueba de envío directo desde el servidor de Railway.",
                ['type' => 'test_push', 'timestamp' => now()->toIso8601String()],
                true
            );

            $report['test_push_result'] = [
                'target_user_id' => $targetUser->id,
                'target_user_name' => $targetUser->name,
                'token_snippet' => substr($targetUser->expo_push_token, 0, 25) . '...',
                'success' => $sendSuccess,
            ];
        } else {
            $report['test_push_result'] = [
                'error' => "User ID {$testUserId} not found or has no push token.",
            ];
        }
    }

    // 7. Actionable Diagnosis
    $diagnoses = [];
    if ($firebaseStatus['sdk_status'] !== 'operational') {
        $diagnoses[] = 'CRITICAL: Google FCM Firebase SDK is NOT operational on Railway backend because FIREBASE_CREDENTIALS (or GOOGLE_APPLICATION_CREDENTIALS) is missing or invalid in Railway environment variables.';
    }
    if ($tokenTypes['null_or_empty'] > 0) {
        $diagnoses[] = "NOTE: {$tokenTypes['null_or_empty']} users currently do not have a push token registered in users.expo_push_token (e.g. logged out or web-only).";
    }
    if ($report['user_devices']['active_devices_with_token'] == 0) {
        $diagnoses[] = 'WARNING: No active device push tokens registered in user_devices table.';
    }

    $report['diagnoses'] = $diagnoses;

    return response()->json($report);
});

// Webhooks
Route::post('/webhooks/mercadopago', [WebhookController::class, 'mercadopago']);
Route::post('/webhooks/paypal', [WebhookController::class, 'paypal']);
Route::post('/webhooks/stripe', [Laravel\Cashier\Http\Controllers\WebhookController::class, 'handleWebhook']);
Route::post('/webhooks/ultramsg/message', [WebhookController::class, 'ultramsgMessage']);

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
    Route::match(['put', 'post'], '/settings/push-token', [SettingsController::class, 'updatePushToken']);
    Route::post('/settings/avatar', [SettingsController::class, 'updateAvatar']);
    Route::post('/settings/accept-disclaimer', [SettingsController::class, 'acceptDisclaimer']);

    Route::get('/user', function (Request $request) {
        return $request->user()->load(['currentLocation', 'circles', 'emergencyContacts']);
    });
});

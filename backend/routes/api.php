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
Route::post('/auth/google', [AuthController::class, 'googleLogin']);
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
    \App\Models\EmailVerification::where('email', $email)->delete();
    $user = \App\Models\User::where('email', $email)->first();
    if (!$user) {
        return response()->json(['message' => "User {$email} not found or already deleted (email verifications purged)."]);
    }
    \App\Models\EmergencyContact::where('user_id', $user->id)->delete();
    \App\Models\CheckIn::where('user_id', $user->id)->delete();
    \App\Models\CurrentLocation::where('user_id', $user->id)->delete();
    \App\Models\LocationHistory::where('user_id', $user->id)->delete();
    \Illuminate\Support\Facades\DB::table('drive_events')->where('user_id', $user->id)->delete();
    \Illuminate\Support\Facades\DB::table('user_devices')->where('user_id', $user->id)->delete();
    \Illuminate\Support\Facades\DB::table('emergency_alerts')->where('user_id', $user->id)->delete();
    \Illuminate\Support\Facades\DB::table('dynamic_geofences')->where('user_id', $user->id)->delete();
    \Illuminate\Support\Facades\DB::table('personal_access_tokens')->where('tokenable_type', \App\Models\User::class)->where('tokenable_id', $user->id)->delete();
    \Illuminate\Support\Facades\DB::table('circle_user')->where('user_id', $user->id)->delete();
    $user->delete();

    return response()->json(['message' => "User {$email} deleted successfully in cascade from Railway DB."]);
});

Route::post('/maintenance/release-phone', function (Request $request) {
    $phone = $request->input('phone');
    if (!$phone) {
        return response()->json(['error' => 'Phone required'], 400);
    }
    $cleanPhone = preg_replace('/[^0-9+]/', '', $phone);
    if (!str_starts_with($cleanPhone, '+')) {
        $cleanPhone = '+' . $cleanPhone;
    }
    $updated = \App\Models\User::where('phone', $cleanPhone)->update(['phone' => null]);
    return response()->json([
        'message' => "Phone {$cleanPhone} released from {$updated} user(s).",
        'updated_count' => $updated
    ]);
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

Route::post('/maintenance/trigger-inactivity-alert', function (Request $request) {
    $email = $request->input('email');
    $user = $email ? \App\Models\User::where('email', $email)->first() : \App\Models\User::whereNotNull('phone')->latest()->first();

    if (! $user) {
        return response()->json(['success' => false, 'error' => 'Usuario no encontrado.'], 404);
    }

    try {
        // Resolve any previous active emergency alert so a fresh inactivity alert is created
        \App\Models\EmergencyAlert::where('user_id', $user->id)
            ->where('status', 'active')
            ->update(['status' => 'resolved']);

        \App\Jobs\SendInactivityAlerts::dispatchSync($user);

        return response()->json([
            'success' => true,
            'message' => "Alerta de inactividad ejecutada para {$user->name} ({$user->email}).",
            'user' => [
                'id' => $user->id,
                'name' => $user->name,
                'email' => $user->email,
                'phone' => $user->phone,
                'notify_self_whatsapp_on_inactivity' => (bool) $user->fresh()->notify_self_whatsapp_on_inactivity,
            ]
        ]);
    } catch (\Throwable $e) {
        return response()->json([
            'success' => false,
            'error' => $e->getMessage(),
        ], 500);
    }
});

Route::get('/maintenance/diagnose-inactivity', function (Request $request) {
    try {
        $email = $request->input('email', 'tamarakshop@gmail.com');
        $user = \App\Models\User::where('email', $email)->first();

        if (!$user) {
            return response()->json(['error' => "User {$email} not found."], 404);
        }

        $nowUtc = now('UTC');
        $timezone = $user->timezone ?? config('app.timezone', 'America/Argentina/Buenos_Aires');
        $nowLocal = now($timezone);

        $isLocal = app()->environment('local');
        $unit = $isLocal ? 'minutes' : 'hours';

        // VerifyInactivity query simulation
        $matchingInactiveUsers = \App\Models\User::where('id', $user->id)
            ->where(function ($query) use ($unit) {
                $query->whereRaw("last_check_in_at < (NOW() AT TIME ZONE 'UTC') - (checkin_interval_hours || ' {$unit}')::interval")
                    ->orWhere(function ($q) use ($unit) {
                        $q->whereNull('last_check_in_at')
                            ->whereRaw("created_at < (NOW() AT TIME ZONE 'UTC') - (checkin_interval_hours || ' {$unit}')::interval");
                    });
            })
            ->whereDoesntHave('emergencyAlerts', function ($query) {
                $query->where('status', 'active');
            })
            ->exists();

        $passedThreshold = \App\Models\User::where('id', $user->id)
            ->where(function ($query) use ($unit) {
                $query->whereRaw("last_check_in_at < (NOW() AT TIME ZONE 'UTC') - (checkin_interval_hours || ' {$unit}')::interval")
                    ->orWhere(function ($q) use ($unit) {
                        $q->whereNull('last_check_in_at')
                            ->whereRaw("created_at < (NOW() AT TIME ZONE 'UTC') - (checkin_interval_hours || ' {$unit}')::interval");
                    });
            })->exists();

        $activeAlerts = \App\Models\EmergencyAlert::where('user_id', $user->id)
            ->where('status', 'active')
            ->get();

        $allAlerts = \App\Models\EmergencyAlert::where('user_id', $user->id)
            ->latest()
            ->take(5)
            ->get();

        $contacts = $user->emergencyContacts()->get();

        $queueDefault = config('queue.default');
        $redisQueues = [];
        if ($queueDefault === 'redis') {
            try {
                $redis = \Illuminate\Support\Facades\Redis::connection();
                $redisQueues = [
                    'default_len' => $redis->llen('queues:default'),
                    'dev_default_len' => $redis->llen('queues:dev_default'),
                    'keys' => array_slice($redis->keys('*'), 0, 20),
                ];
            } catch (\Throwable $e) {
                $redisQueues = ['error' => $e->getMessage()];
            }
        }

        $failedJobs = \Illuminate\Support\Facades\DB::table('failed_jobs')->orderBy('id', 'desc')->take(5)->get();

        $runningProcesses = shell_exec('ps aux 2>&1');

        $runVerify = $request->query('run_verify');
        $verifyOutput = null;
        if ($runVerify) {
            \Illuminate\Support\Facades\Artisan::call('checkins:verify-inactivity');
            $verifyOutput = \Illuminate\Support\Facades\Artisan::output();
        }

        $evolutionDebug = null;
        if (!empty(config('services.evolution.url')) && !empty(config('services.evolution.api_key'))) {
            try {
                $evBase = rtrim(config('services.evolution.url'), '/');
                $evKey = config('services.evolution.api_key');
                $evInst = config('services.evolution.instance', 'estoyok_main');

                $fetchRes = \Illuminate\Support\Facades\Http::withHeaders(['apikey' => $evKey])->timeout(3)->get("{$evBase}/instance/fetchInstances");
                $stateRes = \Illuminate\Support\Facades\Http::withHeaders(['apikey' => $evKey])->timeout(3)->get("{$evBase}/instance/connectionState/{$evInst}");
                $webhookRes = \Illuminate\Support\Facades\Http::withHeaders(['apikey' => $evKey])->timeout(3)->get("{$evBase}/webhook/find/{$evInst}");

                $evolutionDebug = [
                    'all_instances' => collect($fetchRes->json() ?? [])->map(fn($i) => [
                        'name' => $i['name'] ?? null,
                        'status' => $i['connectionStatus'] ?? null,
                    ])->all(),
                    'instance_state' => $stateRes->json(),
                    'current_webhook' => $webhookRes->json(),
                ];
            } catch (\Throwable $e) {
                $evolutionDebug = ['error' => $e->getMessage()];
            }
        }

        return response()->json([
            'evolution_debug' => $evolutionDebug,
            'server_time_utc' => $nowUtc->toDateTimeString(),
            'server_time_local' => $nowLocal->toDateTimeString(),
            'timezone' => $timezone,
            'app_env' => app()->environment(),
            'queue_default' => $queueDefault,
            'redis_queues' => $redisQueues,
            'redis_prefix' => config('database.redis.options.prefix'),
            'redis_host' => parse_url(env('REDIS_URL') ?: '', PHP_URL_HOST) ?: config('database.redis.default.host'),
            'redis_port' => parse_url(env('REDIS_URL') ?: '', PHP_URL_PORT) ?: config('database.redis.default.port'),
            'redis_queue' => config('queue.connections.redis.queue'),
            'db_database' => config('database.connections.pgsql.database'),
            'db_host' => config('database.connections.pgsql.host'),
            'evolution' => [
                'url' => config('services.evolution.url'),
                'instance' => config('services.evolution.instance'),
                'has_api_key' => !empty(config('services.evolution.api_key')),
            ],
            'railway_env' => env('RAILWAY_ENVIRONMENT_NAME'),
            'railway_domain' => env('RAILWAY_PUBLIC_DOMAIN'),
            'last_inactivity_job_received' => \Illuminate\Support\Facades\Cache::get('last_inactivity_job_received'),
            'last_inactivity_job_missing_alert' => \Illuminate\Support\Facades\Cache::get('last_inactivity_job_missing_alert'),
            'last_inactivity_job_self_whatsapp' => \Illuminate\Support\Facades\Cache::get('last_inactivity_job_self_whatsapp'),
            'last_inactivity_job_contacts_result' => \Illuminate\Support\Facades\Cache::get('last_inactivity_job_contacts_result'),
            'last_confirmation_job_result' => \Illuminate\Support\Facades\Cache::get('last_confirmation_job_result'),
            'last_evolution_success' => \Illuminate\Support\Facades\Cache::get('last_evolution_success'),
            'last_evolution_error' => \Illuminate\Support\Facades\Cache::get('last_evolution_error'),
            'last_evolution_simulated' => \Illuminate\Support\Facades\Cache::get('last_evolution_simulated'),
            'last_verify_dispatch_attempt' => \Illuminate\Support\Facades\Cache::get('last_verify_dispatch_attempt'),
            'last_verify_dispatch_error' => \Illuminate\Support\Facades\Cache::get('last_verify_dispatch_error'),
            'git_commit' => trim(shell_exec('git log -n 1 --oneline 2>&1') ?? ''),
            'latest_logs' => file_exists(storage_path('logs/laravel.log')) ? array_slice(file(storage_path('logs/laravel.log')), -30) : [],
            'failed_jobs' => $failedJobs,
            'running_processes' => $runningProcesses,
            'user' => [
                'id' => $user->id,
                'email' => $user->email,
                'name' => $user->name,
                'phone' => $user->phone,
                'last_check_in_at' => $user->last_check_in_at?->toDateTimeString(),
                'created_at' => $user->created_at->toDateTimeString(),
                'checkin_interval_hours' => $user->checkin_interval_hours,
                'hours_since_last_checkin' => $user->last_check_in_at ? round($nowUtc->diffInMinutes($user->last_check_in_at) / 60, 2) : null,
                'minutes_since_last_checkin' => $user->last_check_in_at ? $nowUtc->diffInMinutes($user->last_check_in_at) : null,
                'is_premium' => (bool) $user->is_premium,
                'subscription_status' => $user->subscription_status,
                'subscription_provider' => $user->subscription_provider,
                'trial_ends_at' => $user->trial_ends_at?->toDateTimeString(),
                'has_used_trial' => (bool) $user->has_used_trial,
                'has_premium_access' => $user->hasPremiumAccess(),
                'notify_self_whatsapp_on_inactivity' => (bool) $user->notify_self_whatsapp_on_inactivity,
                'allow_sms_whatsapp_checkin' => (bool) $user->allow_sms_whatsapp_checkin,
                'escalation_enabled' => (bool) $user->escalation_enabled,
                'quiet_hours_enabled' => (bool) $user->quiet_hours_enabled,
                'quiet_hours_start' => $user->quiet_hours_start,
                'quiet_hours_end' => $user->quiet_hours_end,
                'is_in_quiet_hours' => $user->isInQuietHours(),
            ],
            'verify_inactivity_check' => [
                'unit_used' => $unit,
                'passed_threshold_query' => $passedThreshold,
                'has_active_emergency_alert' => $activeAlerts->isNotEmpty(),
                'matches_verify_inactivity_query' => $matchingInactiveUsers,
                'would_skip_due_to_quiet_hours' => $user->isInQuietHours(),
            ],
            'active_emergency_alerts' => $activeAlerts,
            'recent_emergency_alerts' => $allAlerts,
            'emergency_contacts' => $contacts,
            'verify_command_run_output' => $verifyOutput,
        ]);
    } catch (\Throwable $e) {
        return response()->json([
            'exception' => $e->getMessage(),
            'file' => $e->getFile(),
            'line' => $e->getLine(),
        ], 500);
    }
});

$prepareInactivityTestHandler = function (Request $request) {
    $email = $request->input('email', 'tamarakshop@gmail.com');
    $minutesLeft = $request->has('minutes_left') ? (int) $request->input('minutes_left') : 2;
    $user = \App\Models\User::where('email', $email)->first();
    if (!$user) {
        return response()->json(['error' => "User {$email} not found."], 404);
    }

    // 1. Resolve any active emergency alert and clear dispatch lock
    $resolvedCount = \App\Models\EmergencyAlert::where('user_id', $user->id)
        ->where('status', 'active')
        ->update(['status' => 'resolved']);
    \Illuminate\Support\Facades\Cache::forget("inactivity_alert_dispatched_user_{$user->id}");

    // 2. Set user as Premium with active subscription and calculate last_check_in_at based on minutes_left
    $interval = $user->checkin_interval_hours ?? 12;
    $lastCheckIn = $minutesLeft > 0
        ? now('UTC')->subHours($interval)->addMinutes($minutesLeft)
        : now('UTC')->subHours($interval + 1);

    $user->update([
        'is_premium' => true,
        'subscription_status' => 'active',
        'subscription_provider' => 'admin_test',
        'trial_ends_at' => now()->addMonth(),
        'quiet_hours_enabled' => false,
        'notify_self_whatsapp_on_inactivity' => true,
        'allow_sms_whatsapp_checkin' => true,
        'last_check_in_at' => $lastCheckIn,
    ]);

    $timezone = $user->timezone ?? config('app.timezone', 'America/Argentina/Buenos_Aires');
    $expiresAtUtc = $lastCheckIn->copy()->addHours($interval);
    $expiresAtLocal = $expiresAtUtc->copy()->setTimezone($timezone);

    return response()->json([
        'success' => true,
        'message' => $minutesLeft > 0
            ? "Reloj de bienestar preparado para {$email}. Expirará en {$minutesLeft} minutos de forma natural."
            : "Reloj de bienestar vencido inmediatamente para {$email}.",
        'resolved_alerts_count' => $resolvedCount,
        'minutes_left' => $minutesLeft,
        'current_time_utc' => now('UTC')->toDateTimeString(),
        'current_time_local' => now($timezone)->toDateTimeString(),
        'expires_at_utc' => $expiresAtUtc->toDateTimeString(),
        'expires_at_local' => $expiresAtLocal->toDateTimeString(),
        'user' => [
            'id' => $user->id,
            'email' => $user->email,
            'is_premium' => (bool) $user->is_premium,
            'has_premium_access' => $user->hasPremiumAccess(),
            'notify_self_whatsapp_on_inactivity' => (bool) $user->notify_self_whatsapp_on_inactivity,
            'quiet_hours_enabled' => (bool) $user->quiet_hours_enabled,
            'last_check_in_at' => $user->last_check_in_at->toDateTimeString(),
            'checkin_interval_hours' => $user->checkin_interval_hours,
        ]
    ]);
};

Route::match(['get', 'post'], '/maintenance/prepare-inactivity-test', $prepareInactivityTestHandler);
Route::match(['get', 'post'], '/maintenance/set-clock-remaining', $prepareInactivityTestHandler);

Route::match(['get', 'post'], '/maintenance/test-activation-nudge', function (Request $request) {
    $email = $request->input('email', 'tamarakshop@gmail.com');
    $step = $request->input('step', '6h'); // '6h', '24h', '48h', '72h'
    $force = $request->boolean('force', true);

    $user = \App\Models\User::where('email', $email)->first();
    if (! $user) {
        return response()->json(['error' => "Usuario no encontrado con email: {$email}"], 404);
    }

    if (empty($user->expo_push_token)) {
        return response()->json([
            'error' => "El usuario {$email} no tiene expo_push_token registrado en este entorno.",
            'user_id' => $user->id,
        ], 422);
    }

    if ($force) {
        // Limpiamos las claves de caché de prueba para permitir reenvíos inmediatos
        \Illuminate\Support\Facades\Cache::forget("activation_nudge_6h_user_{$user->id}");
        \Illuminate\Support\Facades\Cache::forget("activation_nudge_24h_user_{$user->id}");
        \Illuminate\Support\Facades\Cache::forget("activation_nudge_48h_user_{$user->id}");
        \Illuminate\Support\Facades\Cache::forget("activation_nudge_72h_user_{$user->id}");
    }

    // Configuramos el estado del usuario según el paso a probar
    if ($step === '6h') {
        $user->update([
            'created_at' => now()->subHours(8),
            'last_check_in_at' => null,
            'quiet_hours_enabled' => false,
        ]);
    } elseif ($step === '24h') {
        $user->update([
            'created_at' => now()->subHours(30),
            'last_check_in_at' => now()->subHours(10),
            'quiet_hours_enabled' => false,
        ]);
        // Borramos temporalmente contactos si queremos probar el nudge 24h
        if ($request->boolean('clear_contacts', false)) {
            $user->emergencyContacts()->delete();
        }
    } elseif ($step === '48h') {
        $user->update([
            'created_at' => now()->subHours(50),
            'last_check_in_at' => now()->subHours(10),
            'is_premium' => false,
            'has_used_trial' => false,
            'quiet_hours_enabled' => false,
        ]);
    }

    // Ejecutamos el comando directamente
    \Illuminate\Support\Facades\Artisan::call('users:send-activation-nudges');
    $output = \Illuminate\Support\Facades\Artisan::output();

    return response()->json([
        'success' => true,
        'email' => $user->email,
        'user_id' => $user->id,
        'token_snippet' => substr($user->expo_push_token, 0, 25) . '...',
        'tested_step' => $step,
        'created_at' => (string) $user->created_at,
        'last_check_in_at' => (string) $user->last_check_in_at,
        'artisan_output' => trim($output),
    ]);
});


Route::post('/maintenance/send-test-whatsapp', function (Request $request) {
    $to = $request->input('phone') ?? $request->json('phone');
    if (! $to) {
        $body = json_decode($request->getContent(), true);
        $to = $body['phone'] ?? null;
    }

    if (! $to) {
        return response()->json([
            'success' => false,
            'message' => 'Debes especificar el parámetro "phone" en el cuerpo JSON (ej: {"phone": "5491149790220"}).',
        ], 400);
    }

    $message = $request->input('message') ?? '🔔 Prueba oficial de WhatsApp de Estoy Ok enviada con Evolution API en Railway.';

    $baseUrl = config('services.evolution.url');
    $apiKey = config('services.evolution.api_key');
    $instance = config('services.evolution.instance', 'estoyok_main');

    if (! $baseUrl || ! filter_var($baseUrl, FILTER_VALIDATE_URL)) {
        return response()->json([
            'success' => false,
            'message' => 'Falta configurar la variable EVOLUTION_API_URL en Railway (o la URL no es válida).',
            'current_value' => $baseUrl,
        ], 400);
    }

    try {
        $cleanPhone = preg_replace('/[^0-9]/', '', $to);
        if (str_starts_with($cleanPhone, '54') && !str_starts_with($cleanPhone, '549') && strlen($cleanPhone) === 12) {
            $cleanPhone = '549' . substr($cleanPhone, 2);
        }

        $linkPreview = $request->has('link_preview') ? filter_var($request->input('link_preview'), FILTER_VALIDATE_BOOLEAN) : false;

        $service = app(\App\Services\EvolutionApiService::class);
        $success = $service->sendWhatsApp($cleanPhone, $message, ['linkPreview' => $linkPreview]);
        $health = $service->checkConnectionHealth();

        return response()->json([
            'success' => $success,
            'target_phone' => $to,
            'normalized_phone' => $cleanPhone,
            'link_preview' => $linkPreview,
            'health' => $health,
        ]);
    } catch (\Throwable $e) {
        return response()->json([
            'success' => false,
            'error' => $e->getMessage(),
        ], 500);
    }
});

Route::match(['get', 'post'], '/maintenance/test-job-dispatch', function (Request $request) {
    try {
        $email = $request->input('email', 'tamarakshop@gmail.com');
        $user = \App\Models\User::where('email', $email)->first();
        if (! $user) {
            return response()->json(['error' => 'User not found'], 404);
        }

        $alert = \App\Models\EmergencyAlert::where('user_id', $user->id)
            ->where('status', 'active')
            ->first();

        if (! $alert) {
            $alert = \App\Models\EmergencyAlert::create([
                'user_id' => $user->id,
                'type' => 'inactivity',
                'status' => 'active',
                'expires_at' => now()->addHours(48),
            ]);
        }

        $redis = \Illuminate\Support\Facades\Redis::connection();
        $keysBefore = $redis->keys('*');

        \App\Jobs\SendInactivityAlerts::dispatch($user, $alert->id);

        $keysAfter = $redis->keys('*');
        $lenDefaultAfter = $redis->llen('queues:default');
        $lenDevAfter = $redis->llen('queues:dev_default');

        // Give the worker 2.5 seconds to process
        usleep(2500000);

        $keysFinal = $redis->keys('*');
        $lenDefaultFinal = $redis->llen('queues:default');
        $lenDevFinal = $redis->llen('queues:dev_default');

        return response()->json([
            'success' => true,
            'user' => ['id' => $user->id, 'email' => $user->email, 'phone' => $user->phone],
            'alert_id' => $alert->id,
            'config' => [
                'queue_default' => config('queue.default'),
                'redis_queue' => config('queue.connections.redis.queue'),
                'redis_prefix' => config('database.redis.options.prefix'),
                'cache_driver' => config('cache.default'),
            ],
            'redis_before' => $keysBefore,
            'redis_immediate_after' => [
                'keys' => $keysAfter,
                'default_len' => $lenDefaultAfter,
                'dev_default_len' => $lenDevAfter,
            ],
            'redis_after_2s' => [
                'keys' => $keysFinal,
                'default_len' => $lenDefaultFinal,
                'dev_default_len' => $lenDevFinal,
            ],
            'cache_data' => [
                'last_received' => \Illuminate\Support\Facades\Cache::get('last_inactivity_job_received'),
                'missing_alert' => \Illuminate\Support\Facades\Cache::get('last_inactivity_job_missing_alert'),
                'self_whatsapp' => \Illuminate\Support\Facades\Cache::get('last_inactivity_job_self_whatsapp'),
                'contacts_result' => \Illuminate\Support\Facades\Cache::get('last_inactivity_job_contacts_result'),
            ],
            'failed_jobs' => \Illuminate\Support\Facades\DB::table('failed_jobs')->latest('id')->take(3)->get(),
        ]);
    } catch (\Throwable $e) {
        return response()->json([
            'error' => $e->getMessage(),
            'trace' => $e->getTraceAsString(),
        ], 500);
    }
});

Route::post('/maintenance/restart-evolution-instance', function () {
    $baseUrl = config('services.evolution.url');
    $instance = config('services.evolution.instance', 'estoyok_main');

    if (! $baseUrl || ! filter_var($baseUrl, FILTER_VALIDATE_URL)) {
        return response()->json(['success' => false, 'message' => 'EVOLUTION_API_URL not configured.'], 400);
    }

    try {
        $service = app(\App\Services\EvolutionApiService::class);
        $reconnected = $service->reconnectInstance();
        $health = $service->checkConnectionHealth();

        return response()->json([
            'success' => $reconnected,
            'instance' => $instance,
            'health' => $health,
        ]);
    } catch (\Throwable $e) {
        return response()->json(['success' => false, 'error' => $e->getMessage()], 500);
    }
});

Route::get('/maintenance/logs', function () {
    $logFile = storage_path('logs/laravel.log');
    if (! file_exists($logFile)) {
        return response('No log file found', 404);
    }
    $lines = file($logFile);
    $lastLines = array_slice($lines, -150);
    return response(implode('', $lastLines), 200)->header('Content-Type', 'text/plain');
});

Route::match(['get', 'post'], '/maintenance/trigger-sos-job', function () {
    $alert = \App\Models\EmergencyAlert::where('type', 'silent_sos')->latest()->first();
    if (! $alert) {
        return response()->json(['error' => 'No SOS alert']);
    }
    $user = $alert->user;

    $job = new \App\Jobs\SendSosAlertJob($alert, $user);
    $service = app(\App\Services\WhatsAppServiceInterface::class);
    $job->handle($service);

    return response()->json([
        'handled' => true,
        'alert_id' => $alert->id,
        'user' => $user->name,
    ]);
});

Route::get('/maintenance/test-queue-worker', function () {
    $token = \Illuminate\Support\Str::random(10);
    \Illuminate\Support\Facades\Cache::put("test_queue_{$token}", 'pending', 60);

    dispatch(function () use ($token) {
        \Illuminate\Support\Facades\Cache::put("test_queue_{$token}", 'processed_at_' . now()->toIso8601String(), 60);
    });

    sleep(2);
    $status = \Illuminate\Support\Facades\Cache::get("test_queue_{$token}");

    return response()->json([
        'token' => $token,
        'status' => $status,
        'worker_alive' => is_string($status) && str_starts_with($status, 'processed_at_'),
    ]);
});

Route::get('/maintenance/process-queue-work', function (\Illuminate\Http\Request $request) {
    try {
        $count = (int) $request->input('count', 5);
        $executed = 0;
        for ($i = 0; $i < $count; $i++) {
            \Illuminate\Support\Facades\Artisan::call('queue:work', [
                '--once' => true,
                '--stop-when-empty' => true,
            ]);
            $executed++;
            if (config('queue.default') === 'redis' && \Illuminate\Support\Facades\Redis::connection()->llen('queues:default') === 0) {
                break;
            }
        }

        $redisLen = null;
        if (config('queue.default') === 'redis') {
            $redisLen = \Illuminate\Support\Facades\Redis::connection()->llen('queues:default');
        }

        return response()->json([
            'success' => true,
            'cycles_executed' => $executed,
            'remaining_jobs' => $redisLen,
            'artisan_output' => trim(\Illuminate\Support\Facades\Artisan::output()),
        ]);
    } catch (\Throwable $e) {
        return response()->json([
            'success' => false,
            'error' => $e->getMessage(),
        ], 500);
    }
});

Route::get('/maintenance/test-inactivity-direct', function (\Illuminate\Http\Request $request) {
    try {
        $email = $request->input('email', 'tamarakshop@gmail.com');
        $user = \App\Models\User::where('email', $email)->first();
        if (! $user) {
            return response()->json(['error' => 'User not found'], 404);
        }

        $service = app(\App\Services\WhatsAppServiceInterface::class);
        $health = $service->checkConnectionHealth();

        $selfSent = false;
        $selfError = null;
        if ($user->notify_self_whatsapp_on_inactivity && !empty($user->phone)) {
            $hours = $user->checkin_interval_hours ?? 24;
            $msg = "⚠️ Aviso Estoy Ok: Tu reporte diario de bienestar ha vencido (sin confirmación durante las últimas {$hours} horas). Tus contactos de emergencia han sido alertados. Puedes confirmar tu estado ingresando a la app o respondiendo 'OK' a este mensaje.";
            try {
                $selfSent = $service->sendWhatsApp($user->phone, $msg);
            } catch (\Throwable $e) {
                $selfError = $e->getMessage();
            }
        }

        $contacts = [];
        foreach ($user->emergencyContacts()->where('is_active', true)->get() as $c) {
            $contactSent = false;
            $contactError = null;
            if (!empty($c->phone)) {
                $msg = "Aviso de seguridad: Su contacto '{$user->name}' no ha reportado actividad en la aplicación 'Estoy Ok'.";
                try {
                    $contactSent = $service->sendWhatsApp($c->phone, $msg);
                } catch (\Throwable $e) {
                    $contactError = $e->getMessage();
                }
            }
            $contacts[] = [
                'name' => $c->name,
                'phone' => $c->phone,
                'sent' => $contactSent,
                'error' => $contactError,
            ];
        }

        return response()->json([
            'user' => $user->email,
            'phone' => $user->phone,
            'notify_self' => (bool) $user->notify_self_whatsapp_on_inactivity,
            'has_premium' => $user->hasPremiumAccess(),
            'health' => $health,
            'self_sent' => $selfSent,
            'self_error' => $selfError,
            'contacts' => $contacts,
        ]);
    } catch (\Throwable $e) {
        return response()->json(['error' => $e->getMessage(), 'line' => $e->getLine()], 500);
    }
});

Route::get('/maintenance/sos-debug', function () {
    $latestAlerts = \App\Models\EmergencyAlert::with('user.emergencyContacts')
        ->latest()
        ->take(3)
        ->get();

    $failedJobs = \Illuminate\Support\Facades\DB::table('failed_jobs')
        ->latest('failed_at')
        ->take(3)
        ->get();

    $queueDriver = config('queue.default');

    $pendingJobsCount = 0;
    try {
        if ($queueDriver === 'database') {
            $pendingJobsCount = \Illuminate\Support\Facades\DB::table('jobs')->count();
        } elseif ($queueDriver === 'redis') {
            $pendingJobsCount = \Illuminate\Support\Facades\Redis::connection()->llen('queues:default');
        }
    } catch (\Throwable $e) {
        $pendingJobsCount = 'error: ' . $e->getMessage();
    }

    return response()->json([
        'server_time' => now()->toIso8601String(),
        'queue_driver' => $queueDriver,
        'pending_jobs' => $pendingJobsCount,
        'failed_jobs' => $failedJobs,
        'latest_alerts' => $latestAlerts->map(function ($alert) {
            return [
                'id' => $alert->id,
                'user_id' => $alert->user_id,
                'user_name' => $alert->user?->name,
                'user_phone' => $alert->user?->phone,
                'type' => $alert->type,
                'status' => $alert->status,
                'created_at' => $alert->created_at?->toIso8601String(),
                'emergency_contacts' => $alert->user?->emergencyContacts?->map(function ($contact) {
                    return [
                        'id' => $contact->id,
                        'name' => $contact->name,
                        'phone' => $contact->phone,
                        'is_active' => (bool) $contact->is_active,
                        'priority' => $contact->priority,
                    ];
                }),
            ];
        }),
    ]);
});

Route::get('/maintenance/instance-debug', function () {
    $baseUrl = config('services.evolution.url');
    $apiKey = config('services.evolution.api_key');
    $instance = config('services.evolution.instance', 'estoyok_main');

    $instances = Http::withHeaders(['apikey' => $apiKey])->get(rtrim($baseUrl, '/') . '/instance/fetchInstances')->json();
    $state = Http::withHeaders(['apikey' => $apiKey])->get(rtrim($baseUrl, '/') . '/instance/connectionState/' . $instance)->json();
    $connect = Http::withHeaders(['apikey' => $apiKey])->get(rtrim($baseUrl, '/') . '/instance/connect/' . $instance)->json();

    return response()->json([
        'configured_instance' => $instance,
        'fetch_instances' => $instances,
        'connection_state' => $state,
        'connect_raw' => $connect,
    ]);
});

Route::match(['get', 'post'], '/maintenance/purge-instance', function (Request $request) {
    $baseUrl = config('services.evolution.url');
    $apiKey = config('services.evolution.api_key');
    $instance = config('services.evolution.instance', 'estoyok_main');

    if (! $baseUrl || ! filter_var($baseUrl, FILTER_VALIDATE_URL)) {
        return response()->json(['error' => 'EVOLUTION_API_URL not configured'], 400);
    }

    try {
        $logout = Http::withHeaders(['apikey' => $apiKey])->delete(rtrim($baseUrl, '/') . '/instance/logout/' . $instance)->json();
        sleep(1);
        $delete = Http::withHeaders(['apikey' => $apiKey])->delete(rtrim($baseUrl, '/') . '/instance/delete/' . $instance)->json();

        $deletedOk = false;
        for ($i = 0; $i < 6; $i++) {
            sleep(1);
            $list = Http::withHeaders(['apikey' => $apiKey])->get(rtrim($baseUrl, '/') . '/instance/fetchInstances')->json();
            $found = false;
            if (is_array($list)) {
                foreach ($list as $item) {
                    if (($item['name'] ?? null) === $instance) {
                        $found = true;
                        break;
                    }
                }
            }
            if (! $found) {
                $deletedOk = true;
                break;
            }
        }

        $create = Http::withHeaders(['apikey' => $apiKey])->post(rtrim($baseUrl, '/') . '/instance/create', [
            'instanceName' => $instance,
            'integration' => 'WHATSAPP-BAILEYS',
            'qrcode' => true,
        ])->json();
        sleep(1);

        $connect = Http::withHeaders(['apikey' => $apiKey])->get(rtrim($baseUrl, '/') . '/instance/connect/' . $instance)->json();

        $service = app(\App\Services\EvolutionApiService::class);
        $service->ensureWebhookConfigured($request->root(), true);

        return response()->json([
            'success' => $deletedOk && ! empty($create['instance'] ?? $create['qrcode']),
            'logout' => $logout,
            'delete' => $delete,
            'deleted_ok' => $deletedOk,
            'create' => $create,
            'connect_has_base64' => ! empty($connect['base64'] ?? ($connect['qrcode']['base64'] ?? null)),
            'connect_count' => $connect['count'] ?? 0,
        ]);
    } catch (\Throwable $e) {
        return response()->json(['success' => false, 'error' => $e->getMessage()], 500);
    }
});

Route::get('/maintenance/connect-qr-data', function (Request $request) {
    $baseUrl = config('services.evolution.url');
    $apiKey = config('services.evolution.api_key');
    $instance = config('services.evolution.instance', 'estoyok_main');

    if (! $baseUrl || ! filter_var($baseUrl, FILTER_VALIDATE_URL)) {
        return response()->json(['error' => 'EVOLUTION_API_URL not configured'], 400);
    }

    $state = 'unknown';
    try {
        $stateRes = Http::withHeaders(['apikey' => $apiKey])->timeout(4)->get(rtrim($baseUrl, '/') . '/instance/connectionState/' . $instance);
        $stateData = $stateRes->json();
        $state = $stateData['instance']['state'] ?? ($stateData['state'] ?? null);
    } catch (\Throwable $e) {}

    if (! $state || ! in_array(strtolower($state), ['open', 'connecting', 'close', 'closed'])) {
        try {
            $listRes = Http::withHeaders(['apikey' => $apiKey])->timeout(4)->get(rtrim($baseUrl, '/') . '/instance/fetchInstances');
            $instances = $listRes->json();
            if (is_array($instances)) {
                foreach ($instances as $inst) {
                    if (($inst['name'] ?? null) === $instance) {
                        $state = strtolower($inst['connectionStatus'] ?? 'unknown');
                        break;
                    }
                }
            }
        } catch (\Throwable $e) {}
    }

    $forceQr = $request->has('force_qr') || $request->query('force_qr') == '1';
    $qrBase64 = null;
    $count = 0;
    if ($state !== 'open' || $forceQr) {
        try {
            $connRes = Http::withHeaders(['apikey' => $apiKey])->timeout(4)->get(rtrim($baseUrl, '/') . '/instance/connect/' . $instance);
            $connectData = $connRes->json();
            $rawBase64 = $connectData['base64'] ?? ($connectData['qrcode']['base64'] ?? null);
            if ($rawBase64) {
                $qrBase64 = str_starts_with($rawBase64, 'data:') ? $rawBase64 : 'data:image/png;base64,' . $rawBase64;
            }
            $count = $connectData['count'] ?? 0;
        } catch (\Throwable $e) {}
    }

    return response()->json([
        'state' => $state,
        'qr_base64' => $qrBase64,
        'count' => $count,
        'server_time' => now()->setTimezone('America/Argentina/Buenos_Aires')->format('H:i:s'),
    ])->header('Cache-Control', 'no-store, no-cache, must-revalidate, max-age=0')
      ->header('Pragma', 'no-cache')
      ->header('Expires', '0');
});

Route::match(['get', 'post'], '/maintenance/connect-qr', function (Request $request) {
    $baseUrl = config('services.evolution.url');
    $apiKey = config('services.evolution.api_key');
    $instance = config('services.evolution.instance', 'estoyok_main');

    if (! $baseUrl || ! filter_var($baseUrl, FILTER_VALIDATE_URL)) {
        return response('<h3>EVOLUTION_API_URL no configurada</h3>', 400);
    }

    $action = $request->input('action') ?? $request->query('action');

    // 1. If user requested reset_instance, completely delete and recreate
    if ($action === 'reset_instance') {
        try {
            // Desconectar socket
            Http::withHeaders(['apikey' => $apiKey])->delete(rtrim($baseUrl, '/') . '/instance/logout/' . $instance);
            sleep(1);

            // Borrar instancia en Evolution API
            Http::withHeaders(['apikey' => $apiKey])->delete(rtrim($baseUrl, '/') . '/instance/delete/' . $instance);

            // Aguardar a que PostgreSQL complete el cascade delete (hasta 5 intentos)
            for ($i = 0; $i < 5; $i++) {
                sleep(1);
                $fetchRes = Http::withHeaders(['apikey' => $apiKey])->get(rtrim($baseUrl, '/') . '/instance/fetchInstances');
                $list = $fetchRes->json();
                $found = false;
                if (is_array($list)) {
                    foreach ($list as $item) {
                        if (($item['name'] ?? null) === $instance) {
                            $found = true;
                            break;
                        }
                    }
                }
                if (! $found) {
                    break;
                }
            }

            // Crear instancia limpia con QR
            Http::withHeaders(['apikey' => $apiKey])->post(rtrim($baseUrl, '/') . '/instance/create', [
                'instanceName' => $instance,
                'integration' => 'WHATSAPP-BAILEYS',
                'qrcode' => true,
            ]);
            sleep(1);

            // Configurar webhook
            $service = app(\App\Services\EvolutionApiService::class);
            $service->ensureWebhookConfigured($request->root(), true);
        } catch (\Throwable $e) {}
        return redirect('/api/maintenance/connect-qr?force_qr=1&t=' . time());
    }

    // 2. If user clicked set_webhook, bind Evolution API webhook to this backend
    if ($action === 'set_webhook') {
        try {
            $service = app(\App\Services\EvolutionApiService::class);
            $service->ensureWebhookConfigured($request->root(), true);
        } catch (\Throwable $e) {}
        return redirect('/api/maintenance/connect-qr?t=' . time());
    }

    // 2b. If user clicked reconnect, force a socket reconnect
    if ($action === 'reconnect') {
        try {
            $service = app(\App\Services\EvolutionApiService::class);
            $service->reconnectInstance();
        } catch (\Throwable $e) {}
        return redirect('/api/maintenance/connect-qr?t=' . time());
    }

    // 3. If user clicked logout, disconnect the instance first
    $logoutOutput = null;
    if ($action === 'logout') {
        try {
            $logoutRes = Http::withHeaders(['apikey' => $apiKey])->delete(rtrim($baseUrl, '/') . '/instance/logout/' . $instance);
            $logoutOutput = $logoutRes->json() ?? $logoutRes->body();
        } catch (\Throwable $e) {
            $logoutOutput = ['error' => $e->getMessage()];
        }
        return redirect('/api/maintenance/connect-qr?t=' . time());
    }

    // 4. Check current state & webhook
    $state = 'unknown';
    try {
        $stateRes = Http::withHeaders(['apikey' => $apiKey])->timeout(4)->get(rtrim($baseUrl, '/') . '/instance/connectionState/' . $instance);
        $stateData = $stateRes->json();
        $state = $stateData['instance']['state'] ?? ($stateData['state'] ?? null);
    } catch (\Throwable $e) {}

    if (! $state || ! in_array(strtolower($state), ['open', 'connecting', 'close', 'closed'])) {
        try {
            $listRes = Http::withHeaders(['apikey' => $apiKey])->timeout(4)->get(rtrim($baseUrl, '/') . '/instance/fetchInstances');
            $instances = $listRes->json();
            if (is_array($instances)) {
                foreach ($instances as $inst) {
                    if (($inst['name'] ?? null) === $instance) {
                        $state = strtolower($inst['connectionStatus'] ?? 'unknown');
                        break;
                    }
                }
            }
        } catch (\Throwable $e) {}
    }

    $currentWebhook = 'Desconocido';
    try {
        $whRes = Http::withHeaders(['apikey' => $apiKey])->timeout(4)->get(rtrim($baseUrl, '/') . '/webhook/find/' . $instance);
        $whData = $whRes->json();
        $currentWebhook = $whData['url'] ?? ($whData['webhook']['url'] ?? 'No configurado');
    } catch (\Throwable $e) {}

    // 5. If not open, fetch connect QR
    $qrBase64 = null;
    $connectData = null;
    $showQr = $state !== 'open' || $action === 'force_qr' || $request->has('force_qr');
    if ($showQr) {
        try {
            $connRes = Http::withHeaders(['apikey' => $apiKey])->timeout(4)->get(rtrim($baseUrl, '/') . '/instance/connect/' . $instance);
            $connectData = $connRes->json();
            $rawBase64 = $connectData['base64'] ?? ($connectData['qrcode']['base64'] ?? null);
            if ($rawBase64) {
                $qrBase64 = str_starts_with($rawBase64, 'data:') ? $rawBase64 : 'data:image/png;base64,' . $rawBase64;
            }
        } catch (\Throwable $e) {
            $connectData = ['error' => $e->getMessage()];
        }
    }

    $serverTime = now()->setTimezone('America/Argentina/Buenos_Aires')->format('H:i:s');

    // HTML Output
    $html = '<!DOCTYPE html><html><head><meta charset="utf-8"><title>Estoy Ok - Vinculación WhatsApp QR</title>';
    $html .= '<meta name="viewport" content="width=device-width, initial-scale=1">';
    $html .= '<meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />';
    $html .= '<meta http-equiv="Pragma" content="no-cache" />';
    $html .= '<meta http-equiv="Expires" content="0" />';
    $html .= '<style>body{font-family:system-ui,-apple-system,sans-serif;background:#0b132b;color:#f8fafc;padding:30px;text-align:center;}';
    $html .= '.card{max-width:480px;margin:0 auto;background:#1c2541;padding:30px;border-radius:16px;box-shadow:0 10px 25px rgba(0,0,0,0.5);}';
    $html .= '.badge{display:inline-block;padding:6px 14px;border-radius:20px;font-weight:bold;font-size:14px;margin-bottom:20px;}';
    $html .= '.open{background:#10b981;color:#fff;} .disconnected{background:#ef4444;color:#fff;}';
    $html .= '.btn{background:#00e5d9;color:#0b132b;padding:12px 24px;border:none;border-radius:10px;font-weight:bold;cursor:pointer;text-decoration:none;display:inline-block;margin:10px 5px;}';
    $html .= '.btn-danger{background:#ef4444;color:#fff;}';
    $html .= 'img.qr{background:#fff;padding:15px;border-radius:12px;margin:20px 0;max-width:280px;height:auto;}';
    $html .= '.timer-badge{font-size:12px;color:#00e5d9;margin-bottom:10px;}';
    $html .= '</style></head><body>';
    $html .= '<div class="card">';
    $html .= '<h2>Estoy Ok 📲 Gateway WhatsApp</h2>';
    $html .= '<p>Instancia: <code>' . htmlspecialchars($instance) . '</code> | Servidor: <span id="server-clock">' . $serverTime . '</span></p>';

    $currentHost = $request->getHost();
    $isPointedHere = str_contains($currentWebhook, $currentHost);
    $html .= '<div style="background:#0b132b;padding:12px;border-radius:10px;margin:15px 0;text-align:left;font-size:13px;border:1px solid ' . ($isPointedHere ? '#10b981' : '#f59e0b') . ';">';
    $html .= '<div><strong>🔗 Webhook activo en Evolution:</strong></div>';
    $html .= '<code style="word-break:break-all;color:' . ($isPointedHere ? '#10b981' : '#f59e0b') . ';">' . htmlspecialchars($currentWebhook) . '</code>';
    if (! $isPointedHere) {
        $html .= '<form method="POST" action="/api/maintenance/connect-qr" style="margin-top:10px;">';
        $html .= '<input type="hidden" name="action" value="set_webhook">';
        $html .= '<button type="submit" class="btn" style="background:#3b82f6;color:#fff;padding:8px 16px;font-size:12px;margin:0;">🎯 Apuntar Webhook a este entorno (' . htmlspecialchars($currentHost) . ')</button>';
        $html .= '</form>';
    } else {
        $html .= '<div style="color:#10b981;font-size:11px;margin-top:5px;">✅ Este backend está recibiendo los mensajes de WhatsApp.</div>';
    }
    $html .= '</div>';

    if (! $showQr) {
        $html .= '<div class="badge open">🟢 Conectado (Sesión Abierta)</div>';
        $html .= '<p style="color:#94a3b8;font-size:14px;">La sesión está activa y vinculada correctamente. Si en el futuro necesitas renovarla, puedes usar los botones a continuación:</p>';
        $html .= '<form method="POST" action="/api/maintenance/connect-qr" style="margin-bottom:10px;">';
        $html .= '<input type="hidden" name="action" value="reconnect">';
        $html .= '<button type="submit" class="btn" style="background:#00e5d9;color:#0b132b;">🔄 Forzar Reconexión de Socket (Auto-Heal)</button>';
        $html .= '</form>';
        $html .= '<form method="GET" action="/api/maintenance/connect-qr" style="margin-bottom:10px;">';
        $html .= '<input type="hidden" name="force_qr" value="1">';
        $html .= '<button type="submit" class="btn" style="background:#3b82f6;color:#fff;">📱 Ver / Forzar Código QR de Vinculación</button>';
        $html .= '</form>';
        $html .= '<form method="POST" action="/api/maintenance/connect-qr" style="margin-bottom:10px;">';
        $html .= '<input type="hidden" name="action" value="reset_instance">';
        $html .= '<button type="submit" class="btn" style="background:#f59e0b;color:#0b132b;" onclick="return confirm(\'¿Recrear la instancia desde cero? Se borrarán las tablas y claves residuales de la BD en Railway y se generará un QR 100% limpio.\')">🧹 Recrear Instancia Limpia (Solución a SessionError)</button>';
        $html .= '</form>';
        $html .= '<form method="POST" action="/api/maintenance/connect-qr">';
        $html .= '<input type="hidden" name="action" value="logout">';
        $html .= '<button type="submit" class="btn btn-danger" onclick="return confirm(\'¿Estás seguro de desconectar WhatsApp? Deberás escanear un nuevo código QR desde el celular del bot.\')">🔴 Desconectar (Logout simple)</button>';
        $html .= '</form>';
    } else {
        $html .= '<div class="badge disconnected">🟡 Esperando Escaneo de QR (' . htmlspecialchars($state) . ')</div>';
        if ($qrBase64) {
            $imgSrc = str_starts_with($qrBase64, 'data:') ? $qrBase64 : 'data:image/png;base64,' . $qrBase64;
            $html .= '<div class="timer-badge" id="qr-timer">⚡ QR activo y actualizándose en vivo</div>';
            $html .= '<div><img id="qr-img" class="qr" src="' . $imgSrc . '" alt="Código QR WhatsApp" /></div>';
            $html .= '<p><strong>1.</strong> Abre WhatsApp en el celular del bot (<strong>5492323610697</strong>).</p>';
            $html .= '<p><strong>2.</strong> Ve a <em>Dispositivos vinculados</em> ➔ <em>Vincular un dispositivo</em>.</p>';
            $html .= '<p><strong>3.</strong> Apunta la cámara al código QR (se refresca solo para mantenerse siempre vigente).</p>';
        } else {
            $html .= '<p>Generando código QR de Evolution API...</p>';
            $html .= '<pre style="text-align:left;background:#0b132b;padding:10px;font-size:11px;overflow:auto;">' . htmlspecialchars(json_encode($connectData, JSON_PRETTY_PRINT)) . '</pre>';
        }
        $html .= '<script>
            setInterval(async () => {
                try {
                    const forceParam = window.location.search.includes("force_qr") ? "&force_qr=1" : "";
                    const res = await fetch("/api/maintenance/connect-qr-data?t=" + Date.now() + forceParam);
                    const data = await res.json();
                    if (data.server_time) {
                        const clock = document.getElementById("server-clock");
                        if (clock) clock.innerText = data.server_time;
                    }
                    if (data.state === "open" && !data.qr_base64 && !forceParam) {
                        location.reload();
                        return;
                    }
                    if (data.qr_base64) {
                        const img = document.getElementById("qr-img");
                        if (img) img.src = data.qr_base64;
                        const timer = document.getElementById("qr-timer");
                        if (timer) timer.innerText = "⚡ QR sincronizado en vivo (vigente)";
                    }
                } catch (e) {}
            }, 3000);
        </script>';
        $html .= '<br><a href="/api/maintenance/connect-qr?t=' . time() . '" class="btn">🔄 Refrescar Estado</a>';
    }

    $html .= '</div></body></html>';

    return response($html)
        ->header('Content-Type', 'text/html')
        ->header('Cache-Control', 'no-store, no-cache, must-revalidate, max-age=0')
        ->header('Pragma', 'no-cache')
        ->header('Expires', '0');
});

Route::post('/maintenance/setup-evolution-webhook', function (Request $request) {
    $baseUrl = config('services.evolution.url');
    $apiKey = config('services.evolution.api_key');
    $instance = config('services.evolution.instance', 'estoyok_main');

    if (! $baseUrl || ! filter_var($baseUrl, FILTER_VALIDATE_URL)) {
        return response()->json([
            'success' => false,
            'message' => 'Falta configurar la variable EVOLUTION_API_URL en Railway.',
            'current_value' => $baseUrl,
        ], 400);
    }

    $webhookUrl = $request->input('webhook_url') ?? 'https://api.estoyok24.com/api/webhooks/evolution/message';

    try {
        $url = rtrim($baseUrl, '/') . '/webhook/set/' . $instance;
        $response = Http::withHeaders([
            'apikey' => $apiKey,
            'Content-Type' => 'application/json',
        ])->post($url, [
            'webhook' => [
                'enabled' => true,
                'url' => $webhookUrl,
                'byEvents' => false,
                'base64' => false,
                'events' => [
                    'MESSAGES_UPSERT',
                    'MESSAGES_UPDATE',
                    'SEND_MESSAGE',
                ],
            ],
        ]);

        return response()->json([
            'success' => $response->successful(),
            'instance' => $instance,
            'configured_webhook_url' => $webhookUrl,
            'http_status' => $response->status(),
            'evolution_response' => $response->json() ?? $response->body(),
        ]);
    } catch (\Throwable $e) {
        return response()->json([
            'success' => false,
            'error' => $e->getMessage(),
        ], 500);
    }
});

Route::get('/maintenance/check-evolution-webhook', function () {
    $baseUrl = config('services.evolution.url');
    $apiKey = config('services.evolution.api_key');
    $instance = config('services.evolution.instance', 'estoyok_main');

    if (! $baseUrl || ! filter_var($baseUrl, FILTER_VALIDATE_URL)) {
        return response()->json(['success' => false, 'message' => 'EVOLUTION_API_URL not configured.'], 400);
    }

    try {
        $url = rtrim($baseUrl, '/') . '/webhook/find/' . $instance;
        $response = Http::withHeaders([
            'apikey' => $apiKey,
        ])->get($url);

        return response()->json([
            'success' => $response->successful(),
            'instance' => $instance,
            'http_status' => $response->status(),
            'evolution_response' => $response->json() ?? $response->body(),
        ]);
    } catch (\Throwable $e) {
        return response()->json(['success' => false, 'error' => $e->getMessage()], 500);
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

Route::post('/maintenance/purge-queued-jobs', function () {
    try {
        \Illuminate\Support\Facades\DB::table('failed_jobs')->truncate();
        $updatedGeofences = \Illuminate\Support\Facades\DB::table('geofences')->where('radius', '<', 100)->update(['radius' => 100]);
        if (config('queue.default') === 'redis') {
            try {
                \Illuminate\Support\Facades\Redis::connection()->flushdb();
            } catch (\Throwable $e) {
                // Redis flush optional if keys are handled
            }
        }
        return response()->json([
            'message' => 'Failed jobs purged, queue backlog cleared, and geofences updated to minimum 100m radius.',
            'geofences_upgraded_to_100m' => $updatedGeofences,
        ]);
    } catch (\Throwable $e) {
        return response()->json(['error' => $e->getMessage()], 500);
    }
});

Route::get('/maintenance/diagnose-push', function (Request $request) {
    $report = [];

    // Purge action if requested
    if ($request->query('purge_queues')) {
        try {
            \Illuminate\Support\Facades\DB::table('failed_jobs')->truncate();
            $upgraded = \Illuminate\Support\Facades\DB::table('geofences')->where('radius', '<', 100)->update(['radius' => 100]);
            if (config('queue.default') === 'redis') {
                try {
                    \Illuminate\Support\Facades\Redis::connection()->flushdb();
                } catch (\Throwable $e) {}
            }
            $report['purge_action_result'] = [
                'status' => 'success',
                'failed_jobs_truncated' => true,
                'geofences_upgraded_to_100m' => $upgraded,
            ];
        } catch (\Throwable $e) {
            $report['purge_action_result'] = ['error' => $e->getMessage()];
        }
    }

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
    $recentEvents = \App\Models\GeofenceEvent::with(['user', 'geofence'])
        ->latest('occurred_at')
        ->take(10)
        ->get();
    
    $currentLocations = \App\Models\CurrentLocation::with('user')->get()->map(function($cl) use ($geofences) {
        $distances = [];
        foreach ($geofences as $g) {
            $distResult = \Illuminate\Support\Facades\DB::selectOne(
                'SELECT ST_Distance(center, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography) as distance FROM geofences WHERE id = ?',
                [$cl->longitude, $cl->latitude, $g->id]
            );
            $distances[$g->name] = $distResult ? round((float) $distResult->distance, 1) . 'm' : 'N/A';
        }
        return [
            'user_id' => $cl->user_id,
            'user_name' => $cl->user?->name ?? 'Unknown',
            'latitude' => $cl->latitude,
            'longitude' => $cl->longitude,
            'updated_at' => (string) $cl->updated_at,
            'distances_to_geofences' => $distances,
        ];
    });

    $analiaHistory = \App\Models\LocationHistory::where('user_id', 21)
        ->latest('created_at')
        ->take(5)
        ->get()
        ->map(fn($lh) => [
            'latitude' => $lh->latitude,
            'longitude' => $lh->longitude,
            'created_at' => (string) $lh->created_at,
        ]);

    $report['geofencing_audit'] = [
        'active_geofences_count' => $geofences->count(),
        'geofences' => $geofences->map(fn($g) => ['id' => $g->id, 'name' => $g->name, 'circle_id' => $g->circle_id, 'radius' => $g->radius]),
        'recent_events' => $recentEvents->map(fn($e) => [
            'id' => $e->id,
            'user_name' => $e->user?->name ?? "User #{$e->user_id}",
            'geofence_name' => $e->geofence?->name ?? "Geofence #{$e->geofence_id}",
            'type' => $e->type,
            'occurred_at' => (string) $e->occurred_at,
        ]),
        'current_locations' => $currentLocations,
        'analia_recent_history' => $analiaHistory,
    ];

    // 5. Failed Jobs Audit
    try {
        $failedJobs = \Illuminate\Support\Facades\DB::table('failed_jobs')->latest('failed_at')->take(5)->get();
        $report['failed_jobs_count'] = $failedJobs->count();
        $report['failed_jobs_details'] = $failedJobs->map(fn($fj) => [
            'id' => $fj->id,
            'queue' => $fj->queue,
            'payload_job' => json_decode($fj->payload)->displayName ?? 'Unknown',
            'exception' => substr($fj->exception, 0, 200),
            'failed_at' => $fj->failed_at,
        ]);
    } catch (\Throwable $e) {
        $report['failed_jobs_count'] = 'N/A';
    }

    // 6. Test Push Dispatch if requested
    $testUserId = $request->query('test_user_id');
    if (!empty($testUserId)) {
        $targetUser = \App\Models\User::find($testUserId);
        if ($targetUser && !empty($targetUser->expo_push_token)) {
            $pushService = app(\App\Services\PushNotificationService::class);
            $pushTitle = $request->query('test_title', '🔔 Prueba de Notificación Estoy OK');
            $pushBody = $request->query('test_body', "Hola {$targetUser->name}, esta es una prueba de envío directo desde el servidor de Railway.");
            $sendSuccess = $pushService->sendPush(
                $targetUser->expo_push_token,
                $pushTitle,
                $pushBody,
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

    // 7. WhatsApp & Evolution API Audit
    $evoUrl = config('services.evolution.url');
    $evoKey = config('services.evolution.api_key');
    $evoInstance = config('services.evolution.instance', 'estoyok_main');

    $evoLiveStatus = null;
    $autoSetupResponse = null;

    if ($evoUrl && $evoKey) {
        try {
            $findRes = Http::withHeaders(['apikey' => $evoKey])->timeout(3)->get(rtrim($evoUrl, '/') . '/webhook/find/' . $evoInstance);
            $evoLiveStatus = $findRes->json() ?? $findRes->body();
        } catch (\Throwable $e) {
            $evoLiveStatus = ['error' => $e->getMessage()];
        }

        // Si se solicita ?setup_webhook=true o si no está configurado, auto-configurarlo
        if ($request->query('setup_webhook') || $request->query('auto_setup')) {
            try {
                $setRes = Http::withHeaders(['apikey' => $evoKey, 'Content-Type' => 'application/json'])->timeout(4)->post(rtrim($evoUrl, '/') . '/webhook/set/' . $evoInstance, [
                    'enabled' => true,
                    'url' => 'https://api.estoyok24.com/api/webhooks/evolution/message',
                    'webhookByEvents' => false,
                    'events' => ['MESSAGES_UPSERT', 'MESSAGES_UPDATE', 'SEND_MESSAGE'],
                    'webhook' => [
                        'enabled' => true,
                        'url' => 'https://api.estoyok24.com/api/webhooks/evolution/message',
                        'byEvents' => false,
                        'base64' => false,
                        'events' => ['MESSAGES_UPSERT', 'MESSAGES_UPDATE', 'SEND_MESSAGE'],
                    ],
                ]);
                $autoSetupResponse = $setRes->json() ?? $setRes->body();
            } catch (\Throwable $e) {
                $autoSetupResponse = ['error' => $e->getMessage()];
            }
        }
        $connState = null;
        try {
            $stateRes = Http::withHeaders(['apikey' => $evoKey])->timeout(3)->get(rtrim($evoUrl, '/') . '/instance/connectionState/' . $evoInstance);
            $connState = $stateRes->json() ?? $stateRes->body();
        } catch (\Throwable $e) {
            $connState = ['error' => $e->getMessage()];
        }
    }

    $report['whatsapp_evolution_audit'] = [
        'evolution_url' => $evoUrl,
        'evolution_instance' => $evoInstance,
        'evolution_api_key_configured' => !empty($evoKey),
        'instance_connection_state' => $connState ?? null,
        'live_webhook_from_evolution' => $evoLiveStatus,
        'auto_setup_response' => $autoSetupResponse,
        'recent_incoming_webhooks_history' => \Illuminate\Support\Facades\Cache::get('evolution_webhook_history', []),
        'users_phone_status' => $users->map(fn($u) => [
            'id' => $u->id,
            'name' => $u->name,
            'email' => $u->email,
            'phone' => $u->phone,
            'allow_sms_whatsapp_checkin' => (bool) $u->allow_sms_whatsapp_checkin,
            'last_check_in_at' => (string) $u->last_check_in_at,
            'email_verified_at' => (string) $u->email_verified_at,
        ]),
        'recent_checkins' => \App\Models\CheckIn::with('user')->latest()->take(8)->get()->map(fn($ci) => [
            'id' => $ci->id,
            'user_id' => $ci->user_id,
            'user_name' => $ci->user?->name ?? 'Unknown',
            'source' => $ci->source,
            'created_at' => (string) $ci->created_at,
        ]),
    ];

    // Simulación de matching de WhatsApp si se pasa ?simulate_phone=54911...
    $simPhone = $request->query('simulate_phone');
    if ($simPhone) {
        $cleanSim = preg_replace('/[^0-9]/', '', $simPhone);
        $matchedUser = $users->first(function ($u) use ($cleanSim) {
            if (empty($u->phone)) return false;
            $uPhone = preg_replace('/[^0-9]/', '', $u->phone);
            if ($uPhone === $cleanSim) return true;
            if (str_ends_with($cleanSim, $uPhone) || str_ends_with($uPhone, $cleanSim)) return true;
            if (strlen(substr($cleanSim, -10)) >= 8 && substr($cleanSim, -10) === substr($uPhone, -10)) return true;
            if (strlen(substr($cleanSim, -8)) >= 8 && substr($cleanSim, -8) === substr($uPhone, -8)) return true;
            return false;
        });

        $report['simulation_result'] = [
            'simulated_phone_input' => $simPhone,
            'cleaned_phone' => $cleanSim,
            'matched_user' => $matchedUser ? [
                'id' => $matchedUser->id,
                'name' => $matchedUser->name,
                'email' => $matchedUser->email,
                'phone_in_db' => $matchedUser->phone,
                'allow_sms_whatsapp_checkin' => (bool) $matchedUser->allow_sms_whatsapp_checkin,
                'email_verified_at' => (string) $matchedUser->email_verified_at,
            ] : null,
            'decision' => $matchedUser ? 'SUCCESS_USER_FOUND' : 'FAILED_USER_NOT_FOUND',
        ];
    }

    // 8. Actionable Diagnosis
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
Route::post('/webhooks/evolution/message', [WebhookController::class, 'evolutionMessage']);

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
    Route::post('/subscriptions/verify-google-play', [SubscriptionController::class, 'verifyGooglePlay']);
    Route::post('/subscriptions/sync-google-play', [SubscriptionController::class, 'syncGooglePlay']);
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
    Route::put('/settings/phone', [SettingsController::class, 'updatePhone']);
    Route::put('/settings/notify-self-whatsapp', [SettingsController::class, 'updateNotifySelfWhatsapp']);
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

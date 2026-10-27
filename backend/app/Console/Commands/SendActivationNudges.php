<?php

namespace App\Console\Commands;

use App\Models\User;
use App\Services\PushNotificationService;
use Exception;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Log;

class SendActivationNudges extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'users:send-activation-nudges';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Envía notificaciones push automáticas de activación temprana y nutrición para nuevos usuarios';

    /**
     * Execute the console command.
     */
    public function handle(PushNotificationService $pushService): int
    {
        $this->info('Iniciando evaluación de nudges de activación temprana...');

        $now = now();
        $sentCount = 0;

        // Candidatos: Usuarios creados en los últimos 7 días con token push activo
        $candidates = User::whereNotNull('expo_push_token')
            ->where('expo_push_token', '!=', '')
            ->where('created_at', '>=', $now->copy()->subDays(7))
            ->with(['emergencyContacts'])
            ->get();

        foreach ($candidates as $user) {
            try {
                if ($user->isInQuietHours()) {
                    continue;
                }

                $hoursSinceCreation = $user->created_at->diffInHours($now);
                $firstName = explode(' ', trim($user->name ?: ''))[0] ?: 'Hola';

                // Nudge 1: Si nunca hizo check-in (A partir de las 6 horas, rescata también a los que llevan 1 o 2 días sin reporte)
                if ($hoursSinceCreation >= 6 && is_null($user->last_check_in_at)) {
                    $cacheKey = "activation_nudge_6h_user_{$user->id}";
                    if (!Cache::has($cacheKey)) {
                        $title = "👋 ¡Hola {$firstName}!";
                        $body = "Tu botón Estoy OK está listo. Tocalo para confirmar tu bienestar y activar tu escudo diario.";
                        $data = ['type' => 'activation_checkin', 'screen' => 'Panel'];

                        $sent = $pushService->sendPush($user->expo_push_token, $title, $body, $data);
                        $this->info("Nudge de primer check-in enviado a: {$user->email} - Resultado: " . ($sent ? 'EXITOSO' : 'FALLIDO'));
                        if ($sent) {
                            Cache::put($cacheKey, true, now()->addDays(14));
                            $sentCount++;
                        }
                    } else {
                        $this->info("Nudge de primer check-in ya enviado a: {$user->email} (en cache)");
                    }
                    // Si nunca hizo check-in, priorizamos este aviso antes de pedirle contactos o premium
                    continue;
                }

                // Nudge 2: A partir de las 24 horas si no tiene contactos de emergencia cargados
                if ($hoursSinceCreation >= 24 && $user->emergencyContacts->isEmpty()) {
                    $cacheKey = "activation_nudge_24h_user_{$user->id}";
                    if (!Cache::has($cacheKey)) {
                        $title = "🛡️ {$firstName}, ¿quién te cuidará?";
                        $body = "Agregá a tu contacto de confianza en Ajustes para avisarle automáticamente por WhatsApp si te pasa algo.";
                        $data = ['type' => 'activation_contact', 'screen' => 'Ajustes'];

                        if ($pushService->sendPush($user->expo_push_token, $title, $body, $data)) {
                            Cache::put($cacheKey, true, now()->addDays(14));
                            $sentCount++;
                            $this->info("Nudge de contactos de emergencia enviado a: {$user->email}");
                        }
                    }
                    continue;
                }

                // Nudge 3: A partir de las 48 horas si no es Premium y no ha usado su trial
                if ($hoursSinceCreation >= 48 && !$user->is_premium && !$user->has_used_trial) {
                    $cacheKey = "activation_nudge_48h_user_{$user->id}";
                    if (!Cache::has($cacheKey)) {
                        $title = "⚡ Probá Estoy Ok Premium Gratis";
                        $body = "Tenés 7 días sin cargo para activar WhatsApp automático, SOS de bolsillo y detección de choques.";
                        $data = ['type' => 'activation_trial', 'screen' => 'Premium'];

                        if ($pushService->sendPush($user->expo_push_token, $title, $body, $data)) {
                            Cache::put($cacheKey, true, now()->addDays(14));
                            $sentCount++;
                            $this->info("Nudge de oferta de Trial enviado a: {$user->email}");
                        }
                    }
                    continue;
                }

                // Nudge 4: A las 72 horas si lleva más de 48 horas sin reportar bienestar
                if ($hoursSinceCreation >= 72 && (is_null($user->last_check_in_at) || $user->last_check_in_at->diffInHours($now) >= 48)) {
                    $cacheKey = "activation_nudge_72h_user_{$user->id}";
                    if (!Cache::has($cacheKey)) {
                        $title = "💚 Tranquilidad para los que te quieren";
                        $body = "Un solo toque al día mantiene al día tu tranquilidad. Ingresá y dejá tu reporte de hoy.";
                        $data = ['type' => 'activation_retention', 'screen' => 'Panel'];

                        if ($pushService->sendPush($user->expo_push_token, $title, $body, $data)) {
                            Cache::put($cacheKey, true, now()->addDays(14));
                            $sentCount++;
                            $this->info("Nudge de reenganche enviado a: {$user->email}");
                        }
                    }
                }
            } catch (Exception $e) {
                Log::error("Error enviando activation nudge a usuario {$user->id}: " . $e->getMessage());
            }
        }

        $this->info("Proceso completado. Se enviaron {$sentCount} nudges de activación.");
        return 0;
    }
}

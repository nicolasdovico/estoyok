<?php

namespace App\Console\Commands;

use App\Jobs\SendInactivityAlerts;
use App\Models\User;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\Log;

class VerifyInactivity extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'checkins:verify-inactivity';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Identificar usuarios inactivos que han superado el umbral de check-in';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $this->info('Iniciando verificación de inactividad...');

        $isLocal = app()->environment('local');
        $unit = $isLocal ? 'minutes' : 'hours';

        if ($isLocal) {
            $this->warn('ENTORNO LOCAL DETECTADO: Los intervalos se tratarán como MINUTOS para pruebas.');
        }

        // Buscamos usuarios que han superado su umbral personalizado de check-in
        // y que no tengan ya una alerta de emergencia activa (para evitar duplicar notificaciones)
        $inactiveUsers = User::where(function ($query) use ($unit) {
            // El usuario es inactivo si su último check-in fue hace más de X unidades (horas o minutos)
            // O si nunca hizo uno y su cuenta tiene más de X unidades
            $query->whereRaw("last_check_in_at < (NOW() AT TIME ZONE 'UTC') - (checkin_interval_hours || ' {$unit}')::interval")
                ->orWhere(function ($q) use ($unit) {
                    $q->whereNull('last_check_in_at')
                        ->whereRaw("created_at < (NOW() AT TIME ZONE 'UTC') - (checkin_interval_hours || ' {$unit}')::interval");
                });
        })
            ->whereDoesntHave('emergencyAlerts', function ($query) {
                $query->where('status', 'active')
                    ->where('type', 'inactivity');
            })
            ->get();

        $count = 0;
        foreach ($inactiveUsers as $user) {
            if ($user->isInQuietHours()) {
                Log::info("Usuario {$user->id} ({$user->email}) está en horas silenciosas. Omitiendo alerta de inactividad.");
                continue;
            }

            // Bloqueo atómico de 2 minutos para evitar ejecuciones concurrentes o duplicados en el mismo minuto
            $lockKey = "inactivity_alert_dispatched_user_{$user->id}";
            if (! \Illuminate\Support\Facades\Cache::add($lockKey, true, 120)) {
                Log::info("Alerta de inactividad ya despachada recientemente para usuario {$user->id}, omitiendo duplicado.");
                continue;
            }

            try {
                // Crear la alerta atómicamente para que whereDoesntHave la excluya de inmediato
                $alert = \App\Models\EmergencyAlert::create([
                    'user_id' => $user->id,
                    'type' => 'inactivity',
                    'status' => 'active',
                    'expires_at' => now()->addHours(48),
                ]);

                // Despachar el Job: sincrónico en producción para entrega inmediata sin depender de Redis, y dispatch en tests
                $dispatched = app()->runningUnitTests()
                    ? SendInactivityAlerts::dispatch($user, $alert->id)
                    : SendInactivityAlerts::dispatchSync($user, $alert->id);

                \Illuminate\Support\Facades\Cache::put('last_verify_dispatch_attempt', [
                    'time' => now()->toDateTimeString(),
                    'user_id' => $user->id,
                    'alert_id' => $alert->id,
                    'queue_driver' => config('queue.default'),
                    'redis_queue' => config('queue.connections.redis.queue'),
                    'redis_prefix' => config('database.redis.options.prefix'),
                    'dispatched_class' => is_object($dispatched) ? get_class($dispatched) : gettype($dispatched),
                ], 3600);

                Log::info("Alerta de inactividad {$alert->id} creada y procesada para usuario {$user->id} ({$user->email}).");
                $count++;
            } catch (\Throwable $e) {
                // Si falló el dispatch, eliminar la alerta huérfana y liberar el lock para reintentar en el siguiente minuto
                if (isset($alert) && $alert->exists) {
                    $alert->delete();
                }
                \Illuminate\Support\Facades\Cache::forget($lockKey);

                Log::error("Error crítico despachando alerta de inactividad para usuario {$user->id}: " . $e->getMessage(), [
                    'trace' => $e->getTraceAsString(),
                ]);

                \Illuminate\Support\Facades\Cache::put('last_verify_dispatch_error', [
                    'time' => now()->toDateTimeString(),
                    'user_id' => $user->id,
                    'error' => $e->getMessage(),
                    'trace' => $e->getTraceAsString(),
                ], 3600);
            }
        }

        $this->info("Proceso completado. Se han encolado alertas para {$count} usuarios.");
    }
}

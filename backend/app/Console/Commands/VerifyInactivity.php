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

            // Crear la alerta atómicamente para que whereDoesntHave la excluya de inmediato
            $alert = \App\Models\EmergencyAlert::create([
                'user_id' => $user->id,
                'type' => 'inactivity',
                'status' => 'active',
                'expires_at' => now()->addHours(48),
            ]);

            // Despachar el Job con el ID de la alerta ya persistida
            SendInactivityAlerts::dispatch($user, $alert->id);

            $count++;
        }

        $this->info("Proceso completado. Se han encolado alertas para {$count} usuarios.");
    }
}

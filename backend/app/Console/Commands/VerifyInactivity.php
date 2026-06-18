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
            $query->whereRaw("last_check_in_at < NOW() - (checkin_interval_hours || ' {$unit}')::interval")
                ->orWhere(function ($q) use ($unit) {
                    $q->whereNull('last_check_in_at')
                        ->whereRaw("created_at < NOW() - (checkin_interval_hours || ' {$unit}')::interval");
                });
        })
            ->whereDoesntHave('emergencyAlerts', function ($query) {
                $query->where('status', 'active');
            })
            ->get();

        $count = 0;
        foreach ($inactiveUsers as $user) {
            if ($user->isInQuietHours()) {
                Log::info("Usuario {$user->id} ({$user->email}) está en horas silenciosas. Omitiendo alerta de inactividad.");
                continue;
            }

            // Despachar el Job de alertas
            SendInactivityAlerts::dispatch($user);
            $count++;
        }

        $this->info("Proceso completado. Se han encolado alertas para {$count} usuarios.");
    }
}

<?php

namespace App\Console\Commands;

use App\Jobs\SendInactivityAlerts;
use App\Models\User;
use Illuminate\Console\Command;
use Illuminate\Support\Carbon;

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

        // Por defecto 24 horas. En el futuro esto podría ser configurable por usuario Premium.
        $threshold = Carbon::now()->subHours(24);

        $inactiveUsers = User::where(function ($query) use ($threshold) {
            $query->where('last_check_in_at', '<', $threshold)
                  ->orWhereNull('last_check_in_at');
        })
        ->where('created_at', '<', Carbon::now()->subHours(24)) // Evitar alertar a usuarios recién creados
        ->get();

        $count = 0;
        foreach ($inactiveUsers as $user) {
            // Despachar el Job de alertas
            SendInactivityAlerts::dispatch($user);
            $count++;
        }

        $this->info("Proceso completado. Se han encolado alertas para {$count} usuarios.");
    }
}

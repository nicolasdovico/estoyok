<?php

namespace App\Console\Commands;

use App\Mail\TrialExpiringSoonMail;
use App\Models\User;
use Exception;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Mail;

class SendTrialReminders extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'subscriptions:send-trial-reminders';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Enviar notificaciones preventivas a usuarios cuya prueba gratuita vence en 2 días';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $this->info('Iniciando verificación de pruebas gratuitas por vencer...');

        // Buscamos usuarios en estado 'trialing' cuyo trial venza en los próximos 2 días (<= 48 horas)
        // y a quienes aún no les hayamos enviado el recordatorio.
        $usersToRemind = User::whereNotNull('trial_ends_at')
            ->where('trial_ends_at', '>', now())
            ->where('trial_ends_at', '<=', now()->addDays(2))
            ->whereNull('trial_reminder_sent_at')
            ->get();

        $count = 0;
        foreach ($usersToRemind as $user) {
            try {
                // 1. Enviar Email
                try {
                    Mail::to($user->email)->send(new TrialExpiringSoonMail($user));
                } catch (Exception $e) {
                    Log::warning("No se pudo enviar email de vencimiento de trial a {$user->email}: " . $e->getMessage());
                }

                // 2. Enviar Push Notification si tiene expo_push_token
                if ($user->expo_push_token) {
                    Http::post('https://exp.host/--/api/v2/push/send', [
                        'to' => $user->expo_push_token,
                        'title' => '👑 Tu prueba gratuita vence en 2 días',
                        'body' => 'Tu prueba de Estoy Ok PRO finaliza en 2 días. Si deseas continuar se cobrará $4.99/mes, o puedes cancelar cuando quieras sin costo.',
                        'data' => [
                            'type' => 'trial_expiring_soon',
                            'user_id' => $user->id,
                        ],
                        'priority' => 'high',
                    ]);
                }

                // 3. Marcar reminder como enviado
                $user->update(['trial_reminder_sent_at' => now()]);
                $count++;
            } catch (Exception $e) {
                Log::error("Error procesando aviso de prueba gratis para usuario ID {$user->id}: " . $e->getMessage());
            }
        }

        $this->info("Se enviaron {$count} notificaciones de recordatorio de prueba gratuita.");
        return Command::SUCCESS;
    }
}

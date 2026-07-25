<?php

namespace App\Console\Commands;

use App\Mail\SubscriptionSuspendedMail;
use App\Models\User;
use Exception;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Mail;

class CheckExpiredGracePeriods extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'subscriptions:check-expired-grace-periods';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Verificar periodos de gracia vencidos y revertir cuentas al Plan Gratuito';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $this->info('Iniciando verificación de periodos de gracia vencidos...');

        $expiredUsers = User::where('subscription_status', 'grace_period')
            ->whereNotNull('grace_period_ends_at')
            ->where('grace_period_ends_at', '<=', now())
            ->get();

        $count = 0;
        foreach ($expiredUsers as $user) {
            try {
                // Revertir a plan gratuito
                $user->update([
                    'subscription_status' => 'canceled',
                    'is_premium' => false,
                ]);

                // 1. Enviar Email
                try {
                    Mail::to($user->email)->send(new SubscriptionSuspendedMail($user));
                } catch (Exception $e) {
                    Log::warning("No se pudo enviar email de suspensión a {$user->email}: " . $e->getMessage());
                }

                // 2. Enviar Push Notification
                if ($user->expo_push_token) {
                    Http::post('https://exp.host/--/api/v2/push/send', [
                        'to' => $user->expo_push_token,
                        'title' => '⚠️ Suscripción Estoy Ok PRO pausada',
                        'body' => 'Tu periodo de gracia ha vencido y tu cuenta pasó al Plan Gratuito. Toca aquí para reactivar tu protección PRO.',
                        'data' => [
                            'type' => 'subscription_suspended',
                            'user_id' => $user->id,
                        ],
                        'priority' => 'high',
                    ]);
                }

                $count++;
            } catch (Exception $e) {
                Log::error("Error revirtiendo periodo de gracia para usuario ID {$user->id}: " . $e->getMessage());
            }
        }

        $this->info("Se suspendieron {$count} suscripciones con periodo de gracia vencido.");
        return Command::SUCCESS;
    }
}

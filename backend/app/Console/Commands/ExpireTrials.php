<?php

namespace App\Console\Commands;

use App\Mail\SubscriptionCanceledMail;
use App\Models\User;
use Exception;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Mail;

class ExpireTrials extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'subscriptions:expire-trials';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Expirar pruebas gratuitas vencidas no renovadas y actualizar estado a canceled';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $this->info('Iniciando verificación de pruebas gratuitas vencidas...');

        // 1. Pruebas directas (sin tarjeta) vencidas inmediatamente
        // 2. Pruebas de Google Play vencidas con más de 24 horas de margen sin haber sincronizado pago activo
        $expiredUsers = User::where('subscription_status', 'trialing')
            ->whereNotNull('trial_ends_at')
            ->where(function ($query) {
                $query->where(function ($q) {
                    $q->where('subscription_provider', 'trial')
                      ->where('trial_ends_at', '<=', now());
                })->orWhere(function ($q) {
                    $q->where('subscription_provider', 'google_play')
                      ->where('trial_ends_at', '<=', now()->subHours(24));
                })->orWhere(function ($q) {
                    $q->whereNull('subscription_provider')
                      ->where('trial_ends_at', '<=', now());
                });
            })
            ->get();

        $count = 0;
        $adminEmail = config('mail.admin_notification_email');

        foreach ($expiredUsers as $user) {
            try {
                $user->update([
                    'subscription_status' => 'canceled',
                    'is_premium' => false,
                ]);

                // Notificar al administrador
                if (!empty($adminEmail)) {
                    try {
                        $activePremium = User::where('is_premium', true)->count();
                        Mail::to($adminEmail)->send(new SubscriptionCanceledMail($user, 'Período de prueba vencido sin confirmación de pago', $activePremium));
                    } catch (Exception $e) {
                        Log::warning("No se pudo enviar notificación de baja de prueba al administrador: " . $e->getMessage());
                    }
                }

                $count++;
            } catch (Exception $e) {
                Log::error("Error expirando prueba para usuario ID {$user->id}: " . $e->getMessage());
            }
        }

        $this->info("Se procesaron y cancelaron {$count} pruebas gratuitas vencidas.");
        return Command::SUCCESS;
    }
}

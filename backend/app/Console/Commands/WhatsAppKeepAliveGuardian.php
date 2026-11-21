<?php

namespace App\Console\Commands;

use App\Mail\WhatsAppGatewayDownMail;
use App\Mail\WhatsAppGatewayRecoveredMail;
use App\Services\EvolutionApiService;
use Carbon\Carbon;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Mail;

class WhatsAppKeepAliveGuardian extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'whatsapp:keep-alive-guardian';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Monitorea el socket de WhatsApp en Evolution API, mantiene el túnel caliente, auto-reconecta y alerta por email si está caído';

    protected const ADMIN_ALERT_EMAIL = 'nicolasdovico@gmail.com';
    protected const CACHE_KEY_DOWN_FIRST_SEEN = 'whatsapp_guardian_down_first_seen';
    protected const CACHE_KEY_ALERT_COOLDOWN = 'whatsapp_guardian_alert_cooldown';
    protected const CACHE_KEY_WAS_DOWN = 'whatsapp_guardian_was_down';

    /**
     * Execute the console command.
     */
    public function handle(EvolutionApiService $evolutionService)
    {
        $health = $evolutionService->checkConnectionHealth();
        $status = $health['status'] ?? 'unknown';
        $connected = $health['connected'] ?? false;
        $error = $health['error'] ?? null;

        if (! $connected) {
            $this->warn("WhatsApp Guardian: Socket no saludable (estado: '{$status}'). Iniciando auto-reconexión...");
            Log::warning("WhatsApp Guardian: Socket state was '{$status}'. Auto-reconnecting instance...");

            $reconnected = $evolutionService->reconnectInstance();

            if ($reconnected) {
                $this->info("WhatsApp Guardian: Solicitud de reconexión despachada exitosamente.");
                Log::info("WhatsApp Guardian: Reconnection request dispatched successfully.");
            } else {
                $this->error("WhatsApp Guardian: Falló el intento de auto-reconexión.");
                Log::error("WhatsApp Guardian: Auto-reconnection attempt failed.");
            }

            $this->handleDowntimeAlert($status, $error);

            return 1;
        }

        $this->handleRecovery();

        $this->info("WhatsApp Guardian: Socket saludable (estado: '{$status}'). Latido registrado.");
        Log::debug("WhatsApp Guardian: Heartbeat OK, status: {$status}");

        return 0;
    }

    /**
     * Maneja el seguimiento de caída y el despacho de alerta por email con cooldown de 3 horas.
     */
    protected function handleDowntimeAlert(string $status, ?string $error = null): void
    {
        $now = Carbon::now();

        if (! Cache::has(self::CACHE_KEY_DOWN_FIRST_SEEN)) {
            Cache::put(self::CACHE_KEY_DOWN_FIRST_SEEN, $now->toIso8601String(), $now->copy()->addDays(2));
        }

        // Si ya se envió alerta y estamos en cooldown de 3 horas, no enviamos de nuevo
        if (Cache::has(self::CACHE_KEY_ALERT_COOLDOWN)) {
            $this->comment("WhatsApp Guardian: Alerta ya enviada previamente (cooldown de 3hs activo).");
            return;
        }

        // Enviar alerta por email a administración
        try {
            Mail::to(self::ADMIN_ALERT_EMAIL)->send(
                new WhatsAppGatewayDownMail($status, $now->toDateTimeString(), $error)
            );

            // Cooldown de 3 horas para la siguiente alerta
            Cache::put(self::CACHE_KEY_ALERT_COOLDOWN, true, $now->copy()->addHours(3));
            Cache::put(self::CACHE_KEY_WAS_DOWN, true, $now->copy()->addDays(2));

            $this->info("WhatsApp Guardian: Alerta por correo enviada exitosamente a " . self::ADMIN_ALERT_EMAIL);
            Log::emergency("WhatsApp Guardian: Sent downtime alert email to " . self::ADMIN_ALERT_EMAIL);
        } catch (\Throwable $e) {
            $this->error("WhatsApp Guardian: Error al enviar correo de alerta: " . $e->getMessage());
            Log::error("WhatsApp Guardian: Failed sending downtime alert email: " . $e->getMessage());
        }
    }

    /**
     * Si el servicio se recuperó tras una caída notificada, envía el mail de normalización.
     */
    protected function handleRecovery(): void
    {
        if (Cache::get(self::CACHE_KEY_WAS_DOWN)) {
            $firstSeen = Cache::get(self::CACHE_KEY_DOWN_FIRST_SEEN);
            $downtimeMinutes = null;

            if ($firstSeen) {
                try {
                    $downtimeMinutes = Carbon::parse($firstSeen)->diffInMinutes(Carbon::now());
                } catch (\Throwable $e) {
                    // Ignore parsing error
                }
            }

            try {
                Mail::to(self::ADMIN_ALERT_EMAIL)->send(
                    new WhatsAppGatewayRecoveredMail(Carbon::now()->toDateTimeString(), $downtimeMinutes)
                );
                $this->info("WhatsApp Guardian: Correo de restablecimiento enviado a " . self::ADMIN_ALERT_EMAIL);
                Log::info("WhatsApp Guardian: Sent recovery email to " . self::ADMIN_ALERT_EMAIL);
            } catch (\Throwable $e) {
                Log::error("WhatsApp Guardian: Failed sending recovery email: " . $e->getMessage());
            }

            // Limpiar estados de caída
            Cache::forget(self::CACHE_KEY_WAS_DOWN);
            Cache::forget(self::CACHE_KEY_ALERT_COOLDOWN);
            Cache::forget(self::CACHE_KEY_DOWN_FIRST_SEEN);
        } else {
            // Asegurar limpieza si estuvo intermitente sin disparar alerta
            Cache::forget(self::CACHE_KEY_DOWN_FIRST_SEEN);
        }
    }
}

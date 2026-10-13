<?php

namespace App\Console\Commands;

use App\Services\EvolutionApiService;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\Log;

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
    protected $description = 'Monitorea el socket de WhatsApp en Evolution API, mantiene el túnel caliente y auto-reconecta si está caído';

    /**
     * Execute the console command.
     */
    public function handle(EvolutionApiService $evolutionService)
    {
        $health = $evolutionService->checkConnectionHealth();
        $status = $health['status'] ?? 'unknown';
        $connected = $health['connected'] ?? false;

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

            return 1;
        }

        $this->info("WhatsApp Guardian: Socket saludable (estado: '{$status}'). Latido registrado.");
        Log::debug("WhatsApp Guardian: Heartbeat OK, status: {$status}");

        return 0;
    }
}

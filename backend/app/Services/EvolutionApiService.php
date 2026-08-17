<?php

namespace App\Services;

use Exception;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class EvolutionApiService implements WhatsAppServiceInterface
{
    protected ?string $baseUrl;

    protected ?string $apiKey;

    protected ?string $instance;

    public function __construct()
    {
        $this->baseUrl = rtrim(config('services.evolution.url', 'http://evolution-api:8080'), '/');
        $this->apiKey = config('services.evolution.api_key');
        $this->instance = config('services.evolution.instance', 'estoyok_main');
    }

    public function sendWhatsApp(string $to, string $message, array $parameters = []): bool
    {
        if (! $this->baseUrl || ! $this->apiKey) {
            Log::info("[SIMULATED EVOLUTION WHATSAPP] To: {$to} | Message: {$message}");

            return true;
        }

        try {
            $this->ensureWebhookConfigured();

            $cleanTo = preg_replace('/[^0-9]/', '', $to);

            $url = "{$this->baseUrl}/message/sendText/{$this->instance}";

            $response = Http::withHeaders([
                'apikey' => $this->apiKey,
                'Content-Type' => 'application/json',
            ])->post($url, [
                'number' => $cleanTo,
                'text' => $message,
            ]);

            if ($response->successful()) {
                return true;
            }

            Log::error('Evolution API Error: ' . $response->body());

            return false;
        } catch (Exception $e) {
            Log::error('Evolution API Exception: ' . $e->getMessage());

            return false;
        }
    }

    /**
     * Auto-registra el webhook de recepción en Evolution API si no se ha registrado aún
     */
    public function ensureWebhookConfigured(): void
    {
        if (! $this->baseUrl || ! $this->apiKey) {
            return;
        }

        // Cache check once per day to avoid redundant HTTP requests
        $cacheKey = "evolution_webhook_configured_{$this->instance}";
        if (\Illuminate\Support\Facades\Cache::has($cacheKey)) {
            return;
        }

        try {
            $webhookUrl = 'https://api.estoyok24.com/api/webhooks/evolution/message';
            $url = "{$this->baseUrl}/webhook/set/{$this->instance}";

            $response = Http::withHeaders([
                'apikey' => $this->apiKey,
                'Content-Type' => 'application/json',
            ])->timeout(4)->post($url, [
                'enabled' => true,
                'url' => $webhookUrl,
                'webhookByEvents' => false,
                'events' => [
                    'MESSAGES_UPSERT',
                    'MESSAGES_UPDATE',
                    'SEND_MESSAGE',
                ],
                'webhook' => [
                    'enabled' => true,
                    'url' => $webhookUrl,
                    'byEvents' => false,
                    'base64' => false,
                    'events' => [
                        'MESSAGES_UPSERT',
                        'MESSAGES_UPDATE',
                        'SEND_MESSAGE',
                    ],
                ],
            ]);

            if ($response->successful()) {
                \Illuminate\Support\Facades\Cache::put($cacheKey, true, now()->addDay());
                Log::info("Evolution API Webhook auto-configured successfully for instance {$this->instance}");
            }
        } catch (\Throwable $e) {
            Log::warning("Could not auto-configure Evolution webhook: " . $e->getMessage());
        }
    }
}

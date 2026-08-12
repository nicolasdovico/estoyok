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
}

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
        $this->baseUrl = rtrim(config('services.evolution.url') ?: env('EVOLUTION_API_URL', 'http://evolution-api:8080'), '/');
        $this->apiKey = config('services.evolution.api_key') ?: env('EVOLUTION_API_KEY') ?: env('EVOLUTION_KEY');
        $this->instance = config('services.evolution.instance') ?: env('EVOLUTION_INSTANCE_NAME', 'estoyok_main');
    }

    public function sendWhatsApp(string $to, string $message, array $parameters = []): bool
    {
        if (! $this->baseUrl) {
            $this->baseUrl = rtrim(config('services.evolution.url') ?: env('EVOLUTION_API_URL', 'http://evolution-api:8080'), '/');
        }
        if (! $this->apiKey) {
            $this->apiKey = config('services.evolution.api_key') ?: env('EVOLUTION_API_KEY') ?: env('EVOLUTION_KEY');
        }

        if (! $this->baseUrl || ! $this->apiKey) {
            Log::info("[SIMULATED EVOLUTION WHATSAPP] To: {$to} | Message: {$message}");

            \Illuminate\Support\Facades\Cache::put('last_evolution_simulated', [
                'time' => now()->toDateTimeString(),
                'to' => $to,
                'message' => $message,
                'missing_config' => [
                    'baseUrl' => empty($this->baseUrl),
                    'apiKey' => empty($this->apiKey),
                ],
            ], 3600);

            return true;
        }

        try {
            $this->ensureWebhookConfigured();

            $cleanTo = str_contains($to, '@') ? $to : preg_replace('/[^0-9]/', '', $to);

            // Normalización para móviles de Argentina: asegurar formato canónico 549...
            if (str_starts_with($cleanTo, '54') && !str_starts_with($cleanTo, '549') && strlen($cleanTo) === 12) {
                $cleanTo = '549' . substr($cleanTo, 2);
            }

            $url = "{$this->baseUrl}/message/sendText/{$this->instance}";

            $payload = [
                'number' => $cleanTo,
                'text' => $message,
                'linkPreview' => (bool) ($parameters['linkPreview'] ?? false),
            ];

            if (isset($parameters['delay']) && is_numeric($parameters['delay'])) {
                $payload['delay'] = (int) $parameters['delay'];
            }

            $response = Http::withHeaders([
                'apikey' => $this->apiKey,
                'Content-Type' => 'application/json',
            ])->post($url, $payload);

            if ($response->successful()) {
                \Illuminate\Support\Facades\Cache::put('last_evolution_success', [
                    'time' => now()->toDateTimeString(),
                    'to' => $cleanTo,
                    'status' => $response->status(),
                ], 3600);
                return true;
            }

            $responseBody = $response->body();
            \Illuminate\Support\Facades\Cache::put('last_evolution_error', [
                'time' => now()->toDateTimeString(),
                'to' => $cleanTo,
                'status' => $response->status(),
                'body' => $responseBody,
            ], 3600);

            // Si el socket devolvió Connection Closed / Error 1006 / close, intentar auto-recuperar en caliente y reintentar una vez
            if (
                str_contains($responseBody, 'Connection Closed') ||
                str_contains($responseBody, '1006') ||
                str_contains($responseBody, 'is not open') ||
                str_contains($responseBody, 'Connecting') ||
                str_contains($responseBody, 'close')
            ) {
                Log::warning("Evolution API: Socket closed / Error 1006 detected ({$responseBody}). Attempting hot auto-recovery and retry...");
                $this->reconnectInstance();

                if (! app()->runningUnitTests()) {
                    // Esperar hasta que el socket alcance el estado 'open' (hasta 6 segundos)
                    for ($attempt = 0; $attempt < 6; $attempt++) {
                        usleep(1000000); // 1s
                        $health = $this->checkConnectionHealth();
                        if (($health['connected'] ?? false) || ($health['status'] ?? '') === 'open') {
                            break;
                        }
                    }
                }

                $retryResponse = Http::withHeaders([
                    'apikey' => $this->apiKey,
                    'Content-Type' => 'application/json',
                ])->post($url, $payload);

                if ($retryResponse->successful()) {
                    Log::info("Evolution API: Message sent successfully after hot auto-recovery retry.");
                    \Illuminate\Support\Facades\Cache::put('last_evolution_success', [
                        'time' => now()->toDateTimeString(),
                        'to' => $cleanTo,
                        'status' => $retryResponse->status(),
                        'recovered' => true,
                    ], 3600);
                    return true;
                }

                Log::error('Evolution API Error on retry: ' . $retryResponse->body());
                return false;
            }

            Log::error('Evolution API Error: ' . $responseBody);

            return false;
        } catch (Exception $e) {
            \Illuminate\Support\Facades\Cache::put('last_evolution_error', [
                'time' => now()->toDateTimeString(),
                'to' => $cleanTo ?? $to,
                'exception' => $e->getMessage(),
            ], 3600);
            Log::error('Evolution API Exception: ' . $e->getMessage());

            return false;
        }
    }

    /**
     * Fuerza la reconexión del socket de la instancia en Evolution API
     */
    public function reconnectInstance(): bool
    {
        if (! $this->baseUrl || ! $this->apiKey) {
            return false;
        }

        try {
            $restartUrl = "{$this->baseUrl}/instance/restart/{$this->instance}";
            $restartRes = Http::withHeaders([
                'apikey' => $this->apiKey,
            ])->timeout(8)->post($restartUrl);

            $connectUrl = "{$this->baseUrl}/instance/connect/{$this->instance}";
            $connectRes = Http::withHeaders([
                'apikey' => $this->apiKey,
            ])->timeout(8)->get($connectUrl);

            Log::info("Evolution API: Reconnect invoked for instance {$this->instance}", [
                'restart_status' => $restartRes->status(),
                'connect_status' => $connectRes->status(),
            ]);

            return $restartRes->successful() || $connectRes->successful();
        } catch (\Throwable $e) {
            Log::warning("Evolution API: Reconnection failed: " . $e->getMessage());
            return false;
        }
    }

    /**
     * Verifica la salud del socket de la instancia en Evolution API
     */
    public function checkConnectionHealth(): array
    {
        if (! $this->baseUrl || ! $this->apiKey) {
            return ['status' => 'unconfigured', 'connected' => false];
        }

        try {
            $stateRes = Http::withHeaders(['apikey' => $this->apiKey])
                ->timeout(4)
                ->get("{$this->baseUrl}/instance/connectionState/{$this->instance}");

            $data = $stateRes->json();
            $state = strtolower($data['instance']['state'] ?? ($data['state'] ?? 'unknown'));

            $isHealthy = in_array($state, ['open']);

            return [
                'status' => $state,
                'connected' => $isHealthy,
                'http_code' => $stateRes->status(),
            ];
        } catch (\Throwable $e) {
            return [
                'status' => 'error',
                'connected' => false,
                'error' => $e->getMessage(),
            ];
        }
    }

    /**
     * Auto-registra el webhook de recepción en Evolution API si no se ha registrado aún
     */
    public function ensureWebhookConfigured(?string $explicitBackendUrl = null, bool $force = false): void
    {
        if (! $this->baseUrl || ! $this->apiKey) {
            return;
        }

        // Si estamos en Dev y la instancia es la compartida 'estoyok_main', no sobreescribir el webhook
        // principal de producción para no romper la recepción de WhatsApp en producción.
        if (env('RAILWAY_ENVIRONMENT_NAME') === 'dev' && $this->instance === 'estoyok_main' && ! $explicitBackendUrl) {
            return;
        }

        // Cache check once per day to avoid redundant HTTP requests
        $cacheKey = "evolution_webhook_configured_{$this->instance}";
        if (! $force && ! $explicitBackendUrl && \Illuminate\Support\Facades\Cache::has($cacheKey)) {
            return;
        }

        try {
            $backendUrl = $explicitBackendUrl;
            if (empty($backendUrl)) {
                $backendUrl = config('app.url') ?: env('APP_URL');
            }
            if ((empty($backendUrl) || str_contains($backendUrl, 'localhost') || str_contains($backendUrl, '127.0.0.1')) && request() && request()->root() && ! str_contains(request()->root(), 'localhost')) {
                $backendUrl = request()->root();
            }
            if (empty($backendUrl) || str_contains($backendUrl, 'localhost') || str_contains($backendUrl, '127.0.0.1')) {
                $backendUrl = 'https://api.estoyok24.com';
            }
            $webhookUrl = rtrim($backendUrl, '/') . '/api/webhooks/evolution/message';
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

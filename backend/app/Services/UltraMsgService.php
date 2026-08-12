<?php

namespace App\Services;

use Exception;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class UltraMsgService implements WhatsAppServiceInterface
{
    protected ?string $instanceId;

    protected ?string $token;

    public function __construct()
    {
        $this->instanceId = config('services.ultramsg.instance_id');
        $this->token = config('services.ultramsg.token');
    }

    public function sendWhatsApp(string $to, string $message, array $parameters = []): bool
    {
        if (! $this->instanceId || ! $this->token) {
            Log::info("[SIMULATED ULTRAMSG WHATSAPP] To: {$to} | Message: {$message}");

            return true;
        }

        try {
            // Strip any 'whatsapp:' prefix or non-numeric characters except leading +
            $cleanTo = preg_replace('/[^0-9]/', '', $to);

            $response = Http::asForm()->post("https://api.ultramsg.com/{$this->instanceId}/messages/chat", [
                'token' => $this->token,
                'to' => $cleanTo,
                'body' => $message,
                'priority' => 10,
            ]);

            if ($response->successful()) {
                return true;
            }

            Log::error('UltraMsg WhatsApp API Error: ' . $response->body());

            return false;
        } catch (Exception $e) {
            Log::error('UltraMsg Exception: ' . $e->getMessage());

            return false;
        }
    }
}

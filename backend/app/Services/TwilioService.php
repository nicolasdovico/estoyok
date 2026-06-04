<?php

namespace App\Services;

use Twilio\Rest\Client;
use Illuminate\Support\Facades\Log;
use Exception;

class TwilioService implements WhatsAppServiceInterface
{
    protected Client $client;
    protected string $fromWhatsApp;
    protected string $fromSMS;

    public function __construct()
    {
        $sid = config('services.twilio.sid');
        $token = config('services.twilio.token');
        $this->fromWhatsApp = config('services.twilio.whatsapp_from');
        $this->fromSMS = config('services.twilio.sms_from');

        if ($sid && $token) {
            $this->client = new Client($sid, $token);
        }
    }

    public function sendWhatsApp(string $to, string $message, array $parameters = []): bool
    {
        if (!isset($this->client)) {
            Log::error("Twilio client not initialized. Check credentials.");
            return false;
        }

        try {
            // In Twilio, WhatsApp numbers must be prefixed with 'whatsapp:'
            $to = str_starts_with($to, 'whatsapp:') ? $to : "whatsapp:$to";
            $from = str_starts_with($this->fromWhatsApp, 'whatsapp:') ? $this->fromWhatsApp : "whatsapp:{$this->fromWhatsApp}";

            $this->client->messages->create($to, [
                'from' => $from,
                'body' => $message
            ]);

            return true;
        } catch (Exception $e) {
            Log::error("Twilio WhatsApp Error: " . $e->getMessage());
            return false;
        }
    }

    public function sendSMS(string $to, string $message): bool
    {
        if (!isset($this->client)) {
            Log::error("Twilio client not initialized. Check credentials.");
            return false;
        }

        try {
            $this->client->messages->create($to, [
                'from' => $this->fromSMS,
                'body' => $message
            ]);

            return true;
        } catch (Exception $e) {
            Log::error("Twilio SMS Error: " . $e->getMessage());
            return false;
        }
    }
}

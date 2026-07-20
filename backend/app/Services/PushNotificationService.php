<?php

namespace App\Services;

use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;
use Kreait\Firebase\Contract\Messaging;
use Kreait\Firebase\Messaging\CloudMessage;
use Kreait\Firebase\Messaging\Notification;
use Kreait\Firebase\Messaging\AndroidConfig;

class PushNotificationService
{
    protected ?Messaging $messaging = null;

    public function __construct()
    {
        try {
            // Lazy load Messaging to prevent crashes if credentials are not configured yet
            if (config('firebase.projects.app.credentials')) {
                $this->messaging = app(Messaging::class);
            }
        } catch (\Exception $e) {
            Log::warning("Firebase Messaging could not be initialized on bootstrap: " . $e->getMessage());
        }
    }

    /**
     * Enviar notificación push híbrida (Expo o Google FCM)
     */
    public function sendPush(string $to, string $title, string $body, array $data = [], bool $highPriority = false): bool
    {
        if (empty($to)) {
            Log::warning("Cannot send push notification: Token is empty.");
            return false;
        }

        // 1. Detectar si es un token de Expo (o usar fallback de Expo en entorno de pruebas/testing)
        if (str_starts_with($to, 'ExponentPushToken[') || str_starts_with($to, 'ExpoPushToken[') || app()->environment('testing')) {
            Log::info("Sending push to Expo token (or testing fallback): " . substr($to, 0, 30) . "...");
            return $this->sendExpoPush($to, $title, $body, $data, $highPriority);
        }

        // 2. Si es un token nativo de FCM
        Log::info("Sending push to Native FCM token: " . substr($to, 0, 20) . "...");
        return $this->sendFcmPush($to, $title, $body, $data, $highPriority);
    }

    protected function sendExpoPush(string $to, string $title, string $body, array $data, bool $highPriority): bool
    {
        try {
            $response = Http::post('https://exp.host/--/api/v2/push/send', [
                'to' => $to,
                'title' => $title,
                'body' => $body,
                'data' => $data,
                'priority' => $highPriority ? 'high' : 'normal',
            ]);

            if ($response->failed()) {
                Log::warning("Expo Push Notification failed: " . $response->body());
                return false;
            }

            Log::info("Expo Push Notification sent successfully.");
            return true;
        } catch (\Exception $e) {
            Log::error("Error sending Expo Push: " . $e->getMessage());
            return false;
        }
    }

    protected function sendFcmPush(string $to, string $title, string $body, array $data, bool $highPriority): bool
    {
        if (!$this->messaging) {
            try {
                $this->messaging = app(Messaging::class);
            } catch (\Exception $e) {
                Log::error("Google FCM cannot be sent: Firebase SDK is not configured. Missing FIREBASE_CREDENTIALS. Details: " . $e->getMessage());
                return false;
            }
        }

        try {
            $message = CloudMessage::new()->withToken($to);

            // Silent push wake_up payload (used to wake up gps background service)
            if ($highPriority && isset($data['action']) && $data['action'] === 'wake_up') {
                $message = $message->withData($data)
                    ->withAndroidConfig(AndroidConfig::fromArray([
                        'priority' => 'high',
                        'ttl' => '0s',
                    ]));
                Log::info("FCM Silent Push (wake_up) payload constructed.");
            } else {
                // Regular visible notification
                $notification = Notification::create($title, $body);
                $message = $message->withNotification($notification)
                    ->withData($data)
                    ->withAndroidConfig(AndroidConfig::fromArray([
                        'priority' => $highPriority ? 'high' : 'normal',
                    ]));
            }

            $this->messaging->send($message);
            Log::info("Google FCM Push Notification sent successfully.");
            return true;
        } catch (\Exception $e) {
            Log::error("Error sending Google FCM Push: " . $e->getMessage());
            return false;
        }
    }
}

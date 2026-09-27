<?php

namespace App\Services;

use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class GoogleAuthService
{
    /**
     * Verify Google ID token and return verified user payload or null if invalid.
     *
     * @param string $idToken
     * @return array|null
     */
    public function verifyIdToken(string $idToken): ?array
    {
        if (empty($idToken)) {
            return null;
        }

        try {
            $response = Http::timeout(10)->get('https://oauth2.googleapis.com/tokeninfo', [
                'id_token' => $idToken,
            ]);

            if (!$response->successful()) {
                Log::warning('Google tokeninfo verification failed: ' . $response->body());
                return null;
            }

            $payload = $response->json();

            // 1. Verify issuer
            $validIssuers = ['accounts.google.com', 'https://accounts.google.com'];
            if (!isset($payload['iss']) || !in_array($payload['iss'], $validIssuers, true)) {
                Log::warning('Google token verification failed: invalid issuer ' . ($payload['iss'] ?? 'null'));
                return null;
            }

            // 2. Verify audience (if client ID is configured and not placeholder)
            $configuredClientId = config('services.google.client_id');
            if (!empty($configuredClientId) && $configuredClientId !== 'your_google_web_client_id_here') {
                $aud = $payload['aud'] ?? '';
                if ($aud !== $configuredClientId) {
                    Log::warning("Google token verification failed: audience mismatch. Expected: {$configuredClientId}, received: {$aud}");
                    return null;
                }
            }

            // 3. Verify expiration
            if (isset($payload['exp']) && (int) $payload['exp'] < time()) {
                Log::warning('Google token verification failed: token expired at ' . $payload['exp']);
                return null;
            }

            // 4. Verify email existence
            if (empty($payload['email']) || empty($payload['sub'])) {
                Log::warning('Google token verification failed: missing email or sub in payload');
                return null;
            }

            // 5. Verify email is verified by Google
            $isEmailVerified = filter_var($payload['email_verified'] ?? false, FILTER_VALIDATE_BOOLEAN);
            if (!$isEmailVerified) {
                Log::warning('Google token verification failed: email is not verified by Google');
                return null;
            }

            return [
                'sub' => (string) $payload['sub'],
                'email' => strtolower(trim($payload['email'])),
                'name' => $payload['name'] ?? explode('@', $payload['email'])[0],
                'picture' => $payload['picture'] ?? null,
            ];
        } catch (\Throwable $e) {
            Log::error('Exception while verifying Google ID token: ' . $e->getMessage());
            return null;
        }
    }
}

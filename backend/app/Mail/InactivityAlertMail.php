<?php

namespace App\Mail;

use App\Models\EmergencyAlert;
use App\Models\User;
use Illuminate\Bus\Queueable;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;

class InactivityAlertMail extends Mailable
{
    use Queueable, SerializesModels;

    public string $emergencyUrl;

    public function __construct(
        public User $user,
        string|EmergencyAlert $emergencyUrl,
        public ?string $relationship = null
    ) {
        if ($emergencyUrl instanceof EmergencyAlert) {
            $baseUrl = rtrim(config('app.frontend_url', env('FRONTEND_URL', 'https://estoyok24.com')), '/');
            if (empty($baseUrl) || str_contains($baseUrl, 'localhost') || str_contains($baseUrl, '127.0.0.1') || str_contains($baseUrl, 'railway.app')) {
                $baseUrl = 'https://estoyok24.com';
            }
            $this->emergencyUrl = "{$baseUrl}/emergencia/{$emergencyUrl->id}";
        } else {
            $url = $emergencyUrl;
            if (empty($url) || str_contains($url, 'localhost') || str_contains($url, '127.0.0.1') || str_contains($url, 'railway.app')) {
                $parsedPath = parse_url($url, PHP_URL_PATH) ?? '';
                $url = 'https://estoyok24.com' . $parsedPath;
            }
            $this->emergencyUrl = $url;
        }
    }

    public function envelope(): Envelope
    {
        return new Envelope(
            subject: "⚠️ ALERTA DE SEGURIDAD: Su contacto {$this->user->name} no se ha reportado",
        );
    }

    public function content(): Content
    {
        return new Content(
            markdown: 'emails.inactivity-alert',
        );
    }
}

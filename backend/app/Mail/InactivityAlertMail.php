<?php

namespace App\Mail;

use App\Models\User;
use Illuminate\Bus\Queueable;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;

class InactivityAlertMail extends Mailable
{
    use Queueable, SerializesModels;

    public function __construct(
        public User $user,
        public string $emergencyUrl,
        public ?string $relationship = null
    ) {}

    public function envelope(): Envelope
    {
        return new Envelope(
            subject: "⚠️ ALERTA DE SEGURIDAD: Su contacto {$this->user->name} no se ha reportado",
        );
    }

    public function content(): Content
    {
        return new Content(
            markdown: "emails.inactivity-alert",
        );
    }
}

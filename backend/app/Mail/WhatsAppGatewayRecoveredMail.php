<?php

namespace App\Mail;

use Illuminate\Bus\Queueable;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;

class WhatsAppGatewayRecoveredMail extends Mailable
{
    use Queueable, SerializesModels;

    public string $restoredAt;
    public ?int $downtimeMinutes;

    public function __construct(string $restoredAt, ?int $downtimeMinutes = null)
    {
        $this->restoredAt = $restoredAt;
        $this->downtimeMinutes = $downtimeMinutes;
    }

    public function envelope(): Envelope
    {
        return new Envelope(
            subject: '✅ [ESTADO NORMAL] WhatsApp Reconectado Exitosamente - Estoy Ok'
        );
    }

    public function content(): Content
    {
        return new Content(
            view: 'emails.whatsapp-gateway-recovered'
        );
    }
}

<?php

namespace App\Mail;

use Illuminate\Bus\Queueable;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;

class WhatsAppGatewayDownMail extends Mailable
{
    use Queueable, SerializesModels;

    public string $status;
    public string $detectedAt;
    public ?string $errorMessage;
    public string $connectQrUrl;

    public function __construct(string $status, string $detectedAt, ?string $errorMessage = null)
    {
        $this->status = $status;
        $this->detectedAt = $detectedAt;
        $this->errorMessage = $errorMessage;
        $this->connectQrUrl = 'https://api.estoyok24.com/api/maintenance/connect-qr';
    }

    public function envelope(): Envelope
    {
        return new Envelope(
            subject: '🚨 [ALERTA ESTOY OK] Servicio de WhatsApp Desconectado (' . strtoupper($this->status) . ')'
        );
    }

    public function content(): Content
    {
        return new Content(
            view: 'emails.whatsapp-gateway-down'
        );
    }
}

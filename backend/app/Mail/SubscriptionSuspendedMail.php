<?php

namespace App\Mail;

use App\Models\User;
use Illuminate\Bus\Queueable;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;

class SubscriptionSuspendedMail extends Mailable
{
    use Queueable, SerializesModels;

    public function __construct(public User $user)
    {
    }

    public function envelope(): Envelope
    {
        return new Envelope(
            subject: '⚠️ Tu suscripción Estoy Ok PRO ha vencido',
        );
    }

    public function content(): Content
    {
        return new Content(
            htmlString: "
                <div style='font-family: sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #EF4444; borderRadius: 12px; background-color: #0F172A; color: #FFFFFF;'>
                    <h2 style='color: #EF4444;'>Hola {$this->user->name} ⚠️</h2>
                    <p style='color: #94A3B8;'>Tu suscripción a <strong>Estoy Ok PRO</strong> y el periodo de gracia han finalizado. Tu cuenta ha retornado al Plan Gratuito.</p>
                    <p style='color: #CBD5E1;'>Tus funciones avanzadas (alertas por WhatsApp/SMS, SOS ambiental y telemetría) han sido pausadas. Puedes reactivar tu protección PRO en cualquier momento ingresando a la app.</p>
                    <p style='font-size: 11px; color: #64748B;'>Estoy Ok • Seguridad Familiar en Piloto Automático</p>
                </div>
            ",
        );
    }
}

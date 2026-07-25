<?php

namespace App\Mail;

use App\Models\User;
use Illuminate\Bus\Queueable;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;

class TrialExpiringSoonMail extends Mailable
{
    use Queueable, SerializesModels;

    public function __construct(public User $user)
    {
    }

    public function envelope(): Envelope
    {
        return new Envelope(
            subject: '👑 Tu prueba gratuita de Estoy Ok PRO finaliza en 2 días',
        );
    }

    public function content(): Content
    {
        return new Content(
            htmlString: "
                <div style='font-family: sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #00E5D9; borderRadius: 12px; background-color: #0F172A; color: #FFFFFF;'>
                    <h2 style='color: #00E5D9;'>¡Hola {$this->user->name}! 👑</h2>
                    <p style='color: #94A3B8;'>Te recordamos que tu prueba gratuita de 7 días de <strong>Estoy Ok PRO</strong> finaliza en 2 días.</p>
                    <p style='color: #CBD5E1;'>Tus beneficios activos incluyen:</p>
                    <ul style='color: #00E5D9;'>
                        <li>WhatsApp y SMS ilimitados para alertas de emergencia.</li>
                        <li>S.O.S. Silencioso con grabación de audio ambiental de 15 segundos.</li>
                        <li>Telemetría vehicular y detección inteligente de impactos.</li>
                        <li>Historial de trayectos por 30 días.</li>
                    </ul>
                    <p style='color: #94A3B8;'>Si deseas continuar disfrutando de la tranquilidad de tu familia, no tienes que hacer nada. El cobro mensual ($4.99/mes) se efectuará automáticamente al finalizar los 7 días. Si no deseas continuar, puedes cancelar en cualquier momento desde los Ajustes de la app sin costo alguno.</p>
                    <p style='font-size: 11px; color: #64748B;'>Estoy Ok • Seguridad Familiar en Piloto Automático</p>
                </div>
            ",
        );
    }
}

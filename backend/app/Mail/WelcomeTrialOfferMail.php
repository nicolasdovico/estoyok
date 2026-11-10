<?php

namespace App\Mail;

use Illuminate\Bus\Queueable;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;

class WelcomeTrialOfferMail extends Mailable
{
    use Queueable, SerializesModels;

    public function __construct(
        public string $name,
        public string $appVersion = '1.1.2'
    ) {}

    public function envelope(): Envelope
    {
        return new Envelope(
            subject: "{$this->name}, activamos tus 7 días de regalo en Estoy OK 🛡️",
        );
    }

    public function content(): Content
    {
        return new Content(
            htmlString: "
            <!DOCTYPE html>
            <html lang='es'>
            <head>
                <meta charset='UTF-8'>
                <meta name='viewport' content='width=device-width, initial-scale=1.0'>
            </head>
            <body style='margin: 0; padding: 20px; background-color: #0B1120; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif; color: #F8FAFC;'>
                <div style='max-width: 580px; margin: 0 auto; background-color: #0F172A; border-radius: 16px; border: 1px solid rgba(16, 185, 129, 0.25); overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.5);'>
                    
                    <!-- Header with Logo -->
                    <div style='background: linear-gradient(135deg, #064E3B 0%, #0F172A 100%); padding: 32px 24px; text-align: center; border-bottom: 1px solid rgba(16, 185, 129, 0.2);'>
                        <img src='https://estoyok24.com/logo-square.png' alt='Estoy OK' width='72' height='72' style='display: block; margin: 0 auto 12px; border-radius: 14px;'>
                        <h1 style='margin: 0; font-size: 24px; font-weight: 700; color: #10B981; letter-spacing: -0.5px;'>Estoy OK</h1>
                        <p style='margin: 6px 0 0; font-size: 14px; color: #94A3B8;'>Tranquilidad y Seguridad Familiar en Piloto Automático</p>
                    </div>

                    <!-- Main Content -->
                    <div style='padding: 32px 24px; font-size: 15px; line-height: 1.6; color: #E2E8F0;'>
                        <p style='font-size: 17px; font-weight: 600; color: #FFFFFF; margin-top: 0;'>
                            ¡Hola {$this->name}! 👋
                        </p>
                        
                        <p style='color: #CBD5E1;'>
                            Quería darte la bienvenida a <strong>Estoy OK</strong>. Noté que creaste tu cuenta hace poco para cuidar la tranquilidad de tu familia.
                        </p>

                        <p style='color: #CBD5E1;'>
                            Para que puedas experimentarlo de verdad y sin compromiso, te habilitamos <strong>7 días de prueba gratis completa</strong> para probar las funciones avanzadas:
                        </p>

                        <!-- Feature 1 -->
                        <div style='background-color: #1E293B; border-left: 4px solid #10B981; border-radius: 0 8px 8px 0; padding: 14px 16px; margin-bottom: 14px;'>
                            <strong style='color: #34D399; font-size: 15px;'>🔹 Alertas automáticas por WhatsApp</strong>
                            <p style='margin: 4px 0 0; color: #94A3B8; font-size: 14px;'>
                                Si no confirmas tu bienestar a tu hora habitual, el sistema le envía un WhatsApp automático con tu última ubicación en el mapa a los familiares, amigos o seres queridos que hayas cargado como tus <em>contactos de emergencia</em>.
                            </p>
                        </div>

                        <!-- Feature 2 -->
                        <div style='background-color: #1E293B; border-left: 4px solid #10B981; border-radius: 0 8px 8px 0; padding: 14px 16px; margin-bottom: 14px;'>
                            <strong style='color: #34D399; font-size: 15px;'>🔹 Zonas Seguras y Avisos de Llegada</strong>
                            <p style='margin: 4px 0 0; color: #94A3B8; font-size: 14px;'>
                                Recibes y envías notificaciones automáticas por WhatsApp cuando tus hijos o familiares entran o salen de la escuela, del trabajo o al llegar a casa.
                            </p>
                        </div>

                        <!-- Feature 3 -->
                        <div style='background-color: #1E293B; border-left: 4px solid #10B981; border-radius: 0 8px 8px 0; padding: 14px 16px; margin-bottom: 24px;'>
                            <strong style='color: #34D399; font-size: 15px;'>🔹 Localización y Telemetría vehicular en tiempo real</strong>
                            <p style='margin: 4px 0 0; color: #94A3B8; font-size: 14px;'>
                                Rastreo de trayectos, velocidad segura y botón SOS con grabación ambiental para emergencias.
                            </p>
                        </div>

                        <!-- Steps Box -->
                        <div style='background-color: #131D31; border: 1px dashed rgba(245, 158, 11, 0.4); border-radius: 12px; padding: 20px; margin-bottom: 28px;'>
                            <h3 style='margin: 0 0 12px; color: #FBBF24; font-size: 16px;'>👉 ¿Cómo aprovechar tus 7 días de regalo?</h3>
                            <ol style='margin: 0; padding-left: 20px; color: #CBD5E1; font-size: 14px; line-height: 1.8;'>
                                <li>Abre la app en tu teléfono (asegúrate de tener la versión {$this->appVersion} en Google Play).</li>
                                <li>Carga a tus <strong>contactos de emergencia</strong> (tus familiares o personas de confianza).</li>
                                <li>Toca el banner que dice <strong>\"Probá 7 días GRATIS\"</strong> en el mapa o en la pantalla principal.</li>
                            </ol>
                        </div>

                        <!-- CTA Button -->
                        <div style='text-align: center; margin: 32px 0;'>
                            <a href='https://estoyok24.com/descargar' style='background: linear-gradient(135deg, #10B981 0%, #059669 100%); color: #FFFFFF; text-decoration: none; padding: 14px 32px; border-radius: 10px; font-weight: 700; font-size: 16px; display: inline-block; box-shadow: 0 4px 14px rgba(16, 185, 129, 0.4);'>
                                Abrir Estoy OK en Google Play 📲
                            </a>
                        </div>

                        <p style='color: #94A3B8; font-size: 14px; margin-top: 28px;'>
                            Cualquier duda que tengas o si necesitas una mano configurando a tus familiares, respondeme directamente a este correo y te ayudo personalmente.
                        </p>

                        <div style='margin-top: 24px; padding-top: 20px; border-top: 1px solid rgba(255,255,255,0.1); color: #CBD5E1;'>
                            <strong>Nicolás</strong><br>
                            <span style='font-size: 13px; color: #64748B;'>Creador de Estoy OK</span>
                        </div>
                    </div>

                    <!-- Footer -->
                    <div style='background-color: #0B1120; padding: 16px 24px; text-align: center; border-top: 1px solid rgba(255,255,255,0.05); font-size: 12px; color: #64748B;'>
                        Estoy OK • Protegiendo a quienes más querés • <a href='https://estoyok24.com' style='color: #10B981; text-decoration: none;'>estoyok24.com</a>
                    </div>
                </div>
            </body>
            </html>
            "
        );
    }
}

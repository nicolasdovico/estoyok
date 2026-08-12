<?php

namespace Tests\Feature;

use App\Mail\CheckInReminderMail;
use App\Mail\InactivityAlertMail;
use App\Mail\OtpVerificationMail;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class MailLogoHeaderTest extends TestCase
{
    use RefreshDatabase;

    public function test_inactivity_alert_mail_renders_app_logo_in_header(): void
    {
        $user = User::factory()->create();
        $mailable = new InactivityAlertMail($user, 'https://estoyok24.com/emergencia/test-uuid', 'Padre');

        $html = $mailable->render();

        $this->assertTrue(str_contains($html, 'data:image/png;base64,') || str_contains($html, 'images/logo.png'));
        $this->assertStringContainsString('alt="Estoy Ok"', $html);
        $this->assertStringNotContainsString('laravel.com', $html);
        $this->assertStringNotContainsString('notification-logo', $html);
        $this->assertStringContainsString('Padre', $html);
        $this->assertStringContainsString('https://estoyok24.com/emergencia/test-uuid', $html);
    }

    public function test_checkin_reminder_mail_renders_app_logo_in_header(): void
    {
        $user = User::factory()->create();
        $mailable = new CheckInReminderMail($user, 'https://estoyok24.com');

        $html = $mailable->render();

        $this->assertTrue(str_contains($html, 'data:image/png;base64,') || str_contains($html, 'images/logo.png'));
        $this->assertStringContainsString('alt="Estoy Ok"', $html);
        $this->assertStringNotContainsString('laravel.com', $html);
        $this->assertStringContainsString('aplicación móvil', $html);
        $this->assertStringNotContainsString('Confirmar Bienestar (Estoy OK)', $html);
    }

    public function test_otp_verification_mail_renders_app_logo_in_header(): void
    {
        $mailable = new OtpVerificationMail('123456');

        $html = $mailable->render();

        $this->assertTrue(str_contains($html, 'data:image/png;base64,') || str_contains($html, 'images/logo.png'));
        $this->assertStringContainsString('alt="Estoy Ok"', $html);
        $this->assertStringNotContainsString('laravel.com', $html);
    }
}

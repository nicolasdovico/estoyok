<?php

namespace Tests\Feature;

use App\Mail\NewUserRegisteredMail;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Config;
use Illuminate\Support\Facades\Mail;
use Tests\TestCase;

class NewUserAdminNotificationTest extends TestCase
{
    use RefreshDatabase;

    public function test_admin_receives_email_when_new_user_registers(): void
    {
        Mail::fake();
        Config::set('mail.admin_notification_email', 'admin@estoyok24.com');

        $response = $this->postJson('/api/register', [
            'name' => 'Carlos Gardel',
            'email' => 'carlos@estoyok24.com',
            'password' => 'password123',
            'password_confirmation' => 'password123',
            'phone' => '+5491199887766',
        ]);

        $response->assertStatus(201);

        Mail::assertSent(NewUserRegisteredMail::class, function ($mail) {
            return $mail->hasTo('admin@estoyok24.com') &&
                   $mail->user->email === 'carlos@estoyok24.com' &&
                   $mail->user->name === 'Carlos Gardel' &&
                   $mail->totalUsers >= 1;
        });
    }

    public function test_admin_email_not_sent_when_admin_email_is_empty(): void
    {
        Mail::fake();
        Config::set('mail.admin_notification_email', null);

        $response = $this->postJson('/api/register', [
            'name' => 'Carlos Gardel',
            'email' => 'carlos@estoyok24.com',
            'password' => 'password123',
            'password_confirmation' => 'password123',
        ]);

        $response->assertStatus(201);

        Mail::assertNotSent(NewUserRegisteredMail::class);
    }

    public function test_new_user_registered_mail_renders_correctly(): void
    {
        $user = User::factory()->create([
            'name' => 'Ana Maria',
            'email' => 'ana@example.com',
            'phone' => '+5411223344',
        ]);

        $mailable = new NewUserRegisteredMail($user, 42);
        $rendered = $mailable->render();

        $this->assertStringContainsString('Ana Maria', $rendered);
        $this->assertStringContainsString('ana@example.com', $rendered);
        $this->assertStringContainsString('+5411223344', $rendered);
        $this->assertStringContainsString('42', $rendered);
    }
}

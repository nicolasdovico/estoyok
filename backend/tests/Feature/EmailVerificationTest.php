<?php

namespace Tests\Feature;

use App\Models\EmailVerification;
use App\Models\User;
use App\Mail\OtpVerificationMail;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Mail;
use Tests\TestCase;

class EmailVerificationTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_register_and_receives_otp()
    {
        Mail::fake();

        $response = $this->postJson('/api/register', [
            'name' => 'Juan Pérez',
            'email' => 'juan@example.com',
            'password' => 'secret123',
            'password_confirmation' => 'secret123',
        ]);

        $response->assertStatus(201)
            ->assertJsonStructure(['message', 'email']);

        $this->assertDatabaseHas('users', ['email' => 'juan@example.com']);
        $this->assertDatabaseHas('email_verifications', ['email' => 'juan@example.com']);

        Mail::assertQueued(OtpVerificationMail::class, function ($mail) {
            return $mail->hasTo('juan@example.com') && strlen($mail->code) === 6;
        });
    }

    public function test_user_can_verify_email_with_correct_otp()
    {
        $user = User::factory()->create([
            'email_verified_at' => null,
        ]);

        $code = '123456';
        EmailVerification::create([
            'email' => $user->email,
            'code' => $code,
            'expires_at' => now()->addMinutes(15),
        ]);

        $response = $this->postJson('/api/verify-email', [
            'email' => $user->email,
            'code' => $code,
        ]);

        $response->assertStatus(200)
            ->assertJsonStructure(['token', 'user']);

        $this->assertNotNull($user->fresh()->email_verified_at);
        $this->assertDatabaseMissing('email_verifications', ['email' => $user->email]);
    }

    public function test_user_cannot_verify_email_with_incorrect_otp()
    {
        $user = User::factory()->create([
            'email_verified_at' => null,
        ]);

        EmailVerification::create([
            'email' => $user->email,
            'code' => '111111',
            'expires_at' => now()->addMinutes(15),
        ]);

        $response = $this->postJson('/api/verify-email', [
            'email' => $user->email,
            'code' => '222222',
        ]);

        $response->assertStatus(422);
        $this->assertNull($user->fresh()->email_verified_at);
    }

    public function test_user_cannot_verify_email_with_expired_otp()
    {
        $user = User::factory()->create([
            'email_verified_at' => null,
        ]);

        $code = '123456';
        EmailVerification::create([
            'email' => $user->email,
            'code' => $code,
            'expires_at' => now()->subMinute(),
        ]);

        $response = $this->postJson('/api/verify-email', [
            'email' => $user->email,
            'code' => $code,
        ]);

        $response->assertStatus(422);
        $this->assertNull($user->fresh()->email_verified_at);
    }

    public function test_user_can_resend_otp()
    {
        Mail::fake();

        $user = User::factory()->create([
            'email_verified_at' => null,
        ]);

        $response = $this->postJson('/api/resend-otp', [
            'email' => $user->email,
        ]);

        $response->assertStatus(200);

        Mail::assertQueued(OtpVerificationMail::class, function ($mail) use ($user) {
            return $mail->hasTo($user->email);
        });
    }
}

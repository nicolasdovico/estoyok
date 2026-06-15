<?php

namespace Tests\Feature;

use App\Mail\CheckInReminderMail;
use App\Models\User;
use Carbon\Carbon;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Mail;
use Tests\TestCase;

class SendCheckInRemindersTest extends TestCase
{
    use RefreshDatabase;

    public function test_it_sends_reminders_to_users_approaching_inactivity_threshold()
    {
        Mail::fake();
        Http::fake([
            'https://exp.host/--/api/v2/push/send' => Http::response(['data' => ['status' => 'ok']], 200),
        ]);

        // 1. User within the 1-hour window (interval = 24h, last check-in 23.5h ago) -> SHOULD REMIND
        $user1 = User::factory()->create([
            'email' => 'user1@example.com',
            'checkin_interval_hours' => 24,
            'last_check_in_at' => Carbon::now()->subHours(23)->subMinutes(30),
            'expo_push_token' => 'ExponentPushToken[user1_token]',
            'last_reminder_sent_at' => null,
        ]);

        // 2. User outside the window (interval = 24h, last check-in 22h ago) -> SHOULD NOT REMIND
        $user2 = User::factory()->create([
            'email' => 'user2@example.com',
            'checkin_interval_hours' => 24,
            'last_check_in_at' => Carbon::now()->subHours(22),
            'expo_push_token' => 'ExponentPushToken[user2_token]',
            'last_reminder_sent_at' => null,
        ]);

        // 3. User already inactive (interval = 24h, last check-in 25h ago) -> SHOULD NOT REMIND
        $user3 = User::factory()->create([
            'email' => 'user3@example.com',
            'checkin_interval_hours' => 24,
            'last_check_in_at' => Carbon::now()->subHours(25),
            'expo_push_token' => 'ExponentPushToken[user3_token]',
            'last_reminder_sent_at' => null,
        ]);

        // 4. User inside window but ALREADY REMINDED in this cycle -> SHOULD NOT REMIND
        $user4 = User::factory()->create([
            'email' => 'user4@example.com',
            'checkin_interval_hours' => 24,
            'last_check_in_at' => Carbon::now()->subHours(23)->subMinutes(30),
            'expo_push_token' => 'ExponentPushToken[user4_token]',
            'last_reminder_sent_at' => Carbon::now()->subMinutes(15),
        ]);

        // 5. User inside window, was reminded in PREVIOUS cycle but has checked in since -> SHOULD REMIND
        $user5 = User::factory()->create([
            'email' => 'user5@example.com',
            'checkin_interval_hours' => 24,
            'last_check_in_at' => Carbon::now()->subHours(23)->subMinutes(30), // checked in 23.5h ago
            'expo_push_token' => 'ExponentPushToken[user5_token]',
            'last_reminder_sent_at' => Carbon::now()->subHours(30), // reminded 30h ago (before the 23.5h check-in)
        ]);

        // Run the Artisan command
        $this->artisan('checkins:send-reminders')
            ->expectsOutput('Iniciando envío de recordatorios de check-in...')
            ->expectsOutput('Recordatorio enviado a: user1@example.com')
            ->expectsOutput('Recordatorio enviado a: user5@example.com')
            ->assertExitCode(0);

        // Assert emails were sent only to user1 and user5
        Mail::assertSent(CheckInReminderMail::class, function ($mail) use ($user1) {
            return $mail->hasTo($user1->email);
        });

        Mail::assertSent(CheckInReminderMail::class, function ($mail) use ($user5) {
            return $mail->hasTo($user5->email);
        });

        Mail::assertNotSent(CheckInReminderMail::class, function ($mail) use ($user2) {
            return $mail->hasTo($user2->email);
        });

        Mail::assertNotSent(CheckInReminderMail::class, function ($mail) use ($user3) {
            return $mail->hasTo($user3->email);
        });

        Mail::assertNotSent(CheckInReminderMail::class, function ($mail) use ($user4) {
            return $mail->hasTo($user4->email);
        });

        // Assert HTTP requests (push notifications) were sent
        Http::assertSent(function ($request) use ($user1) {
            return $request->url() === 'https://exp.host/--/api/v2/push/send' &&
                   $request['to'] === $user1->expo_push_token &&
                   $request['title'] === '¿Estás por ahí?' &&
                   $request['body'] === 'Recuerda confirmar tu bienestar para evitar avisar a tus contactos.';
        });

        Http::assertSent(function ($request) use ($user5) {
            return $request->url() === 'https://exp.host/--/api/v2/push/send' &&
                   $request['to'] === $user5->expo_push_token;
        });

        Http::assertNotSent(function ($request) use ($user2) {
            return $request['to'] === $user2->expo_push_token;
        });

        Http::assertNotSent(function ($request) use ($user3) {
            return $request['to'] === $user3->expo_push_token;
        });

        Http::assertNotSent(function ($request) use ($user4) {
            return $request['to'] === $user4->expo_push_token;
        });

        // Assert users' last_reminder_sent_at timestamps were updated
        $this->assertNotNull($user1->fresh()->last_reminder_sent_at);
        $this->assertNotNull($user5->fresh()->last_reminder_sent_at);

        // Assert updated timestamps are roughly equal to now
        $this->assertTrue($user1->fresh()->last_reminder_sent_at->isToday());
    }
}

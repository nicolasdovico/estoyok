<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Mail;
use App\Mail\TrialExpiringSoonMail;
use Tests\TestCase;

class SendTrialRemindersTest extends TestCase
{
    use RefreshDatabase;

    public function test_it_sends_reminder_to_user_whose_trial_expires_in_two_days()
    {
        Mail::fake();
        Http::fake();

        // User expiring in 1.5 days (within 2 days)
        $user = User::factory()->create([
            'trial_ends_at' => now()->addHours(36),
            'subscription_status' => 'trialing',
            'trial_reminder_sent_at' => null,
            'expo_push_token' => 'ExponentPushToken[test-token]',
        ]);

        $this->artisan('subscriptions:send-trial-reminders')
            ->assertExitCode(0);

        $user->refresh();
        $this->assertNotNull($user->trial_reminder_sent_at);

        Mail::assertSent(TrialExpiringSoonMail::class, function ($mail) use ($user) {
            return $mail->hasTo($user->email);
        });

        Http::assertSent(function ($request) {
            return $request->url() === 'https://exp.host/--/api/v2/push/send' &&
                   $request['to'] === 'ExponentPushToken[test-token]' &&
                   $request['data']['type'] === 'trial_expiring_soon';
        });
    }

    public function test_it_skips_users_with_already_sent_reminders_or_far_expiration()
    {
        Mail::fake();

        // User expiring in 5 days (too far)
        $userFar = User::factory()->create([
            'trial_ends_at' => now()->addDays(5),
            'trial_reminder_sent_at' => null,
        ]);

        // User expiring in 1 day but already reminded
        $userReminded = User::factory()->create([
            'trial_ends_at' => now()->addHours(24),
            'trial_reminder_sent_at' => now()->subHours(10),
        ]);

        $this->artisan('subscriptions:send-trial-reminders')
            ->assertExitCode(0);

        Mail::assertNothingSent();
    }
}

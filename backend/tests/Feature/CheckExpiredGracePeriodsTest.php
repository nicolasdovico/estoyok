<?php

namespace Tests\Feature;

use App\Mail\SubscriptionSuspendedMail;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Mail;
use Tests\TestCase;

class CheckExpiredGracePeriodsTest extends TestCase
{
    use RefreshDatabase;

    public function test_it_suspends_users_with_expired_grace_period()
    {
        Mail::fake();
        Http::fake();

        $user = User::factory()->create([
            'is_premium' => true,
            'subscription_status' => 'grace_period',
            'grace_period_ends_at' => now()->subHours(2),
            'expo_push_token' => 'ExponentPushToken[test-token]',
        ]);

        $this->artisan('subscriptions:check-expired-grace-periods')
            ->assertExitCode(0);

        $user->refresh();
        $this->assertFalse($user->is_premium);
        $this->assertEquals('canceled', $user->subscription_status);

        Mail::assertSent(SubscriptionSuspendedMail::class, function ($mail) use ($user) {
            return $mail->hasTo($user->email);
        });

        Http::assertSent(function ($request) {
            return $request->url() === 'https://exp.host/--/api/v2/push/send' &&
                   $request['data']['type'] === 'subscription_suspended';
        });
    }

    public function test_it_does_not_touch_active_grace_period_users()
    {
        Mail::fake();

        $user = User::factory()->create([
            'is_premium' => true,
            'subscription_status' => 'grace_period',
            'grace_period_ends_at' => now()->addDays(3),
        ]);

        $this->artisan('subscriptions:check-expired-grace-periods')
            ->assertExitCode(0);

        $user->refresh();
        $this->assertTrue($user->hasPremiumAccess());
        $this->assertEquals('grace_period', $user->subscription_status);
        Mail::assertNothingSent();
    }
}

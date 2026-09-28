<?php

namespace Tests\Feature;

use App\Mail\SubscriptionCanceledMail;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Config;
use Illuminate\Support\Facades\Mail;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class SubscriptionSyncAndCancelTest extends TestCase
{
    use RefreshDatabase;

    protected function setUp(): void
    {
        parent::setUp();
        Config::set('mail.admin_notification_email', 'admin@estoyok24.com');
        Mail::fake();
    }

    public function test_sync_google_play_sets_active_when_trial_expired(): void
    {
        $user = User::factory()->create([
            'subscription_status' => 'trialing',
            'trial_ends_at' => now()->subDay(),
            'is_premium' => true,
        ]);

        Sanctum::actingAs($user);

        $response = $this->postJson('/api/subscriptions/sync-google-play', [
            'has_active_subscription' => true,
            'purchase_token' => 'GPA.3309-TEST-TOKEN',
            'product_id' => 'estoyok_premium',
            'base_plan_id' => 'monthly-plan',
        ]);

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'active',
                'is_premium' => true,
            ]);

        $user->refresh();
        $this->assertEquals('active', $user->subscription_status);
        $this->assertTrue($user->is_premium);
        $this->assertEquals('google_play', $user->subscription_provider);
        $this->assertEquals('GPA.3309-TEST-TOKEN', $user->subscription_id);
    }

    public function test_sync_google_play_maintains_trialing_when_trial_not_expired(): void
    {
        $user = User::factory()->create([
            'subscription_status' => 'trialing',
            'trial_ends_at' => now()->addDays(5),
            'is_premium' => true,
        ]);

        Sanctum::actingAs($user);

        $response = $this->postJson('/api/subscriptions/sync-google-play', [
            'has_active_subscription' => true,
            'purchase_token' => 'GPA.3309-TEST-TOKEN',
            'product_id' => 'estoyok_premium',
            'base_plan_id' => 'monthly-plan',
        ]);

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'trialing',
                'is_premium' => true,
            ]);

        $user->refresh();
        $this->assertEquals('trialing', $user->subscription_status);
        $this->assertTrue($user->is_premium);
    }

    public function test_sync_google_play_cancels_and_notifies_admin_when_no_active_purchase(): void
    {
        $user = User::factory()->create([
            'name' => 'Nicolás Dovico',
            'email' => 'nicolas@test.com',
            'subscription_status' => 'trialing',
            'subscription_provider' => 'google_play',
            'is_premium' => true,
        ]);

        Sanctum::actingAs($user);

        $response = $this->postJson('/api/subscriptions/sync-google-play', [
            'has_active_subscription' => false,
        ]);

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'canceled',
                'is_premium' => false,
            ]);

        $user->refresh();
        $this->assertEquals('canceled', $user->subscription_status);
        $this->assertFalse($user->is_premium);

        Mail::assertSent(SubscriptionCanceledMail::class, function ($mail) use ($user) {
            return $mail->hasTo('admin@estoyok24.com') &&
                   $mail->user->id === $user->id &&
                   str_contains($mail->reason, 'Google Play');
        });
    }

    public function test_cancel_subscription_notifies_admin(): void
    {
        $user = User::factory()->create([
            'name' => 'Nicolás Dovico',
            'email' => 'nicolas@test.com',
            'subscription_status' => 'active',
            'is_premium' => true,
        ]);

        Sanctum::actingAs($user);

        $response = $this->postJson('/api/subscriptions/cancel');

        $response->assertStatus(200);

        $user->refresh();
        $this->assertEquals('canceled', $user->subscription_status);
        $this->assertFalse($user->is_premium);

        Mail::assertSent(SubscriptionCanceledMail::class, function ($mail) use ($user) {
            return $mail->hasTo('admin@estoyok24.com') &&
                   $mail->user->id === $user->id;
        });
    }

    public function test_expire_trials_command_cancels_and_notifies_admin(): void
    {
        // Expired direct trial
        $user1 = User::factory()->create([
            'name' => 'Usuario Directo',
            'email' => 'directo@test.com',
            'subscription_status' => 'trialing',
            'subscription_provider' => 'trial',
            'trial_ends_at' => now()->subHour(),
            'is_premium' => true,
        ]);

        // Expired Google Play trial with >24h buffer
        $user2 = User::factory()->create([
            'name' => 'Usuario Google Play',
            'email' => 'google@test.com',
            'subscription_status' => 'trialing',
            'subscription_provider' => 'google_play',
            'trial_ends_at' => now()->subHours(25),
            'is_premium' => true,
        ]);

        // Active trial (should not expire)
        $user3 = User::factory()->create([
            'name' => 'Usuario Activo',
            'email' => 'activo@test.com',
            'subscription_status' => 'trialing',
            'subscription_provider' => 'google_play',
            'trial_ends_at' => now()->addDays(3),
            'is_premium' => true,
        ]);

        $this->artisan('subscriptions:expire-trials')->assertExitCode(0);

        $this->assertEquals('canceled', $user1->fresh()->subscription_status);
        $this->assertFalse($user1->fresh()->is_premium);

        $this->assertEquals('canceled', $user2->fresh()->subscription_status);
        $this->assertFalse($user2->fresh()->is_premium);

        $this->assertEquals('trialing', $user3->fresh()->subscription_status);
        $this->assertTrue($user3->fresh()->is_premium);

        Mail::assertSent(SubscriptionCanceledMail::class, 2);
    }
}

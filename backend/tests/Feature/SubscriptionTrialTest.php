<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class SubscriptionTrialTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_start_7_day_free_trial()
    {
        $user = User::factory()->create([
            'is_premium' => false,
            'trial_ends_at' => null,
            'subscription_status' => 'inactive',
        ]);

        $this->actingAs($user);

        $response = $this->postJson('/api/subscriptions/start-trial', [
            'provider' => 'stripe'
        ]);

        $response->assertStatus(200);
        $response->assertJsonStructure(['checkout_url', 'message', 'user']);
        $user->refresh();
        $this->assertTrue($user->is_premium);
        $this->assertEquals('trialing', $user->subscription_status);
    }

    public function test_user_cannot_start_trial_twice()
    {
        $user = User::factory()->create([
            'is_premium' => true,
            'subscription_status' => 'active',
        ]);

        $this->actingAs($user);

        $response = $this->postJson('/api/subscriptions/start-trial', [
            'provider' => 'stripe'
        ]);

        $response->assertStatus(422)
            ->assertJson([
                'message' => 'Ya cuentas con una suscripción Premium activa.'
            ]);
    }

    public function test_user_can_cancel_free_trial_without_charge()
    {
        $user = User::factory()->create([
            'is_premium' => true,
            'trial_ends_at' => now()->addDays(4),
            'subscription_status' => 'trialing',
        ]);

        $this->actingAs($user);

        $response = $this->postJson('/api/subscriptions/cancel');

        $response->assertStatus(200);

        $user->refresh();
        $this->assertEquals('canceled', $user->subscription_status);
        $this->assertFalse($user->is_premium);
    }

    public function test_user_can_verify_google_play_subscription_monthly()
    {
        $user = User::factory()->create([
            'is_premium' => false,
            'subscription_status' => 'inactive',
        ]);

        $this->actingAs($user);

        $response = $this->postJson('/api/subscriptions/verify-google-play', [
            'purchase_token' => 'tok_monthly_test_12345',
            'product_id' => 'estoyok_premium',
            'base_plan_id' => 'monthly-plan',
        ]);

        $response->assertStatus(200)
            ->assertJson([
                'message' => '¡Suscripción de Google Play verificada y activada con éxito!',
            ]);

        $user->refresh();
        $this->assertTrue($user->is_premium);
        $this->assertEquals('trialing', $user->subscription_status);
        $this->assertEquals('google_play', $user->subscription_provider);
        $this->assertEquals('tok_monthly_test_12345', $user->subscription_id);
        $this->assertNotNull($user->trial_ends_at);
        $this->assertNotNull($user->billing_cycle_ends_at);
    }

    public function test_user_can_verify_google_play_subscription_annual()
    {
        $user = User::factory()->create([
            'is_premium' => false,
            'subscription_status' => 'inactive',
        ]);

        $this->actingAs($user);

        $response = $this->postJson('/api/subscriptions/verify-google-play', [
            'purchase_token' => 'tok_annual_test_67890',
            'product_id' => 'estoyok_premium',
            'base_plan_id' => 'annual-plan',
        ]);

        $response->assertStatus(200);

        $user->refresh();
        $this->assertTrue($user->is_premium);
        $this->assertEquals('google_play', $user->subscription_provider);
        $this->assertEquals('tok_annual_test_67890', $user->subscription_id);
        // Annual should be ~1 year in the future
        $this->assertTrue(now()->diffInDays($user->billing_cycle_ends_at) > 300);
    }

    public function test_verify_google_play_requires_parameters()
    {
        $user = User::factory()->create();
        $this->actingAs($user);

        $response = $this->postJson('/api/subscriptions/verify-google-play', []);

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['purchase_token', 'product_id']);
    }
}

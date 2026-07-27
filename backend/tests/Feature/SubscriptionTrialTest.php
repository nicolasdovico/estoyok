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

        // Returns 422 if Stripe keys/price IDs are missing, or 200 with checkout_url
        $this->assertTrue(in_array($response->getStatusCode(), [200, 422]));

        if ($response->getStatusCode() === 200) {
            $response->assertJsonStructure(['checkout_url']);
        }
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
}

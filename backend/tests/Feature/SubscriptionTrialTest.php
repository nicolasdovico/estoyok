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

        $response->assertStatus(200)
            ->assertJsonStructure([
                'message',
                'user' => [
                    'id',
                    'is_on_trial',
                    'trial_days_left',
                    'subscription_status',
                ]
            ]);

        $user->refresh();
        $this->assertTrue($user->is_on_trial);
        $this->assertEquals(7, $user->trial_days_left);
        $this->assertTrue($user->hasPremiumAccess());
        $this->assertEquals('trialing', $user->subscription_status);
    }

    public function test_user_cannot_start_trial_twice()
    {
        $user = User::factory()->create([
            'trial_ends_at' => now()->addDays(5),
            'subscription_status' => 'trialing',
        ]);

        $this->actingAs($user);

        $response = $this->postJson('/api/subscriptions/start-trial');

        $response->assertStatus(422)
            ->assertJson([
                'message' => 'Ya has utilizado o tienes activa la prueba gratuita de 7 días.'
            ]);
    }
}

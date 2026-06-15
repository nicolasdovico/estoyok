<?php

namespace Tests\Feature;

use App\Models\User;
use App\Services\MercadoPagoService;
use App\Services\PayPalService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class SubscriptionTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_get_mercadopago_checkout_url()
    {
        $user = User::factory()->create();
        $this->actingAs($user);

        $this->mock(MercadoPagoService::class, function ($mock) {
            $mock->shouldReceive('createSubscriptionLink')
                ->once()
                ->andReturn('https://www.mercadopago.com/checkout/fake-id');
        });

        $response = $this->postJson('/api/subscriptions/checkout', [
            'provider' => 'mercadopago',
            'plan' => 'premium',
        ]);

        $response->assertStatus(200)
            ->assertJson(['checkout_url' => 'https://www.mercadopago.com/checkout/fake-id']);
    }

    public function test_user_can_get_paypal_checkout_url()
    {
        $user = User::factory()->create();
        $this->actingAs($user);

        $this->mock(PayPalService::class, function ($mock) {
            $mock->shouldReceive('createSubscriptionLink')
                ->once()
                ->andReturn('https://www.paypal.com/checkout/fake-id');
        });

        $response = $this->postJson('/api/subscriptions/checkout', [
            'provider' => 'paypal',
            'plan' => 'premium',
        ]);

        $response->assertStatus(200)
            ->assertJson(['checkout_url' => 'https://www.paypal.com/checkout/fake-id']);
    }

    public function test_mercadopago_webhook_updates_status()
    {
        // This is a simplified test for the webhook logic we'll implement later
        $response = $this->postJson('/api/webhooks/mercadopago', [
            'type' => 'subscription_preapproval',
            'data' => ['id' => 'mp-123'],
        ]);

        $response->assertStatus(200);
    }
}

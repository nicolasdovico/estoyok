<?php

namespace Tests\Feature;

use App\Models\EmergencyContact;
use App\Models\User;
use App\Services\PushNotificationService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Cache;
use Mockery;
use Tests\TestCase;

class SendActivationNudgesTest extends TestCase
{
    use RefreshDatabase;

    protected function tearDown(): void
    {
        Mockery::close();
        parent::tearDown();
    }

    public function test_it_sends_6h_nudge_to_user_who_never_checked_in()
    {
        $user = User::factory()->create([
            'name' => 'Ernesto Perez',
            'created_at' => now()->subHours(8),
            'last_check_in_at' => null,
            'expo_push_token' => 'ExponentPushToken[mock_token_ernesto]',
            'quiet_hours_enabled' => false,
        ]);

        $pushServiceMock = Mockery::mock(PushNotificationService::class);
        $pushServiceMock->shouldReceive('sendPush')
            ->once()
            ->with(
                'ExponentPushToken[mock_token_ernesto]',
                '👋 ¡Hola Ernesto!',
                Mockery::pattern('/Tu botón Estoy OK está listo/'),
                Mockery::subset(['type' => 'activation_checkin'])
            )
            ->andReturn(true);

        $this->app->instance(PushNotificationService::class, $pushServiceMock);

        $this->artisan('users:send-activation-nudges')
            ->expectsOutputToContain('Nudge 6h enviado a: ' . $user->email)
            ->assertExitCode(0);

        $this->assertTrue(Cache::has("activation_nudge_6h_user_{$user->id}"));

        // Correr de nuevo no debería duplicar el envío
        $this->artisan('users:send-activation-nudges')
            ->expectsOutputToContain('Se enviaron 0 nudges de activación.')
            ->assertExitCode(0);
    }

    public function test_it_sends_24h_nudge_to_user_without_emergency_contacts()
    {
        $user = User::factory()->create([
            'name' => 'Alfredo Ramos',
            'created_at' => now()->subHours(30),
            'last_check_in_at' => now()->subHours(10), // ya hizo check-in
            'expo_push_token' => 'ExponentPushToken[mock_token_alfredo]',
            'quiet_hours_enabled' => false,
        ]);

        $pushServiceMock = Mockery::mock(PushNotificationService::class);
        $pushServiceMock->shouldReceive('sendPush')
            ->once()
            ->with(
                'ExponentPushToken[mock_token_alfredo]',
                '🛡️ Alfredo, ¿quién te cuidará?',
                Mockery::pattern('/Agregá a tu contacto de confianza/'),
                Mockery::subset(['type' => 'activation_contact'])
            )
            ->andReturn(true);

        $this->app->instance(PushNotificationService::class, $pushServiceMock);

        $this->artisan('users:send-activation-nudges')
            ->expectsOutputToContain('Nudge 24h enviado a: ' . $user->email)
            ->assertExitCode(0);

        $this->assertTrue(Cache::has("activation_nudge_24h_user_{$user->id}"));
    }

    public function test_it_does_not_send_24h_nudge_if_user_already_has_contacts()
    {
        $user = User::factory()->create([
            'created_at' => now()->subHours(30),
            'expo_push_token' => 'ExponentPushToken[mock_token_test]',
        ]);

        EmergencyContact::factory()->create([
            'user_id' => $user->id,
        ]);

        $pushServiceMock = Mockery::mock(PushNotificationService::class);
        $pushServiceMock->shouldNotReceive('sendPush');

        $this->app->instance(PushNotificationService::class, $pushServiceMock);

        $this->artisan('users:send-activation-nudges')
            ->expectsOutputToContain('Se enviaron 0 nudges de activación.')
            ->assertExitCode(0);
    }

    public function test_it_sends_48h_trial_offer_nudge_to_free_user()
    {
        $user = User::factory()->create([
            'name' => 'Adriana Almada',
            'created_at' => now()->subHours(50),
            'is_premium' => false,
            'has_used_trial' => false,
            'expo_push_token' => 'ExponentPushToken[mock_token_adriana]',
            'quiet_hours_enabled' => false,
        ]);

        EmergencyContact::factory()->create([
            'user_id' => $user->id,
        ]);

        $pushServiceMock = Mockery::mock(PushNotificationService::class);
        $pushServiceMock->shouldReceive('sendPush')
            ->once()
            ->with(
                'ExponentPushToken[mock_token_adriana]',
                '⚡ Probá Estoy Ok Premium Gratis',
                Mockery::pattern('/Tenés 7 días sin cargo/'),
                Mockery::subset(['type' => 'activation_trial'])
            )
            ->andReturn(true);

        $this->app->instance(PushNotificationService::class, $pushServiceMock);

        $this->artisan('users:send-activation-nudges')
            ->expectsOutputToContain('Nudge 48h (Trial) enviado a: ' . $user->email)
            ->assertExitCode(0);

        $this->assertTrue(Cache::has("activation_nudge_48h_user_{$user->id}"));
    }
}

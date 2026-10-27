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

    public function test_it_sends_first_checkin_nudge_to_user_who_never_checked_in()
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
            ->expectsOutputToContain('Nudge de primer check-in enviado a: ' . $user->email)
            ->assertExitCode(0);

        $this->assertTrue(Cache::has("activation_nudge_6h_user_{$user->id}"));

        // Correr de nuevo no debería duplicar el envío
        $this->artisan('users:send-activation-nudges')
            ->expectsOutputToContain('Se enviaron 0 nudges de activación.')
            ->assertExitCode(0);
    }

    public function test_it_rescues_user_registered_days_ago_who_never_checked_in()
    {
        // Caso de Ernesto (#42) o Alfredo (#43) registrados hace 2 días
        $user = User::factory()->create([
            'name' => 'Alfredo Ramos',
            'created_at' => now()->subDays(2),
            'last_check_in_at' => null,
            'expo_push_token' => 'ExponentPushToken[mock_token_alfredo]',
            'quiet_hours_enabled' => false,
        ]);

        $pushServiceMock = Mockery::mock(PushNotificationService::class);
        $pushServiceMock->shouldReceive('sendPush')
            ->once()
            ->with(
                'ExponentPushToken[mock_token_alfredo]',
                '👋 ¡Hola Alfredo!',
                Mockery::pattern('/Tu botón Estoy OK está listo/'),
                Mockery::subset(['type' => 'activation_checkin'])
            )
            ->andReturn(true);

        $this->app->instance(PushNotificationService::class, $pushServiceMock);

        $this->artisan('users:send-activation-nudges')
            ->expectsOutputToContain('Nudge de primer check-in enviado a: ' . $user->email)
            ->assertExitCode(0);

        $this->assertTrue(Cache::has("activation_nudge_6h_user_{$user->id}"));
    }

    public function test_it_sends_contact_nudge_to_user_who_did_checkin_but_has_no_contacts()
    {
        $user = User::factory()->create([
            'name' => 'Adriana Almada',
            'created_at' => now()->subHours(30),
            'last_check_in_at' => now()->subHours(10), // ya hizo check-in
            'expo_push_token' => 'ExponentPushToken[mock_token_adriana]',
            'quiet_hours_enabled' => false,
        ]);

        $pushServiceMock = Mockery::mock(PushNotificationService::class);
        $pushServiceMock->shouldReceive('sendPush')
            ->once()
            ->with(
                'ExponentPushToken[mock_token_adriana]',
                '🛡️ Adriana, ¿quién te cuidará?',
                Mockery::pattern('/Agregá a tu contacto de confianza/'),
                Mockery::subset(['type' => 'activation_contact'])
            )
            ->andReturn(true);

        $this->app->instance(PushNotificationService::class, $pushServiceMock);

        $this->artisan('users:send-activation-nudges')
            ->expectsOutputToContain('Nudge de contactos de emergencia enviado a: ' . $user->email)
            ->assertExitCode(0);

        $this->assertTrue(Cache::has("activation_nudge_24h_user_{$user->id}"));
    }

    public function test_it_does_not_send_contact_nudge_if_user_already_has_contacts()
    {
        $user = User::factory()->create([
            'created_at' => now()->subHours(30),
            'last_check_in_at' => now()->subHours(10),
            'is_premium' => true,
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

    public function test_it_sends_trial_offer_nudge_to_free_user_with_contacts_and_checkin()
    {
        $user = User::factory()->create([
            'name' => 'Carlos Gomez',
            'created_at' => now()->subHours(50),
            'last_check_in_at' => now()->subHours(5), // activo
            'is_premium' => false,
            'has_used_trial' => false,
            'expo_push_token' => 'ExponentPushToken[mock_token_carlos]',
            'quiet_hours_enabled' => false,
        ]);

        EmergencyContact::factory()->create([
            'user_id' => $user->id,
        ]);

        $pushServiceMock = Mockery::mock(PushNotificationService::class);
        $pushServiceMock->shouldReceive('sendPush')
            ->once()
            ->with(
                'ExponentPushToken[mock_token_carlos]',
                '⚡ Probá Estoy Ok Premium Gratis',
                Mockery::pattern('/Tenés 7 días sin cargo/'),
                Mockery::subset(['type' => 'activation_trial'])
            )
            ->andReturn(true);

        $this->app->instance(PushNotificationService::class, $pushServiceMock);

        $this->artisan('users:send-activation-nudges')
            ->expectsOutputToContain('Nudge de oferta de Trial enviado a: ' . $user->email)
            ->assertExitCode(0);

        $this->assertTrue(Cache::has("activation_nudge_48h_user_{$user->id}"));
    }
}

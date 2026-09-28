<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\UserDevice;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Http;
use Tests\TestCase;

class GoogleAuthTest extends TestCase
{
    use RefreshDatabase;

    private function getValidGooglePayload(array $overrides = []): array
    {
        $clientId = config('services.google.client_id') ?: 'test-google-client-id';

        return array_merge([
            'iss' => 'https://accounts.google.com',
            'aud' => $clientId,
            'sub' => 'google-user-id-12345',
            'email' => 'nuevo.google@example.com',
            'email_verified' => 'true',
            'name' => 'Carlos Google',
            'picture' => 'https://example.com/avatar.jpg',
            'exp' => (string) (time() + 3600),
        ], $overrides);
    }

    public function test_new_user_can_register_and_login_with_google()
    {
        Http::fake([
            'https://oauth2.googleapis.com/tokeninfo*' => Http::response($this->getValidGooglePayload(), 200),
        ]);

        $response = $this->postJson('/api/auth/google', [
            'id_token' => 'valid-mock-google-id-token',
            'device_uuid' => 'test-device-uuid-1',
            'device_name' => 'Pixel 8 Pro',
            'platform' => 'android',
        ]);

        $response->assertStatus(200)
            ->assertJsonStructure([
                'token',
                'user' => [
                    'id',
                    'name',
                    'email',
                ],
                'device_uuid',
            ]);

        $this->assertDatabaseHas('users', [
            'email' => 'nuevo.google@example.com',
            'google_id' => 'google-user-id-12345',
            'name' => 'Carlos Google',
        ]);

        $user = User::where('email', 'nuevo.google@example.com')->first();
        $this->assertNotNull($user->email_verified_at, 'El email debe estar verificado al crearse por Google.');

        $this->assertDatabaseHas('user_devices', [
            'user_id' => $user->id,
            'device_uuid' => 'test-device-uuid-1',
            'is_active' => true,
        ]);
    }

    public function test_existing_email_user_links_google_id_and_verifies_email()
    {
        $user = User::factory()->create([
            'email' => 'existente@example.com',
            'google_id' => null,
            'email_verified_at' => null,
        ]);

        Http::fake([
            'https://oauth2.googleapis.com/tokeninfo*' => Http::response($this->getValidGooglePayload([
                'iss' => 'accounts.google.com',
                'sub' => 'google-user-id-99999',
                'email' => 'existente@example.com',
                'name' => 'Usuario Existente',
            ]), 200),
        ]);

        $response = $this->postJson('/api/auth/google', [
            'id_token' => 'valid-mock-google-id-token',
            'device_uuid' => 'test-device-uuid-2',
            'device_name' => 'Galaxy S24',
            'platform' => 'android',
        ]);

        $response->assertStatus(200)
            ->assertJsonStructure(['token', 'user', 'device_uuid']);

        $user->refresh();
        $this->assertEquals('google-user-id-99999', $user->google_id);
        $this->assertNotNull($user->email_verified_at, 'El usuario existente sin verificar debe quedar verificado.');
    }

    public function test_existing_google_user_logs_in_successfully()
    {
        $user = User::factory()->create([
            'email' => 'recurrente@example.com',
            'google_id' => 'google-user-id-77777',
            'email_verified_at' => now(),
        ]);

        Http::fake([
            'https://oauth2.googleapis.com/tokeninfo*' => Http::response($this->getValidGooglePayload([
                'sub' => 'google-user-id-77777',
                'email' => 'recurrente@example.com',
            ]), 200),
        ]);

        $response = $this->postJson('/api/auth/google', [
            'id_token' => 'valid-mock-google-id-token',
            'device_uuid' => 'test-device-uuid-3',
            'device_name' => 'Motorola Edge',
            'platform' => 'android',
        ]);

        $response->assertStatus(200)
            ->assertJson([
                'user' => [
                    'id' => $user->id,
                    'email' => 'recurrente@example.com',
                ],
            ]);
    }

    public function test_invalid_google_token_returns_validation_error()
    {
        Http::fake([
            'https://oauth2.googleapis.com/tokeninfo*' => Http::response([
                'error_description' => 'Invalid Value',
            ], 400),
        ]);

        $response = $this->postJson('/api/auth/google', [
            'id_token' => 'invalid-token-string',
        ]);

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['id_token']);
    }

    public function test_google_login_enforces_single_mobile_device_policy()
    {
        $user = User::factory()->create([
            'email' => 'multidevice@example.com',
            'google_id' => 'google-sub-multidevice',
        ]);

        // Prior device
        $priorDevice = UserDevice::create([
            'user_id' => $user->id,
            'device_uuid' => 'old-device-uuid',
            'device_name' => 'Old Phone',
            'platform' => 'android',
            'is_active' => true,
            'push_token' => 'old-push-token',
        ]);

        Http::fake([
            'https://oauth2.googleapis.com/tokeninfo*' => Http::response($this->getValidGooglePayload([
                'sub' => 'google-sub-multidevice',
                'email' => 'multidevice@example.com',
            ]), 200),
        ]);

        $response = $this->postJson('/api/auth/google', [
            'id_token' => 'valid-mock-google-id-token',
            'device_uuid' => 'new-device-uuid',
            'device_name' => 'New Phone',
            'platform' => 'android',
        ]);

        $response->assertStatus(200);

        $this->assertFalse($priorDevice->fresh()->is_active, 'El dispositivo anterior debe ser desactivado.');
        $this->assertTrue(UserDevice::where('device_uuid', 'new-device-uuid')->first()->is_active, 'El nuevo dispositivo debe estar activo.');
    }
}

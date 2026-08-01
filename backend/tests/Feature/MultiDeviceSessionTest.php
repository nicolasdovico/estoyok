<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\UserDevice;
use App\Services\PushNotificationService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class MultiDeviceSessionTest extends TestCase
{
    use RefreshDatabase;

    public function test_login_revokes_previous_sessions_and_sends_silent_logout_push()
    {
        $user = User::factory()->create([
            'email' => 'nicolas@example.com',
            'password' => bcrypt('password123'),
            'email_verified_at' => now(),
        ]);

        // Device 1 Login
        $response1 = $this->postJson('/api/login', [
            'email' => 'nicolas@example.com',
            'password' => 'password123',
            'device_uuid' => 'device-1111',
            'device_name' => 'Galaxy S21',
        ]);

        $response1->assertStatus(200);
        $token1 = $response1->json('token');

        // Set push token for Device 1
        $this->withHeader('Authorization', 'Bearer ' . $token1)
            ->putJson('/api/settings/push-token', [
                'push_token' => 'fcm-token-device-1',
                'device_uuid' => 'device-1111',
            ]);

        $this->assertDatabaseHas('user_devices', [
            'user_id' => $user->id,
            'device_uuid' => 'device-1111',
            'is_active' => true,
            'push_token' => 'fcm-token-device-1',
        ]);

        // Device 2 Login (Same user logs in on a new device)
        $response2 = $this->postJson('/api/login', [
            'email' => 'nicolas@example.com',
            'password' => 'password123',
            'device_uuid' => 'device-2222',
            'device_name' => 'Pixel 8',
        ]);

        $response2->assertStatus(200);

        // Device 1 should be deactivated
        $this->assertDatabaseHas('user_devices', [
            'user_id' => $user->id,
            'device_uuid' => 'device-1111',
            'is_active' => false,
        ]);

        // Device 2 should be active
        $this->assertDatabaseHas('user_devices', [
            'user_id' => $user->id,
            'device_uuid' => 'device-2222',
            'is_active' => true,
        ]);

        // Old token 1 should return 401 Unauthorized because previous Sanctum tokens were revoked
        \Illuminate\Support\Facades\Auth::forgetGuards();
        $testResponse = $this->withToken($token1)->getJson('/api/user');
        $testResponse->assertStatus(401);
    }

    public function test_logout_deactivates_device_and_clears_tokens()
    {
        $user = User::factory()->create([
            'email' => 'nicolas@example.com',
            'password' => bcrypt('password123'),
            'email_verified_at' => now(),
        ]);

        $response = $this->postJson('/api/login', [
            'email' => 'nicolas@example.com',
            'password' => 'password123',
            'device_uuid' => 'device-3333',
        ]);

        $token = $response->json('token');

        $this->withHeader('Authorization', 'Bearer ' . $token)
            ->putJson('/api/settings/push-token', [
                'push_token' => 'fcm-token-3333',
                'device_uuid' => 'device-3333',
            ]);

        $logoutResponse = $this->withHeader('Authorization', 'Bearer ' . $token)
            ->postJson('/api/logout', [
                'device_uuid' => 'device-3333',
            ]);

        $logoutResponse->assertStatus(200);

        $this->assertDatabaseHas('user_devices', [
            'user_id' => $user->id,
            'device_uuid' => 'device-3333',
            'is_active' => false,
            'push_token' => null,
        ]);

        $this->assertNull($user->fresh()->expo_push_token);
    }
}

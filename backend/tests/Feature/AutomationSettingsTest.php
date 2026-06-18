<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class AutomationSettingsTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_update_automation_settings()
    {
        $user = User::factory()->create([
            'wifi_checkin_enabled' => false,
            'safe_wifi_ssid' => null,
            'sensor_checkin_enabled' => false,
        ]);

        $response = $this->actingAs($user)
            ->putJson('/api/settings/automation', [
                'wifi_checkin_enabled' => true,
                'safe_wifi_ssid' => 'HomeWifi_SSID',
                'sensor_checkin_enabled' => true,
            ]);

        $response->assertStatus(200)
            ->assertJson([
                'wifi_checkin_enabled' => true,
                'safe_wifi_ssid' => 'HomeWifi_SSID',
                'sensor_checkin_enabled' => true,
            ]);

        $this->assertTrue((bool) $user->fresh()->wifi_checkin_enabled);
        $this->assertEquals('HomeWifi_SSID', $user->fresh()->safe_wifi_ssid);
        $this->assertTrue((bool) $user->fresh()->sensor_checkin_enabled);
    }

    public function test_update_automation_settings_validation()
    {
        $user = User::factory()->create();

        // Missing fields
        $this->actingAs($user)
            ->putJson('/api/settings/automation', [
                'safe_wifi_ssid' => 'HomeWifi_SSID',
            ])->assertStatus(422);

        // Invalid types
        $this->actingAs($user)
            ->putJson('/api/settings/automation', [
                'wifi_checkin_enabled' => 'not-a-bool',
                'safe_wifi_ssid' => 12345,
                'sensor_checkin_enabled' => 'not-a-bool',
            ])->assertStatus(422);
    }

    public function test_checkin_with_custom_source_is_logged()
    {
        $user = User::factory()->create();

        // Silent Wi-Fi check-in
        $response = $this->actingAs($user)
            ->postJson('/api/check-in', [
                'source' => 'wifi',
            ]);

        $response->assertStatus(200);

        $this->assertDatabaseHas('check_ins', [
            'user_id' => $user->id,
            'source' => 'wifi',
        ]);

        // Silent Movement check-in
        $this->actingAs($user)
            ->postJson('/api/check-in', [
                'source' => 'movement',
            ])->assertStatus(200);

        $this->assertDatabaseHas('check_ins', [
            'user_id' => $user->id,
            'source' => 'movement',
        ]);
    }

    public function test_checkin_source_validation()
    {
        $user = User::factory()->create();

        // Invalid source
        $this->actingAs($user)
            ->postJson('/api/check-in', [
                'source' => 'invalid-source-type',
            ])->assertStatus(422);
    }

    public function test_checkin_history_lists_sources()
    {
        $user = User::factory()->create();

        $user->checkIns()->create(['source' => 'manual']);
        $user->checkIns()->create(['source' => 'wifi']);
        $user->checkIns()->create(['source' => 'movement']);

        $response = $this->actingAs($user)
            ->getJson('/api/check-ins');

        $response->assertStatus(200)
            ->assertJsonCount(3)
            ->assertJsonFragment(['source' => 'manual'])
            ->assertJsonFragment(['source' => 'wifi'])
            ->assertJsonFragment(['source' => 'movement']);
    }
}

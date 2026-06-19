<?php

namespace Tests\Feature;

use App\Models\CurrentLocation;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class SensorStatusTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_update_sensor_status_via_dedicated_endpoint()
    {
        $user = User::factory()->create();

        $response = $this->actingAs($user)->putJson('/api/locations/sensor-status', [
            'is_tracking_active' => false,
            'gps_enabled' => false,
        ]);

        $response->assertStatus(200)
            ->assertJsonFragment([
                'message' => 'Sensor status updated successfully',
            ]);

        $this->assertDatabaseHas('current_locations', [
            'user_id' => $user->id,
            'is_tracking_active' => false,
            'gps_enabled' => false,
        ]);

        $this->assertNotNull($user->currentLocation->last_seen_at);
    }

    public function test_location_update_saves_sensor_status()
    {
        $user = User::factory()->create();

        $response = $this->actingAs($user)->postJson('/api/locations/update', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'accuracy' => 10.0,
            'is_tracking_active' => true,
            'gps_enabled' => false,
        ]);

        $response->assertStatus(200);

        $this->assertDatabaseHas('current_locations', [
            'user_id' => $user->id,
            'is_tracking_active' => true,
            'gps_enabled' => false,
        ]);
    }

    public function test_is_offline_accessor_behavior()
    {
        $user = User::factory()->create();

        // 1. New location update sets last_seen_at to now, should not be offline
        $currentLocation = CurrentLocation::create([
            'user_id' => $user->id,
            'accuracy' => 10,
            'recorded_at' => now(),
            'location' => \DB::raw("ST_GeomFromText('POINT(-58.3816 -34.6037)', 4326)"),
            'is_tracking_active' => true,
            'gps_enabled' => true,
            'last_seen_at' => now(),
        ]);

        $this->assertFalse($currentLocation->is_offline);

        // 2. Set last_seen_at to 20 minutes ago, should be offline since tracking is active
        $currentLocation->update(['last_seen_at' => now()->subMinutes(20)]);
        $this->assertTrue($currentLocation->fresh()->is_offline);

        // 3. Set tracking to inactive, should not show as offline even if last_seen_at is old
        $currentLocation->update(['is_tracking_active' => false]);
        $this->assertFalse($currentLocation->fresh()->is_offline);
    }
}

<?php

namespace Tests\Feature;

use App\Jobs\SendBatteryAlertJob;
use App\Models\Circle;
use App\Models\CurrentLocation;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Queue;
use Tests\TestCase;

class BatteryAlertTest extends TestCase
{
    use RefreshDatabase;

    public function test_location_update_saves_battery_level()
    {
        $user = User::factory()->create();

        $response = $this->actingAs($user)->postJson('/api/locations/update', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'accuracy' => 10.0,
            'battery_level' => 0.85,
        ]);

        $response->assertStatus(200);

        $this->assertDatabaseHas('current_locations', [
            'user_id' => $user->id,
            'battery_level' => 0.85,
            'is_battery_low' => false,
        ]);
    }

    public function test_crossing_threshold_downwards_triggers_alert_if_enabled()
    {
        Queue::fake([SendBatteryAlertJob::class]);

        $user = User::factory()->create([
            'low_battery_alerts_enabled' => true,
        ]);

        // Establish initial location with normal battery
        $this->actingAs($user)->postJson('/api/locations/update', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'battery_level' => 0.20,
        ]);

        Queue::assertNothingPushed();

        // Update location crossing 15% downwards
        $this->actingAs($user)->postJson('/api/locations/update', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'battery_level' => 0.12,
        ]);

        Queue::assertPushed(SendBatteryAlertJob::class, function ($job) use ($user) {
            return $job->user->id === $user->id && $job->batteryLevel === 0.12;
        });

        $this->assertDatabaseHas('current_locations', [
            'user_id' => $user->id,
            'battery_level' => 0.12,
            'is_battery_low' => true,
        ]);

        $this->assertNotNull($user->fresh()->last_battery_alert_sent_at);
    }

    public function test_crossing_threshold_downwards_does_not_trigger_alert_if_disabled()
    {
        Queue::fake([SendBatteryAlertJob::class]);

        $user = User::factory()->create([
            'low_battery_alerts_enabled' => false,
        ]);

        // Establish initial location with normal battery
        $this->actingAs($user)->postJson('/api/locations/update', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'battery_level' => 0.20,
        ]);

        // Update location crossing 15% downwards
        $this->actingAs($user)->postJson('/api/locations/update', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'battery_level' => 0.12,
        ]);

        Queue::assertNothingPushed();

        $this->assertDatabaseHas('current_locations', [
            'user_id' => $user->id,
            'battery_level' => 0.12,
            'is_battery_low' => true,
        ]);
    }

    public function test_low_battery_alert_rate_limiting_ignores_repeated_lows()
    {
        Queue::fake([SendBatteryAlertJob::class]);

        $user = User::factory()->create([
            'low_battery_alerts_enabled' => true,
            'last_battery_alert_sent_at' => now()->subMinutes(30), // Sent 30 minutes ago
        ]);

        // Establish previous state as normal battery, but with recent alert sent (simulated restart/reconnect)
        CurrentLocation::create([
            'user_id' => $user->id,
            'accuracy' => 10,
            'recorded_at' => now(),
            'location' => \DB::raw("ST_GeomFromText('POINT(-58.3816 -34.6037)', 4326)"),
            'battery_level' => 0.20,
            'is_battery_low' => false,
        ]);

        // Update location crossing 15% downwards
        $this->actingAs($user)->postJson('/api/locations/update', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'battery_level' => 0.10,
        ]);

        // Alert should not be dispatched due to 1-hour rate limit
        Queue::assertNothingPushed();

        // Advance user alert timestamp to > 1 hour ago
        $user->update(['last_battery_alert_sent_at' => now()->subMinutes(70)]);
        $currentLocation = CurrentLocation::where('user_id', $user->id)->first();
        $currentLocation->update(['is_battery_low' => false]); // reset low state to trigger crossing again

        // Update location crossing 15% downwards again
        $this->actingAs($user)->postJson('/api/locations/update', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'battery_level' => 0.08,
        ]);

        Queue::assertPushed(SendBatteryAlertJob::class);
    }

    public function test_user_can_update_low_battery_alerts_setting()
    {
        $user = User::factory()->create([
            'low_battery_alerts_enabled' => true,
        ]);

        $response = $this->actingAs($user)->putJson('/api/settings/privacy', [
            'low_battery_alerts_enabled' => false,
        ]);

        $response->assertStatus(200)
            ->assertJsonFragment([
                'low_battery_alerts_enabled' => false,
            ]);

        $this->assertFalse($user->fresh()->low_battery_alerts_enabled);
    }
}

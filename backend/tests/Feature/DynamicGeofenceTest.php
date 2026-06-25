<?php

namespace Tests\Feature;

use App\Jobs\ProcessDynamicGeofencing;
use App\Models\Circle;
use App\Models\DynamicGeofence;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Http;
use Tests\TestCase;

class DynamicGeofenceTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_update_proximity_alerts_privacy_settings()
    {
        $user = User::factory()->create(['proximity_alerts_enabled' => true]);

        $response = $this->actingAs($user)->putJson('/api/settings/proximity-alerts', [
            'proximity_alerts_enabled' => false,
        ]);

        $response->assertStatus(200)
            ->assertJson([
                'message' => 'Proximity alerts settings updated successfully',
                'proximity_alerts_enabled' => false,
            ]);

        $this->assertFalse((bool) $user->refresh()->proximity_alerts_enabled);
    }

    public function test_user_cannot_create_dynamic_geofence_with_non_member()
    {
        $initiator = User::factory()->create();
        $target = User::factory()->create();

        $response = $this->actingAs($initiator)->postJson('/api/dynamic-geofences', [
            'target_id' => $target->id,
            'safe_radius_meters' => 50,
        ]);

        $response->assertStatus(403)
            ->assertJsonFragment(['message' => 'El miembro no pertenece a tus círculos.']);
    }

    public function test_user_cannot_create_dynamic_geofence_if_target_has_privacy_disabled()
    {
        $initiator = User::factory()->create();
        $target = User::factory()->create(['proximity_alerts_enabled' => false]);

        $circle = Circle::create(['name' => 'Fam', 'owner_id' => $initiator->id]);
        $circle->users()->attach([$initiator->id, $target->id]);

        $response = $this->actingAs($initiator)->postJson('/api/dynamic-geofences', [
            'target_id' => $target->id,
            'safe_radius_meters' => 50,
        ]);

        $response->assertStatus(403)
            ->assertJsonFragment(['message' => 'Este miembro ha desactivado las alertas de proximidad relativas por privacidad.']);
    }

    public function test_user_can_create_dynamic_geofence_and_notifies_target()
    {
        $initiator = User::factory()->create();
        $target = User::factory()->create(['proximity_alerts_enabled' => true, 'expo_push_token' => 'target-token']);

        $circle = Circle::create(['name' => 'Fam', 'owner_id' => $initiator->id]);
        $circle->users()->attach([$initiator->id, $target->id]);

        // Create an existing active geofence to verify it gets deactivated
        $oldGeofence = DynamicGeofence::create([
            'initiator_id' => $initiator->id,
            'target_id' => $target->id,
            'safe_radius_meters' => 100,
            'is_active' => true,
        ]);

        Http::fake();

        $response = $this->actingAs($initiator)->postJson('/api/dynamic-geofences', [
            'target_id' => $target->id,
            'safe_radius_meters' => 50,
        ]);

        $response->assertStatus(201);
        $this->assertFalse($oldGeofence->refresh()->is_active);

        $newGeofence = DynamicGeofence::active()->where('initiator_id', $initiator->id)->first();
        $this->assertNotNull($newGeofence);
        $this->assertEquals(50, $newGeofence->safe_radius_meters);

        Http::assertSent(function ($request) {
            return $request['title'] === '📡 Radar de Proximidad Activado';
        });
    }

    public function test_user_can_list_active_dynamic_geofences()
    {
        $user = User::factory()->create();
        $other = User::factory()->create();

        DynamicGeofence::create([
            'initiator_id' => $user->id,
            'target_id' => $other->id,
            'safe_radius_meters' => 50,
            'is_active' => true,
        ]);

        DynamicGeofence::create([
            'initiator_id' => $other->id,
            'target_id' => $user->id,
            'safe_radius_meters' => 100,
            'is_active' => true,
        ]);

        // Inactive one
        DynamicGeofence::create([
            'initiator_id' => $user->id,
            'target_id' => $other->id,
            'safe_radius_meters' => 30,
            'is_active' => false,
        ]);

        $response = $this->actingAs($user)->getJson('/api/dynamic-geofences/active');

        $response->assertStatus(200)
            ->assertJsonCount(2);
    }

    public function test_user_can_deactivate_dynamic_geofence()
    {
        $initiator = User::factory()->create(['expo_push_token' => 'initiator-token']);
        $target = User::factory()->create(['expo_push_token' => 'target-token']);

        $geofence = DynamicGeofence::create([
            'initiator_id' => $initiator->id,
            'target_id' => $target->id,
            'safe_radius_meters' => 50,
            'is_active' => true,
        ]);

        Http::fake();

        $response = $this->actingAs($initiator)->postJson("/api/dynamic-geofences/{$geofence->id}/deactivate");

        $response->assertStatus(200);
        $this->assertFalse($geofence->refresh()->is_active);

        Http::assertSent(function ($request) {
            return $request['title'] === '📡 Radar de Proximidad Finalizado';
        });
    }

    public function test_location_update_returns_active_dynamic_geofence_status()
    {
        $initiator = User::factory()->create();
        $target = User::factory()->create();

        $response = $this->actingAs($initiator)->postJson('/api/locations/update', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'accuracy' => 10,
        ]);

        $response->assertStatus(200)
            ->assertJsonFragment(['active_dynamic_geofence' => false]);

        DynamicGeofence::create([
            'initiator_id' => $initiator->id,
            'target_id' => $target->id,
            'safe_radius_meters' => 50,
            'is_active' => true,
        ]);

        $response = $this->actingAs($initiator)->postJson('/api/locations/update', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'accuracy' => 10,
        ]);

        $response->assertStatus(200)
            ->assertJsonFragment(['active_dynamic_geofence' => true]);
    }

    public function test_process_dynamic_geofencing_job_detects_breach_and_recovery()
    {
        $initiator = User::factory()->create(['name' => 'Tutor', 'expo_push_token' => 'tutor-token']);
        $target = User::factory()->create(['name' => 'Niño']);

        $geofence = DynamicGeofence::create([
            'initiator_id' => $initiator->id,
            'target_id' => $target->id,
            'safe_radius_meters' => 50, // 50m
            'is_active' => true,
        ]);

        // Record location for tutor: Obelisco (-34.6037, -58.3816)
        DB::table('current_locations')->insert([
            'user_id' => $initiator->id,
            'accuracy' => 10,
            'recorded_at' => now(),
            'last_seen_at' => now(),
            'location' => DB::raw("ST_GeomFromText('POINT(-58.3816 -34.6037)', 4326)"),
        ]);

        // Record location for child: Obelisco (-34.6037, -58.3816) - Distance is 0
        DB::table('current_locations')->insert([
            'user_id' => $target->id,
            'accuracy' => 10,
            'recorded_at' => now(),
            'last_seen_at' => now(),
            'location' => DB::raw("ST_GeomFromText('POINT(-58.3816 -34.6037)', 4326)"),
        ]);

        Http::fake();

        // 1. Run job when they are close
        $job = new ProcessDynamicGeofencing($target, -34.6037, -58.3816);
        $job->handle();

        // No push sent because they are close
        Http::assertNothingSent();

        // 2. Update child to be far away (e.g. latitude shifted by 0.001 degrees, approx 110 meters)
        DB::table('current_locations')
            ->where('user_id', $target->id)
            ->update([
                'location' => DB::raw("ST_GeomFromText('POINT(-58.3816 -34.6048)', 4326)"),
            ]);

        $job = new ProcessDynamicGeofencing($target, -34.6048, -58.3816);
        $job->handle();

        // Push should be sent for breach
        Http::assertSent(function ($request) {
            return $request['title'] === '🚨 Alerta de Proximidad' && str_contains($request['body'], 'alejado demasiado');
        });

        // Clear HTTP fake recorder history
        Http::fake();

        // Run again while far away, should not send again due to caching
        $job = new ProcessDynamicGeofencing($target, -34.6048, -58.3816);
        $job->handle();

        Http::assertNothingSent();

        // 3. Move child back to Obelisco (close)
        DB::table('current_locations')
            ->where('user_id', $target->id)
            ->update([
                'location' => DB::raw("ST_GeomFromText('POINT(-58.3816 -34.6037)', 4326)"),
            ]);

        $job = new ProcessDynamicGeofencing($target, -34.6037, -58.3816);
        $job->handle();

        // Restored push should be sent
        Http::assertSent(function ($request) {
            return $request['title'] === '🛡️ Proximidad Restablecida';
        });
    }
}

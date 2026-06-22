<?php

namespace Tests\Feature;

use App\Jobs\SendSpeedingAlertJob;
use App\Models\Circle;
use App\Models\CurrentLocation;
use App\Models\DriveEvent;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Queue;
use Tests\TestCase;

class DriveDetectionTest extends TestCase
{
    use RefreshDatabase;

    public function test_location_update_saves_speed_and_driving_status()
    {
        $user = User::factory()->create();

        // 8.5 m/s = 30.6 km/h
        $response = $this->actingAs($user)->postJson('/api/locations/update', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'accuracy' => 10.0,
            'speed' => 8.5,
            'is_driving' => true,
        ]);

        $response->assertStatus(200);

        $this->assertDatabaseHas('current_locations', [
            'user_id' => $user->id,
            'speed' => 30.6,
            'is_driving' => true,
        ]);

        $this->assertDatabaseHas('location_histories', [
            'user_id' => $user->id,
            'speed' => 30.6,
            'is_driving' => true,
        ]);
    }

    public function test_driving_creates_new_drive_event()
    {
        $user = User::factory()->create();

        // Al reportar is_driving = true, debe crear un nuevo DriveEvent
        $response = $this->actingAs($user)->postJson('/api/locations/update', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'speed' => 10.0, // 36 km/h
            'is_driving' => true,
        ]);

        $response->assertStatus(200);

        $this->assertDatabaseHas('drive_events', [
            'user_id' => $user->id,
            'end_time' => null,
            'max_speed' => 36.0,
            'exceeded_speed_limit' => false,
        ]);
    }

    public function test_speeding_records_event_and_triggers_alert()
    {
        Queue::fake([SendSpeedingAlertJob::class]);

        $owner = User::factory()->create();
        $user = User::factory()->create();

        $circle = Circle::create([
            'name' => 'Circulo de prueba',
            'owner_id' => $owner->id,
            'speed_limit' => 100,
        ]);

        $circle->users()->attach($user->id, ['role' => 'member']);

        // 35 m/s = 126 km/h (excede límite de 100)
        $response = $this->actingAs($user)->postJson('/api/locations/update', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'speed' => 35.0,
            'is_driving' => true,
        ]);

        $response->assertStatus(200);

        $this->assertDatabaseHas('drive_events', [
            'user_id' => $user->id,
            'max_speed' => 126.0,
            'exceeded_speed_limit' => true,
        ]);

        Queue::assertPushed(SendSpeedingAlertJob::class, function ($job) use ($owner, $user) {
            return $job->owner->id === $owner->id &&
                   $job->user->id === $user->id &&
                   $job->speed == 126.0 &&
                   $job->limit == 100;
        });
    }

    public function test_not_driving_closes_drive_event()
    {
        $user = User::factory()->create();

        // 1. Iniciar trayecto
        $this->actingAs($user)->postJson('/api/locations/update', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'speed' => 10.0,
            'is_driving' => true,
        ]);

        $this->assertDatabaseHas('drive_events', [
            'user_id' => $user->id,
            'end_time' => null,
        ]);

        // 2. Reportar que se detuvo
        $this->actingAs($user)->postJson('/api/locations/update', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'speed' => 0.0,
            'is_driving' => false,
        ]);

        $event = DriveEvent::where('user_id', $user->id)->first();
        $this->assertNotNull($event->end_time);
    }

    public function test_cleanup_active_drives_command()
    {
        $user = User::factory()->create();

        // Crear una ubicación actual desactualizada (hace 40 minutos)
        $lastSeen = now()->subMinutes(40);
        CurrentLocation::create([
            'user_id' => $user->id,
            'accuracy' => 10.0,
            'recorded_at' => $lastSeen,
            'last_seen_at' => $lastSeen,
            'location' => \Illuminate\Support\Facades\DB::raw("ST_GeomFromText('POINT(-58.3816 -34.6037)', 4326)"),
            'is_tracking_active' => true,
            'gps_enabled' => true,
        ]);

        // Crear un DriveEvent activo que quedó colgado
        $event = DriveEvent::create([
            'user_id' => $user->id,
            'start_time' => $lastSeen,
            'max_speed' => 50.0,
            'exceeded_speed_limit' => false,
        ]);

        // Correr el comando de limpieza
        $this->artisan('drive:cleanup-active')
            ->expectsOutput("Se cerraron 1 trayectos vehiculares huérfanos.")
            ->assertExitCode(0);

        $this->assertNotNull($event->fresh()->end_time);
        $this->assertEquals($lastSeen->toDateTimeString(), $event->fresh()->end_time->toDateTimeString());
    }

    public function test_update_circle_speed_limit()
    {
        $owner = User::factory()->create();
        $circle = Circle::create([
            'name' => 'Circulo de prueba',
            'owner_id' => $owner->id,
            'speed_limit' => 120,
        ]);

        $circle->users()->attach($owner->id, ['role' => 'admin']);

        // Actualizar límite
        $response = $this->actingAs($owner)->putJson("/api/circles/{$circle->id}/speed-limit", [
            'speed_limit' => 130,
        ]);

        $response->assertStatus(200);
        $this->assertEquals(130, $circle->fresh()->speed_limit);

        // Intentar actualizar con otro usuario no miembro
        $otherUser = User::factory()->create();
        $response = $this->actingAs($otherUser)->putJson("/api/circles/{$circle->id}/speed-limit", [
            'speed_limit' => 110,
        ]);

        $response->assertStatus(403);
    }
}

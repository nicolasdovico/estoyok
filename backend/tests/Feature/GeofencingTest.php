<?php

namespace Tests\Feature;

use App\Jobs\ProcessGeofencing;
use App\Models\Circle;
use App\Models\Geofence;
use App\Models\GeofenceEvent;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Http;
use Tests\TestCase;

class GeofencingTest extends TestCase
{
    use RefreshDatabase;

    public function test_location_update_creates_records_and_dispatches_job()
    {
        $user = User::factory()->create();
        $this->actingAs($user);

        $response = $this->postJson('/api/locations/update', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'accuracy' => 10.5,
        ]);

        $response->assertStatus(200);

        $this->assertDatabaseHas('current_locations', [
            'user_id' => $user->id,
            'accuracy' => 10.5,
        ]);

        $this->assertDatabaseHas('location_histories', [
            'user_id' => $user->id,
            'accuracy' => 10.5,
        ]);
    }

    public function test_geofencing_job_detects_entry_and_sends_notification()
    {
        $owner = User::factory()->create(['name' => 'Owner', 'expo_push_token' => 'token-1']);
        $member = User::factory()->create(['name' => 'Member', 'expo_push_token' => 'token-2']);

        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id]);
        $circle->users()->attach([$owner->id, $member->id]);

        $geofence = Geofence::create([
            'circle_id' => $circle->id,
            'name' => 'Obelisco',
            'radius' => 500,
            'center' => DB::raw("ST_GeomFromText('POINT(-58.3816 -34.6037)', 4326)"),
        ]);

        Http::fake();

        // INSIDE
        $job = new ProcessGeofencing($member, -34.6037, -58.3816);
        $job->handle();

        $this->assertDatabaseHas('geofence_events', [
            'user_id' => $member->id,
            'geofence_id' => $geofence->id,
            'type' => 'entry',
        ]);

        Http::assertSent(function ($request) {
            return str_contains($request['body'], 'ingresado a');
        });
    }

    public function test_geofencing_job_detects_exit_and_sends_notification()
    {
        $owner = User::factory()->create(['name' => 'Owner', 'expo_push_token' => 'token-1']);
        $member = User::factory()->create(['name' => 'Member']);

        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id]);
        $circle->users()->attach([$owner->id, $member->id]);

        $geofence = Geofence::create([
            'circle_id' => $circle->id,
            'name' => 'Obelisco',
            'radius' => 500,
            'center' => DB::raw("ST_GeomFromText('POINT(-58.3816 -34.6037)', 4326)"),
        ]);

        // Pre-record an ENTRY event
        GeofenceEvent::create([
            'user_id' => $member->id,
            'geofence_id' => $geofence->id,
            'type' => 'entry',
            'occurred_at' => now()->subMinutes(10),
        ]);

        Http::fake();

        // OUTSIDE (Mendoza)
        $job = new ProcessGeofencing($member, -32.8895, -68.8458);
        $job->handle();

        $this->assertDatabaseHas('geofence_events', [
            'user_id' => $member->id,
            'geofence_id' => $geofence->id,
            'type' => 'exit',
        ]);

        Http::assertSent(function ($request) {
            return str_contains($request['body'], 'salido de');
        });
    }

    public function test_geofencing_job_prevents_duplicate_entry_notifications()
    {
        $owner = User::factory()->create(['expo_push_token' => 'token-1']);
        $member = User::factory()->create();

        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id]);
        $circle->users()->attach([$owner->id, $member->id]);

        $geofence = Geofence::create([
            'circle_id' => $circle->id,
            'name' => 'Obelisco',
            'radius' => 500,
            'center' => DB::raw("ST_GeomFromText('POINT(-58.3816 -34.6037)', 4326)"),
        ]);

        // Pre-record an ENTRY event
        GeofenceEvent::create([
            'user_id' => $member->id,
            'geofence_id' => $geofence->id,
            'type' => 'entry',
            'occurred_at' => now()->subMinutes(10),
        ]);

        Http::fake();

        // STILL INSIDE
        $job = new ProcessGeofencing($member, -34.6037, -58.3816);
        $job->handle();

        // No new event should be created, no notification sent
        $this->assertEquals(1, GeofenceEvent::count());
        Http::assertNothingSent();
    }

    public function test_geofencing_job_applies_hysteresis_and_cooldown_to_prevent_false_exit_alerts()
    {
        $owner = User::factory()->create(['name' => 'Owner', 'expo_push_token' => 'token-1']);
        $member = User::factory()->create(['name' => 'Member']);

        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id]);
        $circle->users()->attach([$owner->id, $member->id]);

        $geofence = Geofence::create([
            'circle_id' => $circle->id,
            'name' => 'Casa',
            'radius' => 50,
            'center' => DB::raw("ST_GeomFromText('POINT(-59.14591 -34.59382)', 4326)"),
        ]);

        // Pre-record an ENTRY event 1 minute ago (cooldown active)
        GeofenceEvent::create([
            'user_id' => $member->id,
            'geofence_id' => $geofence->id,
            'type' => 'entry',
            'occurred_at' => now()->subMinute(),
        ]);

        Http::fake();

        // Slight drift (60m away - inside hysteresis buffer of 50m + 30m = 80m, and inside 3-min cooldown)
        $job = new ProcessGeofencing($member, -34.5943, -59.1465);
        $job->handle();

        // No new event or notification sent
        $this->assertEquals(1, GeofenceEvent::count());
        Http::assertNothingSent();
    }
}

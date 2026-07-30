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

    public function test_geofencing_job_pending_exit_dwell_time_requires_confirmation()
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

        // Pre-record an ENTRY event 10 minutes ago
        GeofenceEvent::create([
            'user_id' => $member->id,
            'geofence_id' => $geofence->id,
            'type' => 'entry',
            'occurred_at' => now()->subMinutes(10),
        ]);

        Http::fake();

        // 1st update outside (120m away - stationary/walking speed)
        $job1 = new ProcessGeofencing($member, -34.5948, -59.1470, 10.0, null, 2.0);
        $job1->handle();

        // Pending exit stored, no exit event recorded yet
        $this->assertEquals(1, GeofenceEvent::count());
        Http::assertNothingSent();

        // Simulate 3 minutes passing for pending exit
        $pendingKey = "geofence_pending_exit_{$member->id}_{$geofence->id}";
        cache()->put($pendingKey, now()->subMinutes(3)->timestamp, now()->addMinutes(10));

        // 2nd update outside 3 minutes later
        $job2 = new ProcessGeofencing($member, -34.5948, -59.1470, 10.0, null, 2.0);
        $job2->handle();

        // Exit confirmed!
        $this->assertDatabaseHas('geofence_events', [
            'user_id' => $member->id,
            'geofence_id' => $geofence->id,
            'type' => 'exit',
        ]);
    }

    public function test_geofencing_job_pending_exit_cancelled_on_gps_rebound()
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

        GeofenceEvent::create([
            'user_id' => $member->id,
            'geofence_id' => $geofence->id,
            'type' => 'entry',
            'occurred_at' => now()->subMinutes(10),
        ]);

        Http::fake();

        // 1st update outside (120m away)
        $job1 = new ProcessGeofencing($member, -34.5948, -59.1470, 10.0, null, 2.0);
        $job1->handle();

        $pendingKey = "geofence_pending_exit_{$member->id}_{$geofence->id}";
        $this->assertTrue(cache()->has($pendingKey));

        // 2nd update rebound inside (center of house)
        $job2 = new ProcessGeofencing($member, -34.59382, -59.14591, 10.0, null, 0.0);
        $job2->handle();

        // Pending exit cancelled silently!
        $this->assertFalse(cache()->has($pendingKey));
        $this->assertEquals(1, GeofenceEvent::count());
        Http::assertNothingSent();
    }

    public function test_geofencing_job_suppresses_exit_when_connected_to_safe_wifi()
    {
        $owner = User::factory()->create(['name' => 'Owner', 'expo_push_token' => 'token-1']);
        $member = User::factory()->create(['name' => 'Member', 'safe_wifi_ssid' => 'MiWifiDeCasa']);

        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id]);
        $circle->users()->attach([$owner->id, $member->id]);

        $geofence = Geofence::create([
            'circle_id' => $circle->id,
            'name' => 'Casa',
            'radius' => 50,
            'center' => DB::raw("ST_GeomFromText('POINT(-59.14591 -34.59382)', 4326)"),
        ]);

        GeofenceEvent::create([
            'user_id' => $member->id,
            'geofence_id' => $geofence->id,
            'type' => 'entry',
            'occurred_at' => now()->subMinutes(10),
        ]);

        Http::fake();

        // GPS jump outside (150m away) BUT connected to home Wi-Fi
        $job = new ProcessGeofencing($member, -34.5950, -59.1472, 10.0, 'MiWifiDeCasa', 0.0);
        $job->handle();

        // Exit is completely suppressed!
        $this->assertEquals(1, GeofenceEvent::count());
        Http::assertNothingSent();
    }

    public function test_update_push_token_clears_token_from_other_users()
    {
        $user1 = User::factory()->create(['expo_push_token' => 'shared-device-token']);
        $user2 = User::factory()->create();

        $this->actingAs($user2);

        $response = $this->putJson('/api/settings/push-token', [
            'push_token' => 'shared-device-token',
        ]);

        $response->assertStatus(200);

        $this->assertNull($user1->fresh()->expo_push_token);
        $this->assertEquals('shared-device-token', $user2->fresh()->expo_push_token);
    }

    public function test_logout_clears_push_token()
    {
        $user = User::factory()->create(['expo_push_token' => 'test-token']);

        $this->actingAs($user);

        $response = $this->postJson('/api/logout');

        $response->assertStatus(200);
        $this->assertNull($user->fresh()->expo_push_token);
    }

    public function test_geofencing_alert_does_not_send_to_same_push_token_as_initiator()
    {
        $owner = User::factory()->create(['name' => 'Owner', 'expo_push_token' => 'same-token']);
        $member = User::factory()->create(['name' => 'Member', 'expo_push_token' => 'same-token']);

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

        // Should NOT send push because owner has the same push token as member
        Http::assertNothingSent();
    }
}

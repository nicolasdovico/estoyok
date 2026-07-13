<?php

namespace Tests\Feature;

use App\Models\Circle;
use App\Models\User;
use App\Models\DriveEvent;
use Carbon\Carbon;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class DriveReportsTest extends TestCase
{
    use RefreshDatabase;

    public function test_unauthenticated_user_cannot_access_drives()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id]);
        $member = User::factory()->create();

        $response = $this->getJson("/api/circles/{$circle->id}/members/{$member->id}/drives");

        $response->assertStatus(401);
    }

    public function test_user_not_in_circle_cannot_access_drives()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id]);
        $member = User::factory()->create();
        $circle->users()->attach($member->id, ['role' => 'member']);

        $stranger = User::factory()->create();

        $response = $this->actingAs($stranger)->getJson("/api/circles/{$circle->id}/members/{$member->id}/drives");

        $response->assertStatus(403)
            ->assertJsonFragment(['message' => 'No tienes acceso a este círculo']);
    }

    public function test_standard_user_gets_only_most_recent_drive()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id]);
        $requester = User::factory()->create(['is_premium' => false]);
        $member = User::factory()->create();

        $circle->users()->attach($requester->id, ['role' => 'member']);
        $circle->users()->attach($member->id, ['role' => 'member']);

        // Create 2 drives
        DriveEvent::create([
            'user_id' => $member->id,
            'start_time' => Carbon::now()->subHours(2),
            'end_time' => Carbon::now()->subHours(1),
            'max_speed' => 90.0,
            'exceeded_speed_limit' => false
        ]);

        DriveEvent::create([
            'user_id' => $member->id,
            'start_time' => Carbon::now()->subMinutes(30),
            'end_time' => Carbon::now()->subMinutes(10),
            'max_speed' => 100.0,
            'exceeded_speed_limit' => false
        ]);

        $response = $this->actingAs($requester)->getJson("/api/circles/{$circle->id}/members/{$member->id}/drives");

        $response->assertStatus(200);
        $data = $response->json();
        $this->assertFalse($data['is_premium']);
        $this->assertCount(1, $data['drives']); // Standard user only gets 1 drive
    }

    public function test_premium_user_gets_all_recent_drives()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id]);
        $requester = User::factory()->create(['is_premium' => true]);
        $member = User::factory()->create();

        $circle->users()->attach($requester->id, ['role' => 'member']);
        $circle->users()->attach($member->id, ['role' => 'member']);

        // Create 2 drives
        DriveEvent::create([
            'user_id' => $member->id,
            'start_time' => Carbon::now()->subHours(2),
            'end_time' => Carbon::now()->subHours(1),
            'max_speed' => 90.0,
            'exceeded_speed_limit' => false
        ]);

        DriveEvent::create([
            'user_id' => $member->id,
            'start_time' => Carbon::now()->subMinutes(30),
            'end_time' => Carbon::now()->subMinutes(10),
            'max_speed' => 100.0,
            'exceeded_speed_limit' => false
        ]);

        $response = $this->actingAs($requester)->getJson("/api/circles/{$circle->id}/members/{$member->id}/drives");

        $response->assertStatus(200);
        $data = $response->json();
        $this->assertTrue($data['is_premium']);
        $this->assertCount(2, $data['drives']); // Premium user gets both drives
    }

    public function test_drive_event_analyzes_telemetry_successfully()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id, 'speed_limit' => 80]);
        $requester = User::factory()->create(['is_premium' => true]);
        $member = User::factory()->create();

        $circle->users()->attach($requester->id, ['role' => 'member']);
        $circle->users()->attach($member->id, ['role' => 'member']);

        $startTime = Carbon::now()->subMinutes(30);
        $endTime = Carbon::now()->subMinutes(10);

        $drive = DriveEvent::create([
            'user_id' => $member->id,
            'start_time' => $startTime,
            'end_time' => $endTime,
            'max_speed' => 100.0,
            'exceeded_speed_limit' => true
        ]);

        // Insert location histories for this drive to simulate:
        // 1. Normal point: speed 40 km/h
        // 2. Rapid Acceleration: speed 60 km/h (+20 km/h in 2s)
        // 3. Speeding: speed 90 km/h (> 80 speed limit)
        // 4. Hard Brake: speed 40 km/h (-50 km/h in 2s)

        // Point 1
        DB::table('location_histories')->insert([
            'user_id' => $member->id,
            'accuracy' => 5.0,
            'recorded_at' => $startTime->copy()->addSeconds(10),
            'location' => DB::raw("ST_GeomFromText('POINT(-58.3816 -34.6037)', 4326)"),
            'speed' => 40.0,
            'is_driving' => true,
            'created_at' => Carbon::now(),
            'updated_at' => Carbon::now(),
        ]);

        // Point 2 (Rapid acceleration)
        DB::table('location_histories')->insert([
            'user_id' => $member->id,
            'accuracy' => 5.0,
            'recorded_at' => $startTime->copy()->addSeconds(12),
            'location' => DB::raw("ST_GeomFromText('POINT(-58.3820 -34.6040)', 4326)"),
            'speed' => 45.0,
            'is_driving' => true,
            'created_at' => Carbon::now(),
            'updated_at' => Carbon::now(),
        ]);

        // Point 3 (Speeding)
        DB::table('location_histories')->insert([
            'user_id' => $member->id,
            'accuracy' => 5.0,
            'recorded_at' => $startTime->copy()->addSeconds(14),
            'location' => DB::raw("ST_GeomFromText('POINT(-58.3830 -34.6050)', 4326)"),
            'speed' => 90.0,
            'is_driving' => true,
            'created_at' => Carbon::now(),
            'updated_at' => Carbon::now(),
        ]);

        // Point 4 (Hard brake)
        DB::table('location_histories')->insert([
            'user_id' => $member->id,
            'accuracy' => 5.0,
            'recorded_at' => $startTime->copy()->addSeconds(16),
            'location' => DB::raw("ST_GeomFromText('POINT(-58.3835 -34.6055)', 4326)"),
            'speed' => 40.0,
            'is_driving' => true,
            'created_at' => Carbon::now(),
            'updated_at' => Carbon::now(),
        ]);

        $response = $this->actingAs($requester)->getJson("/api/circles/{$circle->id}/members/{$member->id}/drives");

        $response->assertStatus(200);
        $data = $response->json();

        $this->assertCount(1, $data['drives']);
        $driveReport = $data['drives'][0];

        $this->assertEquals(100.0, $driveReport['max_speed']);
        $this->assertTrue($driveReport['exceeded_speed_limit']);
        $this->assertGreaterThan(0.0, $driveReport['distance_km']);

        // Check telemetry events
        $events = $driveReport['events'];
        $this->assertCount(1, $events['hard_brakes']);
        $this->assertCount(1, $events['rapid_accelerations']);
        $this->assertCount(1, $events['speeding']);

        // Calculate expected safety score: 100 - (1 hard brake * 5) - (1 rapid accel * 3) - (1 speeding * 10) = 82
        $this->assertEquals(82, $driveReport['safety_score']);
        $this->assertCount(4, $driveReport['route_points']);
    }
}

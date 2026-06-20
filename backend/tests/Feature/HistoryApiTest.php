<?php

namespace Tests\Feature;

use App\Models\Circle;
use App\Models\User;
use Carbon\Carbon;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class HistoryApiTest extends TestCase
{
    use RefreshDatabase;

    protected function setUp(): void
    {
        parent::setUp();
        // PostGIS could be needed, table migrations are loaded automatically
    }

    public function test_unauthenticated_user_cannot_access_history()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id]);
        $member = User::factory()->create();

        $response = $this->getJson("/api/circles/{$circle->id}/members/{$member->id}/history");

        $response->assertStatus(401);
    }

    public function test_user_not_in_circle_cannot_access_history()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id]);
        $member = User::factory()->create();
        $circle->users()->attach($member->id, ['role' => 'member']);

        $stranger = User::factory()->create();

        $response = $this->actingAs($stranger)->getJson("/api/circles/{$circle->id}/members/{$member->id}/history");

        $response->assertStatus(403)
            ->assertJsonFragment(['message' => 'No tienes acceso a este círculo']);
    }

    public function test_standard_user_can_access_history_of_last_24_hours()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id]);
        $requester = User::factory()->create(['is_premium' => false]);
        $member = User::factory()->create();

        $circle->users()->attach($requester->id, ['role' => 'member']);
        $circle->users()->attach($member->id, ['role' => 'member']);

        // Seed 1 point for today
        DB::table('location_histories')->insert([
            'user_id' => $member->id,
            'accuracy' => 10.0,
            'recorded_at' => Carbon::now(),
            'location' => DB::raw("ST_GeomFromText('POINT(-58.3816 -34.6037)', 4326)"),
            'created_at' => Carbon::now(),
            'updated_at' => Carbon::now(),
        ]);

        $response = $this->actingAs($requester)->getJson("/api/circles/{$circle->id}/members/{$member->id}/history?date=" . Carbon::today()->toDateString());

        $response->assertStatus(200);
        $this->assertCount(1, $response->json());
    }

    public function test_standard_user_is_blocked_from_history_older_than_24_hours()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id]);
        $requester = User::factory()->create(['is_premium' => false]);
        $member = User::factory()->create();

        $circle->users()->attach($requester->id, ['role' => 'member']);
        $circle->users()->attach($member->id, ['role' => 'member']);

        // Query date: 2 days ago
        $twoDaysAgo = Carbon::today()->subDays(2);

        $response = $this->actingAs($requester)->getJson("/api/circles/{$circle->id}/members/{$member->id}/history?date=" . $twoDaysAgo->toDateString());

        $response->assertStatus(403)
            ->assertJsonFragment([
                'message' => 'El acceso al historial de más de 24 horas es una funcionalidad exclusiva de cuentas Premium.'
            ]);
    }

    public function test_premium_user_can_access_history_up_to_30_days_ago()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id]);
        $requester = User::factory()->create(['is_premium' => true]);
        $member = User::factory()->create();

        $circle->users()->attach($requester->id, ['role' => 'member']);
        $circle->users()->attach($member->id, ['role' => 'member']);

        $tenDaysAgo = Carbon::today()->subDays(10);

        // Seed 1 point for 10 days ago (will fall into the location_histories_default partition)
        DB::table('location_histories')->insert([
            'user_id' => $member->id,
            'accuracy' => 10.0,
            'recorded_at' => $tenDaysAgo,
            'location' => DB::raw("ST_GeomFromText('POINT(-58.3816 -34.6037)', 4326)"),
            'created_at' => Carbon::now(),
            'updated_at' => Carbon::now(),
        ]);

        $response = $this->actingAs($requester)->getJson("/api/circles/{$circle->id}/members/{$member->id}/history?date=" . $tenDaysAgo->toDateString());

        $response->assertStatus(200);
        $this->assertCount(1, $response->json());
    }

    public function test_premium_user_is_blocked_from_history_older_than_30_days()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id]);
        $requester = User::factory()->create(['is_premium' => true]);
        $member = User::factory()->create();

        $circle->users()->attach($requester->id, ['role' => 'member']);
        $circle->users()->attach($member->id, ['role' => 'member']);

        // Query date: 40 days ago
        $fortyDaysAgo = Carbon::today()->subDays(40);

        $response = $this->actingAs($requester)->getJson("/api/circles/{$circle->id}/members/{$member->id}/history?date=" . $fortyDaysAgo->toDateString());

        $response->assertStatus(403)
            ->assertJsonFragment([
                'message' => 'El historial de ubicaciones solo está disponible para los últimos 30 días.'
            ]);
    }

    public function test_postgis_simplification_logic()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Test Circle', 'owner_id' => $owner->id]);
        $requester = User::factory()->create(['is_premium' => true]);
        $member = User::factory()->create();

        $circle->users()->attach($requester->id, ['role' => 'member']);
        $circle->users()->attach($member->id, ['role' => 'member']);

        // Seed 10 sequential points on a straight line: they should simplify to just start and end points
        for ($i = 0; $i < 10; $i++) {
            $offset = $i * 0.00001; // very small offset (~1m), should be simplified away
            $time = Carbon::today()->startOfDay()->addMinutes($i * 5);
            DB::table('location_histories')->insert([
                'user_id' => $member->id,
                'accuracy' => 10.0,
                'recorded_at' => $time,
                'location' => DB::raw("ST_GeomFromText('POINT(" . (-58.3816 + $offset) . " " . (-34.6037 + $offset) . ")', 4326)"),
                'created_at' => Carbon::now(),
                'updated_at' => Carbon::now(),
            ]);
        }

        $response = $this->actingAs($requester)->getJson("/api/circles/{$circle->id}/members/{$member->id}/history?date=" . Carbon::today()->toDateString());

        $response->assertStatus(200);
        // The simplified output should contain fewer points than 10 because tolerance is ~5.5m and offset was ~1m.
        // It will only keep the start and end points (or very few vertices).
        $this->assertLessThan(10, count($response->json()));
        $this->assertGreaterThanOrEqual(2, count($response->json()));
    }
}

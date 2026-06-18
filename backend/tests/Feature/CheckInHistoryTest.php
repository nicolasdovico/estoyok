<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\CheckIn;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class CheckInHistoryTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_perform_check_in_and_it_creates_check_in_log()
    {
        $user = User::factory()->create();

        $this->assertDatabaseEmpty('check_ins');

        $response = $this->actingAs($user)->postJson('/api/check-in');

        $response->assertStatus(200)
            ->assertJsonStructure(['message', 'last_check_in_at']);

        $this->assertDatabaseHas('check_ins', [
            'user_id' => $user->id,
        ]);
        $this->assertCount(1, $user->checkIns);
    }

    public function test_user_can_retrieve_check_in_history()
    {
        $user = User::factory()->create();
        
        // Create 3 check-ins manually
        $user->checkIns()->create();
        $user->checkIns()->create();
        $user->checkIns()->create();

        $response = $this->actingAs($user)->getJson('/api/check-ins');

        $response->assertStatus(200)
            ->assertJsonCount(3)
            ->assertJsonStructure([
                '*' => ['id', 'user_id', 'created_at', 'updated_at']
            ]);
    }

    public function test_user_retrieves_latest_20_records()
    {
        $user = User::factory()->create();

        // Create 25 check-ins with distinct timestamps
        for ($i = 0; $i < 25; $i++) {
            $checkIn = new CheckIn();
            $checkIn->user_id = $user->id;
            $checkIn->created_at = now()->subMinutes(25 - $i);
            $checkIn->timestamps = false;
            $checkIn->save();
        }

        $response = $this->actingAs($user)->getJson('/api/check-ins');

        $response->assertStatus(200)
            ->assertJsonCount(20);

        // Assert that the first returned check-in is indeed the latest one
        $this->assertEquals($user->checkIns()->latest()->first()->id, $response->json()[0]['id']);
    }

    public function test_user_cannot_retrieve_check_in_history_unauthenticated()
    {
        $response = $this->getJson('/api/check-ins');

        $response->assertStatus(401);
    }
}

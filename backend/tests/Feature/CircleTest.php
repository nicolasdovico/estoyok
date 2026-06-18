<?php

namespace Tests\Feature;

use App\Models\Circle;
use App\Models\User;
use App\Models\Geofence;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class CircleTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_list_their_circles()
    {
        $user = User::factory()->create();
        $circle = Circle::create(['name' => 'Familia A', 'owner_id' => $user->id]);
        $circle->users()->attach($user->id, ['role' => 'admin']);

        $response = $this->actingAs($user)->getJson('/api/circles');

        $response->assertStatus(200)
            ->assertJsonCount(1)
            ->assertJsonFragment(['name' => 'Familia A']);
    }

    public function test_user_can_create_a_circle()
    {
        $user = User::factory()->create();

        $response = $this->actingAs($user)->postJson('/api/circles', [
            'name' => 'Mi Nuevo Circulo',
        ]);

        $response->assertStatus(201)
            ->assertJsonFragment(['name' => 'Mi Nuevo Circulo']);

        $this->assertDatabaseHas('circles', [
            'name' => 'Mi Nuevo Circulo',
            'owner_id' => $user->id,
        ]);

        $circle = Circle::first();
        $this->assertDatabaseHas('circle_user', [
            'circle_id' => $circle->id,
            'user_id' => $user->id,
            'role' => 'admin',
        ]);
    }

    public function test_user_can_join_a_circle_with_valid_code()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Circulo de Prueba', 'owner_id' => $owner->id]);
        $circle->users()->attach($owner->id, ['role' => 'admin']);

        $inviteCode = $circle->invite_code;

        $user = User::factory()->create();

        $response = $this->actingAs($user)->postJson('/api/circles/join', [
            'invite_code' => $inviteCode,
        ]);

        $response->assertStatus(200)
            ->assertJsonFragment(['name' => 'Circulo de Prueba']);

        $this->assertDatabaseHas('circle_user', [
            'circle_id' => $circle->id,
            'user_id' => $user->id,
            'role' => 'member',
        ]);
    }

    public function test_user_cannot_join_with_invalid_code()
    {
        $user = User::factory()->create();

        $response = $this->actingAs($user)->postJson('/api/circles/join', [
            'invite_code' => 'INVALID123',
        ]);

        $response->assertStatus(404);
    }

    public function test_user_cannot_join_circle_twice()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Circulo de Prueba', 'owner_id' => $owner->id]);
        $circle->users()->attach($owner->id, ['role' => 'admin']);

        $user = User::factory()->create();
        $circle->users()->attach($user->id, ['role' => 'member']);

        $response = $this->actingAs($user)->postJson('/api/circles/join', [
            'invite_code' => $circle->invite_code,
        ]);

        $response->assertStatus(400)
            ->assertJsonFragment(['message' => 'Ya perteneces a este círculo']);
    }

    public function test_user_can_leave_circle()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Circulo de Prueba', 'owner_id' => $owner->id]);
        $circle->users()->attach($owner->id, ['role' => 'admin']);

        $user = User::factory()->create();
        $circle->users()->attach($user->id, ['role' => 'member']);

        $response = $this->actingAs($user)->deleteJson("/api/circles/{$circle->id}/members/{$user->id}");

        $response->assertStatus(200)
            ->assertJsonFragment(['message' => 'Has abandonado el círculo exitosamente.']);

        $this->assertDatabaseMissing('circle_user', [
            'circle_id' => $circle->id,
            'user_id' => $user->id,
        ]);
    }

    public function test_owner_leaving_deletes_circle()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Circulo de Prueba', 'owner_id' => $owner->id]);
        $circle->users()->attach($owner->id, ['role' => 'admin']);

        $response = $this->actingAs($owner)->deleteJson("/api/circles/{$circle->id}/members/{$owner->id}");

        $response->assertStatus(200);

        $this->assertDatabaseMissing('circles', [
            'id' => $circle->id,
        ]);
    }

    public function test_admin_can_remove_member()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Circulo de Prueba', 'owner_id' => $owner->id]);
        $circle->users()->attach($owner->id, ['role' => 'admin']);

        $user = User::factory()->create();
        $circle->users()->attach($user->id, ['role' => 'member']);

        $response = $this->actingAs($owner)->deleteJson("/api/circles/{$circle->id}/members/{$user->id}");

        $response->assertStatus(200)
            ->assertJsonFragment(['message' => 'Miembro eliminado exitosamente.']);

        $this->assertDatabaseMissing('circle_user', [
            'circle_id' => $circle->id,
            'user_id' => $user->id,
        ]);
    }

    public function test_non_admin_cannot_remove_member()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Circulo de Prueba', 'owner_id' => $owner->id]);
        $circle->users()->attach($owner->id, ['role' => 'admin']);

        $userA = User::factory()->create();
        $userB = User::factory()->create();
        $circle->users()->attach($userA->id, ['role' => 'member']);
        $circle->users()->attach($userB->id, ['role' => 'member']);

        $response = $this->actingAs($userA)->deleteJson("/api/circles/{$circle->id}/members/{$userB->id}");

        $response->assertStatus(403);

        $this->assertDatabaseHas('circle_user', [
            'circle_id' => $circle->id,
            'user_id' => $userB->id,
        ]);
    }

    public function test_owner_can_delete_circle()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Circulo de Prueba', 'owner_id' => $owner->id]);
        $circle->users()->attach($owner->id, ['role' => 'admin']);

        $response = $this->actingAs($owner)->deleteJson("/api/circles/{$circle->id}");

        $response->assertStatus(200);

        $this->assertDatabaseMissing('circles', [
            'id' => $circle->id,
        ]);
    }

    public function test_non_owner_cannot_delete_circle()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Circulo de Prueba', 'owner_id' => $owner->id]);
        $circle->users()->attach($owner->id, ['role' => 'admin']);

        $user = User::factory()->create();
        $circle->users()->attach($user->id, ['role' => 'member']);

        $response = $this->actingAs($user)->deleteJson("/api/circles/{$circle->id}");

        $response->assertStatus(403);

        $this->assertDatabaseHas('circles', [
            'id' => $circle->id,
        ]);
    }
}

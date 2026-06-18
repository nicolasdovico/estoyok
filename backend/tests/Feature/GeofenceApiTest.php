<?php

namespace Tests\Feature;

use App\Models\Circle;
use App\Models\Geofence;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class GeofenceApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_list_geofences_in_their_circle()
    {
        $user = User::factory()->create();
        $circle = Circle::create(['name' => 'Circulo A', 'owner_id' => $user->id]);
        $circle->users()->attach($user->id, ['role' => 'admin']);

        Geofence::create([
            'circle_id' => $circle->id,
            'name' => 'Casa',
            'radius' => 150,
            'type' => 'entry_exit',
            'center' => DB::raw("ST_GeomFromText('POINT(-58.3816 -34.6037)', 4326)"),
        ]);

        $response = $this->actingAs($user)->getJson("/api/circles/{$circle->id}/geofences");

        $response->assertStatus(200)
            ->assertJsonCount(1)
            ->assertJsonFragment(['name' => 'Casa'])
            ->assertJsonFragment(['latitude' => -34.6037])
            ->assertJsonFragment(['longitude' => -58.3816]);
    }

    public function test_user_cannot_list_geofences_if_not_in_circle()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Circulo A', 'owner_id' => $owner->id]);
        $circle->users()->attach($owner->id, ['role' => 'admin']);

        $user = User::factory()->create();

        $response = $this->actingAs($user)->getJson("/api/circles/{$circle->id}/geofences");

        $response->assertStatus(403);
    }

    public function test_user_can_create_geofence_for_entire_circle()
    {
        $user = User::factory()->create();
        $circle = Circle::create(['name' => 'Circulo A', 'owner_id' => $user->id]);
        $circle->users()->attach($user->id, ['role' => 'admin']);

        $response = $this->actingAs($user)->postJson("/api/circles/{$circle->id}/geofences", [
            'name' => 'Escuela',
            'radius' => 200,
            'type' => 'entry',
            'latitude' => -34.6037,
            'longitude' => -58.3816,
        ]);

        $response->assertStatus(201)
            ->assertJsonFragment(['name' => 'Escuela', 'user_id' => null]);

        $this->assertDatabaseHas('geofences', [
            'circle_id' => $circle->id,
            'name' => 'Escuela',
            'radius' => 200,
            'type' => 'entry',
            'user_id' => null,
        ]);
    }

    public function test_user_can_create_custom_member_geofence()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Circulo A', 'owner_id' => $owner->id]);
        $circle->users()->attach($owner->id, ['role' => 'admin']);

        $member = User::factory()->create();
        $circle->users()->attach($member->id, ['role' => 'member']);

        $response = $this->actingAs($owner)->postJson("/api/circles/{$circle->id}/geofences", [
            'name' => 'Trabajo de Member',
            'radius' => 300,
            'type' => 'exit',
            'latitude' => -34.1234,
            'longitude' => -58.5678,
            'user_id' => $member->id,
        ]);

        $response->assertStatus(201)
            ->assertJsonFragment(['name' => 'Trabajo de Member', 'user_id' => $member->id]);

        $this->assertDatabaseHas('geofences', [
            'circle_id' => $circle->id,
            'name' => 'Trabajo de Member',
            'user_id' => $member->id,
        ]);
    }

    public function test_cannot_create_geofence_for_non_member()
    {
        $owner = User::factory()->create();
        $circle = Circle::create(['name' => 'Circulo A', 'owner_id' => $owner->id]);
        $circle->users()->attach($owner->id, ['role' => 'admin']);

        $stranger = User::factory()->create();

        $response = $this->actingAs($owner)->postJson("/api/circles/{$circle->id}/geofences", [
            'name' => 'Geocerca Invalida',
            'radius' => 300,
            'type' => 'exit',
            'latitude' => -34.1234,
            'longitude' => -58.5678,
            'user_id' => $stranger->id,
        ]);

        $response->assertStatus(400)
            ->assertJsonFragment(['message' => 'El usuario especificado no pertenece a este círculo']);
    }

    public function test_user_can_update_geofence()
    {
        $user = User::factory()->create();
        $circle = Circle::create(['name' => 'Circulo A', 'owner_id' => $user->id]);
        $circle->users()->attach($user->id, ['role' => 'admin']);

        $geofence = Geofence::create([
            'circle_id' => $circle->id,
            'name' => 'Casa original',
            'radius' => 150,
            'type' => 'entry_exit',
            'center' => DB::raw("ST_GeomFromText('POINT(-58.3816 -34.6037)', 4326)"),
        ]);

        $response = $this->actingAs($user)->putJson("/api/geofences/{$geofence->id}", [
            'name' => 'Casa Modificada',
            'radius' => 250,
        ]);

        $response->assertStatus(200)
            ->assertJsonFragment(['name' => 'Casa Modificada', 'radius' => 250]);

        $this->assertDatabaseHas('geofences', [
            'id' => $geofence->id,
            'name' => 'Casa Modificada',
            'radius' => 250,
        ]);
    }

    public function test_user_can_delete_geofence()
    {
        $user = User::factory()->create();
        $circle = Circle::create(['name' => 'Circulo A', 'owner_id' => $user->id]);
        $circle->users()->attach($user->id, ['role' => 'admin']);

        $geofence = Geofence::create([
            'circle_id' => $circle->id,
            'name' => 'Por eliminar',
            'radius' => 150,
            'type' => 'entry_exit',
            'center' => DB::raw("ST_GeomFromText('POINT(-58.3816 -34.6037)', 4326)"),
        ]);

        $response = $this->actingAs($user)->deleteJson("/api/geofences/{$geofence->id}");

        $response->assertStatus(200);

        $this->assertDatabaseMissing('geofences', [
            'id' => $geofence->id,
        ]);
    }
}

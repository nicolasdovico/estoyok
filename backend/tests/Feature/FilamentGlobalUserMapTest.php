<?php

namespace Tests\Feature;

use App\Models\CurrentLocation;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class FilamentGlobalUserMapTest extends TestCase
{
    use RefreshDatabase;

    public function test_guest_cannot_access_global_user_map(): void
    {
        $response = $this->get('/admin/mapa-global');

        $response->assertRedirect('/admin/login');
    }

    public function test_non_admin_user_cannot_access_global_user_map(): void
    {
        $regularUser = User::factory()->create([
            'email' => 'regular@example.com',
        ]);

        $response = $this->actingAs($regularUser)->get('/admin/mapa-global');

        $response->assertForbidden();
    }

    public function test_admin_user_can_access_global_user_map(): void
    {
        $admin = User::factory()->create([
            'name' => 'Admin Nicolás',
            'email' => 'nicolasdovico@gmail.com',
        ]);

        $tester1 = User::factory()->create([
            'name' => 'Tester Europa',
            'email' => 'tester.eu@example.com',
        ]);

        CurrentLocation::create([
            'user_id' => $tester1->id,
            'location' => DB::raw("ST_SetSRID(ST_MakePoint(2.3522, 48.8566), 4326)"), // París
            'accuracy' => 10.0,
            'battery_level' => 90.0,
            'is_tracking_active' => true,
            'recorded_at' => now(),
            'last_seen_at' => now(),
        ]);

        $tester2 = User::factory()->create([
            'name' => 'Tester Argentina',
            'email' => 'tester.ar@example.com',
        ]);

        CurrentLocation::create([
            'user_id' => $tester2->id,
            'location' => DB::raw("ST_SetSRID(ST_MakePoint(-58.3816, -34.6037), 4326)"), // Buenos Aires
            'accuracy' => 15.0,
            'battery_level' => 75.0,
            'is_tracking_active' => true,
            'recorded_at' => now(),
            'last_seen_at' => now(),
        ]);

        $response = $this->actingAs($admin)->get('/admin/mapa-global');

        $response->assertOk();
        $response->assertSee('Mapa Global de Conexiones');
        $response->assertSee('Tester Europa');
        $response->assertSee('Tester Argentina');
        $response->assertSee('48.8566');
        $response->assertSee('-34.6037');
    }
}

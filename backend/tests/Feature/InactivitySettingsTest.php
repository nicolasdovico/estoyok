<?php

namespace Tests\Feature;

use App\Models\EmergencyContact;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class InactivitySettingsTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_list_emergency_contacts()
    {
        $user = User::factory()->create();
        EmergencyContact::factory()->count(3)->create(['user_id' => $user->id]);

        $response = $this->actingAs($user)->getJson('/api/emergency-contacts');

        $response->assertStatus(200)
            ->assertJsonCount(3);
    }

    public function test_user_can_create_emergency_contact()
    {
        $user = User::factory()->create();
        $data = [
            'name' => 'Mamá',
            'phone' => '+541122334455',
            'relationship' => 'Madre',
            'email' => 'mama@example.com',
        ];

        $response = $this->actingAs($user)->postJson('/api/emergency-contacts', $data);

        $response->assertStatus(201)
            ->assertJsonFragment(['name' => 'Mamá', 'relationship' => 'Madre']);

        $this->assertDatabaseHas('emergency_contacts', ['user_id' => $user->id, 'name' => 'Mamá']);
    }

    public function test_user_can_update_checkin_interval()
    {
        $user = User::factory()->create(['checkin_interval_hours' => 24]);

        $response = $this->actingAs($user)->putJson('/api/settings/checkin-interval', [
            'checkin_interval_hours' => 12,
        ]);

        $response->assertStatus(200)
            ->assertJsonFragment(['checkin_interval_hours' => 12]);

        $this->assertEquals(12, $user->fresh()->checkin_interval_hours);
    }

    public function test_user_cannot_update_others_contacts()
    {
        $user1 = User::factory()->create();
        $user2 = User::factory()->create();
        $contact = EmergencyContact::factory()->create(['user_id' => $user1->id]);

        $response = $this->actingAs($user2)->putJson("/api/emergency-contacts/{$contact->id}", [
            'name' => 'Hacker',
        ]);

        $response->assertStatus(403);
    }
}

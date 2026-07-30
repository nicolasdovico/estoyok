<?php

namespace Tests\Feature;

use App\Models\Circle;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Cache;
use Tests\TestCase;

class CircleMemberReminderTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_send_reminder_push_to_circle_member()
    {
        $user1 = User::factory()->create(['expo_push_token' => 'ExponentPushToken[TokenUser1]']);
        $user2 = User::factory()->create(['expo_push_token' => 'ExponentPushToken[TokenUser2]']);

        $circle = Circle::create([
            'name' => 'Familia Test',
            'owner_id' => $user1->id,
        ]);
        $circle->users()->attach($user1->id, ['role' => 'admin']);
        $circle->users()->attach($user2->id, ['role' => 'member']);

        $this->actingAs($user1);

        $response = $this->postJson("/api/circles/{$circle->id}/members/{$user2->id}/remind");

        $response->assertStatus(200)
                 ->assertJson(['message' => "Recordatorio enviado exitosamente a {$user2->name}"]);
    }

    public function test_remind_member_cooldown_prevents_spam()
    {
        $user1 = User::factory()->create(['expo_push_token' => 'ExponentPushToken[TokenUser1]']);
        $user2 = User::factory()->create(['expo_push_token' => 'ExponentPushToken[TokenUser2]']);

        $circle = Circle::create([
            'name' => 'Familia Test',
            'owner_id' => $user1->id,
        ]);
        $circle->users()->attach($user1->id, ['role' => 'admin']);
        $circle->users()->attach($user2->id, ['role' => 'member']);

        $this->actingAs($user1);

        $this->postJson("/api/circles/{$circle->id}/members/{$user2->id}/remind")->assertStatus(200);

        // Second call should return 429 Rate Limited
        $secondResponse = $this->postJson("/api/circles/{$circle->id}/members/{$user2->id}/remind");
        $secondResponse->assertStatus(429);
    }
}

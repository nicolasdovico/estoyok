<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class DisclaimerAcceptanceTest extends TestCase
{
    use RefreshDatabase;

    public function test_authenticated_user_can_accept_disclaimer(): void
    {
        $user = User::factory()->create([
            'disclaimer_accepted_at' => null,
        ]);

        $this->assertNull($user->disclaimer_accepted_at);

        $response = $this->actingAs($user, 'sanctum')
            ->postJson('/api/settings/accept-disclaimer');

        $response->assertStatus(200)
            ->assertJsonStructure([
                'message',
                'disclaimer_accepted_at',
                'user',
            ]);

        $this->assertNotNull($user->fresh()->disclaimer_accepted_at);
    }
}

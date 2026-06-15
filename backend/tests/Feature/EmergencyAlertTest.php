<?php

namespace Tests\Feature;

use App\Jobs\SendInactivityAlerts;
use App\Models\CurrentLocation;
use App\Models\EmergencyAlert;
use App\Models\User;
use App\Services\WhatsAppServiceInterface;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Str;
use Mockery;
use Tests\TestCase;

class EmergencyAlertTest extends TestCase
{
    use RefreshDatabase;

    public function test_inactivity_job_creates_emergency_alert()
    {
        $user = User::factory()->create(['name' => 'John Doe']);
        $whatsAppService = Mockery::mock(WhatsAppServiceInterface::class);
        $whatsAppService->shouldReceive('sendWhatsApp')->andReturn(true);

        $job = new SendInactivityAlerts($user);
        $job->handle($whatsAppService);

        $this->assertDatabaseHas('emergency_alerts', [
            'user_id' => $user->id,
            'type' => 'inactivity',
            'status' => 'active',
        ]);
    }

    public function test_can_fetch_emergency_alert_details()
    {
        $user = User::factory()->create(['name' => 'John Doe', 'last_check_in_at' => now()]);
        $alert = EmergencyAlert::create([
            'user_id' => $user->id,
            'type' => 'inactivity',
            'status' => 'active',
            'expires_at' => now()->addHours(1),
        ]);

        CurrentLocation::create([
            'user_id' => $user->id,
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'location' => 'POINT(-58.3816 -34.6037)',
            'recorded_at' => now(),
        ]);

        $response = $this->getJson("/api/emergency-alerts/{$alert->id}");

        $response->assertStatus(200)
            ->assertJson([
                'user_name' => 'John Doe',
                'status' => 'active',
                'location' => [
                    'latitude' => -34.6037,
                    'longitude' => -58.3816,
                ],
            ]);
    }

    public function test_returns_404_for_invalid_alert()
    {
        $uuid = (string) Str::uuid();
        $response = $this->getJson("/api/emergency-alerts/{$uuid}");
        $response->assertStatus(404);
    }

    public function test_returns_404_for_expired_alert()
    {
        $user = User::factory()->create();
        $alert = EmergencyAlert::create([
            'user_id' => $user->id,
            'expires_at' => now()->subHour(),
        ]);

        $response = $this->getJson("/api/emergency-alerts/{$alert->id}");
        $response->assertStatus(404)
            ->assertJson(['message' => 'Alert link expired']);
    }

    public function test_checkin_resolves_active_alerts()
    {
        $user = User::factory()->create();
        $alert1 = EmergencyAlert::create([
            'user_id' => $user->id,
            'type' => 'inactivity',
            'status' => 'active',
            'expires_at' => now()->addHours(24),
        ]);
        $alert2 = EmergencyAlert::create([
            'user_id' => $user->id,
            'type' => 'inactivity',
            'status' => 'active',
            'expires_at' => now()->addHours(24),
        ]);

        $this->actingAs($user)
            ->postJson('/api/check-in')
            ->assertStatus(200);

        $this->assertEquals('resolved', $alert1->fresh()->status);
        $this->assertEquals('resolved', $alert2->fresh()->status);
    }

    public function test_resolved_alert_hides_location()
    {
        $user = User::factory()->create(['name' => 'John Doe', 'last_check_in_at' => now()]);
        $alert = EmergencyAlert::create([
            'user_id' => $user->id,
            'type' => 'inactivity',
            'status' => 'resolved',
            'expires_at' => now()->addHours(1),
        ]);

        CurrentLocation::create([
            'user_id' => $user->id,
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'location' => 'POINT(-58.3816 -34.6037)',
            'recorded_at' => now(),
        ]);

        $response = $this->getJson("/api/emergency-alerts/{$alert->id}");

        $response->assertStatus(200)
            ->assertJson([
                'user_name' => 'John Doe',
                'status' => 'resolved',
                'location' => null,
            ]);
    }
}

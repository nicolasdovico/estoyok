<?php

namespace Tests\Feature;

use App\Jobs\SendInactivityAlerts;
use App\Models\EmergencyContact;
use App\Models\EmergencyAlert;
use App\Models\User;
use App\Services\WhatsAppServiceInterface;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Queue;
use Illuminate\Support\Facades\Http;
use Mockery;
use Tests\TestCase;

class InactivityEscalationTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_update_escalation_settings()
    {
        $user = User::factory()->create();

        $response = $this->actingAs($user)->putJson('/api/settings/escalation', [
            'escalation_enabled' => true,
            'escalation_interval_minutes' => 25,
        ]);

        $response->assertStatus(200)
            ->assertJson([
                'message' => 'Escalation settings updated successfully',
                'escalation_enabled' => true,
                'escalation_interval_minutes' => 25,
            ]);

        $user->refresh();
        $this->assertTrue($user->escalation_enabled);
        $this->assertEquals(25, $user->escalation_interval_minutes);
    }

    public function test_user_can_reorder_emergency_contacts()
    {
        $user = User::factory()->create();

        $c1 = EmergencyContact::create(['user_id' => $user->id, 'name' => 'Contact A', 'phone' => '+111111111', 'priority' => 1]);
        $c2 = EmergencyContact::create(['user_id' => $user->id, 'name' => 'Contact B', 'phone' => '+222222222', 'priority' => 2]);
        $c3 = EmergencyContact::create(['user_id' => $user->id, 'name' => 'Contact C', 'phone' => '+333333333', 'priority' => 3]);

        // Reorder to: C (id: 3), A (id: 1), B (id: 2)
        $response = $this->actingAs($user)->postJson('/api/emergency-contacts/reorder', [
            'ids' => [$c3->id, $c1->id, $c2->id]
        ]);

        $response->assertStatus(200);

        // Verify priorities in database
        $this->assertEquals(1, $c3->fresh()->priority);
        $this->assertEquals(2, $c1->fresh()->priority);
        $this->assertEquals(3, $c2->fresh()->priority);

        // Verify that GET /api/emergency-contacts returns them in order
        $indexResponse = $this->actingAs($user)->getJson('/api/emergency-contacts');
        $indexResponse->assertStatus(200);
        $this->assertEquals($c3->id, $indexResponse->json()[0]['id']);
        $this->assertEquals($c1->id, $indexResponse->json()[1]['id']);
        $this->assertEquals($c2->id, $indexResponse->json()[2]['id']);
    }

    public function test_escalation_notifies_first_contact_and_queues_second_delayed()
    {
        Queue::fake();
        Http::fake();

        $user = User::factory()->create([
            'is_premium' => true,
            'escalation_enabled' => true,
            'escalation_interval_minutes' => 10,
        ]);

        $c1 = EmergencyContact::create(['user_id' => $user->id, 'name' => 'Jane Doe', 'phone' => '+5491122334455', 'email' => 'jane@example.com', 'priority' => 1, 'is_active' => true]);
        $c2 = EmergencyContact::create(['user_id' => $user->id, 'name' => 'John Smith', 'phone' => '+5491166778899', 'email' => 'john@example.com', 'priority' => 2, 'is_active' => true]);

        // Mock WhatsApp Service
        $this->mock(WhatsAppServiceInterface::class, function ($mock) use ($c1) {
            $mock->shouldReceive('sendWhatsApp')
                ->once()
                ->with($c1->phone, Mockery::any())
                ->andReturn(true);
        });

        // Execute initial job
        $job = new SendInactivityAlerts($user);
        $job->handle(app(WhatsAppServiceInterface::class));

        // Assert emergency alert record was created
        $this->assertDatabaseHas('emergency_alerts', [
            'user_id' => $user->id,
            'status' => 'active',
        ]);

        $alert = EmergencyAlert::where('user_id', $user->id)->first();

        // Assert the next job was enqueued with 10 minutes delay
        Queue::assertPushed(SendInactivityAlerts::class, function ($job) use ($user, $alert) {
            return $job->user->id === $user->id 
                && $job->emergencyAlertId === $alert->id 
                && $job->contactIndex === 1
                && $job->delay instanceof \DateTimeInterface 
                && abs(now()->diffInMinutes($job->delay) - 10) <= 1;
        });
    }

    public function test_escalation_stops_if_alert_is_resolved()
    {
        Queue::fake();
        Http::fake();

        $user = User::factory()->create([
            'is_premium' => true,
            'escalation_enabled' => true,
        ]);

        $c1 = EmergencyContact::create(['user_id' => $user->id, 'name' => 'Jane Doe', 'phone' => '+5491122334455', 'priority' => 1, 'is_active' => true]);

        $alert = EmergencyAlert::create([
            'user_id' => $user->id,
            'status' => 'resolved', // Resolved!
            'type' => 'inactivity',
        ]);

        $this->mock(WhatsAppServiceInterface::class, function ($mock) {
            $mock->shouldNotReceive('sendWhatsApp');
            $mock->shouldNotReceive('sendSMS');
        });

        // Execute job for step 1 (second contact)
        $job = new SendInactivityAlerts($user, $alert->id, 1);
        $job->handle(app(WhatsAppServiceInterface::class));

        // It should abort immediately without checking in or queuing more
        Queue::assertNotPushed(SendInactivityAlerts::class);
    }

    public function test_verify_inactivity_command_ignores_users_with_active_alerts()
    {
        Queue::fake();

        $user = User::factory()->create([
            'last_check_in_at' => now()->subHours(25),
            'checkin_interval_hours' => 24,
        ]);

        // Create an active emergency alert for this user
        EmergencyAlert::create([
            'user_id' => $user->id,
            'status' => 'active',
            'type' => 'inactivity',
        ]);

        // Run verify command
        $this->artisan('checkins:verify-inactivity');

        // It should NOT push the SendInactivityAlerts job since they already have an active alert
        Queue::assertNotPushed(SendInactivityAlerts::class);
    }
}

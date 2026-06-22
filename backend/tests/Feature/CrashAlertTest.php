<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\CrashEvent;
use App\Models\EmergencyAlert;
use App\Models\EmergencyContact;
use App\Services\WhatsAppServiceInterface;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Http;
use Mockery;
use Tests\TestCase;

class CrashAlertTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_report_crash_event_dispatches_alerts()
    {
        // 1. Create a user (premium so both SMS and WhatsApp are sent)
        $user = User::factory()->create([
            'is_premium' => true,
            'name' => 'John Doe',
        ]);

        $contact = EmergencyContact::create([
            'user_id' => $user->id,
            'name' => 'Jane Contact',
            'phone' => '+5491122334455',
            'is_active' => true,
        ]);

        // 2. Fake Expo Push Notification API
        Http::fake([
            'https://exp.host/--/api/v2/push/send' => Http::response(['status' => 'ok'], 200),
        ]);

        // 3. Mock Twilio/WhatsApp Service
        $this->mock(WhatsAppServiceInterface::class, function ($mock) use ($contact) {
            // Should send both SMS and WhatsApp because the user is premium
            $mock->shouldReceive('sendSMS')
                ->once()
                ->with($contact->phone, Mockery::pattern('/ALERTA DE ACCIDENTE/'))
                ->andReturn(true);

            $mock->shouldReceive('sendWhatsApp')
                ->once()
                ->with($contact->phone, Mockery::pattern('/ALERTA DE ACCIDENTE/'))
                ->andReturn(true);
        });

        // 4. Request the crash endpoint
        $response = $this->actingAs($user)->postJson('/api/alerts/crash', [
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'speed' => 60.5,
            'g_force' => 4.8,
        ]);

        $response->assertStatus(201);
        $response->assertJsonStructure([
            'message',
            'crash_event' => ['id', 'user_id', 'latitude', 'longitude', 'speed_at_impact', 'g_force', 'status'],
            'emergency_alert' => ['id', 'user_id', 'type', 'status'],
        ]);

        // 5. Verify database records
        $this->assertDatabaseHas('crash_events', [
            'user_id' => $user->id,
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'speed_at_impact' => 60.5,
            'g_force' => 4.8,
            'status' => 'confirmed',
        ]);

        $this->assertDatabaseHas('emergency_alerts', [
            'user_id' => $user->id,
            'type' => 'crash',
            'status' => 'active',
        ]);
    }

    public function test_user_can_resolve_crash_event_as_false_alarm()
    {
        $user = User::factory()->create();

        // 1. Pre-create a crash event and an active crash emergency alert
        $crashEvent = CrashEvent::create([
            'user_id' => $user->id,
            'latitude' => -34.6037,
            'longitude' => -58.3816,
            'speed_at_impact' => 70,
            'g_force' => 5.2,
            'status' => 'confirmed',
        ]);

        $emergencyAlert = EmergencyAlert::create([
            'user_id' => $user->id,
            'type' => 'crash',
            'status' => 'active',
            'expires_at' => now()->addHours(48),
        ]);

        // 2. Call the false-alarm endpoint
        $response = $this->actingAs($user)->postJson("/api/alerts/crash/{$crashEvent->id}/false-alarm");

        $response->assertStatus(200);
        $response->assertJsonFragment([
            'status' => 'false_alarm',
        ]);

        // 3. Verify status changes in database
        $this->assertEquals('false_alarm', $crashEvent->fresh()->status);
        $this->assertEquals('resolved', $emergencyAlert->fresh()->status);
    }
}

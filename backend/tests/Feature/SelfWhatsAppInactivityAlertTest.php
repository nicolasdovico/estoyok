<?php

namespace Tests\Feature;

use App\Jobs\SendInactivityAlerts;
use App\Models\EmergencyAlert;
use App\Models\EmergencyContact;
use App\Models\User;
use App\Services\WhatsAppServiceInterface;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Http;
use Mockery;
use Tests\TestCase;

class SelfWhatsAppInactivityAlertTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_cannot_enable_notify_self_whatsapp_without_phone()
    {
        $user = User::factory()->create([
            'phone' => null,
            'notify_self_whatsapp_on_inactivity' => false,
        ]);

        $response = $this->actingAs($user)->putJson('/api/settings/notify-self-whatsapp', [
            'notify_self_whatsapp_on_inactivity' => true,
        ]);

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['phone']);

        $this->assertFalse($user->fresh()->notify_self_whatsapp_on_inactivity);
    }

    public function test_user_can_enable_and_disable_notify_self_whatsapp_when_phone_registered()
    {
        $user = User::factory()->create([
            'phone' => '+5491155554444',
            'notify_self_whatsapp_on_inactivity' => false,
        ]);

        $response = $this->actingAs($user)->putJson('/api/settings/notify-self-whatsapp', [
            'notify_self_whatsapp_on_inactivity' => true,
        ]);

        $response->assertStatus(200)
            ->assertJsonFragment([
                'notify_self_whatsapp_on_inactivity' => true,
            ]);

        $this->assertTrue($user->fresh()->notify_self_whatsapp_on_inactivity);

        // Can disable
        $response = $this->actingAs($user)->putJson('/api/settings/notify-self-whatsapp', [
            'notify_self_whatsapp_on_inactivity' => false,
        ]);

        $response->assertStatus(200)
            ->assertJsonFragment([
                'notify_self_whatsapp_on_inactivity' => false,
            ]);

        $this->assertFalse($user->fresh()->notify_self_whatsapp_on_inactivity);
    }

    public function test_inactivity_job_sends_whatsapp_to_user_themselves_when_enabled()
    {
        $user = User::factory()->create([
            'phone' => '+5491155554444',
            'notify_self_whatsapp_on_inactivity' => true,
            'checkin_interval_hours' => 24,
            'is_premium' => false,
        ]);

        Http::fake([
            'https://exp.host/--/api/v2/push/send' => Http::response(['status' => 'ok'], 200),
        ]);

        $this->mock(WhatsAppServiceInterface::class, function ($mock) use ($user) {
            $mock->shouldReceive('sendWhatsApp')
                ->once()
                ->with(
                    $user->phone,
                    Mockery::pattern('/reporte diario de bienestar ha vencido/')
                )
                ->andReturn(true);
        });

        $job = new SendInactivityAlerts($user);
        $job->handle(app(WhatsAppServiceInterface::class));

        $this->assertTrue(true);
    }

    public function test_inactivity_job_does_not_send_self_whatsapp_when_disabled()
    {
        $user = User::factory()->create([
            'phone' => '+5491155554444',
            'notify_self_whatsapp_on_inactivity' => false,
            'is_premium' => false,
        ]);

        $this->mock(WhatsAppServiceInterface::class, function ($mock) {
            $mock->shouldNotReceive('sendWhatsApp');
        });

        $job = new SendInactivityAlerts($user);
        $job->handle(app(WhatsAppServiceInterface::class));

        $this->assertTrue(true);
    }

    public function test_inactivity_job_does_not_repeat_self_alert_on_escalation_steps()
    {
        $user = User::factory()->create([
            'phone' => '+5491155554444',
            'notify_self_whatsapp_on_inactivity' => true,
            'escalation_enabled' => true,
            'is_premium' => false,
        ]);

        $alert = EmergencyAlert::create([
            'user_id' => $user->id,
            'type' => 'inactivity',
            'status' => 'active',
            'expires_at' => now()->addHours(48),
        ]);

        EmergencyContact::create([
            'user_id' => $user->id,
            'name' => 'Contacto 1',
            'email' => 'c1@example.com',
            'phone' => '+5491111111111',
            'priority' => 1,
            'is_active' => true,
        ]);

        EmergencyContact::create([
            'user_id' => $user->id,
            'name' => 'Contacto 2',
            'email' => 'c2@example.com',
            'phone' => '+5491122222222',
            'priority' => 2,
            'is_active' => true,
        ]);

        // contactIndex 1 should NOT send WhatsApp to user
        $this->mock(WhatsAppServiceInterface::class, function ($mock) use ($user) {
            $mock->shouldNotReceive('sendWhatsApp')
                ->with($user->phone, Mockery::any());
        });

        $job = new SendInactivityAlerts($user, $alert->id, 1);
        $job->handle(app(WhatsAppServiceInterface::class));

        $this->assertTrue(true);
    }
}

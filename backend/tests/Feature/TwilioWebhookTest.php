<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\EmergencyAlert;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class TwilioWebhookTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_not_found_returns_empty_xml()
    {
        $response = $this->post('/api/webhooks/twilio/message', [
            'From' => '+12345678900',
            'Body' => 'OK',
        ]);

        $response->assertStatus(200);
        $response->assertHeader('Content-Type', 'text/xml; charset=UTF-8');
        $this->assertEquals(
            '<?xml version="1.0" encoding="UTF-8"?><Response></Response>',
            $response->getContent()
        );
    }

    public function test_user_not_verified_returns_empty_xml()
    {
        $user = User::factory()->create([
            'phone' => '+5491122334455',
            'email_verified_at' => null,
            'allow_sms_whatsapp_checkin' => true,
        ]);

        $response = $this->post('/api/webhooks/twilio/message', [
            'From' => '+5491122334455',
            'Body' => 'OK',
        ]);

        $response->assertStatus(200);
        $this->assertEquals(
            '<?xml version="1.0" encoding="UTF-8"?><Response></Response>',
            $response->getContent()
        );
    }

    public function test_user_has_feature_disabled_returns_help_message()
    {
        $user = User::factory()->create([
            'phone' => '+5491122334455',
            'email_verified_at' => now(),
            'allow_sms_whatsapp_checkin' => false,
        ]);

        $response = $this->post('/api/webhooks/twilio/message', [
            'From' => '+5491122334455',
            'Body' => 'OK',
        ]);

        $response->assertStatus(200);
        $this->assertStringContainsString(
            'La opción de Check-in por SMS/WhatsApp no está activa en tu cuenta',
            $response->getContent()
        );
    }

    public function test_user_sends_invalid_pattern_returns_help_message()
    {
        $user = User::factory()->create([
            'phone' => '+5491122334455',
            'email_verified_at' => now(),
            'allow_sms_whatsapp_checkin' => true,
        ]);

        $response = $this->post('/api/webhooks/twilio/message', [
            'From' => '+5491122334455',
            'Body' => 'hola',
        ]);

        $response->assertStatus(200);
        $this->assertStringContainsString(
            'No reconocimos tu respuesta. Para reportar que estás bien, responde con: OK, ESTOY OK, 1 o BIEN',
            $response->getContent()
        );
    }

    public function test_user_sends_valid_pattern_performs_check_in_and_returns_success()
    {
        $user = User::factory()->create([
            'phone' => '+5491122334455',
            'email_verified_at' => now(),
            'allow_sms_whatsapp_checkin' => true,
            'last_check_in_at' => now()->subHours(2),
        ]);

        $this->assertDatabaseEmpty('check_ins');

        $response = $this->post('/api/webhooks/twilio/message', [
            'From' => '+5491122334455',
            'Body' => 'estoy ok',
        ]);

        $response->assertStatus(200);
        $this->assertStringContainsString(
            'Bienestar verificado con éxito en Estoy Ok. ¡Gracias!',
            $response->getContent()
        );

        $this->assertDatabaseHas('check_ins', [
            'user_id' => $user->id,
        ]);
        $this->assertNotNull($user->fresh()->last_check_in_at);
        $this->assertTrue($user->fresh()->last_check_in_at->isAfter(now()->subSeconds(5)));
    }

    public function test_user_sends_valid_pattern_with_whatsapp_prefix_performs_check_in()
    {
        $user = User::factory()->create([
            'phone' => '+5491122334455',
            'email_verified_at' => now(),
            'allow_sms_whatsapp_checkin' => true,
        ]);

        $response = $this->post('/api/webhooks/twilio/message', [
            'From' => 'whatsapp:+5491122334455',
            'Body' => '1',
        ]);

        $response->assertStatus(200);
        $this->assertStringContainsString(
            'Bienestar verificado con éxito en Estoy Ok. ¡Gracias!',
            $response->getContent()
        );

        $this->assertDatabaseHas('check_ins', [
            'user_id' => $user->id,
        ]);
    }

    public function test_user_check_in_resolves_active_emergency_alerts()
    {
        $user = User::factory()->create([
            'phone' => '+5491122334455',
            'email_verified_at' => now(),
            'allow_sms_whatsapp_checkin' => true,
        ]);

        $alert = EmergencyAlert::create([
            'user_id' => $user->id,
            'status' => 'active',
        ]);

        $response = $this->post('/api/webhooks/twilio/message', [
            'From' => '+5491122334455',
            'Body' => 'bien',
        ]);

        $response->assertStatus(200);
        $this->assertEquals('resolved', $alert->fresh()->status);
    }
}

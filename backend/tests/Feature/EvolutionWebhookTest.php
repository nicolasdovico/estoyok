<?php

namespace Tests\Feature;

use App\Models\EmergencyAlert;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class EvolutionWebhookTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_not_found_returns_json_status()
    {
        $response = $this->postJson('/api/webhooks/evolution/message', [
            'event' => 'messages.upsert',
            'data' => [
                'key' => [
                    'remoteJid' => '5491123456789@s.whatsapp.net',
                    'fromMe' => false,
                ],
                'message' => [
                    'conversation' => 'OK',
                ],
            ],
        ]);

        $response->assertStatus(200);
        $response->assertJson(['status' => 'user_not_found']);
    }

    public function test_user_not_verified_returns_json_status()
    {
        $user = User::factory()->create([
            'phone' => '+5491122334455',
            'email_verified_at' => null,
            'allow_sms_whatsapp_checkin' => true,
        ]);

        $response = $this->postJson('/api/webhooks/evolution/message', [
            'event' => 'messages.upsert',
            'data' => [
                'key' => [
                    'remoteJid' => '5491122334455@s.whatsapp.net',
                    'fromMe' => false,
                ],
                'message' => [
                    'conversation' => 'OK',
                ],
            ],
        ]);

        $response->assertStatus(200);
        $response->assertJson(['status' => 'user_not_found']);
    }

    public function test_user_has_feature_disabled_returns_json_status()
    {
        $user = User::factory()->create([
            'phone' => '+5491122334455',
            'email_verified_at' => now(),
            'allow_sms_whatsapp_checkin' => false,
        ]);

        $response = $this->postJson('/api/webhooks/evolution/message', [
            'event' => 'messages.upsert',
            'data' => [
                'key' => [
                    'remoteJid' => '5491122334455@s.whatsapp.net',
                    'fromMe' => false,
                ],
                'message' => [
                    'conversation' => 'OK',
                ],
            ],
        ]);

        $response->assertStatus(200);
        $response->assertJson(['status' => 'checkin_disabled']);
    }

    public function test_user_sends_invalid_pattern_returns_json_status()
    {
        $user = User::factory()->create([
            'phone' => '+5491122334455',
            'email_verified_at' => now(),
            'allow_sms_whatsapp_checkin' => true,
        ]);

        $response = $this->postJson('/api/webhooks/evolution/message', [
            'event' => 'messages.upsert',
            'data' => [
                'key' => [
                    'remoteJid' => '5491122334455@s.whatsapp.net',
                    'fromMe' => false,
                ],
                'message' => [
                    'conversation' => 'hola',
                ],
            ],
        ]);

        $response->assertStatus(200);
        $response->assertJson(['status' => 'unrecognized_body']);
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

        $response = $this->postJson('/api/webhooks/evolution/message', [
            'event' => 'messages.upsert',
            'data' => [
                'key' => [
                    'remoteJid' => '5491122334455@s.whatsapp.net',
                    'fromMe' => false,
                ],
                'message' => [
                    'conversation' => 'estoy ok',
                ],
            ],
        ]);

        $response->assertStatus(200);
        $response->assertJson(['status' => 'success']);

        $this->assertDatabaseHas('check_ins', [
            'user_id' => $user->id,
            'source' => 'whatsapp',
        ]);
        $this->assertNotNull($user->fresh()->last_check_in_at);
        $this->assertTrue($user->fresh()->last_check_in_at->isAfter(now()->subSeconds(5)));
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

        $response = $this->postJson('/api/webhooks/evolution/message', [
            'event' => 'messages.upsert',
            'data' => [
                'key' => [
                    'remoteJid' => '5491122334455@s.whatsapp.net',
                    'fromMe' => false,
                ],
                'message' => [
                    'conversation' => 'bien',
                ],
            ],
        ]);

        $response->assertStatus(200);
        $this->assertEquals('resolved', $alert->fresh()->status);
    }
}

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
        $this->assertTrue($user->fresh()->last_check_in_at->isAfter(now()->subMinute()));
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

    public function test_user_with_notify_self_whatsapp_can_checkin_even_if_allow_sms_checkin_is_false()
    {
        $user = User::factory()->create([
            'phone' => '+5491122334455',
            'email_verified_at' => now(),
            'allow_sms_whatsapp_checkin' => false,
            'notify_self_whatsapp_on_inactivity' => true,
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
        $response->assertJson(['status' => 'success']);
        $this->assertTrue($user->fresh()->allow_sms_whatsapp_checkin);
        $this->assertNotNull($user->fresh()->last_check_in_at);
    }

    public function test_user_identified_via_lid_and_push_name_exact_match()
    {
        // Partial match user
        $partialUser = User::factory()->create([
            'name' => 'Nicolas',
            'phone' => '+5491111111111',
            'email_verified_at' => now(),
            'allow_sms_whatsapp_checkin' => true,
        ]);

        // Exact match user
        $exactUser = User::factory()->create([
            'name' => 'Nicolás Dovico',
            'phone' => '+5491149790220',
            'email_verified_at' => now(),
            'allow_sms_whatsapp_checkin' => false,
            'notify_self_whatsapp_on_inactivity' => true,
        ]);

        $response = $this->postJson('/api/webhooks/evolution/message', [
            'event' => 'messages.upsert',
            'data' => [
                'key' => [
                    'remoteJid' => '251556368760970@lid',
                    'fromMe' => false,
                ],
                'pushName' => 'Nicolás Dovico',
                'message' => [
                    'conversation' => 'OK',
                ],
            ],
        ]);

        $response->assertStatus(200);
        $response->assertJson(['status' => 'success']);

        // Assert exact user was updated, NOT partial user
        $this->assertNotNull($exactUser->fresh()->last_check_in_at);
        $this->assertNull($partialUser->fresh()->last_check_in_at);
    }
}


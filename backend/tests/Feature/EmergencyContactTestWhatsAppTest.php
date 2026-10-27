<?php

namespace Tests\Feature;

use App\Models\EmergencyContact;
use App\Models\User;
use App\Services\WhatsAppServiceInterface;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Cache;
use Mockery;
use Tests\TestCase;

class EmergencyContactTestWhatsAppTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_send_test_whatsapp_to_their_contact()
    {
        $user = User::factory()->create(['name' => 'Nicolás']);
        $contact = EmergencyContact::factory()->create([
            'user_id' => $user->id,
            'name' => 'Mamá',
            'phone' => '+5491149790220',
            'is_active' => true,
        ]);

        $mockWa = Mockery::mock(WhatsAppServiceInterface::class);
        $mockWa->shouldReceive('sendWhatsApp')
            ->once()
            ->withArgs(function ($to, $message) {
                return $to === '+5491149790220'
                    && str_contains($message, 'Mamá')
                    && str_contains($message, 'Nicolás')
                    && str_contains($message, 'contacto de seguridad en Estoy Ok');
            })
            ->andReturn(true);

        $this->app->instance(WhatsAppServiceInterface::class, $mockWa);

        $response = $this->actingAs($user)->postJson("/api/emergency-contacts/{$contact->id}/send-test-whatsapp");

        $response->assertStatus(200)
            ->assertJson([
                'success' => true,
                'message' => 'Mensaje de prueba de WhatsApp enviado correctamente al contacto.',
                'contact' => [
                    'id' => $contact->id,
                    'name' => 'Mamá',
                    'phone' => '+5491149790220',
                ]
            ]);
    }

    public function test_cannot_send_test_whatsapp_to_other_users_contact()
    {
        $user1 = User::factory()->create();
        $user2 = User::factory()->create();
        $contact = EmergencyContact::factory()->create([
            'user_id' => $user1->id,
            'phone' => '+5491149790220',
            'is_active' => true,
        ]);

        $response = $this->actingAs($user2)->postJson("/api/emergency-contacts/{$contact->id}/send-test-whatsapp");

        $response->assertStatus(403);
    }

    public function test_cannot_send_test_whatsapp_to_inactive_contact()
    {
        $user = User::factory()->create();
        $contact = EmergencyContact::factory()->create([
            'user_id' => $user->id,
            'phone' => '+5491149790220',
            'is_active' => false,
        ]);

        $response = $this->actingAs($user)->postJson("/api/emergency-contacts/{$contact->id}/send-test-whatsapp");

        $response->assertStatus(422);
    }

    public function test_cooldown_prevents_spamming_test_whatsapp()
    {
        $user = User::factory()->create();
        $contact = EmergencyContact::factory()->create([
            'user_id' => $user->id,
            'phone' => '+5491149790220',
            'is_active' => true,
        ]);

        $mockWa = Mockery::mock(WhatsAppServiceInterface::class);
        $mockWa->shouldReceive('sendWhatsApp')->once()->andReturn(true);
        $this->app->instance(WhatsAppServiceInterface::class, $mockWa);

        // Primer envío OK
        $response1 = $this->actingAs($user)->postJson("/api/emergency-contacts/{$contact->id}/send-test-whatsapp");
        $response1->assertStatus(200);

        // Segundo envío inmediato -> 429
        $response2 = $this->actingAs($user)->postJson("/api/emergency-contacts/{$contact->id}/send-test-whatsapp");
        $response2->assertStatus(429);
    }
}

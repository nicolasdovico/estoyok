<?php

namespace Tests\Feature;

use App\Jobs\SendInactivityAlerts;
use App\Models\EmergencyContact;
use App\Models\User;
use App\Services\WhatsAppServiceInterface;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Http;
use Tests\TestCase;
use Mockery;

class WhatsAppAlertTest extends TestCase
{
    use RefreshDatabase;

    public function test_send_inactivity_alerts_dispatches_whatsapp_for_premium_user()
    {
        // 1. Create a premium user with emergency contacts
        $user = User::factory()->create([
            'is_premium' => true,
            'name' => 'John Doe'
        ]);

        $contact = EmergencyContact::create([
            'user_id' => $user->id,
            'name' => 'Jane Doe',
            'phone' => '+5491122334455',
            'email' => 'jane@example.com'
        ]);

        // 2. Mock Expo Push API
        Http::fake([
            'https://exp.host/--/api/v2/push/send' => Http::response(['status' => 'ok'], 200),
        ]);

        // 3. Mock WhatsApp Service
        $this->mock(WhatsAppServiceInterface::class, function ($mock) use ($contact) {
            $mock->shouldReceive('sendWhatsApp')
                ->once()
                ->with($contact->phone, Mockery::pattern('/John Doe/'))
                ->andReturn(true);
        });

        // 4. Execute the job
        $job = new SendInactivityAlerts($user);
        $job->handle(app(WhatsAppServiceInterface::class));

        // No exceptions means success in this context
        $this->assertTrue(true);
    }

    public function test_send_inactivity_alerts_falls_back_to_sms_if_whatsapp_fails()
    {
        // 1. Create a premium user with emergency contacts
        $user = User::factory()->create([
            'is_premium' => true,
            'name' => 'John Doe'
        ]);

        $contact = EmergencyContact::create([
            'user_id' => $user->id,
            'name' => 'Jane Doe',
            'phone' => '+5491122334455',
        ]);

        // 2. Mock WhatsApp Service to fail WhatsApp but succeed SMS
        $this->mock(WhatsAppServiceInterface::class, function ($mock) use ($contact) {
            $mock->shouldReceive('sendWhatsApp')
                ->once()
                ->andReturn(false);

            $mock->shouldReceive('sendSMS')
                ->once()
                ->with($contact->phone, Mockery::pattern('/John Doe/'))
                ->andReturn(true);
        });

        // 3. Execute the job
        $job = new SendInactivityAlerts($user);
        $job->handle(app(WhatsAppServiceInterface::class));

        $this->assertTrue(true);
    }

    public function test_send_inactivity_alerts_does_not_send_whatsapp_for_free_user()
    {
        // 1. Create a free user
        $user = User::factory()->create([
            'is_premium' => false,
            'name' => 'Free User'
        ]);

        EmergencyContact::create([
            'user_id' => $user->id,
            'name' => 'Contact',
            'phone' => '+123456789',
        ]);

        // 2. Mock WhatsApp Service - should NOT be called
        $this->mock(WhatsAppServiceInterface::class, function ($mock) {
            $mock->shouldNotReceive('sendWhatsApp');
            $mock->shouldNotReceive('sendSMS');
        });

        // 3. Execute the job
        $job = new SendInactivityAlerts($user);
        $job->handle(app(WhatsAppServiceInterface::class));

        $this->assertTrue(true);
    }
}

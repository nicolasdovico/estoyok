<?php

namespace Tests\Feature;

use App\Jobs\SendSosAlertJob;
use App\Models\EmergencyAlert;
use App\Models\EmergencyContact;
use App\Models\User;
use App\Services\EvolutionApiService;
use App\Services\WhatsAppServiceInterface;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Queue;
use Mockery;
use Tests\TestCase;

class SosAlertJobAndGuardianTest extends TestCase
{
    use RefreshDatabase;

    public function test_sos_controller_dispatches_send_sos_alert_job(): void
    {
        Queue::fake();

        $user = User::factory()->create([
            'is_premium' => true,
        ]);

        $response = $this->actingAs($user)->postJson('/api/emergency-alerts/sos');

        $response->assertStatus(201);

        Queue::assertPushed(SendSosAlertJob::class, function ($job) use ($user) {
            return $job->user->id === $user->id && $job->alert->type === 'silent_sos';
        });
    }

    public function test_send_sos_alert_job_sends_whatsapp_to_active_contacts(): void
    {
        $user = User::factory()->create(['name' => 'Marcos Test']);

        $contact1 = EmergencyContact::create([
            'user_id' => $user->id,
            'name' => 'Contacto Activo 1',
            'phone' => '+5491100000001',
            'is_active' => true,
            'priority' => 1,
        ]);

        $contact2 = EmergencyContact::create([
            'user_id' => $user->id,
            'name' => 'Contacto Activo 2',
            'phone' => '+5491100000002',
            'is_active' => true,
            'priority' => 2,
        ]);

        $inactiveContact = EmergencyContact::create([
            'user_id' => $user->id,
            'name' => 'Contacto Inactivo',
            'phone' => '+5491100000003',
            'is_active' => false,
            'priority' => 3,
        ]);

        $alert = EmergencyAlert::create([
            'user_id' => $user->id,
            'type' => 'silent_sos',
            'status' => 'active',
            'expires_at' => now()->addHours(24),
        ]);

        $whatsAppService = Mockery::mock(WhatsAppServiceInterface::class);

        $whatsAppService->shouldReceive('sendWhatsApp')
            ->once()
            ->with($contact1->phone, Mockery::pattern('/Marcos Test/'))
            ->andReturn(true);

        $whatsAppService->shouldReceive('sendWhatsApp')
            ->once()
            ->with($contact2->phone, Mockery::pattern('/Marcos Test/'))
            ->andReturn(true);

        $whatsAppService->shouldNotReceive('sendWhatsApp')
            ->with($inactiveContact->phone, Mockery::any());

        $job = new SendSosAlertJob($alert, $user);
        $job->handle($whatsAppService);

        $this->assertTrue(true);
    }

    public function test_send_sos_alert_job_skips_when_alert_is_resolved(): void
    {
        $user = User::factory()->create(['name' => 'Marcos Test']);

        $contact = EmergencyContact::create([
            'user_id' => $user->id,
            'name' => 'Contacto',
            'phone' => '+5491100000001',
            'is_active' => true,
        ]);

        $alert = EmergencyAlert::create([
            'user_id' => $user->id,
            'type' => 'silent_sos',
            'status' => 'resolved',
            'expires_at' => now()->addHours(24),
        ]);

        $whatsAppService = Mockery::mock(WhatsAppServiceInterface::class);
        $whatsAppService->shouldNotReceive('sendWhatsApp');

        $job = new SendSosAlertJob($alert, $user);
        $job->handle($whatsAppService);

        $this->assertTrue(true);
    }

    public function test_send_sos_alert_job_handles_exceptions_gracefully(): void
    {
        $user = User::factory()->create(['name' => 'Marcos Test']);

        $contact = EmergencyContact::create([
            'user_id' => $user->id,
            'name' => 'Contacto',
            'phone' => '+5491100000001',
            'is_active' => true,
        ]);

        $alert = EmergencyAlert::create([
            'user_id' => $user->id,
            'type' => 'silent_sos',
            'status' => 'active',
            'expires_at' => now()->addHours(24),
        ]);

        $whatsAppService = Mockery::mock(WhatsAppServiceInterface::class);
        $whatsAppService->shouldReceive('sendWhatsApp')
            ->once()
            ->andThrow(new \RuntimeException('Connection failed'));

        $job = new SendSosAlertJob($alert, $user);
        $job->handle($whatsAppService);

        $this->assertTrue(true);
    }

    public function test_whatsapp_guardian_command_when_healthy(): void
    {
        $evolutionService = Mockery::mock(EvolutionApiService::class);
        $evolutionService->shouldReceive('checkConnectionHealth')
            ->once()
            ->andReturn([
                'connected' => true,
                'status' => 'open',
            ]);

        $this->app->instance(EvolutionApiService::class, $evolutionService);

        $this->artisan('whatsapp:keep-alive-guardian')
            ->assertExitCode(0);
    }

    public function test_whatsapp_guardian_command_triggers_reconnect_when_unhealthy(): void
    {
        $evolutionService = Mockery::mock(EvolutionApiService::class);
        $evolutionService->shouldReceive('checkConnectionHealth')
            ->once()
            ->andReturn([
                'connected' => false,
                'status' => 'close',
            ]);

        $evolutionService->shouldReceive('reconnectInstance')
            ->once()
            ->andReturn(true);

        $this->app->instance(EvolutionApiService::class, $evolutionService);

        $this->artisan('whatsapp:keep-alive-guardian')
            ->assertExitCode(1);
    }
}

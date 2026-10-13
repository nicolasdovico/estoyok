<?php

namespace Tests\Feature;

use App\Services\EvolutionApiService;
use Illuminate\Support\Facades\Config;
use Illuminate\Support\Facades\Http;
use Tests\TestCase;

class EvolutionApiServiceTest extends TestCase
{
    protected function setUp(): void
    {
        parent::setUp();
        Config::set('services.evolution.url', 'https://evolution.test');
        Config::set('services.evolution.api_key', 'test_key');
        Config::set('services.evolution.instance', 'test_instance');
    }

    public function test_send_whatsapp_sends_link_preview_false_by_default(): void
    {
        Http::fake([
            'https://evolution.test/webhook/set/test_instance' => Http::response(['status' => 'ok'], 200),
            'https://evolution.test/message/sendText/test_instance' => Http::response(['status' => 'SUCCESS'], 200),
        ]);

        $service = new EvolutionApiService();
        $result = $service->sendWhatsApp('+5491149790220', 'Mensaje con link https://estoyok24.com/emergencia/123');

        $this->assertTrue($result);

        Http::assertSent(function ($request) {
            if (!str_contains($request->url(), '/message/sendText/test_instance')) {
                return false;
            }

            $data = $request->data();
            return $data['number'] === '5491149790220'
                && $data['linkPreview'] === false
                && str_contains($data['text'], 'https://estoyok24.com/emergencia/123');
        });
    }

    public function test_send_whatsapp_normalizes_argentina_mobile_numbers(): void
    {
        Http::fake([
            'https://evolution.test/webhook/set/test_instance' => Http::response(['status' => 'ok'], 200),
            'https://evolution.test/message/sendText/test_instance' => Http::response(['status' => 'SUCCESS'], 200),
        ]);

        $service = new EvolutionApiService();
        // Number with 5411... (12 digits, missing 9)
        $result = $service->sendWhatsApp('+541149790220', 'Hola mundo');

        $this->assertTrue($result);

        Http::assertSent(function ($request) {
            if (!str_contains($request->url(), '/message/sendText/test_instance')) {
                return false;
            }

            return $request->data()['number'] === '5491149790220';
        });
    }

    public function test_send_whatsapp_allows_link_preview_override_and_delay(): void
    {
        Http::fake([
            'https://evolution.test/webhook/set/test_instance' => Http::response(['status' => 'ok'], 200),
            'https://evolution.test/message/sendText/test_instance' => Http::response(['status' => 'SUCCESS'], 200),
        ]);

        $service = new EvolutionApiService();
        $result = $service->sendWhatsApp('5491149790220', 'Mensaje con preview', [
            'linkPreview' => true,
            'delay' => 1500,
        ]);

        $this->assertTrue($result);

        Http::assertSent(function ($request) {
            if (!str_contains($request->url(), '/message/sendText/test_instance')) {
                return false;
            }

            $data = $request->data();
            return $data['linkPreview'] === true && $data['delay'] === 1500;
        });
    }

    public function test_check_connection_health_returns_connected_when_state_is_open(): void
    {
        Http::fake([
            'https://evolution.test/instance/connectionState/test_instance' => Http::response([
                'instance' => ['state' => 'open'],
            ], 200),
        ]);

        $service = new EvolutionApiService();
        $health = $service->checkConnectionHealth();

        $this->assertTrue($health['connected']);
        $this->assertEquals('open', $health['status']);
    }

    public function test_check_connection_health_returns_not_connected_when_state_is_close(): void
    {
        Http::fake([
            'https://evolution.test/instance/connectionState/test_instance' => Http::response([
                'instance' => ['state' => 'close'],
            ], 200),
        ]);

        $service = new EvolutionApiService();
        $health = $service->checkConnectionHealth();

        $this->assertFalse($health['connected']);
        $this->assertEquals('close', $health['status']);
    }

    public function test_reconnect_instance_calls_restart_and_connect(): void
    {
        Http::fake([
            'https://evolution.test/instance/restart/test_instance' => Http::response(['status' => 'SUCCESS'], 200),
            'https://evolution.test/instance/connect/test_instance' => Http::response(['status' => 'CONNECTING'], 200),
        ]);

        $service = new EvolutionApiService();
        $result = $service->reconnectInstance();

        $this->assertTrue($result);

        Http::assertSent(function ($request) {
            return str_contains($request->url(), '/instance/restart/test_instance') && $request->method() === 'PUT';
        });

        Http::assertSent(function ($request) {
            return str_contains($request->url(), '/instance/connect/test_instance') && $request->method() === 'GET';
        });
    }

    public function test_send_whatsapp_retries_and_recovers_when_connection_closed(): void
    {
        $callCount = 0;
        Http::fake([
            'https://evolution.test/webhook/set/test_instance' => Http::response(['status' => 'ok'], 200),
            'https://evolution.test/instance/restart/test_instance' => Http::response(['status' => 'SUCCESS'], 200),
            'https://evolution.test/instance/connect/test_instance' => Http::response(['status' => 'CONNECTING'], 200),
            'https://evolution.test/message/sendText/test_instance' => function () use (&$callCount) {
                $callCount++;
                if ($callCount === 1) {
                    return Http::response(['error' => 'Connection Closed'], 400);
                }
                return Http::response(['status' => 'SUCCESS'], 200);
            },
        ]);

        $service = new EvolutionApiService();
        $result = $service->sendWhatsApp('5491149790220', 'Mensaje de prueba');

        $this->assertTrue($result);
        $this->assertEquals(2, $callCount);
    }
}


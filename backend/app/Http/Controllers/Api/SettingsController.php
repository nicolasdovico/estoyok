<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use OpenApi\Attributes as OA;

class SettingsController extends Controller
{
    #[OA\Put(
        path: '/settings/checkin-interval',
        summary: 'Actualizar intervalo de check-in',
        tags: ['Configuración'],
        security: [['sanctum' => []]],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['checkin_interval_hours'],
                properties: [
                    new OA\Property(property: 'checkin_interval_hours', type: 'integer', example: 24, minimum: 1, maximum: 168),
                ]
            )
        ),
        responses: [
            new OA\Response(response: 200, description: 'Intervalo actualizado'),
        ]
    )]
    public function updateCheckinInterval(Request $request)
    {
        $validated = $request->validate([
            'checkin_interval_hours' => 'required|integer|min:1|max:168',
        ]);

        $user = Auth::user();
        $user->update($validated);

        return response()->json([
            'message' => 'Check-in interval updated successfully',
            'checkin_interval_hours' => $user->checkin_interval_hours,
        ]);
    }

    #[OA\Put(
        path: '/settings/quiet-hours',
        summary: 'Actualizar configuración de horas silenciosas',
        tags: ['Configuración'],
        security: [['sanctum' => []]],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['quiet_hours_enabled', 'quiet_hours_start', 'quiet_hours_end', 'timezone'],
                properties: [
                    new OA\Property(property: 'quiet_hours_enabled', type: 'boolean', example: true),
                    new OA\Property(property: 'quiet_hours_start', type: 'string', example: '23:00'),
                    new OA\Property(property: 'quiet_hours_end', type: 'string', example: '07:00'),
                    new OA\Property(property: 'timezone', type: 'string', example: 'America/Argentina/Buenos_Aires'),
                ]
            )
        ),
        responses: [
            new OA\Response(response: 200, description: 'Configuración actualizada'),
        ]
    )]
    public function updateQuietHours(Request $request)
    {
        if ($request->has('quiet_hours_start')) {
            $request->merge([
                'quiet_hours_start' => substr($request->input('quiet_hours_start'), 0, 5)
            ]);
        }
        if ($request->has('quiet_hours_end')) {
            $request->merge([
                'quiet_hours_end' => substr($request->input('quiet_hours_end'), 0, 5)
            ]);
        }

        $timezone = $request->input('timezone');
        if ($timezone) {
            // First check if it is already a canonical identifier
            if (!in_array($timezone, \DateTimeZone::listIdentifiers())) {
                // Try to resolve it by matching the city part
                $parts = explode('/', $timezone);
                $city = end($parts);
                if ($city) {
                    $resolved = null;
                    foreach (\DateTimeZone::listIdentifiers() as $id) {
                        if (str_ends_with($id, '/' . $city)) {
                            $resolved = $id;
                            break;
                        }
                    }
                    if ($resolved) {
                        $timezone = $resolved;
                        $request->merge(['timezone' => $timezone]);
                    }
                }
            }

            // Verify with PHP DateTimeZone construction
            try {
                new \DateTimeZone($timezone);
            } catch (\Exception $e) {
                $request->merge(['timezone' => 'America/Argentina/Buenos_Aires']);
            }
        }

        $validated = $request->validate([
            'quiet_hours_enabled' => 'present|boolean',
            'quiet_hours_start' => 'required|date_format:H:i',
            'quiet_hours_end' => 'required|date_format:H:i',
            'timezone' => 'required|timezone',
        ]);

        $user = Auth::user();
        $user->update($validated);

        return response()->json([
            'message' => 'Quiet hours settings updated successfully',
            'quiet_hours_enabled' => $user->quiet_hours_enabled,
            'quiet_hours_start' => substr($user->quiet_hours_start, 0, 5),
            'quiet_hours_end' => substr($user->quiet_hours_end, 0, 5),
            'timezone' => $user->timezone,
        ]);
    }

    #[OA\Put(
        path: '/settings/sms-whatsapp-checkin',
        summary: 'Actualizar configuración de check-in por SMS/WhatsApp',
        tags: ['Configuración'],
        security: [['sanctum' => []]],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['allow_sms_whatsapp_checkin'],
                properties: [
                    new OA\Property(property: 'allow_sms_whatsapp_checkin', type: 'boolean', example: true),
                ]
            )
        ),
        responses: [
            new OA\Response(
                response: 200,
                description: 'Configuración actualizada',
                content: new OA\JsonContent(
                    properties: [
                        new OA\Property(property: 'message', type: 'string', example: 'SMS/WhatsApp check-in settings updated successfully'),
                        new OA\Property(property: 'allow_sms_whatsapp_checkin', type: 'boolean', example: true),
                    ]
                )
            ),
        ]
    )]
    public function updateSmsWhatsappCheckin(Request $request)
    {
        $validated = $request->validate([
            'allow_sms_whatsapp_checkin' => 'required|boolean',
        ]);

        $user = Auth::user();
        $user->update($validated);

        return response()->json([
            'message' => 'SMS/WhatsApp check-in settings updated successfully',
            'allow_sms_whatsapp_checkin' => $user->allow_sms_whatsapp_checkin,
        ]);
    }
}

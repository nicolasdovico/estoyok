<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
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
        if ($timezone && !in_array($timezone, \DateTimeZone::listIdentifiers())) {
            $parts = explode('/', $timezone);
            $city = end($parts);
            $resolved = null;
            if ($city && $city !== $timezone) {
                foreach (\DateTimeZone::listIdentifiers() as $id) {
                    if (str_ends_with($id, '/' . $city)) {
                        $resolved = $id;
                        break;
                    }
                }
            }
            $timezone = $resolved ?: 'America/Argentina/Buenos_Aires';
            $request->merge(['timezone' => $timezone]);
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

        if ($user->allow_sms_whatsapp_checkin) {
            try {
                app(\App\Services\EvolutionApiService::class)->ensureWebhookConfigured();
            } catch (\Throwable $e) {
                \Illuminate\Support\Facades\Log::warning("Could not auto-configure Evolution webhook on toggle: " . $e->getMessage());
            }
        }

        return response()->json([
            'message' => 'SMS/WhatsApp check-in settings updated successfully',
            'allow_sms_whatsapp_checkin' => $user->allow_sms_whatsapp_checkin,
        ]);
    }

    #[OA\Put(
        path: '/settings/escalation',
        summary: 'Actualizar configuración de escalamiento secuencial',
        tags: ['Configuración'],
        security: [['sanctum' => []]],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['escalation_enabled', 'escalation_interval_minutes'],
                properties: [
                    new OA\Property(property: 'escalation_enabled', type: 'boolean', example: true),
                    new OA\Property(property: 'escalation_interval_minutes', type: 'integer', example: 15, minimum: 1, maximum: 1440),
                ]
            )
        ),
        responses: [
            new OA\Response(
                response: 200,
                description: 'Configuración actualizada',
                content: new OA\JsonContent(
                    properties: [
                        new OA\Property(property: 'message', type: 'string', example: 'Escalation settings updated successfully'),
                        new OA\Property(property: 'escalation_enabled', type: 'boolean', example: true),
                        new OA\Property(property: 'escalation_interval_minutes', type: 'integer', example: 15),
                    ]
                )
            ),
        ]
    )]
    public function updateEscalation(Request $request)
    {
        $validated = $request->validate([
            'escalation_enabled' => 'required|boolean',
            'escalation_interval_minutes' => 'required|integer|min:1|max:1440',
        ]);

        $user = Auth::user();
        $user->update($validated);

        return response()->json([
            'message' => 'Escalation settings updated successfully',
            'escalation_enabled' => $user->escalation_enabled,
            'escalation_interval_minutes' => $user->escalation_interval_minutes,
        ]);
    }

    #[OA\Put(
        path: '/settings/privacy',
        summary: 'Actualizar configuración de privacidad y alertas de batería',
        tags: ['Configuración'],
        security: [['sanctum' => []]],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                properties: [
                    new OA\Property(property: 'share_contact_responses', type: 'boolean', example: true),
                    new OA\Property(property: 'low_battery_alerts_enabled', type: 'boolean', example: true),
                ]
            )
        ),
        responses: [
            new OA\Response(
                response: 200,
                description: 'Configuración de privacidad y batería actualizada',
                content: new OA\JsonContent(
                    properties: [
                        new OA\Property(property: 'message', type: 'string', example: 'Privacy settings updated successfully'),
                        new OA\Property(property: 'share_contact_responses', type: 'boolean', example: true),
                        new OA\Property(property: 'low_battery_alerts_enabled', type: 'boolean', example: true),
                    ]
                )
            ),
        ]
    )]
    public function updatePrivacy(Request $request)
    {
        $validated = $request->validate([
            'share_contact_responses' => 'sometimes|boolean',
            'low_battery_alerts_enabled' => 'sometimes|boolean',
        ]);

        $user = Auth::user();
        $user->update($validated);

        return response()->json([
            'message' => 'Privacy settings updated successfully',
            'share_contact_responses' => $user->share_contact_responses,
            'low_battery_alerts_enabled' => $user->low_battery_alerts_enabled,
        ]);
    }

    #[OA\Put(
        path: '/settings/automation',
        summary: 'Actualizar configuración de automatización de check-in (Wi-Fi y Podómetro)',
        tags: ['Configuración'],
        security: [['sanctum' => []]],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['wifi_checkin_enabled', 'sensor_checkin_enabled'],
                properties: [
                    new OA\Property(property: 'wifi_checkin_enabled', type: 'boolean', example: false),
                    new OA\Property(property: 'safe_wifi_ssid', type: 'string', example: 'MiWifiDeCasa', nullable: true),
                    new OA\Property(property: 'sensor_checkin_enabled', type: 'boolean', example: false),
                ]
            )
        ),
        responses: [
            new OA\Response(
                response: 200,
                description: 'Configuración de automatización actualizada',
                content: new OA\JsonContent(
                    properties: [
                        new OA\Property(property: 'message', type: 'string', example: 'Automation settings updated successfully'),
                        new OA\Property(property: 'wifi_checkin_enabled', type: 'boolean', example: false),
                        new OA\Property(property: 'safe_wifi_ssid', type: 'string', example: 'MiWifiDeCasa', nullable: true),
                        new OA\Property(property: 'sensor_checkin_enabled', type: 'boolean', example: false),
                    ]
                )
            ),
        ]
    )]
    public function updateAutomation(Request $request)
    {
        $validated = $request->validate([
            'wifi_checkin_enabled' => 'required|boolean',
            'safe_wifi_ssid' => 'nullable|string|max:255',
            'sensor_checkin_enabled' => 'required|boolean',
        ]);

        $user = Auth::user();
        $user->update($validated);

        return response()->json([
            'message' => 'Automation settings updated successfully',
            'wifi_checkin_enabled' => $user->wifi_checkin_enabled,
            'safe_wifi_ssid' => $user->safe_wifi_ssid,
            'sensor_checkin_enabled' => $user->sensor_checkin_enabled,
        ]);
    }

    #[OA\Put(
        path: '/settings/proximity-alerts',
        summary: 'Actualizar configuración de alertas de proximidad relativas',
        tags: ['Configuración'],
        security: [['sanctum' => []]],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['proximity_alerts_enabled'],
                properties: [
                    new OA\Property(property: 'proximity_alerts_enabled', type: 'boolean', example: true),
                ]
            )
        ),
        responses: [
            new OA\Response(
                response: 200,
                description: 'Configuración actualizada',
                content: new OA\JsonContent(
                    properties: [
                        new OA\Property(property: 'message', type: 'string', example: 'Proximity alerts settings updated successfully'),
                        new OA\Property(property: 'proximity_alerts_enabled', type: 'boolean', example: true),
                    ]
                )
            ),
        ]
    )]
    public function updateProximityAlerts(Request $request)
    {
        $validated = $request->validate([
            'proximity_alerts_enabled' => 'required|boolean',
        ]);

        $user = Auth::user();
        $user->update($validated);

        return response()->json([
            'message' => 'Proximity alerts settings updated successfully',
            'proximity_alerts_enabled' => (bool) $user->proximity_alerts_enabled,
        ]);
    }

    #[OA\Post(
        path: '/settings/avatar',
        summary: 'Subir o actualizar la foto de perfil (avatar)',
        tags: ['Configuración'],
        security: [['sanctum' => []]],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\MediaType(
                mediaType: 'multipart/form-data',
                schema: new OA\Schema(
                    required: ['avatar'],
                    properties: [
                        new OA\Property(property: 'avatar', description: 'Imagen del avatar (jpeg, png, jpg, max 2MB)', type: 'string', format: 'binary')
                    ]
                )
            )
        ),
        responses: [
            new OA\Response(
                response: 200,
                description: 'Avatar actualizado exitosamente',
                content: new OA\JsonContent(
                    properties: [
                        new OA\Property(property: 'message', type: 'string', example: 'Avatar updated successfully'),
                        new OA\Property(property: 'avatar_url', type: 'string', example: 'http://localhost/storage/avatars/abc.jpg')
                    ]
                )
            ),
            new OA\Response(response: 400, description: 'Error en la validación')
        ]
    )]
    public function updateAvatar(Request $request)
    {
        $request->validate([
            'avatar' => 'required|image|mimes:jpeg,png,jpg|max:2048',
        ]);

        $user = Auth::user();

        if ($request->hasFile('avatar')) {
            // Delete old avatar if it exists
            if ($user->avatar_path) {
                \Storage::disk('public')->delete($user->avatar_path);
            }

            // Store new avatar in 'public' disk
            $path = $request->file('avatar')->store('avatars', 'public');
            
            $user->update([
                'avatar_path' => $path
            ]);
        }

        return response()->json([
            'message' => 'Avatar updated successfully',
            'avatar_url' => $user->avatar_url,
        ]);
    }

    #[OA\Put(
        path: '/settings/push-token',
        summary: 'Actualizar el token de notificaciones push (FCM / Expo)',
        tags: ['Configuración'],
        security: [['sanctum' => []]],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['push_token'],
                properties: [
                    new OA\Property(property: 'push_token', type: 'string', example: 'ExponentPushToken[12345]'),
                ]
            )
        ),
        responses: [
            new OA\Response(response: 200, description: 'Token actualizado'),
        ]
    )]
    public function updatePushToken(Request $request)
    {
        $validated = $request->validate([
            'push_token' => 'required|string|max:500',
            'device_uuid' => 'nullable|string',
        ]);

        $user = Auth::user();

        // 1. Disassociate this push token from any other users or devices
        User::where('expo_push_token', $validated['push_token'])
            ->where('id', '!=', $user->id)
            ->update(['expo_push_token' => null]);

        \App\Models\UserDevice::where('push_token', $validated['push_token'])
            ->where('user_id', '!=', $user->id)
            ->update(['push_token' => null, 'is_active' => false]);

        // 2. Update or associate token in user_devices table
        $deviceUuid = $validated['device_uuid'] ?? null;
        if ($deviceUuid) {
            \App\Models\UserDevice::updateOrCreate(
                ['user_id' => $user->id, 'device_uuid' => $deviceUuid],
                [
                    'push_token' => $validated['push_token'],
                    'platform' => str_starts_with($deviceUuid, 'web-') ? 'web' : 'android',
                    'is_active' => true,
                    'last_active_at' => now(),
                ]
            );
        } else {
            $device = \App\Models\UserDevice::where('user_id', $user->id)
                ->where('is_active', true)
                ->latest('last_active_at')
                ->first();

            if ($device) {
                $device->update([
                    'push_token' => $validated['push_token'],
                    'is_active' => true,
                    'last_active_at' => now(),
                ]);
            } else {
                \App\Models\UserDevice::create([
                    'user_id' => $user->id,
                    'device_uuid' => 'android-' . md5($user->id . '-' . microtime(true)),
                    'device_name' => 'Android Device',
                    'platform' => 'android',
                    'push_token' => $validated['push_token'],
                    'is_active' => true,
                    'last_active_at' => now(),
                ]);
            }
        }

        $user->update([
            'expo_push_token' => $validated['push_token']
        ]);

        return response()->json([
            'message' => 'Push token updated successfully',
            'push_token' => $user->expo_push_token,
        ]);
    }

    #[OA\Post(
        path: '/settings/accept-disclaimer',
        summary: 'Registrar la aceptación de términos y descargo de responsabilidad',
        tags: ['Configuración'],
        security: [['sanctum' => []]],
        responses: [
            new OA\Response(
                response: 200,
                description: 'Descargo de responsabilidad aceptado',
                content: new OA\JsonContent(
                    properties: [
                        new OA\Property(property: 'message', type: 'string', example: 'Disclaimer accepted successfully'),
                        new OA\Property(property: 'disclaimer_accepted_at', type: 'string', example: '2026-07-29T11:30:00Z'),
                    ]
                )
            ),
        ]
    )]
    public function acceptDisclaimer(Request $request)
    {
        $user = Auth::user();
        if (!$user->disclaimer_accepted_at) {
            $user->update([
                'disclaimer_accepted_at' => now(),
            ]);
        }

        return response()->json([
            'message' => 'Disclaimer accepted successfully',
            'disclaimer_accepted_at' => $user->disclaimer_accepted_at?->toISOString(),
            'user' => $user->fresh(),
        ]);
    }

    #[OA\Put(
        path: '/settings/phone',
        summary: 'Actualizar número de teléfono/WhatsApp personal del usuario',
        tags: ['Configuración'],
        security: [['sanctum' => []]],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                properties: [
                    new OA\Property(property: 'phone', type: 'string', example: '+5491122334455'),
                ]
            )
        ),
        responses: [
            new OA\Response(response: 200, description: 'Teléfono actualizado'),
        ]
    )]
    public function updatePhone(Request $request)
    {
        $validated = $request->validate([
            'phone' => 'nullable|string|max:30',
        ]);

        $user = Auth::user();
        $rawPhone = $validated['phone'] ?? null;
        $cleanPhone = !empty($rawPhone) ? preg_replace('/[^0-9+]/', '', $rawPhone) : null;
        if ($cleanPhone && !str_starts_with($cleanPhone, '+')) {
            $cleanPhone = '+' . $cleanPhone;
        }

        $user->update(['phone' => $cleanPhone]);

        if ($user->allow_sms_whatsapp_checkin) {
            try {
                app(\App\Services\EvolutionApiService::class)->ensureWebhookConfigured();
            } catch (\Throwable $e) {
                \Illuminate\Support\Facades\Log::warning("Could not auto-configure Evolution webhook on phone update: " . $e->getMessage());
            }
        }

        return response()->json([
            'message' => 'Número de teléfono actualizado correctamente',
            'phone' => $user->phone,
        ]);
    }
}


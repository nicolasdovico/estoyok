<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\DynamicGeofence;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;
use OpenApi\Attributes as OA;

class DynamicGeofenceController extends Controller
{
    #[OA\Get(
        path: '/dynamic-geofences/active',
        summary: 'Listar geocercas móviles activas involucradas para el usuario autenticado',
        tags: ['Geocercas Dinámicas'],
        security: [['sanctum' => []]],
        responses: [
            new OA\Response(response: 200, description: 'Lista de geocercas móviles activas'),
        ]
    )]
    public function active()
    {
        $user = Auth::user();

        $activeGeofences = DynamicGeofence::active()
            ->where(function ($query) use ($user) {
                $query->where('initiator_id', $user->id)
                      ->orWhere('target_id', $user->id);
            })
            ->with(['initiator.currentLocation', 'target.currentLocation'])
            ->get();

        return response()->json($activeGeofences);
    }

    #[OA\Post(
        path: '/dynamic-geofences',
        summary: 'Crear una geocerca móvil (alerta de proximidad dinámica)',
        tags: ['Geocercas Dinámicas'],
        security: [['sanctum' => []]],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['target_id', 'safe_radius_meters'],
                properties: [
                    new OA\Property(property: 'target_id', type: 'integer', example: 2),
                    new OA\Property(property: 'safe_radius_meters', type: 'integer', example: 50),
                ]
            )
        ),
        responses: [
            new OA\Response(response: 201, description: 'Geocerca móvil creada exitosamente'),
            new OA\Response(response: 403, description: 'No autorizado / Privacidad inhabilitada por el familiar'),
        ]
    )]
    public function store(Request $request)
    {
        $validated = $request->validate([
            'target_id' => 'required|integer|exists:users,id',
            'safe_radius_meters' => 'required|integer|min:10|max:5000',
        ]);

        $user = Auth::user();
        $targetId = $validated['target_id'];

        // 1. Validar que el target pertenece a algún círculo del usuario
        $isInSameCircle = $user->circles()->whereHas('users', function ($q) use ($targetId) {
            $q->where('users.id', $targetId);
        })->exists();

        if (!$isInSameCircle) {
            return response()->json(['message' => 'El miembro no pertenece a tus círculos.'], 403);
        }

        // 2. Control de Privacidad: si el target tiene proximity_alerts_enabled = false
        $target = User::findOrFail($targetId);
        if (!$target->proximity_alerts_enabled) {
            return response()->json([
                'message' => 'Este miembro ha desactivado las alertas de proximidad relativas por privacidad.'
            ], 403);
        }

        // 3. Desactivar cualquier otra geocerca activa del iniciador
        DynamicGeofence::where('initiator_id', $user->id)
            ->where('is_active', true)
            ->update(['is_active' => false]);

        // 4. Crear la geocerca dinámica
        $geofence = DynamicGeofence::create([
            'initiator_id' => $user->id,
            'target_id' => $targetId,
            'safe_radius_meters' => $validated['safe_radius_meters'],
            'is_active' => true,
        ]);

        // 5. Notificar por push al destino para iniciar tracking de alta frecuencia
        if ($target->expo_push_token) {
            app(\App\Services\PushNotificationService::class)->sendPush(
                $target->expo_push_token,
                '📡 Radar de Proximidad Activado',
                "{$user->name} ha iniciado un radar de proximidad contigo.",
                [
                    'type' => 'dynamic_geofence_activated',
                    'geofence_id' => (string) $geofence->id,
                    'initiator_name' => $user->name,
                ],
                true
            );
        }

        return response()->json($geofence->load(['initiator', 'target']), 201);
    }

    #[OA\Post(
        path: '/dynamic-geofences/{id}/deactivate',
        summary: 'Desactivar una geocerca móvil activa',
        tags: ['Geocercas Dinámicas'],
        security: [['sanctum' => []]],
        parameters: [
            new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer')),
        ],
        responses: [
            new OA\Response(response: 200, description: 'Geocerca móvil desactivada'),
            new OA\Response(response: 403, description: 'No autorizado'),
        ]
    )]
    public function deactivate($id)
    {
        $user = Auth::user();
        $geofence = DynamicGeofence::findOrFail($id);

        if ($geofence->initiator_id !== $user->id && $geofence->target_id !== $user->id) {
            return response()->json(['message' => 'No autorizado.'], 403);
        }

        $geofence->update(['is_active' => false]);

        // Notificar al otro participante
        $otherUserId = $user->id === $geofence->initiator_id ? $geofence->target_id : $geofence->initiator_id;
        $otherUser = User::find($otherUserId);

        if ($otherUser && $otherUser->expo_push_token) {
            app(\App\Services\PushNotificationService::class)->sendPush(
                $otherUser->expo_push_token,
                '📡 Radar de Proximidad Finalizado',
                "El radar de proximidad ha sido desactivado por {$user->name}.",
                [
                    'type' => 'dynamic_geofence_deactivated',
                    'geofence_id' => (string) $geofence->id,
                ],
                true
            );
        }

        return response()->json([
            'message' => 'Geocerca móvil desactivada exitosamente.',
            'geofence' => $geofence,
        ]);
    }
}

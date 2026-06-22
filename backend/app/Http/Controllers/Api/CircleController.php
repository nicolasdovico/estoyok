<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Circle;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use OpenApi\Attributes as OA;

class CircleController extends Controller
{
    #[OA\Get(
        path: '/circles',
        summary: 'Listar círculos del usuario autenticado',
        tags: ['Círculos'],
        security: [['sanctum' => []]],
        responses: [
            new OA\Response(
                response: 200,
                description: 'Lista de círculos del usuario con sus miembros y geocercas'
            ),
        ]
    )]
    public function index()
    {
        $user = Auth::user();
        $circles = $user->circles()->with(['users.currentLocation', 'users.activeEmergencyAlerts', 'owner', 'geofences.user'])->get();
        return response()->json($circles);
    }

    #[OA\Post(
        path: '/circles',
        summary: 'Crear un nuevo círculo de seguridad',
        tags: ['Círculos'],
        security: [['sanctum' => []]],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['name'],
                properties: [
                    new OA\Property(property: 'name', type: 'string', example: 'Familia Perez'),
                ]
            )
        ),
        responses: [
            new OA\Response(response: 201, description: 'Círculo creado exitosamente'),
        ]
    )]
    public function store(Request $request)
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
        ]);

        $user = Auth::user();

        $circle = Circle::create([
            'name' => $validated['name'],
            'owner_id' => $user->id,
        ]);

        // El creador entra como administrador
        $circle->users()->attach($user->id, ['role' => 'admin']);

        return response()->json($circle->load(['users.currentLocation', 'owner', 'geofences']), 201);
    }

    #[OA\Post(
        path: '/circles/join',
        summary: 'Unirse a un círculo existente mediante código de invitación',
        tags: ['Círculos'],
        security: [['sanctum' => []]],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['invite_code'],
                properties: [
                    new OA\Property(property: 'invite_code', type: 'string', example: 'ABC123XYZ0'),
                ]
            )
        ),
        responses: [
            new OA\Response(response: 200, description: 'Unido al círculo exitosamente'),
            new OA\Response(response: 404, description: 'Código de invitación inválido'),
            new OA\Response(response: 400, description: 'Ya eres miembro de este círculo'),
        ]
    )]
    public function join(Request $request)
    {
        $validated = $request->validate([
            'invite_code' => 'required|string|size:10',
        ]);

        $user = Auth::user();
        $inviteCode = strtoupper($validated['invite_code']);

        $circle = Circle::where('invite_code', $inviteCode)->first();

        if (!$circle) {
            return response()->json(['message' => 'Código de invitación inválido'], 404);
        }

        // Verificar si ya es miembro
        if ($circle->users()->where('user_id', $user->id)->exists()) {
            return response()->json(['message' => 'Ya perteneces a este círculo'], 400);
        }

        // Unirse como miembro estándar
        $circle->users()->attach($user->id, ['role' => 'member']);

        return response()->json($circle->load(['users.currentLocation', 'owner', 'geofences']));
    }

    #[OA\Delete(
        path: '/circles/{circle}/members/{member}',
        summary: 'Eliminar un miembro del círculo o abandonar el círculo',
        tags: ['Círculos'],
        security: [['sanctum' => []]],
        parameters: [
            new OA\Parameter(name: 'circle', in: 'path', required: true, schema: new OA\Schema(type: 'integer')),
            new OA\Parameter(name: 'member', in: 'path', required: true, schema: new OA\Schema(type: 'integer')),
        ],
        responses: [
            new OA\Response(response: 200, description: 'Miembro eliminado o abandono exitoso'),
            new OA\Response(response: 403, description: 'No tienes permisos para realizar esta acción'),
            new OA\Response(response: 404, description: 'Círculo o miembro no encontrado'),
        ]
    )]
    public function removeMember(Circle $circle, User $member)
    {
        $user = Auth::user();

        // Verificar si el usuario autenticado pertenece al círculo
        $authMember = $circle->users()->where('user_id', $user->id)->first();
        if (!$authMember) {
            return response()->json(['message' => 'No perteneces a este círculo'], 403);
        }

        // Si el usuario quiere salirse a sí mismo del círculo
        if ($user->id === $member->id) {
            // Si es el dueño (owner) del círculo, no puede salirse sin eliminar el círculo o transferirlo.
            // Para simplificar, eliminamos el círculo completo si el dueño decide salirse/eliminarlo.
            if ($circle->owner_id === $user->id) {
                $circle->delete();
                return response()->json(['message' => 'Has abandonado el círculo y el círculo ha sido eliminado porque eras el dueño.']);
            }

            $circle->users()->detach($user->id);
            return response()->json(['message' => 'Has abandonado el círculo exitosamente.']);
        }

        // Si quiere eliminar a otra persona del círculo, debe ser admin o el owner
        $role = $authMember->pivot->role;
        if ($role !== 'admin' && $circle->owner_id !== $user->id) {
            return response()->json(['message' => 'No tienes permisos para eliminar miembros de este círculo'], 403);
        }

        // Verificar que el miembro que se desea eliminar realmente pertenezca al círculo
        if (!$circle->users()->where('user_id', $member->id)->exists()) {
            return response()->json(['message' => 'El miembro no pertenece a este círculo'], 404);
        }

        // No se puede expulsar al dueño del círculo
        if ($circle->owner_id === $member->id) {
            return response()->json(['message' => 'No se puede expulsar al dueño del círculo'], 400);
        }

        $circle->users()->detach($member->id);

        return response()->json(['message' => 'Miembro eliminado exitosamente.']);
    }

    #[OA\Delete(
        path: '/circles/{circle}',
        summary: 'Eliminar un círculo de seguridad completo',
        tags: ['Círculos'],
        security: [['sanctum' => []]],
        parameters: [
            new OA\Parameter(name: 'circle', in: 'path', required: true, schema: new OA\Schema(type: 'integer')),
        ],
        responses: [
            new OA\Response(response: 200, description: 'Círculo eliminado exitosamente'),
            new OA\Response(response: 403, description: 'Solo el dueño puede eliminar el círculo'),
        ]
    )]
    public function destroy(Circle $circle)
    {
        $user = Auth::user();

        if ($circle->owner_id !== $user->id) {
            return response()->json(['message' => 'Solo el creador del círculo puede eliminarlo'], 403);
        }

        $circle->delete();

        return response()->json(['message' => 'Círculo eliminado exitosamente.']);
    }

    #[OA\Put(
        path: '/circles/{circle}/speed-limit',
        summary: 'Actualizar el límite de velocidad de un círculo',
        tags: ['Círculos'],
        security: [['sanctum' => []]],
        parameters: [
            new OA\Parameter(name: 'circle', in: 'path', required: true, schema: new OA\Schema(type: 'integer')),
        ],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['speed_limit'],
                properties: [
                    new OA\Property(property: 'speed_limit', type: 'integer', example: 120),
                ]
            )
        ),
        responses: [
            new OA\Response(response: 200, description: 'Límite de velocidad actualizado exitosamente'),
            new OA\Response(response: 403, description: 'No tienes permisos para modificar este círculo'),
        ]
    )]
    public function updateSpeedLimit(Request $request, Circle $circle)
    {
        $user = Auth::user();

        // Verificar si el usuario autenticado es dueño o administrador del círculo
        $authMember = $circle->users()->where('user_id', $user->id)->first();
        if (!$authMember || ($authMember->pivot->role !== 'admin' && $circle->owner_id !== $user->id)) {
            return response()->json(['message' => 'No tienes permisos para modificar el límite de velocidad de este círculo'], 403);
        }

        $validated = $request->validate([
            'speed_limit' => 'required|integer|min:10|max:250',
        ]);

        $circle->update([
            'speed_limit' => $validated['speed_limit'],
        ]);

        return response()->json([
            'message' => 'Límite de velocidad actualizado exitosamente',
            'circle' => $circle,
        ]);
    }
}

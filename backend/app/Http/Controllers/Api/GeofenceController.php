<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Circle;
use App\Models\Geofence;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;
use OpenApi\Attributes as OA;

class GeofenceController extends Controller
{
    #[OA\Get(
        path: '/circles/{circle}/geofences',
        summary: 'Listar perímetros (geocercas) de un círculo',
        tags: ['Geocercas'],
        security: [['sanctum' => []]],
        parameters: [
            new OA\Parameter(name: 'circle', in: 'path', required: true, schema: new OA\Schema(type: 'integer')),
        ],
        responses: [
            new OA\Response(response: 200, description: 'Lista de geocercas'),
            new OA\Response(response: 403, description: 'No tienes acceso a este círculo'),
        ]
    )]
    public function index(Circle $circle)
    {
        $user = Auth::user();

        if (!$circle->users()->where('user_id', $user->id)->exists()) {
            return response()->json(['message' => 'No tienes acceso a este círculo'], 403);
        }

        return response()->json($circle->geofences()->with('user')->get());
    }

    #[OA\Post(
        path: '/circles/{circle}/geofences',
        summary: 'Crear un perímetro (geocerca) en el círculo',
        tags: ['Geocercas'],
        security: [['sanctum' => []]],
        parameters: [
            new OA\Parameter(name: 'circle', in: 'path', required: true, schema: new OA\Schema(type: 'integer')),
        ],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['name', 'radius', 'type', 'latitude', 'longitude'],
                properties: [
                    new OA\Property(property: 'name', type: 'string', example: 'Escuela'),
                    new OA\Property(property: 'radius', type: 'number', example: 200),
                    new OA\Property(property: 'type', type: 'string', example: 'entry_exit'),
                    new OA\Property(property: 'latitude', type: 'number', example: -34.6037),
                    new OA\Property(property: 'longitude', type: 'number', example: -58.3816),
                    new OA\Property(property: 'user_id', type: 'integer', nullable: true, example: 2),
                ]
            )
        ),
        responses: [
            new OA\Response(response: 201, description: 'Geocerca creada exitosamente'),
            new OA\Response(response: 403, description: 'No tienes acceso a este círculo'),
            new OA\Response(response: 400, description: 'El usuario especificado no pertenece al círculo'),
        ]
    )]
    public function store(Request $request, Circle $circle)
    {
        $user = Auth::user();

        // Verificar pertenencia al círculo
        if (!$circle->users()->where('user_id', $user->id)->exists()) {
            return response()->json(['message' => 'No tienes acceso a este círculo'], 403);
        }

        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'radius' => 'required|numeric|min:10|max:10000',
            'type' => 'required|string|in:entry,exit,entry_exit',
            'latitude' => 'required|numeric|between:-90,90',
            'longitude' => 'required|numeric|between:-180,180',
            'user_id' => 'nullable|integer|exists:users,id',
        ]);

        // Si se especifica un user_id, verificar que pertenezca al círculo
        if (!empty($validated['user_id'])) {
            if (!$circle->users()->where('user_id', $validated['user_id'])->exists()) {
                return response()->json(['message' => 'El usuario especificado no pertenece a este círculo'], 400);
            }
        }

        $geofence = Geofence::create([
            'circle_id' => $circle->id,
            'user_id' => $validated['user_id'] ?? null,
            'name' => $validated['name'],
            'radius' => $validated['radius'],
            'type' => $validated['type'],
            'center' => DB::raw("ST_GeomFromText('POINT({$validated['longitude']} {$validated['latitude']})', 4326)"),
        ]);

        // Volver a consultar para obtener con los atributos latitude y longitude dinámicos
        $geofence = Geofence::with('user')->find($geofence->id);

        return response()->json($geofence, 201);
    }

    #[OA\Put(
        path: '/geofences/{geofence}',
        summary: 'Actualizar un perímetro (geocerca) existente',
        tags: ['Geocercas'],
        security: [['sanctum' => []]],
        parameters: [
            new OA\Parameter(name: 'geofence', in: 'path', required: true, schema: new OA\Schema(type: 'integer')),
        ],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                properties: [
                    new OA\Property(property: 'name', type: 'string', example: 'Casa'),
                    new OA\Property(property: 'radius', type: 'number', example: 150),
                    new OA\Property(property: 'type', type: 'string', example: 'entry'),
                    new OA\Property(property: 'latitude', type: 'number', example: -34.6037),
                    new OA\Property(property: 'longitude', type: 'number', example: -58.3816),
                    new OA\Property(property: 'user_id', type: 'integer', nullable: true),
                    new OA\Property(property: 'is_active', type: 'boolean', example: true),
                ]
            )
        ),
        responses: [
            new OA\Response(response: 200, description: 'Geocerca actualizada exitosamente'),
            new OA\Response(response: 403, description: 'No tienes acceso a este círculo'),
            new OA\Response(response: 400, description: 'El usuario especificado no pertenece al círculo'),
        ]
    )]
    public function update(Request $request, Geofence $geofence)
    {
        $user = Auth::user();
        $circle = $geofence->circle;

        // Verificar pertenencia al círculo
        if (!$circle->users()->where('user_id', $user->id)->exists()) {
            return response()->json(['message' => 'No tienes acceso a este círculo'], 403);
        }

        $validated = $request->validate([
            'name' => 'sometimes|required|string|max:255',
            'radius' => 'sometimes|required|numeric|min:10|max:10000',
            'type' => 'sometimes|required|string|in:entry,exit,entry_exit',
            'latitude' => 'sometimes|required|numeric|between:-90,90',
            'longitude' => 'sometimes|required|numeric|between:-180,180',
            'user_id' => 'nullable|integer|exists:users,id',
            'is_active' => 'sometimes|required|boolean',
        ]);

        if (array_key_exists('user_id', $validated) && !empty($validated['user_id'])) {
            if (!$circle->users()->where('user_id', $validated['user_id'])->exists()) {
                return response()->json(['message' => 'El usuario especificado no pertenece a este círculo'], 400);
            }
        }

        $updateData = [];
        if (isset($validated['name'])) $updateData['name'] = $validated['name'];
        if (isset($validated['radius'])) $updateData['radius'] = $validated['radius'];
        if (isset($validated['type'])) $updateData['type'] = $validated['type'];
        if (isset($validated['is_active'])) $updateData['is_active'] = $validated['is_active'];
        if (array_key_exists('user_id', $validated)) $updateData['user_id'] = $validated['user_id'];

        if (isset($validated['latitude']) && isset($validated['longitude'])) {
            $updateData['center'] = DB::raw("ST_GeomFromText('POINT({$validated['longitude']} {$validated['latitude']})', 4326)");
        }

        $geofence->update($updateData);

        // Volver a consultar
        $geofence = Geofence::with('user')->find($geofence->id);

        return response()->json($geofence);
    }

    #[OA\Delete(
        path: '/geofences/{geofence}',
        summary: 'Eliminar un perímetro (geocerca)',
        tags: ['Geocercas'],
        security: [['sanctum' => []]],
        parameters: [
            new OA\Parameter(name: 'geofence', in: 'path', required: true, schema: new OA\Schema(type: 'integer')),
        ],
        responses: [
            new OA\Response(response: 200, description: 'Geocerca eliminada exitosamente'),
            new OA\Response(response: 403, description: 'No tienes acceso a este círculo'),
        ]
    )]
    public function destroy(Geofence $geofence)
    {
        $user = Auth::user();
        $circle = $geofence->circle;

        // Verificar pertenencia al círculo
        if (!$circle->users()->where('user_id', $user->id)->exists()) {
            return response()->json(['message' => 'No tienes acceso a este círculo'], 403);
        }

        $geofence->delete();

        return response()->json(['message' => 'Geocerca eliminada exitosamente.']);
    }
}

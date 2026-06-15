<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\EmergencyContact;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use OpenApi\Attributes as OA;

class EmergencyContactController extends Controller
{
    #[OA\Get(
        path: '/emergency-contacts',
        summary: 'Listar contactos de emergencia',
        tags: ['Contactos de Emergencia'],
        security: [['sanctum' => []]],
        responses: [
            new OA\Response(response: 200, description: 'Lista de contactos'),
        ]
    )]
    public function index()
    {
        return response()->json(Auth::user()->emergencyContacts);
    }

    #[OA\Post(
        path: '/emergency-contacts',
        summary: 'Crear contacto de emergencia',
        tags: ['Contactos de Emergencia'],
        security: [['sanctum' => []]],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['name', 'phone'],
                properties: [
                    new OA\Property(property: 'name', type: 'string', example: 'Mamá'),
                    new OA\Property(property: 'phone', type: 'string', example: '+541122334455'),
                    new OA\Property(property: 'email', type: 'string', format: 'email', example: 'mama@example.com'),
                    new OA\Property(property: 'relationship', type: 'string', example: 'Madre'),
                    new OA\Property(property: 'is_active', type: 'boolean', example: true),
                ]
            )
        ),
        responses: [
            new OA\Response(response: 201, description: 'Contacto creado'),
        ]
    )]
    public function store(Request $request)
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'phone' => 'required|string|max:20',
            'email' => 'nullable|email|max:255',
            'relationship' => 'nullable|string|max:100',
            'is_active' => 'boolean',
        ]);

        $contact = Auth::user()->emergencyContacts()->create($validated);

        return response()->json($contact, 201);
    }

    #[OA\Get(
        path: '/emergency-contacts/{id}',
        summary: 'Obtener un contacto específico',
        tags: ['Contactos de Emergencia'],
        security: [['sanctum' => []]],
        parameters: [
            new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer')),
        ],
        responses: [
            new OA\Response(response: 200, description: 'Datos del contacto'),
            new OA\Response(response: 403, description: 'No autorizado'),
        ]
    )]
    public function show(EmergencyContact $emergencyContact)
    {
        if ($emergencyContact->user_id !== Auth::id()) {
            return response()->json(['message' => 'Unauthorized'], 403);
        }

        return response()->json($emergencyContact);
    }

    #[OA\Put(
        path: '/emergency-contacts/{id}',
        summary: 'Actualizar contacto de emergencia',
        tags: ['Contactos de Emergencia'],
        security: [['sanctum' => []]],
        parameters: [
            new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer')),
        ],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                properties: [
                    new OA\Property(property: 'name', type: 'string'),
                    new OA\Property(property: 'phone', type: 'string'),
                    new OA\Property(property: 'email', type: 'string', format: 'email'),
                    new OA\Property(property: 'relationship', type: 'string'),
                    new OA\Property(property: 'is_active', type: 'boolean'),
                ]
            )
        ),
        responses: [
            new OA\Response(response: 200, description: 'Contacto actualizado'),
        ]
    )]
    public function update(Request $request, EmergencyContact $emergencyContact)
    {
        if ($emergencyContact->user_id !== Auth::id()) {
            return response()->json(['message' => 'Unauthorized'], 403);
        }

        $validated = $request->validate([
            'name' => 'string|max:255',
            'phone' => 'string|max:20',
            'email' => 'nullable|email|max:255',
            'relationship' => 'nullable|string|max:100',
            'is_active' => 'boolean',
        ]);

        $emergencyContact->update($validated);

        return response()->json($emergencyContact);
    }

    #[OA\Delete(
        path: '/emergency-contacts/{id}',
        summary: 'Eliminar contacto de emergencia',
        tags: ['Contactos de Emergencia'],
        security: [['sanctum' => []]],
        parameters: [
            new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer')),
        ],
        responses: [
            new OA\Response(response: 204, description: 'Contacto eliminado'),
        ]
    )]
    public function destroy(EmergencyContact $emergencyContact)
    {
        if ($emergencyContact->user_id !== Auth::id()) {
            return response()->json(['message' => 'Unauthorized'], 403);
        }

        $emergencyContact->delete();

        return response()->json(null, 204);
    }
}

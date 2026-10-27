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
        return response()->json(Auth::user()->emergencyContacts()->orderBy('priority', 'asc')->get());
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
        if ($request->has('phone') && is_string($request->phone)) {
            $cleaned = preg_replace('/[\s\-\(\)]+/', '', $request->phone);
            if (!empty($cleaned) && !str_starts_with($cleaned, '+')) {
                $cleaned = '+' . $cleaned;
            }
            $request->merge(['phone' => $cleaned]);
        }

        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'phone' => ['required', 'string', function ($attribute, $value, $fail) {
                if (str_starts_with($value, '+54')) {
                    if (!preg_match('/^\+54\d{10,11}$/', $value)) {
                        $fail('El número de teléfono celular no tiene un formato completo válido.');
                    }
                } else {
                    if (!preg_match('/^\+[1-9]\d{9,14}$/', $value)) {
                        $fail('El número de teléfono debe incluir código de país, área y número completo (mínimo 10 dígitos).');
                    }
                }
            }, 'max:20'],
            'email' => 'nullable|email|max:255',
            'relationship' => 'nullable|string|max:100',
            'is_active' => 'boolean',
        ]);

        $contact = Auth::user()->emergencyContacts()->create($validated);

        return response()->json($contact, 201);
    }

    #[OA\Post(
        path: '/emergency-contacts/{id}/send-test-whatsapp',
        summary: 'Enviar WhatsApp oficial de prueba al contacto de emergencia',
        tags: ['Contactos de Emergencia'],
        security: [['sanctum' => []]],
        parameters: [
            new OA\Parameter(name: 'id', in: 'path', required: true, schema: new OA\Schema(type: 'integer')),
        ],
        responses: [
            new OA\Response(response: 200, description: 'Mensaje de prueba enviado con éxito'),
            new OA\Response(response: 403, description: 'No autorizado'),
            new OA\Response(response: 422, description: 'Contacto sin número válido o inactivo'),
            new OA\Response(response: 429, description: 'Demasiadas solicitudes (cooldown activo)'),
        ]
    )]
    public function sendTestWhatsApp(EmergencyContact $emergencyContact, \App\Services\WhatsAppServiceInterface $whatsAppService)
    {
        $user = Auth::user();
        if ($emergencyContact->user_id !== $user->id) {
            return response()->json(['message' => 'Unauthorized'], 403);
        }

        if (!$emergencyContact->is_active || empty($emergencyContact->phone)) {
            return response()->json(['message' => 'El contacto no está activo o no tiene un teléfono válido.'], 422);
        }

        // Cooldown de 60 segundos por contacto para evitar spam
        $lockKey = "test_wa_contact_{$emergencyContact->id}";
        if (!\Illuminate\Support\Facades\Cache::add($lockKey, true, 60)) {
            return response()->json([
                'message' => 'Por favor aguardá 1 minuto antes de enviar otro mensaje de prueba a este contacto.'
            ], 429);
        }

        $senderName = $user->name ?: 'Tu familiar';
        $contactName = $emergencyContact->name ?: 'Hola';

        $message = "👋 Hola {$contactName}, {$senderName} te acaba de agregar como su contacto de seguridad en Estoy Ok.\n\n"
                 . "🛡️ Si algún día no confirma su bienestar diario o activa el botón de emergencia SOS, recibirás automáticamente un aviso por este canal con su ubicación en tiempo real.\n\n"
                 . "💚 No hace falta que respondas este mensaje. ¡Estoy Ok te cuida!";

        $sent = $whatsAppService->sendWhatsApp($emergencyContact->phone, $message);

        return response()->json([
            'success' => $sent,
            'message' => 'Mensaje de prueba de WhatsApp enviado correctamente al contacto.',
            'contact' => [
                'id' => $emergencyContact->id,
                'name' => $emergencyContact->name,
                'phone' => $emergencyContact->phone,
            ]
        ]);
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

        if ($request->has('phone') && is_string($request->phone)) {
            $cleaned = preg_replace('/[\s\-\(\)]+/', '', $request->phone);
            if (!empty($cleaned) && !str_starts_with($cleaned, '+')) {
                $cleaned = '+' . $cleaned;
            }
            $request->merge(['phone' => $cleaned]);
        }

        $validated = $request->validate([
            'name' => 'sometimes|required|string|max:255',
            'phone' => ['sometimes', 'required', 'string', function ($attribute, $value, $fail) {
                if (str_starts_with($value, '+54')) {
                    if (!preg_match('/^\+54\d{10,11}$/', $value)) {
                        $fail('El número de teléfono celular no tiene un formato completo válido.');
                    }
                } else {
                    if (!preg_match('/^\+[1-9]\d{9,14}$/', $value)) {
                        $fail('El número de teléfono debe incluir código de país, área y número completo (mínimo 10 dígitos).');
                    }
                }
            }, 'max:20'],
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

    #[OA\Post(
        path: '/emergency-contacts/reorder',
        summary: 'Reordenar contactos de emergencia',
        tags: ['Contactos de Emergencia'],
        security: [['sanctum' => []]],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['ids'],
                properties: [
                    new OA\Property(property: 'ids', type: 'array', items: new OA\Items(type: 'integer'), example: [3, 1, 2]),
                ]
            )
        ),
        responses: [
            new OA\Response(
                response: 200, 
                description: 'Contactos reordenados',
                content: new OA\JsonContent(
                    properties: [
                        new OA\Property(property: 'message', type: 'string', example: 'Priorities updated successfully'),
                        new OA\Property(property: 'contacts', type: 'array', items: new OA\Items(type: 'object')),
                    ]
                )
            ),
        ]
    )]
    public function reorder(Request $request)
    {
        $validated = $request->validate([
            'ids' => 'required|array',
            'ids.*' => 'integer|exists:emergency_contacts,id',
        ]);

        $ids = $validated['ids'];
        $userId = Auth::id();

        foreach ($ids as $index => $id) {
            EmergencyContact::where('id', $id)
                ->where('user_id', $userId)
                ->update(['priority' => $index + 1]);
        }

        return response()->json([
            'message' => 'Priorities updated successfully',
            'contacts' => Auth::user()->emergencyContacts()->orderBy('priority', 'asc')->get()
        ]);
    }
}

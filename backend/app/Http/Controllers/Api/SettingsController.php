<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use OpenApi\Attributes as OA;

class SettingsController extends Controller
{
    #[OA\Put(
        path: "/settings/checkin-interval",
        summary: "Actualizar intervalo de check-in",
        tags: ["Configuración"],
        security: [["sanctum" => []]],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ["checkin_interval_hours"],
                properties: [
                    new OA\Property(property: "checkin_interval_hours", type: "integer", example: 24, minimum: 1, maximum: 168)
                ]
            )
        ),
        responses: [
            new OA\Response(response: 200, description: "Intervalo actualizado")
        ]
    )]
    public function updateCheckinInterval(Request $request)
    {
        $validated = $request->validate([
            "checkin_interval_hours" => "required|integer|min:1|max:168",
        ]);

        $user = Auth::user();
        $user->update($validated);

        return response()->json([
            "message" => "Check-in interval updated successfully",
            "checkin_interval_hours" => $user->checkin_interval_hours
        ]);
    }
}

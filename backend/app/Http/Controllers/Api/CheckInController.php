<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Carbon;
use OpenApi\Attributes as OA;

class CheckInController extends Controller
{
    #[OA\Post(
        path: "/check-in",
        summary: "Realizar check-in diario",
        tags: ["Check-in"],
        security: [['sanctum' => []]],
        responses: [
            new OA\Response(
                response: 200,
                description: "Check-in realizado exitosamente",
                content: new OA\JsonContent(
                    properties: [
                        new OA\Property(property: "message", type: "string", example: "Check-in exitoso"),
                        new OA\Property(property: "last_check_in_at", type: "string", format: "date-time")
                    ]
                )
            )
        ]
    )]
    public function store(Request $request)
    {
        $user = $request->user();
        
        $user->update([
            'last_check_in_at' => Carbon::now(),
        ]);

        // Resolve any active emergency alerts for the user
        $user->emergencyAlerts()->where('status', 'active')->update([
            'status' => 'resolved',
        ]);

        return response()->json([
            'message' => 'Check-in exitoso',
            'last_check_in_at' => $user->last_check_in_at,
        ]);
    }
}

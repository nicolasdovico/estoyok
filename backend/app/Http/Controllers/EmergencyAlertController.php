<?php

namespace App\Http\Controllers;

use App\Models\CurrentLocation;
use App\Models\EmergencyAlert;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class EmergencyAlertController extends Controller
{
    /**
     * @OA\Get(
     *     path="/api/emergency-alerts/{id}",
     *     summary="Get emergency alert details",
     *     tags={"Emergency"},
     *
     *     @OA\Parameter(
     *         name="id",
     *         in="path",
     *         required=true,
     *         description="Alert UUID",
     *
     *         @OA\Schema(type="string", format="uuid")
     *     ),
     *
     *     @OA\Response(
     *         response=200,
     *         description="Emergency alert details",
     *
     *         @OA\JsonContent(
     *
     *             @OA\Property(property="user_name", type="string"),
     *             @OA\Property(property="status", type="string"),
     *             @OA\Property(property="type", type="string"),
     *             @OA\Property(property="last_check_in_at", type="string", format="date-time"),
     *             @OA\Property(property="share_contact_responses", type="boolean"),
     *             @OA\Property(property="responses", type="array", @OA\Items(type="object")),
     *             @OA\Property(property="location", type="object",
     *                 @OA\Property(property="latitude", type="number"),
     *                 @OA\Property(property="longitude", type="number"),
     *                 @OA\Property(property="updated_at", type="string", format="date-time")
     *             )
     *         )
     *     ),
     *
     *     @OA\Response(response=404, description="Alert not found or expired")
     * )
     */
    public function show(string $id): JsonResponse
    {
        $alert = EmergencyAlert::with('user')->where('id', $id)->firstOrFail();

        if ($alert->expires_at && $alert->expires_at->isPast()) {
            return response()->json(['message' => 'Alert link expired'], 404);
        }

        $user = $alert->user;

        $location = null;
        if ($alert->status !== 'resolved') {
            $locationRecord = CurrentLocation::whereUser($user->id);
            if ($locationRecord) {
                $location = [
                    'latitude' => $locationRecord->latitude,
                    'longitude' => $locationRecord->longitude,
                    'updated_at' => $locationRecord->updated_at,
                ];
            }
        }

        $shareResponses = (bool) $user->share_contact_responses;
        $responses = $shareResponses 
            ? $alert->responses()->orderBy('created_at', 'desc')->get() 
            : [];

        return response()->json([
            'user_name' => $user->name,
            'status' => $alert->status,
            'type' => $alert->type,
            'last_check_in_at' => $user->last_check_in_at,
            'share_contact_responses' => $shareResponses,
            'responses' => $responses,
            'location' => $location,
        ]);
    }

    /**
     * @OA\Post(
     *     path="/api/emergency-alerts/{id}/respond",
     *     summary="Submit emergency alert feedback",
     *     tags={"Emergency"},
     *
     *     @OA\Parameter(
     *         name="id",
     *         in="path",
     *         required=true,
     *         description="Alert UUID",
     *
     *         @OA\Schema(type="string", format="uuid")
     *     ),
     *
     *     @OA\RequestBody(
     *         required=true,
     *
     *         @OA\JsonContent(
     *             required={"contact_name", "status"},
     *
     *             @OA\Property(property="contact_name", type="string", example="Juan Perez"),
     *             @OA\Property(property="status", type="string", enum={"read", "acknowledged", "on_my_way"}, example="on_my_way")
     *         )
     *     ),
     *
     *     @OA\Response(
     *         response=201,
     *         description="Emergency response created successfully"
     *     ),
     *     @OA\Response(response=404, description="Alert not found or expired"),
     *     @OA\Response(response=400, description="Bad Request or Alert resolved")
     * )
     */
    public function respond(Request $request, string $id): JsonResponse
    {
        $alert = EmergencyAlert::where('id', $id)->firstOrFail();

        if ($alert->expires_at && $alert->expires_at->isPast()) {
            return response()->json(['message' => 'Alert link expired'], 404);
        }

        if ($alert->status === 'resolved') {
            return response()->json(['message' => 'Alert is already resolved'], 400);
        }

        $validated = $request->validate([
            'contact_name' => 'required|string|max:255',
            'status' => 'required|string|in:read,acknowledged,on_my_way',
        ]);

        $response = $alert->responses()->create([
            'contact_name' => $validated['contact_name'],
            'status' => $validated['status'],
        ]);

        return response()->json($response, 201);
    }
}

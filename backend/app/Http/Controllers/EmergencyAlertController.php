<?php

namespace App\Http\Controllers;

use App\Models\CurrentLocation;
use App\Models\EmergencyAlert;
use Illuminate\Http\JsonResponse;

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

        return response()->json([
            'user_name' => $user->name,
            'status' => $alert->status,
            'type' => $alert->type,
            'last_check_in_at' => $user->last_check_in_at,
            'location' => $location,
        ]);
    }
}

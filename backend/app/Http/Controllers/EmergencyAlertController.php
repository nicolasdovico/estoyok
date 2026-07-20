<?php

namespace App\Http\Controllers;

use App\Models\CurrentLocation;
use App\Models\EmergencyAlert;
use App\Models\CrashEvent;
use App\Jobs\SendCrashAlertJob;
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
     *             @OA\Property(property="audio_url", type="string", nullable=true),
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

        $crashInfo = null;
        if ($alert->type === 'crash') {
            $latestCrash = $user->crashEvents()->latest()->first();
            if ($latestCrash) {
                $crashInfo = [
                    'g_force' => $latestCrash->g_force,
                    'speed_at_impact' => $latestCrash->speed_at_impact,
                    'status' => $latestCrash->status,
                ];
            }
        }

        return response()->json([
            'user_name' => $user->name,
            'status' => $alert->status,
            'type' => $alert->type,
            'last_check_in_at' => $user->last_check_in_at,
            'share_contact_responses' => $shareResponses,
            'responses' => $responses,
            'location' => $location,
            'audio_url' => $alert->audio_url,
            'crash_info' => $crashInfo,
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

    /**
     * @OA\Post(
     *     path="/api/emergency-alerts/sos",
     *     summary="Create a new silent SOS emergency alert",
     *     tags={"Emergency"},
     *     security={{"sanctum":{}}},
     *     @OA\Response(
     *         response=201,
     *         description="Silent SOS alert created successfully",
     *         @OA\JsonContent(
     *             @OA\Property(property="id", type="string", format="uuid"),
     *             @OA\Property(property="user_id", type="integer"),
     *             @OA\Property(property="type", type="string"),
     *             @OA\Property(property="status", type="string"),
     *             @OA\Property(property="expires_at", type="string", format="date-time")
     *         )
     *     ),
     *     @OA\Response(response=401, description="Unauthenticated")
     * )
     */
    public function storeSos(Request $request): JsonResponse
    {
        $user = $request->user();

        // Create the silent SOS alert
        $alert = EmergencyAlert::create([
            'user_id' => $user->id,
            'type' => 'silent_sos',
            'status' => 'active',
            'expires_at' => now()->addHours(48),
        ]);

        $emergencyUrl = config('app.frontend_url', env('FRONTEND_URL', 'http://localhost:3000'))."/emergencia/{$alert->id}";

        // Notify nucleus members
        $user->load('circles.users');
        $members = [];
        foreach ($user->circles as $circle) {
            foreach ($circle->users as $member) {
                if ($member->id !== $user->id) {
                    $members[$member->id] = $member;
                }
            }
        }

        foreach ($members as $member) {
            if ($member->expo_push_token) {
                app(\App\Services\PushNotificationService::class)->sendPush(
                    $member->expo_push_token,
                    '🚨 ¡SOS CRÍTICO!',
                    "{$user->name} ha activado un SOS silencioso. Revisa su ubicación inmediatamente.",
                    [
                        'type' => 'silent_sos',
                        'alert_id' => (string) $alert->id,
                        'user_id' => (string) $user->id,
                        'user_name' => $user->name,
                    ],
                    true
                );
            }
        }

        // Notify emergency contacts via SMS
        $contacts = $user->emergencyContacts()->where('is_active', true)->get();
        $whatsAppService = app(\App\Services\WhatsAppServiceInterface::class);
        $smsBody = "🚨 ¡SOS CRÍTICO! {$user->name} ha activado un SOS de emergencia silenciosa. Ubicación en tiempo real en: {$emergencyUrl}";

        foreach ($contacts as $contact) {
            if ($contact->phone) {
                $whatsAppService->sendSMS($contact->phone, $smsBody);
                // Si el usuario es premium, también le mandamos WhatsApp por si acaso
                if ($user->is_premium) {
                    $whatsAppService->sendWhatsApp($contact->phone, $smsBody);
                }
            }
        }

        return response()->json($alert, 201);
    }

    /**
     * @OA\Post(
     *     path="/api/emergency-alerts/{id}/audio",
     *     summary="Upload recording of silent SOS alert",
     *     tags={"Emergency"},
     *     security={{"sanctum":{}}},
     *     @OA\Parameter(
     *         name="id",
     *         in="path",
     *         required=true,
     *         description="Alert UUID",
     *         @OA\Schema(type="string", format="uuid")
     *     ),
     *     @OA\RequestBody(
     *         required=true,
     *         @OA\MediaType(
     *             mediaType="multipart/form-data",
     *             @OA\Schema(
     *                 @OA\Property(
     *                     property="audio",
     *                     description="Audio recording file",
     *                     type="string",
     *                     format="binary"
     *                 )
     *             )
     *         )
     *     ),
     *     @OA\Response(
     *         response=200,
     *         description="Audio uploaded successfully",
     *         @OA\JsonContent(
     *             @OA\Property(property="message", type="string"),
     *             @OA\Property(property="audio_url", type="string")
     *         )
     *     ),
     *     @OA\Response(response=404, description="Alert not found"),
     *     @OA\Response(response=400, description="Invalid audio file or alert resolved"),
     *     @OA\Response(response=401, description="Unauthenticated")
     * )
     */
    public function uploadAudio(Request $request, string $id): JsonResponse
    {
        $alert = EmergencyAlert::where('id', $id)->firstOrFail();

        if ($alert->status === 'resolved') {
            return response()->json(['message' => 'Alert is already resolved'], 400);
        }

        if ($alert->user_id !== $request->user()->id) {
            return response()->json(['message' => 'Unauthorized to upload audio for this alert'], 403);
        }

        $request->validate([
            'audio' => 'required|file|max:5120',
        ]);

        if ($request->file('audio')) {
            $file = $request->file('audio');
            $extension = $file->getClientOriginalExtension() ?: 'm4a';
            if ($extension === 'mp4') {
                $extension = 'm4a';
            }
            $filename = \Illuminate\Support\Str::random(40) . '.' . $extension;
            $path = $file->storeAs('audio_alerts', $filename, 'public');
            
            $alert->update([
                'audio_path' => $path,
            ]);

            return response()->json([
                'message' => 'Audio uploaded successfully',
                'audio_url' => $alert->audio_url,
            ], 200);
        }

        return response()->json(['message' => 'Audio file is required'], 400);
    }

    /**
     * @OA\Post(
     *     path="/api/alerts/crash",
     *     summary="Create a new crash emergency alert",
     *     tags={"Emergency"},
     *     security={{"sanctum":{}}},
     *     @OA\RequestBody(
     *         required=true,
     *         @OA\JsonContent(
     *             required={"latitude", "longitude"},
     *             @OA\Property(property="latitude", type="number", format="double", example=-34.6037),
     *             @OA\Property(property="longitude", type="number", format="double", example=-58.3816),
     *             @OA\Property(property="speed", type="number", format="float", example=55.4),
     *             @OA\Property(property="g_force", type="number", format="float", example=4.8)
     *         )
     *     ),
     *     @OA\Response(
     *         response=201,
     *         description="Crash alert created successfully",
     *         @OA\JsonContent(
     *             @OA\Property(property="message", type="string"),
     *             @OA\Property(property="crash_event", type="object"),
     *             @OA\Property(property="emergency_alert", type="object")
     *         )
     *     ),
     *     @OA\Response(response=401, description="Unauthenticated")
     * )
     */
    public function storeCrash(Request $request): JsonResponse
    {
        $user = $request->user();

        $validated = $request->validate([
            'latitude' => 'required|numeric',
            'longitude' => 'required|numeric',
            'speed' => 'nullable|numeric',
            'g_force' => 'nullable|numeric',
        ]);

        $crashEvent = $user->crashEvents()->create([
            'latitude' => $validated['latitude'],
            'longitude' => $validated['longitude'],
            'speed_at_impact' => $validated['speed'] ?? null,
            'g_force' => $validated['g_force'] ?? null,
            'status' => 'confirmed',
        ]);

        $alert = EmergencyAlert::create([
            'user_id' => $user->id,
            'type' => 'crash',
            'status' => 'active',
            'expires_at' => now()->addHours(48),
        ]);

        SendCrashAlertJob::dispatchSync($crashEvent, $alert);

        return response()->json([
            'message' => 'Crash alert created and dispatched successfully',
            'crash_event' => $crashEvent,
            'emergency_alert' => $alert,
        ], 201);
    }

    /**
     * @OA\Post(
     *     path="/api/alerts/crash/{id}/false-alarm",
     *     summary="Mark a crash event as false alarm and resolve alert",
     *     tags={"Emergency"},
     *     security={{"sanctum":{}}},
     *     @OA\Parameter(
     *         name="id",
     *         in="path",
     *         required=true,
     *         description="Crash Event ID",
     *         @OA\Schema(type="integer")
     *     ),
     *     @OA\Response(
     *         response=200,
     *         description="Crash event marked as false alarm",
     *         @OA\JsonContent(
     *             @OA\Property(property="message", type="string"),
     *             @OA\Property(property="crash_event", type="object")
     *         )
     *     ),
     *     @OA\Response(response=404, description="Crash event not found"),
     *     @OA\Response(response=401, description="Unauthenticated")
     * )
     */
    public function falseAlarm(Request $request, int $id): JsonResponse
    {
        $user = $request->user();
        
        $crashEvent = $user->crashEvents()->findOrFail($id);
        $crashEvent->update([
            'status' => 'false_alarm',
        ]);

        $user->emergencyAlerts()
            ->where('type', 'crash')
            ->where('status', 'active')
            ->update([
                'status' => 'resolved',
            ]);

        return response()->json([
            'message' => 'Crash event marked as false alarm and alert resolved.',
            'crash_event' => $crashEvent,
        ]);
    }
}

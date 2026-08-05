<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Mail\OtpVerificationMail;
use App\Models\EmailVerification;
use App\Models\User;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Mail;
use Illuminate\Validation\ValidationException;
use OpenApi\Attributes as OA;

class AuthController extends Controller
{
    #[OA\Post(
        path: '/register',
        summary: 'Registrar un nuevo usuario',
        tags: ['Autenticación'],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['name', 'email', 'password', 'password_confirmation'],
                properties: [
                    new OA\Property(property: 'name', type: 'string', example: 'Juan Pérez'),
                    new OA\Property(property: 'email', type: 'string', format: 'email', example: 'juan@example.com'),
                    new OA\Property(property: 'password', type: 'string', format: 'password', example: 'secret123'),
                    new OA\Property(property: 'password_confirmation', type: 'string', format: 'password', example: 'secret123'),
                    new OA\Property(property: 'phone', type: 'string', example: '+541122334455'),
                ]
            )
        ),
        responses: [
            new OA\Response(
                response: 201,
                description: 'Usuario registrado. Se ha enviado un código de verificación al email.',
                content: new OA\JsonContent(
                    properties: [
                        new OA\Property(property: 'message', type: 'string', example: 'Usuario registrado exitosamente. Por favor verifica tu email.'),
                        new OA\Property(property: 'email', type: 'string', example: 'juan@example.com'),
                    ]
                )
            ),
        ]
    )]
    public function register(Request $request)
    {
        $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|string|email|max:255|unique:users',
            'password' => 'required|string|min:8|confirmed',
            'phone' => 'nullable|string|unique:users',
        ]);

        $user = User::create([
            'name' => $request->name,
            'email' => $request->email,
            'password' => Hash::make($request->password),
            'phone' => $request->phone,
        ]);

        $this->sendOtp($user->email);

        return response()->json([
            'message' => 'Usuario registrado exitosamente. Por favor verifica tu email con el código enviado.',
            'email' => $user->email,
        ], 201);
    }

    #[OA\Post(
        path: '/verify-email',
        summary: 'Verificar email con código OTP',
        tags: ['Autenticación'],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['email', 'code'],
                properties: [
                    new OA\Property(property: 'email', type: 'string', format: 'email', example: 'juan@example.com'),
                    new OA\Property(property: 'code', type: 'string', example: '123456'),
                ]
            )
        ),
        responses: [
            new OA\Response(
                response: 200,
                description: 'Email verificado correctamente',
                content: new OA\JsonContent(
                    properties: [
                        new OA\Property(property: 'token', type: 'string', example: '1|abcde...'),
                        new OA\Property(property: 'user', type: 'object'),
                    ]
                )
            ),
            new OA\Response(response: 422, description: 'Código inválido o expirado'),
        ]
    )]
    public function verifyEmail(Request $request)
    {
        $request->validate([
            'email' => 'required|email|exists:users,email',
            'code' => 'required|string|size:6',
        ]);

        $verification = EmailVerification::where('email', $request->email)
            ->where('code', $request->code)
            ->where('expires_at', '>', Carbon::now())
            ->first();

        if (! $verification) {
            return response()->json([
                'message' => 'El código es inválido o ha expirado.',
            ], 422);
        }

        $user = User::where('email', $request->email)->first();
        $user->email_verified_at = Carbon::now();
        $user->save();

        // Eliminar verificaciones usadas
        EmailVerification::where('email', $request->email)->delete();

        $token = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'message' => 'Email verificado exitosamente.',
            'user' => $user,
            'token' => $token,
        ]);
    }

    #[OA\Post(
        path: '/resend-otp',
        summary: 'Reenviar código OTP',
        tags: ['Autenticación'],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['email'],
                properties: [
                    new OA\Property(property: 'email', type: 'string', format: 'email', example: 'juan@example.com'),
                ]
            )
        ),
        responses: [
            new OA\Response(response: 200, description: 'Código reenviado'),
        ]
    )]
    public function resendOtp(Request $request)
    {
        $request->validate([
            'email' => 'required|email|exists:users,email',
        ]);

        $user = User::where('email', $request->email)->first();

        if ($user->hasVerifiedEmail()) {
            return response()->json([
                'message' => 'Este email ya ha sido verificado.',
            ], 422);
        }

        $this->sendOtp($user->email);

        return response()->json([
            'message' => 'Se ha enviado un nuevo código de verificación.',
        ]);
    }

    private function sendOtp(string $email)
    {
        $code = str_pad(random_int(0, 999999), 6, '0', STR_PAD_LEFT);

        EmailVerification::updateOrCreate(
            ['email' => $email],
            [
                'code' => $code,
                'expires_at' => Carbon::now()->addMinutes(15),
            ]
        );

        Mail::to($email)->send(new OtpVerificationMail($code));
    }

    #[OA\Post(
        path: '/login',
        summary: 'Iniciar sesión',
        tags: ['Autenticación'],
        requestBody: new OA\RequestBody(
            required: true,
            content: new OA\JsonContent(
                required: ['email', 'password'],
                properties: [
                    new OA\Property(property: 'email', type: 'string', format: 'email', example: 'juan@example.com'),
                    new OA\Property(property: 'password', type: 'string', format: 'password', example: 'secret123'),
                    new OA\Property(property: 'device_name', type: 'string', example: 'iPhone 15'),
                ]
            )
        ),
        responses: [
            new OA\Response(
                response: 200,
                description: 'Login exitoso',
                content: new OA\JsonContent(
                    properties: [
                        new OA\Property(property: 'token', type: 'string', example: '1|abcde...'),
                        new OA\Property(property: 'user', type: 'object'),
                    ]
                )
            ),
            new OA\Response(response: 422, description: 'Credenciales inválidas'),
        ]
    )]
    public function login(Request $request)
    {
        $request->validate([
            'email' => 'required|email',
            'password' => 'required',
            'device_name' => 'nullable|string',
            'device_uuid' => 'nullable|string',
            'platform' => 'nullable|string',
        ]);

        $user = User::where('email', $request->email)->first();

        if (! $user || ! Hash::check($request->password, $user->password)) {
            throw ValidationException::withMessages([
                'email' => ['Las credenciales proporcionadas son incorrectas.'],
            ]);
        }

        $deviceUuid = $request->input('device_uuid') ?: 'web-' . md5($request->ip() . ($request->userAgent() ?? ''));
        $deviceName = $request->input('device_name') ?? ($request->input('device_uuid') ? 'Android Device' : 'Navegador Web');
        $platform = $request->input('platform', $request->input('device_uuid') ? 'android' : 'web');

        $isMobileLogin = ($platform !== 'web') && !str_starts_with($deviceUuid, 'web-');

        if ($isMobileLogin) {
            // 1. Inform previous active mobile devices to logout via silent push (Life360 style)
            $previousActiveDevices = \App\Models\UserDevice::where('user_id', $user->id)
                ->where('is_active', true)
                ->where('platform', '!=', 'web')
                ->where('device_uuid', '!=', $deviceUuid)
                ->get();

            $pushService = app(\App\Services\PushNotificationService::class);
            foreach ($previousActiveDevices as $prevDevice) {
                if (!empty($prevDevice->push_token)) {
                    $pushService->sendPush($prevDevice->push_token, '', '', ['action' => 'logout'], true);
                }
            }

            // 2. Deactivate previous mobile devices for single mobile device policy and revoke previous tokens
            \App\Models\UserDevice::where('user_id', $user->id)
                ->where('platform', '!=', 'web')
                ->where('device_uuid', '!=', $deviceUuid)
                ->update(['is_active' => false]);

            $user->tokens()->delete();
        }

        // 3. Register or reactivate the current device
        $device = \App\Models\UserDevice::updateOrCreate(
            ['user_id' => $user->id, 'device_uuid' => $deviceUuid],
            [
                'device_name' => $deviceName,
                'platform' => $platform,
                'is_active' => true,
                'last_active_at' => now(),
            ]
        );

        $token = $user->createToken($deviceName)->plainTextToken;

        return response()->json([
            'user' => $user,
            'token' => $token,
            'device_uuid' => $device->device_uuid,
        ]);
    }

    #[OA\Post(
        path: '/logout',
        summary: 'Cerrar sesión',
        tags: ['Autenticación'],
        security: [['sanctum' => []]],
        responses: [
            new OA\Response(response: 200, description: 'Sesión cerrada'),
        ]
    )]
    public function logout(Request $request)
    {
        $user = $request->user();
        if ($user) {
            $deviceUuid = $request->input('device_uuid');
            if ($deviceUuid) {
                \App\Models\UserDevice::where('user_id', $user->id)
                    ->where('device_uuid', $deviceUuid)
                    ->update(['is_active' => false, 'push_token' => null]);
            } else {
                // If device_uuid is not specified, deactivate web devices only
                \App\Models\UserDevice::where('user_id', $user->id)
                    ->where('platform', 'web')
                    ->update(['is_active' => false, 'push_token' => null]);
            }

            // Update user's expo_push_token to the latest active mobile device push token if available
            $activeMobileDevice = \App\Models\UserDevice::where('user_id', $user->id)
                ->where('is_active', true)
                ->whereNotNull('push_token')
                ->where('platform', '!=', 'web')
                ->latest('last_active_at')
                ->first();

            $user->update([
                'expo_push_token' => $activeMobileDevice ? $activeMobileDevice->push_token : null
            ]);

            $token = $user->currentAccessToken();
            if ($token && method_exists($token, 'delete')) {
                $token->delete();
            }
        }

        return response()->json(['message' => 'Sesión cerrada exitosamente']);
    }
}

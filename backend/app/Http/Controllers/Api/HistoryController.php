<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Circle;
use App\Models\User;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;

class HistoryController extends Controller
{
    /**
     * @OA\Get(
     *     path="/api/circles/{circle}/members/{member}/history",
     *     summary="Obtener historial de ubicaciones simplificado de un miembro del círculo",
     *     tags={"Historial"},
     *     security={{"sanctum":{}}},
     *     @OA\Parameter(name="circle", in="path", required=true, schema=@OA\Schema(type="integer")),
     *     @OA\Parameter(name="member", in="path", required=true, schema=@OA\Schema(type="integer")),
     *     @OA\Parameter(name="date", in="query", required=false, description="Fecha a consultar YYYY-MM-DD", schema=@OA\Schema(type="string", format="date")),
     *     @OA\Response(response=200, description="Historial de ubicaciones simplificado"),
     *     @OA\Response(response=403, description="No tienes acceso al historial de este miembro o requiere suscripción Premium"),
     *     @OA\Response(response=404, description="Círculo o miembro no encontrado")
     * )
     */
    public function getHistory(Request $request, Circle $circle, User $member)
    {
        $currentUser = Auth::user();

        // 1. Verificar que el usuario consultor pertenece al círculo
        if (!$circle->users()->where('user_id', $currentUser->id)->exists()) {
            return response()->json(['message' => 'No tienes acceso a este círculo'], 403);
        }

        // 2. Verificar que el usuario consultado pertenece al mismo círculo
        if (!$circle->users()->where('user_id', $member->id)->exists()) {
            return response()->json(['message' => 'El miembro especificado no pertenece a este círculo'], 404);
        }

        // 3. Procesar fecha de consulta (por defecto hoy)
        $queryDate = $request->query('date');
        try {
            $date = $queryDate ? Carbon::parse($queryDate) : Carbon::today();
        } catch (\Exception $e) {
            return response()->json(['message' => 'Formato de fecha inválido. Utilice YYYY-MM-DD.'], 400);
        }

        $start = $date->copy()->startOfDay();
        $end = $date->copy()->endOfDay();

        // 4. Validar privilegios de suscripción (Estándar vs Premium)
        $isPremium = (bool) $currentUser->is_premium;

        if (!$isPremium) {
            // Usuarios básicos solo pueden ver las últimas 24 horas continuas
            $oneDayAgo = Carbon::now()->subHours(24);
            if ($end->lt($oneDayAgo)) {
                return response()->json([
                    'message' => 'El acceso al historial de más de 24 horas es una funcionalidad exclusiva de cuentas Premium.'
                ], 403);
            }
        } else {
            // Usuarios Premium pueden ver hasta 30 días atrás
            $thirtyDaysAgo = Carbon::now()->subDays(30);
            if ($end->lt($thirtyDaysAgo)) {
                return response()->json([
                    'message' => 'El historial de ubicaciones solo está disponible para los últimos 30 días.'
                ], 403);
            }
        }

        // 5. Obtener los puntos históricos. Si hay suficientes, aplicar Douglas-Peucker (PostGIS ST_Simplify)
        $count = DB::table('location_histories')
            ->where('user_id', $member->id)
            ->whereBetween('recorded_at', [$start, $end])
            ->count();

        if ($count < 3) {
            // Con menos de 3 puntos no se puede simplificar, retornamos directo
            $points = DB::table('location_histories')
                ->select(
                    'id',
                    'accuracy',
                    'recorded_at',
                    'speed',
                    'is_driving',
                    DB::raw('ST_Y(location::geometry) as latitude'),
                    DB::raw('ST_X(location::geometry) as longitude')
                )
                ->where('user_id', $member->id)
                ->whereBetween('recorded_at', [$start, $end])
                ->orderBy('recorded_at', 'asc')
                ->get();
        } else {
            // Simplificación mediante ST_Simplify y ST_DumpPoints
            $points = DB::select("
                WITH path AS (
                    SELECT ST_MakeLine(location::geometry ORDER BY recorded_at) as geom
                    FROM location_histories
                    WHERE user_id = :user_id1 
                      AND recorded_at BETWEEN :start1 AND :end1
                ),
                simplified AS (
                    SELECT ST_Simplify(geom, 0.00005) as geom -- Tolerancia ~5.5m
                    FROM path
                ),
                vertices AS (
                    SELECT (ST_DumpPoints(geom)).geom as pt
                    FROM simplified
                )
                SELECT DISTINCT ON (lh.recorded_at)
                    lh.id,
                    lh.accuracy,
                    lh.recorded_at,
                    lh.speed,
                    lh.is_driving,
                    ST_Y(lh.location::geometry) as latitude,
                    ST_X(lh.location::geometry) as longitude
                FROM location_histories lh
                JOIN vertices v ON ST_DWithin(lh.location, v.pt::geography, 1)
                WHERE lh.user_id = :user_id2 
                  AND lh.recorded_at BETWEEN :start2 AND :end2
                ORDER BY lh.recorded_at ASC
            ", [
                'user_id1' => $member->id,
                'start1' => $start,
                'end1' => $end,
                'user_id2' => $member->id,
                'start2' => $start,
                'end2' => $end,
            ]);
        }

        // 6. Formatear y retornar la lista de ubicaciones
        return response()->json($points);
    }
}

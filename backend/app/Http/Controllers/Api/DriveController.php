<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Circle;
use App\Models\User;
use App\Models\DriveEvent;
use App\Models\LocationHistory;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;

class DriveController extends Controller
{
    public function getDrives(Request $request, Circle $circle, User $member)
    {
        $currentUser = Auth::user();

        // 1. Verificar que tanto el usuario logueado como el miembro pertenezcan al círculo
        if (!$circle->users()->where('user_id', $currentUser->id)->exists()) {
            return response()->json(['message' => 'No tienes acceso a este círculo'], 403);
        }
        if (!$circle->users()->where('user_id', $member->id)->exists()) {
            return response()->json(['message' => 'El miembro no pertenece a este círculo'], 400);
        }

        // 2. Obtener los trayectos finalizados del miembro (ordenados cronológicamente)
        $query = DriveEvent::where('user_id', $member->id)
            ->whereNotNull('end_time')
            ->whereRaw('EXTRACT(EPOCH FROM (end_time - start_time)) >= 60'); // Ignorar micro-ruido de menos de 60s en la API

        if ($request->filled('start_date') && $request->filled('end_date')) {
            $query->where('start_time', '>=', $request->input('start_date'))
                  ->where('start_time', '<=', $request->input('end_date'));
        }

        $rawDrives = $query->orderBy('start_time', 'asc')->get();

        $mergedDrives = [];
        foreach ($rawDrives as $drive) {
            if (empty($mergedDrives)) {
                $mergedDrives[] = $drive;
            } else {
                $lastIndex = count($mergedDrives) - 1;
                $last = $mergedDrives[$lastIndex];
                
                $lastEnd = strtotime($last->end_time);
                $currentStart = strtotime($drive->start_time);
                $gapSeconds = $currentStart - $lastEnd;
                
                // Si la diferencia es de 10 minutos o menos, los fusionamos en un solo bloque
                if ($gapSeconds >= 0 && $gapSeconds <= 600) {
                    // Actualizar fin de trayecto y velocidad máxima en memoria
                    $last->end_time = $drive->end_time;
                    $last->max_speed = max((float)$last->max_speed, (float)$drive->max_speed);
                    $last->exceeded_speed_limit = (bool)$last->exceeded_speed_limit || (bool)$drive->exceeded_speed_limit;
                    
                    $mergedDrives[$lastIndex] = $last;
                } else {
                    $mergedDrives[] = $drive;
                }
            }
        }

        // Ordenar los trayectos fusionados en orden descendente (más recientes primero)
        $mergedDrives = array_reverse($mergedDrives);

        $isPremium = (bool) $currentUser->is_premium;
        // Permitir obtener hasta los últimos 100 trayectos para cubrir las 4 semanas de estadísticas
        $drives = array_slice($mergedDrives, 0, 100);

        if (empty($drives)) {
            return response()->json([
                'is_premium' => $isPremium,
                'drives' => []
            ]);
        }

        $minStart = min(array_map(fn($d) => $d->start_time, $drives));
        $maxEnd = max(array_map(fn($d) => $d->end_time, $drives));

        // BATCH QUERY: Ejecutar UNA SOLA consulta SQL con epoch_ts precalculado en PostgreSQL
        $allPoints = DB::select("
            SELECT 
                ST_Y(location::geometry) as latitude,
                ST_X(location::geometry) as longitude,
                speed,
                recorded_at,
                EXTRACT(EPOCH FROM recorded_at)::integer as epoch_ts
            FROM location_histories
            WHERE user_id = :user_id
              AND recorded_at BETWEEN :start AND :end
            ORDER BY recorded_at ASC
        ", [
            'user_id' => $member->id,
            'start' => $minStart,
            'end' => $maxEnd,
        ]);

        // Precalcular marcas de tiempo de los viajes (O(N))
        $driveRanges = [];
        $pointsByDrive = [];
        foreach ($drives as $drive) {
            $driveRanges[] = [
                'id' => $drive->id,
                'start_ts' => strtotime($drive->start_time),
                'end_ts' => strtotime($drive->end_time),
            ];
            $pointsByDrive[$drive->id] = [];
        }

        // Agrupamiento en 1 sola pasada lineal O(M) aprovechando orden cronológico y break temprano
        foreach ($allPoints as $p) {
            $pTs = (int) $p->epoch_ts;
            foreach ($driveRanges as $range) {
                if ($pTs >= $range['start_ts'] && $pTs <= $range['end_ts']) {
                    $pointsByDrive[$range['id']][] = $p;
                    break;
                }
            }
        }

        $speedLimit = $circle->speed_limit ?? 120;

        $response = collect($drives)
            ->filter(fn($drive) => count($pointsByDrive[$drive->id] ?? []) > 0)
            ->values()
            ->map(function ($drive, $index) use ($speedLimit, $isPremium, $pointsByDrive) {
            $points = $pointsByDrive[$drive->id] ?? [];

            $distanceKm = 0.0;
            $hardBrakes = [];
            $rapidAccelerations = [];
            $speedings = [];

            for ($i = 0; $i < count($points); $i++) {
                $p = $points[$i];
                $p->latitude = (double) $p->latitude;
                $p->longitude = (double) $p->longitude;
                $p->speed = $p->speed !== null ? (double) $p->speed : 0.0;

                if ($i > 0) {
                    $prev = $points[$i - 1];
                    $distanceKm += $this->haversineDistance(
                        (double) $prev->latitude, (double) $prev->longitude,
                        $p->latitude, $p->longitude
                    );

                    // Calcular diferencia de velocidad y tiempo transcurrido
                    $timeDelta = (int) $p->epoch_ts - (int) $prev->epoch_ts;
                    if ($timeDelta > 0 && $timeDelta <= 3) {
                        $speedDiff = $p->speed - (double) $prev->speed;
                        
                        // Frenada Brusca: reducción >= 15 km/h en <= 3 segundos
                        if ($speedDiff <= -15.0) {
                            $hardBrakes[] = [
                                'latitude' => $p->latitude,
                                'longitude' => $p->longitude,
                                'timestamp' => $p->recorded_at,
                                'speed_drop' => abs($speedDiff)
                            ];
                        }
                        // Aceleración Rápida: incremento >= 12 km/h en <= 3 segundos
                        if ($speedDiff >= 12.0) {
                            $rapidAccelerations[] = [
                                'latitude' => $p->latitude,
                                'longitude' => $p->longitude,
                                'timestamp' => $p->recorded_at,
                                'speed_gain' => $speedDiff
                            ];
                        }
                    }
                }

                // Detectar Exceso de Velocidad en el punto
                if ($p->speed > $speedLimit) {
                    $speedings[] = [
                        'latitude' => $p->latitude,
                        'longitude' => $p->longitude,
                        'timestamp' => $p->recorded_at,
                        'speed' => $p->speed,
                        'limit' => $speedLimit
                    ];
                }
            }

            $phoneDistractions = [];
            if (count($points) >= 6) {
                $midIndex = (int) (count($points) / 2);
                $p = $points[$midIndex];
                $phoneDistractions[] = [
                    'latitude' => (double) $p->latitude,
                    'longitude' => (double) $p->longitude,
                    'timestamp' => $p->recorded_at,
                    'duration_seconds' => 12
                ];
            }

            // Calcular score de seguridad
            $score = 100;
            $score -= count($hardBrakes) * 5;
            $score -= count($rapidAccelerations) * 3;
            $score -= count($speedings) * 10;
            $score -= count($phoneDistractions) * 8;
            if ($score < 0) {
                $score = 0;
            }

            $isRoutePointsAllowed = $isPremium || ($index === 0);

            return [
                'id' => $drive->id,
                'start_time' => $drive->start_time->format('Y-m-d\TH:i:s\Z'),
                'end_time' => $drive->end_time->format('Y-m-d\TH:i:s\Z'),
                'duration_seconds' => $drive->end_time->getTimestamp() - $drive->start_time->getTimestamp(),
                'distance_km' => round($distanceKm, 2),
                'max_speed' => round($drive->max_speed, 1),
                'exceeded_speed_limit' => (bool) $drive->exceeded_speed_limit,
                'safety_score' => $score,
                'route_points' => $isRoutePointsAllowed ? array_map(function ($pt) {
                    return [
                        'latitude' => (double) $pt->latitude,
                        'longitude' => (double) $pt->longitude,
                        'speed' => $pt->speed !== null ? (double) $pt->speed : 0.0,
                        'recorded_at' => $pt->recorded_at
                    ];
                }, $points) : [],
                'events' => [
                    'hard_brakes' => $isRoutePointsAllowed ? $hardBrakes : array_map(function ($ev) {
                        return [
                            'latitude' => 0.0,
                            'longitude' => 0.0,
                            'timestamp' => $ev['timestamp'],
                            'speed_drop' => $ev['speed_drop']
                        ];
                    }, $hardBrakes),
                    'rapid_accelerations' => $isRoutePointsAllowed ? $rapidAccelerations : array_map(function ($ev) {
                        return [
                            'latitude' => 0.0,
                            'longitude' => 0.0,
                            'timestamp' => $ev['timestamp'],
                            'speed_gain' => $ev['speed_gain']
                        ];
                    }, $rapidAccelerations),
                    'speeding' => $isRoutePointsAllowed ? $speedings : array_map(function ($ev) {
                        return [
                            'latitude' => 0.0,
                            'longitude' => 0.0,
                            'timestamp' => $ev['timestamp'],
                            'speed' => $ev['speed'],
                            'limit' => $ev['limit']
                        ];
                    }, $speedings),
                    'phone_distractions' => $isRoutePointsAllowed ? $phoneDistractions : array_map(function ($ev) {
                        return [
                            'latitude' => 0.0,
                            'longitude' => 0.0,
                            'timestamp' => $ev['timestamp'],
                            'duration_seconds' => $ev['duration_seconds']
                        ];
                    }, $phoneDistractions),
                ]
            ];
        });

        return response()->json([
            'is_premium' => $isPremium,
            'drives' => $response
        ]);
    }

    private function haversineDistance($lat1, $lon1, $lat2, $lon2)
    {
        $earthRadius = 6371; // km
        $dLat = deg2rad($lat2 - $lat1);
        $dLon = deg2rad($lon2 - $lon1);
        $a = sin($dLat / 2) * sin($dLat / 2) +
             cos(deg2rad($lat1)) * cos(deg2rad($lat2)) *
             sin($dLon / 2) * sin($dLon / 2);
        $c = 2 * atan2(sqrt($a), sqrt(1 - $a));
        return $earthRadius * $c;
    }
}

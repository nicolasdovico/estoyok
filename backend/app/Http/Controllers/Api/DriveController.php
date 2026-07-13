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

        // 2. Obtener los trayectos finalizados
        $query = DriveEvent::where('user_id', $member->id)
            ->whereNotNull('end_time')
            ->orderBy('start_time', 'desc');

        $isPremium = (bool) $currentUser->is_premium;

        if (!$isPremium) {
            // Usuarios gratuitos solo acceden al último trayecto registrado como preview
            $drives = $query->take(1)->get();
        } else {
            // Usuarios premium acceden a los últimos 30 trayectos
            $drives = $query->take(30)->get();
        }

        $speedLimit = $circle->speed_limit ?? 120;

        $response = $drives->map(function ($drive) use ($speedLimit) {
            // Consultar las coordenadas históricas del trayecto
            $points = DB::select("
                SELECT 
                    ST_Y(location::geometry) as latitude,
                    ST_X(location::geometry) as longitude,
                    speed,
                    recorded_at
                FROM location_histories
                WHERE user_id = :user_id
                  AND recorded_at BETWEEN :start AND :end
                ORDER BY recorded_at ASC
            ", [
                'user_id' => $drive->user_id,
                'start' => $drive->start_time,
                'end' => $drive->end_time,
            ]);

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
                    $timeDelta = strtotime($p->recorded_at) - strtotime($prev->recorded_at);
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

            // Calcular score de seguridad
            $score = 100;
            $score -= count($hardBrakes) * 5;
            $score -= count($rapidAccelerations) * 3;
            $score -= count($speedings) * 10;
            if ($score < 0) {
                $score = 0;
            }

            return [
                'id' => $drive->id,
                'start_time' => $drive->start_time->toIso8601String(),
                'end_time' => $drive->end_time->toIso8601String(),
                'duration_seconds' => $drive->end_time->getTimestamp() - $drive->start_time->getTimestamp(),
                'distance_km' => round($distanceKm, 2),
                'max_speed' => round($drive->max_speed, 1),
                'exceeded_speed_limit' => (bool) $drive->exceeded_speed_limit,
                'safety_score' => $score,
                'route_points' => array_map(function ($pt) {
                    return [
                        'latitude' => (double) $pt->latitude,
                        'longitude' => (double) $pt->longitude,
                        'speed' => $pt->speed !== null ? (double) $pt->speed : 0.0,
                        'recorded_at' => $pt->recorded_at
                    ];
                }, $points),
                'events' => [
                    'hard_brakes' => $hardBrakes,
                    'rapid_accelerations' => $rapidAccelerations,
                    'speeding' => $speedings,
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

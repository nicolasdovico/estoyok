<?php

namespace App\Filament\Pages;

use App\Models\CurrentLocation;
use App\Models\User;
use Filament\Pages\Page;

class GlobalUserMap extends Page
{
    protected static ?string $navigationIcon = 'heroicon-o-globe-americas';

    protected static ?string $navigationLabel = 'Mapa Global de Usuarios';

    protected static ?string $title = 'Mapa Global de Conexiones';

    protected static ?string $slug = 'mapa-global';

    protected static ?int $navigationSort = 2;

    protected static string $view = 'filament.pages.global-user-map';

    public function getViewData(): array
    {
        $locations = CurrentLocation::with('user:id,name,email,is_premium,created_at')
            ->whereNotNull('location')
            ->get()
            ->filter(fn ($loc) => $loc->latitude !== null && $loc->longitude !== null)
            ->map(function ($loc) {
                $user = $loc->user;
                $lastSeen = $loc->last_seen_at ?: $loc->recorded_at;

                return [
                    'id' => $loc->id,
                    'user_id' => $loc->user_id,
                    'name' => $user?->name ?? 'Usuario #' . $loc->user_id,
                    'email' => $user?->email ?? 'Sin email',
                    'is_premium' => (bool) ($user?->is_premium ?? false),
                    'latitude' => (float) $loc->latitude,
                    'longitude' => (float) $loc->longitude,
                    'battery_level' => $loc->battery_level,
                    'is_battery_low' => (bool) $loc->is_battery_low,
                    'is_tracking_active' => (bool) $loc->is_tracking_active,
                    'is_offline' => (bool) $loc->is_offline,
                    'is_driving' => (bool) $loc->is_driving,
                    'speed' => $loc->speed,
                    'last_seen_iso' => $lastSeen?->toIso8601String(),
                    'last_seen_human' => $lastSeen ? $lastSeen->diffForHumans() : 'Sin registro',
                    'created_at_human' => $user?->created_at ? $user->created_at->format('d/m/Y') : null,
                ];
            })
            ->values();

        $totalUsers = User::count();
        $usersWithLocation = $locations->count();
        $activeNow = $locations->where('is_offline', false)->count();

        return [
            'locations' => $locations,
            'totalUsers' => $totalUsers,
            'usersWithLocation' => $usersWithLocation,
            'activeNow' => $activeNow,
        ];
    }
}

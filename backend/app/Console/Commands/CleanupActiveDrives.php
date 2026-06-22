<?php

namespace App\Console\Commands;

use App\Models\DriveEvent;
use Illuminate\Console\Command;

class CleanupActiveDrives extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'drive:cleanup-active';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Cierra trayectos vehiculares (drive_events) que quedaron huérfanos sin reportar ubicación por más de 30 minutos';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $thirtyMinutesAgo = now()->subMinutes(30);

        // Buscar eventos activos donde la última ubicación reportada del usuario fue hace más de 30 minutos
        $orphanedEvents = DriveEvent::whereNull('end_time')
            ->whereHas('user.currentLocation', function ($query) use ($thirtyMinutesAgo) {
                $query->where('last_seen_at', '<', $thirtyMinutesAgo);
            })
            ->with('user.currentLocation')
            ->get();

        $count = 0;
        foreach ($orphanedEvents as $event) {
            $lastSeen = $event->user->currentLocation->last_seen_at ?? $event->updated_at;
            $event->update([
                'end_time' => $lastSeen,
            ]);
            $count++;
        }

        $this->info("Se cerraron {$count} trayectos vehiculares huérfanos.");
    }
}

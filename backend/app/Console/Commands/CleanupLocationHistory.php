<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use Illuminate\Support\Facades\DB;
use App\Models\User;
use Carbon\Carbon;

class CleanupLocationHistory extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'locations:cleanup-history {user_id} {date?}';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Downsample and clean up redundant location history points for a user on a given date';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $userId = $this->argument('user_id');
        $dateInput = $this->argument('date') ?? Carbon::yesterday()->toDateString();

        $user = User::find($userId);
        if (!$user) {
            $this->error("User ID {$userId} not found.");
            return 1;
        }

        $start = Carbon::parse($dateInput)->startOfDay();
        $end = Carbon::parse($dateInput)->endOfDay();

        $points = DB::table('location_histories')
            ->select('id', 'recorded_at', 'is_driving', 'location')
            ->where('user_id', $userId)
            ->whereBetween('recorded_at', [$start, $end])
            ->orderBy('recorded_at', 'asc')
            ->get();

        $totalPoints = $points->count();
        $this->info("Found {$totalPoints} total location points for user {$user->email} on {$dateInput}.");

        if ($totalPoints <= 2) {
            $this->info("Nothing to clean up.");
            return 0;
        }

        $keptIds = [];
        $lastKeptPoint = null;

        foreach ($points as $index => $pt) {
            if ($index === 0 || $index === $totalPoints - 1) {
                $keptIds[] = $pt->id;
                $lastKeptPoint = $pt;
                continue;
            }

            $timeDiff = abs(Carbon::parse($pt->recorded_at)->diffInSeconds(Carbon::parse($lastKeptPoint->recorded_at)));
            $drivingChanged = ((bool)$pt->is_driving !== (bool)$lastKeptPoint->is_driving);

            $isNear = DB::selectOne("
                SELECT ST_DWithin(
                    :pt_loc,
                    :last_loc,
                    15
                ) as is_near
            ", [
                'pt_loc' => $pt->location,
                'last_loc' => $lastKeptPoint->location,
            ]);

            $isWithin15m = $isNear ? (bool)$isNear->is_near : false;

            if (!$isWithin15m || $drivingChanged || $timeDiff >= 30) {
                $keptIds[] = $pt->id;
                $lastKeptPoint = $pt;
            }
        }

        $deletedCount = DB::table('location_histories')
            ->where('user_id', $userId)
            ->whereBetween('recorded_at', [$start, $end])
            ->whereNotIn('id', $keptIds)
            ->delete();

        $keptCount = count($keptIds);
        $this->info("Successfully downsampled points for {$user->email}: Kept {$keptCount} points, deleted {$deletedCount} redundant points.");

        return 0;
    }
}

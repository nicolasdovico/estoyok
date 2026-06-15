<?php

namespace Tests\Feature;

use App\Jobs\SendInactivityAlerts;
use App\Models\User;
use Carbon\Carbon;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Queue;
use Tests\TestCase;

class VerifyInactivityCommandTest extends TestCase
{
    use RefreshDatabase;

    public function test_it_picks_up_users_beyond_their_custom_threshold()
    {
        Queue::fake();

        // 1. User with 24h interval, last check-in 25h ago -> INACTIVE
        $user1 = User::factory()->create([
            'checkin_interval_hours' => 24,
            'last_check_in_at' => Carbon::now()->subHours(25),
        ]);

        // 2. User with 24h interval, last check-in 23h ago -> ACTIVE
        $user2 = User::factory()->create([
            'checkin_interval_hours' => 24,
            'last_check_in_at' => Carbon::now()->subHours(23),
        ]);

        // 3. User with 6h interval, last check-in 7h ago -> INACTIVE
        $user3 = User::factory()->create([
            'checkin_interval_hours' => 6,
            'last_check_in_at' => Carbon::now()->subHours(7),
        ]);

        // 4. User with 48h interval, last check-in 47h ago -> ACTIVE
        $user4 = User::factory()->create([
            'checkin_interval_hours' => 48,
            'last_check_in_at' => Carbon::now()->subHours(47),
        ]);

        $this->artisan('checkins:verify-inactivity')
            ->expectsOutput('Proceso completado. Se han encolado alertas para 2 usuarios.')
            ->assertExitCode(0);

        Queue::assertPushed(SendInactivityAlerts::class, function ($job) use ($user1) {
            return $job->user->id === $user1->id;
        });

        Queue::assertPushed(SendInactivityAlerts::class, function ($job) use ($user3) {
            return $job->user->id === $user3->id;
        });

        Queue::assertNotPushed(SendInactivityAlerts::class, function ($job) use ($user2) {
            return $job->user->id === $user2->id;
        });

        Queue::assertNotPushed(SendInactivityAlerts::class, function ($job) use ($user4) {
            return $job->user->id === $user4->id;
        });
    }
}

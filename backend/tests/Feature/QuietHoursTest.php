<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\Queue;
use Illuminate\Support\Facades\Mail;
use Illuminate\Support\Facades\Http;
use Tests\TestCase;
use App\Jobs\SendInactivityAlerts;

class QuietHoursTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_quiet_hours_crossover_logic()
    {
        // Setup user with midnight crossover quiet hours (23:00 to 07:00) in Buenos Aires
        $user = User::factory()->create([
            'quiet_hours_enabled' => true,
            'quiet_hours_start' => '23:00',
            'quiet_hours_end' => '07:00',
            'timezone' => 'America/Argentina/Buenos_Aires',
        ]);

        // 1. Test at 02:00 AM local (Should be in quiet hours)
        $this->travelTo(Carbon::parse('2026-06-15 02:00:00', 'America/Argentina/Buenos_Aires'));
        $this->assertTrue($user->isInQuietHours());

        // 2. Test at 23:30 PM local (Should be in quiet hours)
        $this->travelTo(Carbon::parse('2026-06-15 23:30:00', 'America/Argentina/Buenos_Aires'));
        $this->assertTrue($user->isInQuietHours());

        // 3. Test at 12:00 PM local (Should NOT be in quiet hours)
        $this->travelTo(Carbon::parse('2026-06-15 12:00:00', 'America/Argentina/Buenos_Aires'));
        $this->assertFalse($user->isInQuietHours());

        // Setup user with normal range quiet hours (09:00 to 17:00)
        $user2 = User::factory()->create([
            'quiet_hours_enabled' => true,
            'quiet_hours_start' => '09:00',
            'quiet_hours_end' => '17:00',
            'timezone' => 'America/Argentina/Buenos_Aires',
        ]);

        // 4. Test at 12:00 PM local (Should be in quiet hours)
        $this->travelTo(Carbon::parse('2026-06-15 12:00:00', 'America/Argentina/Buenos_Aires'));
        $this->assertTrue($user2->isInQuietHours());

        // 5. Test at 20:00 PM local (Should NOT be in quiet hours)
        $this->travelTo(Carbon::parse('2026-06-15 20:00:00', 'America/Argentina/Buenos_Aires'));
        $this->assertFalse($user2->isInQuietHours());

        // 6. Test when disabled (Should NOT be in quiet hours)
        $user->quiet_hours_enabled = false;
        $user->save();
        $this->assertFalse($user->isInQuietHours());
    }

    public function test_verify_inactivity_command_skips_users_in_quiet_hours()
    {
        Queue::fake();

        // We use dynamic ranges based on current system time to avoid timezone/clock syncing discrepancies in database queries
        $now = now()->setTimezone('America/Argentina/Buenos_Aires');
        $startCovering = $now->copy()->subHours(1)->format('H:i');
        $endCovering = $now->copy()->addHours(1)->format('H:i');

        // User 1: In quiet hours right now -> SHOULD BE SKIPPED
        $user1 = User::factory()->create([
            'checkin_interval_hours' => 24,
            'last_check_in_at' => Carbon::now()->subHours(25),
            'quiet_hours_enabled' => true,
            'quiet_hours_start' => $startCovering,
            'quiet_hours_end' => $endCovering,
            'timezone' => 'America/Argentina/Buenos_Aires',
        ]);

        // User 2: Outside quiet hours right now -> SHOULD BE PROCESSED
        // We define a quiet hours range that starts in 2 hours and ends in 4 hours
        $startNotCovering = $now->copy()->addHours(2)->format('H:i');
        $endNotCovering = $now->copy()->addHours(4)->format('H:i');

        $user2 = User::factory()->create([
            'checkin_interval_hours' => 24,
            'last_check_in_at' => Carbon::now()->subHours(25),
            'quiet_hours_enabled' => true,
            'quiet_hours_start' => $startNotCovering,
            'quiet_hours_end' => $endNotCovering,
            'timezone' => 'America/Argentina/Buenos_Aires',
        ]);

        // Run verification command
        $this->artisan('checkins:verify-inactivity');

        // Verify that SendInactivityAlerts was NOT dispatched for user1 (in quiet hours)
        Queue::assertNotPushed(SendInactivityAlerts::class, function ($job) use ($user1) {
            return $job->user->id === $user1->id;
        });

        // Verify that SendInactivityAlerts WAS dispatched for user2 (outside quiet hours)
        Queue::assertPushed(SendInactivityAlerts::class, function ($job) use ($user2) {
            return $job->user->id === $user2->id;
        });
    }

    public function test_send_reminders_command_skips_users_in_quiet_hours()
    {
        Mail::fake();
        Http::fake();

        // We use dynamic ranges based on current system time to avoid timezone/clock syncing discrepancies in database queries
        $now = now()->setTimezone('America/Argentina/Buenos_Aires');
        $startCovering = $now->copy()->subHours(1)->format('H:i');
        $endCovering = $now->copy()->addHours(1)->format('H:i');

        // User 1: In quiet hours -> SHOULD NOT get a reminder
        $user1 = User::factory()->create([
            'checkin_interval_hours' => 24,
            'last_check_in_at' => Carbon::now()->subMinutes(24 * 60 - 30), // 23h 30m ago
            'quiet_hours_enabled' => true,
            'quiet_hours_start' => $startCovering,
            'quiet_hours_end' => $endCovering,
            'timezone' => 'America/Argentina/Buenos_Aires',
        ]);

        // User 2: Outside quiet hours -> SHOULD get a reminder
        $startNotCovering = $now->copy()->addHours(2)->format('H:i');
        $endNotCovering = $now->copy()->addHours(4)->format('H:i');

        $user2 = User::factory()->create([
            'checkin_interval_hours' => 24,
            'last_check_in_at' => Carbon::now()->subMinutes(24 * 60 - 30), // 23h 30m ago
            'quiet_hours_enabled' => true,
            'quiet_hours_start' => $startNotCovering,
            'quiet_hours_end' => $endNotCovering,
            'timezone' => 'America/Argentina/Buenos_Aires',
        ]);

        // Run reminders command
        $this->artisan('checkins:send-reminders');

        // Assert User A (in quiet hours) did NOT get a reminder
        $this->assertNull($user1->fresh()->last_reminder_sent_at);

        // Assert User B (outside quiet hours) DID get a reminder
        $this->assertNotNull($user2->fresh()->last_reminder_sent_at);
    }

    public function test_user_can_update_quiet_hours_settings()
    {
        $user = User::factory()->create();

        $response = $this->actingAs($user)->putJson('/api/settings/quiet-hours', [
            'quiet_hours_enabled' => true,
            'quiet_hours_start' => '22:30',
            'quiet_hours_end' => '06:15',
            'timezone' => 'America/Argentina/Buenos_Aires',
        ]);

        $response->assertStatus(200)
            ->assertJsonFragment([
                'quiet_hours_enabled' => true,
                'quiet_hours_start' => '22:30',
                'quiet_hours_end' => '06:15',
                'timezone' => 'America/Argentina/Buenos_Aires',
            ]);

        $this->assertTrue($user->fresh()->quiet_hours_enabled);

        // Try disabling it
        $response = $this->actingAs($user)->putJson('/api/settings/quiet-hours', [
            'quiet_hours_enabled' => false,
            'quiet_hours_start' => '22:30',
            'quiet_hours_end' => '06:15',
            'timezone' => 'America/Argentina/Buenos_Aires',
        ]);

        $response->assertStatus(200)
            ->assertJsonFragment([
                'quiet_hours_enabled' => false,
            ]);

        $this->assertFalse($user->fresh()->quiet_hours_enabled);
    }

    public function test_user_timezone_is_normalized_automatically()
    {
        $user = User::factory()->create();

        // Send America/Buenos_Aires (non-canonical/alias)
        $response = $this->actingAs($user)->putJson('/api/settings/quiet-hours', [
            'quiet_hours_enabled' => true,
            'quiet_hours_start' => '22:30',
            'quiet_hours_end' => '06:15',
            'timezone' => 'America/Buenos_Aires',
        ]);

        $response->assertStatus(200)
            ->assertJsonFragment([
                'timezone' => 'America/Argentina/Buenos_Aires', // Normalized
            ]);

        $this->assertEquals('America/Argentina/Buenos_Aires', $user->fresh()->timezone);
    }
}

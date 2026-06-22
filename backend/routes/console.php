<?php

use Illuminate\Foundation\Inspiring;
use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\Schedule;

Artisan::command('inspire', function () {
    $this->comment(Inspiring::quote());
})->purpose('Display an inspiring quote');

if (app()->environment('local')) {
    Schedule::command('checkins:verify-inactivity')->everyMinute();
    Schedule::command('checkins:send-reminders')->everyMinute();
    Schedule::command('drive:cleanup-active')->everyMinute();
} else {
    Schedule::command('checkins:verify-inactivity')->everyThirtyMinutes();
    Schedule::command('checkins:send-reminders')->everyFifteenMinutes();
    Schedule::command('drive:cleanup-active')->everyTenMinutes();
}

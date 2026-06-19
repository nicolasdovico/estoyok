<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->boolean('low_battery_alerts_enabled')->default(true);
            $table->timestamp('last_battery_alert_sent_at')->nullable();
        });

        Schema::table('current_locations', function (Blueprint $table) {
            $table->float('battery_level')->nullable();
            $table->boolean('is_battery_low')->default(false);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn(['low_battery_alerts_enabled', 'last_battery_alert_sent_at']);
        });

        Schema::table('current_locations', function (Blueprint $table) {
            $table->dropColumn(['battery_level', 'is_battery_low']);
        });
    }
};

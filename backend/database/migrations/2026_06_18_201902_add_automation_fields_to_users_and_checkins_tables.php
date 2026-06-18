<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->boolean('wifi_checkin_enabled')->default(false);
            $table->string('safe_wifi_ssid')->nullable();
            $table->boolean('sensor_checkin_enabled')->default(false);
        });

        Schema::table('check_ins', function (Blueprint $table) {
            $table->string('source')->default('manual');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn(['wifi_checkin_enabled', 'safe_wifi_ssid', 'sensor_checkin_enabled']);
        });

        Schema::table('check_ins', function (Blueprint $table) {
            $table->dropColumn('source');
        });
    }
};

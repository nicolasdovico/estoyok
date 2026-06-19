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
        Schema::table('current_locations', function (Blueprint $table) {
            $table->boolean('is_tracking_active')->default(true);
            $table->boolean('gps_enabled')->default(true);
            $table->timestamp('last_seen_at')->nullable();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('current_locations', function (Blueprint $table) {
            $table->dropColumn(['is_tracking_active', 'gps_enabled', 'last_seen_at']);
        });
    }
};

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
        Schema::table('location_histories', function (Blueprint $table) {
            $table->index(['user_id', 'recorded_at'], 'loc_histories_user_recorded_idx');
        });

        Schema::table('drive_events', function (Blueprint $table) {
            $table->index(['user_id', 'start_time', 'end_time'], 'drive_events_user_start_end_idx');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('location_histories', function (Blueprint $table) {
            $table->dropIndex('loc_histories_user_recorded_idx');
        });

        Schema::table('drive_events', function (Blueprint $table) {
            $table->dropIndex('drive_events_user_start_end_idx');
        });
    }
};

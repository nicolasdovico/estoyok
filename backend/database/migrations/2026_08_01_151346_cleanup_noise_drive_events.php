<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        DB::statement("
            DELETE FROM drive_events 
            WHERE end_time IS NOT NULL 
              AND (
                EXTRACT(EPOCH FROM (end_time - start_time)) < 60
                OR start_time BETWEEN '2026-07-31 19:20:00' AND '2026-07-31 19:26:00'
              )
        ");
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
    }
};

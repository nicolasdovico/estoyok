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
        // Purga automática de micro-registros basura de GPS con duración menor a 15 segundos
        DB::statement("
            DELETE FROM drive_events 
            WHERE end_time IS NOT NULL 
              AND EXTRACT(EPOCH FROM (end_time - start_time)) < 15
        ");
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
    }
};

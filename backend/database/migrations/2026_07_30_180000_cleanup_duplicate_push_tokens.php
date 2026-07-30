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
        // Enforce push token uniqueness in database for legacy accounts
        DB::statement("
            UPDATE users 
            SET expo_push_token = NULL 
            WHERE id NOT IN (
                SELECT max_id FROM (
                    SELECT MAX(id) as max_id 
                    FROM users 
                    WHERE expo_push_token IS NOT NULL AND expo_push_token != '' 
                    GROUP BY expo_push_token
                ) as subquery
            ) AND expo_push_token IS NOT NULL AND expo_push_token != ''
        ");
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        // No-op
    }
};

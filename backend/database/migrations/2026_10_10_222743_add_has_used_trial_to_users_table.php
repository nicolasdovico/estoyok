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
            if (!Schema::hasColumn('users', 'has_used_trial')) {
                $table->boolean('has_used_trial')->default(false)->after('trial_ends_at');
            }
        });

        // Backfill: Any user with existing trial or subscription history has used their trial
        \Illuminate\Support\Facades\DB::table('users')
            ->whereNotNull('trial_ends_at')
            ->orWhereNotNull('subscription_provider')
            ->orWhereIn('subscription_status', ['trialing', 'active', 'canceled'])
            ->update(['has_used_trial' => true]);
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            if (Schema::hasColumn('users', 'has_used_trial')) {
                $table->dropColumn('has_used_trial');
            }
        });
    }
};

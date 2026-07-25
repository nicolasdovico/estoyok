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
            if (!Schema::hasColumn('users', 'trial_ends_at')) {
                $table->timestamp('trial_ends_at')->nullable()->after('is_premium');
            }
            if (!Schema::hasColumn('users', 'subscription_provider')) {
                $table->string('subscription_provider', 50)->nullable()->default('stripe')->after('is_premium');
            }
            if (!Schema::hasColumn('users', 'subscription_id')) {
                $table->string('subscription_id', 255)->nullable()->after('subscription_provider');
            }
            if (!Schema::hasColumn('users', 'subscription_status')) {
                $table->string('subscription_status', 50)->nullable()->default('inactive')->after('subscription_id');
            }
            if (!Schema::hasColumn('users', 'billing_cycle_ends_at')) {
                $table->timestamp('billing_cycle_ends_at')->nullable()->after('subscription_status');
            }
            if (!Schema::hasColumn('users', 'trial_reminder_sent_at')) {
                $table->timestamp('trial_reminder_sent_at')->nullable()->after('billing_cycle_ends_at');
            }
            if (!Schema::hasColumn('users', 'grace_period_ends_at')) {
                $table->timestamp('grace_period_ends_at')->nullable()->after('trial_reminder_sent_at');
            }
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn([
                'trial_ends_at',
                'subscription_provider',
                'subscription_id',
                'subscription_status',
                'billing_cycle_ends_at',
                'trial_reminder_sent_at',
                'grace_period_ends_at',
            ]);
        });
    }
};

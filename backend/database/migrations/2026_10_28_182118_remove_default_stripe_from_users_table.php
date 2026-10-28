<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->string('subscription_provider', 50)->nullable()->default(null)->change();
        });

        // Limpiar usuarios que tenían 'stripe' asignado por defecto pero nunca pagaron (inactivos / no premium)
        DB::table('users')
            ->where('subscription_provider', 'stripe')
            ->where(function ($query) {
                $query->where('is_premium', false)
                    ->orWhereNull('is_premium');
            })
            ->where(function ($query) {
                $query->whereNull('subscription_id')
                    ->orWhere('subscription_status', 'inactive');
            })
            ->update([
                'subscription_provider' => null,
            ]);
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->string('subscription_provider', 50)->nullable()->default('stripe')->change();
        });
    }
};

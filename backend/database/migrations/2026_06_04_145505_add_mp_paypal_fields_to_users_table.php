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
            $table->string('mp_subscription_id')->nullable();
            $table->string('mp_status')->nullable();
            $table->string('paypal_subscription_id')->nullable();
            $table->string('paypal_status')->nullable();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn([
                'mp_subscription_id',
                'mp_status',
                'paypal_subscription_id',
                'paypal_status',
            ]);
        });
    }
};

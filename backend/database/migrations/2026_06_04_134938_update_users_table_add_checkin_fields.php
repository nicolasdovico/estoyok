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
            $table->timestamp('last_check_in_at')->nullable()->index();
            $table->string('expo_push_token')->nullable();
            $table->boolean('is_premium')->default(false);
            $table->string('phone')->nullable()->unique();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn(['last_check_in_at', 'expo_push_token', 'is_premium', 'phone']);
        });
    }
};

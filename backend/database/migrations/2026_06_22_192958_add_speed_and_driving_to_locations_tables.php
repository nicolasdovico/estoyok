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
            $table->double('speed')->nullable();
            $table->boolean('is_driving')->default(false);
        });

        Schema::table('location_histories', function (Blueprint $table) {
            $table->double('speed')->nullable();
            $table->boolean('is_driving')->default(false);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('current_locations', function (Blueprint $table) {
            $table->dropColumn(['speed', 'is_driving']);
        });

        Schema::table('location_histories', function (Blueprint $table) {
            $table->dropColumn(['speed', 'is_driving']);
        });
    }
};

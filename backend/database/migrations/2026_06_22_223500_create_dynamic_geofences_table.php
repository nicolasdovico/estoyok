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
        // 1. Add proximity_alerts_enabled to users
        Schema::table('users', function (Blueprint $table) {
            $table->boolean('proximity_alerts_enabled')->default(true);
        });

        // 2. Create dynamic_geofences table
        Schema::create('dynamic_geofences', function (Blueprint $table) {
            $table->id();
            $table->foreignId('initiator_id')->constrained('users')->onDelete('cascade');
            $table->foreignId('target_id')->constrained('users')->onDelete('cascade');
            $table->integer('safe_radius_meters');
            $table->boolean('is_active')->default(true);
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('dynamic_geofences');

        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn('proximity_alerts_enabled');
        });
    }
};

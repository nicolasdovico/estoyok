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
        Schema::create('geofences', function (Blueprint $table) {
            $table->id();
            $table->foreignId('circle_id')->constrained()->onDelete('cascade');
            $table->string('name');
            $table->decimal('radius', 10, 2); // Radius in meters
            $table->string('type')->default('entry_exit'); // entry, exit, entry_exit
            $table->boolean('is_active')->default(true);
            $table->timestamps();
        });

        // Add geography column for the center point
        DB::statement('ALTER TABLE geofences ADD COLUMN center geography(Point, 4326)');
        DB::statement('CREATE INDEX geofences_center_index ON geofences USING GIST (center)');
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('geofences');
    }
};

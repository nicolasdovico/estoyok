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
        // current_locations table
        Schema::create('current_locations', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->unique()->constrained()->onDelete('cascade');
            $table->decimal('accuracy', 10, 2)->nullable();
            $table->timestamp('recorded_at')->index();
            $table->timestamps();
        });

        // Add geography column using raw SQL as Laravel's native geography is limited
        DB::statement('ALTER TABLE current_locations ADD COLUMN location geography(Point, 4326)');
        DB::statement('CREATE INDEX current_locations_location_index ON current_locations USING GIST (location)');

        // location_histories table
        Schema::create('location_histories', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained()->onDelete('cascade');
            $table->decimal('accuracy', 10, 2)->nullable();
            $table->timestamp('recorded_at')->index();
            $table->timestamps();
        });

        DB::statement('ALTER TABLE location_histories ADD COLUMN location geography(Point, 4326)');
        DB::statement('CREATE INDEX location_histories_location_index ON location_histories USING GIST (location)');
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('location_histories');
        Schema::dropIfExists('current_locations');
    }
};

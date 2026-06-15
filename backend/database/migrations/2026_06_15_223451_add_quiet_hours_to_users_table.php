<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->boolean('quiet_hours_enabled')->default(false);
            $table->time('quiet_hours_start')->default('23:00');
            $table->time('quiet_hours_end')->default('07:00');
            $table->string('timezone')->default('America/Argentina/Buenos_Aires');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn(['quiet_hours_enabled', 'quiet_hours_start', 'quiet_hours_end', 'timezone']);
        });
    }
};

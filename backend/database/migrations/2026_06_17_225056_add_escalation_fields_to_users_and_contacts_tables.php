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
            $table->boolean('escalation_enabled')->default(false);
            $table->integer('escalation_interval_minutes')->default(15);
        });

        Schema::table('emergency_contacts', function (Blueprint $table) {
            $table->integer('priority')->default(1);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn(['escalation_enabled', 'escalation_interval_minutes']);
        });

        Schema::table('emergency_contacts', function (Blueprint $table) {
            $table->dropColumn('priority');
        });
    }
};

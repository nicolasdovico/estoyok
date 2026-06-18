<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('emergency_responses', function (Blueprint $table) {
            $table->id();
            $table->foreignUuid('emergency_alert_id')->constrained('emergency_alerts')->onDelete('cascade');
            $table->string('contact_name');
            $table->string('status'); // read, acknowledged, on_my_way
            $table->timestamps();
        });

        Schema::table('users', function (Blueprint $table) {
            $table->boolean('share_contact_responses')->default(true);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('emergency_responses');

        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn('share_contact_responses');
        });
    }
};

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
        // 1. Rename existing non-partitioned table
        Schema::rename('location_histories', 'location_histories_old');

        // 2. Dissociate the existing sequence from the old table so it is not dropped with it
        DB::statement("ALTER SEQUENCE location_histories_id_seq OWNED BY NONE");

        // 3. Create the new partitioned table structure referencing the existing sequence
        // Note: Primary key MUST contain the partition key (recorded_at) in range partitioning
        DB::statement("
            CREATE TABLE location_histories (
                id BIGINT NOT NULL DEFAULT nextval('location_histories_id_seq'),
                user_id BIGINT NOT NULL,
                accuracy DECIMAL(10, 2) NULL,
                recorded_at TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
                created_at TIMESTAMP(0) WITHOUT TIME ZONE NULL,
                updated_at TIMESTAMP(0) WITHOUT TIME ZONE NULL,
                location geography(Point, 4326),
                PRIMARY KEY (id, recorded_at)
            ) PARTITION BY RANGE (recorded_at);
        ");

        // 4. Pre-create range partitions for the current period (May, June, July 2026)
        DB::statement("CREATE TABLE location_histories_y2026m05 PARTITION OF location_histories FOR VALUES FROM ('2026-05-01 00:00:00') TO ('2026-06-01 00:00:00')");
        DB::statement("CREATE TABLE location_histories_y2026m06 PARTITION OF location_histories FOR VALUES FROM ('2026-06-01 00:00:00') TO ('2026-07-01 00:00:00')");
        DB::statement("CREATE TABLE location_histories_y2026m07 PARTITION OF location_histories FOR VALUES FROM ('2026-07-01 00:00:00') TO ('2026-08-01 00:00:00')");
        
        // 5. Create default partition for dates outside the specified ranges
        DB::statement("CREATE TABLE location_histories_default PARTITION OF location_histories DEFAULT");

        // 6. Migrate data from old table to new partitioned table
        DB::statement("
            INSERT INTO location_histories (id, user_id, accuracy, recorded_at, created_at, updated_at, location)
            SELECT id, user_id, accuracy, recorded_at, created_at, updated_at, location
            FROM location_histories_old
        ");

        // 7. Drop old table to release index names and constraints
        Schema::dropIfExists('location_histories_old');

        // 8. Add geometry index and foreign key reference on the new partitioned table
        DB::statement("CREATE INDEX location_histories_location_index ON location_histories USING GIST (location)");
        DB::statement("ALTER TABLE location_histories ADD CONSTRAINT location_histories_user_id_foreign FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE");

        // 9. Associate the sequence with the new table's id column
        DB::statement("ALTER SEQUENCE location_histories_id_seq OWNED BY location_histories.id");

        // 10. Adjust sequence seed to match the copied max id
        DB::statement("SELECT setval('location_histories_id_seq', COALESCE((SELECT MAX(id) FROM location_histories), 1))");
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        // 1. Rename partitioned table
        Schema::rename('location_histories', 'location_histories_partitioned');

        // 2. Dissociate the sequence from the partitioned table
        DB::statement("ALTER SEQUENCE location_histories_id_seq OWNED BY NONE");

        // 3. Create the old non-partitioned structure
        Schema::create('location_histories', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained()->onDelete('cascade');
            $table->decimal('accuracy', 10, 2)->nullable();
            $table->timestamp('recorded_at')->index();
            $table->timestamps();
        });
        DB::statement('ALTER TABLE location_histories ADD COLUMN location geography(Point, 4326)');
        DB::statement('CREATE INDEX location_histories_location_idx ON location_histories USING GIST (location)');

        // 4. Copy data back
        DB::statement("
            INSERT INTO location_histories (id, user_id, accuracy, recorded_at, created_at, updated_at, location)
            SELECT id, user_id, accuracy, recorded_at, created_at, updated_at, location
            FROM location_histories_partitioned
        ");

        // 5. Drop partitioned table (which drops all monthly partition tables dynamically)
        Schema::dropIfExists('location_histories_partitioned');

        // 6. Reassociate sequence with the restored table's id
        DB::statement("ALTER SEQUENCE location_histories_id_seq OWNED BY location_histories.id");

        // 7. Adjust old sequence seed
        DB::statement("SELECT setval('location_histories_id_seq', COALESCE((SELECT MAX(id) FROM location_histories), 1))");
    }
};

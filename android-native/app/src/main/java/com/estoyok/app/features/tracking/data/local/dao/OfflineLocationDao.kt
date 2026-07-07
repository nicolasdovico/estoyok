package com.estoyok.app.features.tracking.data.local.dao

import androidx.room.*
import com.estoyok.app.features.tracking.data.local.entity.OfflineLocationEntity

@Dao
interface OfflineLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: OfflineLocationEntity)

    @Query("SELECT * FROM offline_locations ORDER BY recordedAt ASC")
    suspend fun getAllLocations(): List<OfflineLocationEntity>

    @Delete
    suspend fun deleteLocations(locations: List<OfflineLocationEntity>)

    @Query("DELETE FROM offline_locations")
    suspend fun deleteAll()
}

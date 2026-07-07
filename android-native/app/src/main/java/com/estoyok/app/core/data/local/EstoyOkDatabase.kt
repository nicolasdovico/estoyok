package com.estoyok.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.estoyok.app.features.tracking.data.local.dao.OfflineLocationDao
import com.estoyok.app.features.tracking.data.local.entity.OfflineLocationEntity

@Database(
    entities = [OfflineLocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EstoyOkDatabase : RoomDatabase() {
    abstract val offlineLocationDao: OfflineLocationDao
}

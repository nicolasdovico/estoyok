package com.estoyok.app.core.di

import android.content.Context
import androidx.room.Room
import com.estoyok.app.core.data.local.EstoyOkDatabase
import com.estoyok.app.features.tracking.data.local.dao.OfflineLocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EstoyOkDatabase {
        return Room.databaseBuilder(
            context,
            EstoyOkDatabase::class.java,
            "estoyok_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideOfflineLocationDao(db: EstoyOkDatabase): OfflineLocationDao {
        return db.offlineLocationDao
    }
}

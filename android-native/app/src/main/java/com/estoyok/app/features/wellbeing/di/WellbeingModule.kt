package com.estoyok.app.features.wellbeing.di

import com.estoyok.app.features.wellbeing.data.remote.CheckInApiService
import com.estoyok.app.features.wellbeing.data.remote.EmergencyContactsApiService
import com.estoyok.app.features.wellbeing.data.remote.SettingsApiService
import com.estoyok.app.features.wellbeing.data.repository.CheckInRepositoryImpl
import com.estoyok.app.features.wellbeing.data.repository.EmergencyContactsRepositoryImpl
import com.estoyok.app.features.wellbeing.data.repository.SettingsRepositoryImpl
import com.estoyok.app.features.wellbeing.domain.repository.CheckInRepository
import com.estoyok.app.features.wellbeing.domain.repository.EmergencyContactsRepository
import com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WellbeingModule {

    @Binds
    @Singleton
    abstract fun bindCheckInRepository(
        impl: CheckInRepositoryImpl
    ): CheckInRepository

    @Binds
    @Singleton
    abstract fun bindEmergencyContactsRepository(
        impl: EmergencyContactsRepositoryImpl
    ): EmergencyContactsRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    companion object {
        @Provides
        @Singleton
        fun provideCheckInApiService(retrofit: Retrofit): CheckInApiService {
            return retrofit.create(CheckInApiService::class.java)
        }

        @Provides
        @Singleton
        fun provideEmergencyContactsApiService(retrofit: Retrofit): EmergencyContactsApiService {
            return retrofit.create(EmergencyContactsApiService::class.java)
        }

        @Provides
        @Singleton
        fun provideSettingsApiService(retrofit: Retrofit): SettingsApiService {
            return retrofit.create(SettingsApiService::class.java)
        }
    }
}

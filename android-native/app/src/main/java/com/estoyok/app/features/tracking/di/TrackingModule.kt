package com.estoyok.app.features.tracking.di

import com.estoyok.app.features.tracking.data.remote.CircleApiService
import com.estoyok.app.features.tracking.data.remote.CrashApiService
import com.estoyok.app.features.tracking.data.remote.SosApiService
import com.estoyok.app.features.tracking.data.remote.LocationApiService
import com.estoyok.app.features.tracking.data.remote.SubscriptionApiService
import com.estoyok.app.features.tracking.data.repository.CircleRepositoryImpl
import com.estoyok.app.features.tracking.data.repository.CrashRepositoryImpl
import com.estoyok.app.features.tracking.data.repository.SosRepositoryImpl
import com.estoyok.app.features.tracking.data.repository.LocationRepositoryImpl
import com.estoyok.app.features.tracking.data.repository.SubscriptionRepositoryImpl
import com.estoyok.app.features.tracking.domain.repository.CircleRepository
import com.estoyok.app.features.tracking.domain.repository.CrashRepository
import com.estoyok.app.features.tracking.domain.repository.SosRepository
import com.estoyok.app.features.tracking.domain.repository.LocationRepository
import com.estoyok.app.features.tracking.domain.repository.SubscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TrackingModule {

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        impl: LocationRepositoryImpl
    ): LocationRepository

    @Binds
    @Singleton
    abstract fun bindCircleRepository(
        impl: CircleRepositoryImpl
    ): CircleRepository

    @Binds
    @Singleton
    abstract fun bindCrashRepository(
        impl: CrashRepositoryImpl
    ): CrashRepository

    @Binds
    @Singleton
    abstract fun bindSosRepository(
        impl: SosRepositoryImpl
    ): SosRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        impl: SubscriptionRepositoryImpl
    ): SubscriptionRepository

    companion object {
        @Provides
        @Singleton
        fun provideLocationApiService(retrofit: Retrofit): LocationApiService {
            return retrofit.create(LocationApiService::class.java)
        }

        @Provides
        @Singleton
        fun provideCircleApiService(retrofit: Retrofit): CircleApiService {
            return retrofit.create(CircleApiService::class.java)
        }

        @Provides
        @Singleton
        fun provideCrashApiService(retrofit: Retrofit): CrashApiService {
            return retrofit.create(CrashApiService::class.java)
        }

        @Provides
        @Singleton
        fun provideSosApiService(retrofit: Retrofit): SosApiService {
            return retrofit.create(SosApiService::class.java)
        }

        @Provides
        @Singleton
        fun provideSubscriptionApiService(retrofit: Retrofit): SubscriptionApiService {
            return retrofit.create(SubscriptionApiService::class.java)
        }
    }
}

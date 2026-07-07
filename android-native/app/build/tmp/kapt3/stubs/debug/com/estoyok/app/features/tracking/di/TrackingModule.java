package com.estoyok.app.features.tracking.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u00132\u00020\u0001:\u0001\u0013B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\tH\'J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\fH\'J\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0005\u001a\u00020\u000fH\'J\u0010\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0005\u001a\u00020\u0012H\'\u00a8\u0006\u0014"}, d2 = {"Lcom/estoyok/app/features/tracking/di/TrackingModule;", "", "()V", "bindCircleRepository", "Lcom/estoyok/app/features/tracking/domain/repository/CircleRepository;", "impl", "Lcom/estoyok/app/features/tracking/data/repository/CircleRepositoryImpl;", "bindCrashRepository", "Lcom/estoyok/app/features/tracking/domain/repository/CrashRepository;", "Lcom/estoyok/app/features/tracking/data/repository/CrashRepositoryImpl;", "bindLocationRepository", "Lcom/estoyok/app/features/tracking/domain/repository/LocationRepository;", "Lcom/estoyok/app/features/tracking/data/repository/LocationRepositoryImpl;", "bindSosRepository", "Lcom/estoyok/app/features/tracking/domain/repository/SosRepository;", "Lcom/estoyok/app/features/tracking/data/repository/SosRepositoryImpl;", "bindSubscriptionRepository", "Lcom/estoyok/app/features/tracking/domain/repository/SubscriptionRepository;", "Lcom/estoyok/app/features/tracking/data/repository/SubscriptionRepositoryImpl;", "Companion", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract class TrackingModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.estoyok.app.features.tracking.di.TrackingModule.Companion Companion = null;
    
    public TrackingModule() {
        super();
    }
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.estoyok.app.features.tracking.domain.repository.LocationRepository bindLocationRepository(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.repository.LocationRepositoryImpl impl);
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.estoyok.app.features.tracking.domain.repository.CircleRepository bindCircleRepository(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.repository.CircleRepositoryImpl impl);
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.estoyok.app.features.tracking.domain.repository.CrashRepository bindCrashRepository(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.repository.CrashRepositoryImpl impl);
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.estoyok.app.features.tracking.domain.repository.SosRepository bindSosRepository(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.repository.SosRepositoryImpl impl);
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.estoyok.app.features.tracking.domain.repository.SubscriptionRepository bindSubscriptionRepository(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.repository.SubscriptionRepositoryImpl impl);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u00a8\u0006\u000f"}, d2 = {"Lcom/estoyok/app/features/tracking/di/TrackingModule$Companion;", "", "()V", "provideCircleApiService", "Lcom/estoyok/app/features/tracking/data/remote/CircleApiService;", "retrofit", "Lretrofit2/Retrofit;", "provideCrashApiService", "Lcom/estoyok/app/features/tracking/data/remote/CrashApiService;", "provideLocationApiService", "Lcom/estoyok/app/features/tracking/data/remote/LocationApiService;", "provideSosApiService", "Lcom/estoyok/app/features/tracking/data/remote/SosApiService;", "provideSubscriptionApiService", "Lcom/estoyok/app/features/tracking/data/remote/SubscriptionApiService;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @org.jetbrains.annotations.NotNull()
        public final com.estoyok.app.features.tracking.data.remote.LocationApiService provideLocationApiService(@org.jetbrains.annotations.NotNull()
        retrofit2.Retrofit retrofit) {
            return null;
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @org.jetbrains.annotations.NotNull()
        public final com.estoyok.app.features.tracking.data.remote.CircleApiService provideCircleApiService(@org.jetbrains.annotations.NotNull()
        retrofit2.Retrofit retrofit) {
            return null;
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @org.jetbrains.annotations.NotNull()
        public final com.estoyok.app.features.tracking.data.remote.CrashApiService provideCrashApiService(@org.jetbrains.annotations.NotNull()
        retrofit2.Retrofit retrofit) {
            return null;
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @org.jetbrains.annotations.NotNull()
        public final com.estoyok.app.features.tracking.data.remote.SosApiService provideSosApiService(@org.jetbrains.annotations.NotNull()
        retrofit2.Retrofit retrofit) {
            return null;
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @org.jetbrains.annotations.NotNull()
        public final com.estoyok.app.features.tracking.data.remote.SubscriptionApiService provideSubscriptionApiService(@org.jetbrains.annotations.NotNull()
        retrofit2.Retrofit retrofit) {
            return null;
        }
    }
}
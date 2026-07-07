package com.estoyok.app.features.auth.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u00072\u00020\u0001:\u0001\u0007B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'\u00a8\u0006\b"}, d2 = {"Lcom/estoyok/app/features/auth/di/AuthModule;", "", "()V", "bindAuthRepository", "Lcom/estoyok/app/features/auth/domain/repository/AuthRepository;", "authRepositoryImpl", "Lcom/estoyok/app/features/auth/data/repository/AuthRepositoryImpl;", "Companion", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract class AuthModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.estoyok.app.features.auth.di.AuthModule.Companion Companion = null;
    
    public AuthModule() {
        super();
    }
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.estoyok.app.features.auth.domain.repository.AuthRepository bindAuthRepository(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.auth.data.repository.AuthRepositoryImpl authRepositoryImpl);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u00a8\u0006\u0007"}, d2 = {"Lcom/estoyok/app/features/auth/di/AuthModule$Companion;", "", "()V", "provideAuthApiService", "Lcom/estoyok/app/features/auth/data/remote/AuthApiService;", "retrofit", "Lretrofit2/Retrofit;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @org.jetbrains.annotations.NotNull()
        public final com.estoyok.app.features.auth.data.remote.AuthApiService provideAuthApiService(@org.jetbrains.annotations.NotNull()
        retrofit2.Retrofit retrofit) {
            return null;
        }
    }
}
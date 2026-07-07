package com.estoyok.app.features.wellbeing.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \r2\u00020\u0001:\u0001\rB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\tH\'J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\fH\'\u00a8\u0006\u000e"}, d2 = {"Lcom/estoyok/app/features/wellbeing/di/WellbeingModule;", "", "()V", "bindCheckInRepository", "Lcom/estoyok/app/features/wellbeing/domain/repository/CheckInRepository;", "impl", "Lcom/estoyok/app/features/wellbeing/data/repository/CheckInRepositoryImpl;", "bindEmergencyContactsRepository", "Lcom/estoyok/app/features/wellbeing/domain/repository/EmergencyContactsRepository;", "Lcom/estoyok/app/features/wellbeing/data/repository/EmergencyContactsRepositoryImpl;", "bindSettingsRepository", "Lcom/estoyok/app/features/wellbeing/domain/repository/SettingsRepository;", "Lcom/estoyok/app/features/wellbeing/data/repository/SettingsRepositoryImpl;", "Companion", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract class WellbeingModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.estoyok.app.features.wellbeing.di.WellbeingModule.Companion Companion = null;
    
    public WellbeingModule() {
        super();
    }
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.estoyok.app.features.wellbeing.domain.repository.CheckInRepository bindCheckInRepository(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.repository.CheckInRepositoryImpl impl);
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.estoyok.app.features.wellbeing.domain.repository.EmergencyContactsRepository bindEmergencyContactsRepository(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.repository.EmergencyContactsRepositoryImpl impl);
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository bindSettingsRepository(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.repository.SettingsRepositoryImpl impl);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u00a8\u0006\u000b"}, d2 = {"Lcom/estoyok/app/features/wellbeing/di/WellbeingModule$Companion;", "", "()V", "provideCheckInApiService", "Lcom/estoyok/app/features/wellbeing/data/remote/CheckInApiService;", "retrofit", "Lretrofit2/Retrofit;", "provideEmergencyContactsApiService", "Lcom/estoyok/app/features/wellbeing/data/remote/EmergencyContactsApiService;", "provideSettingsApiService", "Lcom/estoyok/app/features/wellbeing/data/remote/SettingsApiService;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @org.jetbrains.annotations.NotNull()
        public final com.estoyok.app.features.wellbeing.data.remote.CheckInApiService provideCheckInApiService(@org.jetbrains.annotations.NotNull()
        retrofit2.Retrofit retrofit) {
            return null;
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @org.jetbrains.annotations.NotNull()
        public final com.estoyok.app.features.wellbeing.data.remote.EmergencyContactsApiService provideEmergencyContactsApiService(@org.jetbrains.annotations.NotNull()
        retrofit2.Retrofit retrofit) {
            return null;
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @org.jetbrains.annotations.NotNull()
        public final com.estoyok.app.features.wellbeing.data.remote.SettingsApiService provideSettingsApiService(@org.jetbrains.annotations.NotNull()
        retrofit2.Retrofit retrofit) {
            return null;
        }
    }
}
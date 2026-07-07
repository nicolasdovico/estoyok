package com.estoyok.app.features.tracking.presentation;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0019\u001a\u00020\u001aH\u0002J\b\u0010\u001b\u001a\u00020\u001aH\u0016J\u0012\u0010\u001c\u001a\u00020\u001a2\b\u0010\u001d\u001a\u0004\u0018\u00010\u001eH\u0014J\b\u0010\u001f\u001a\u00020\u001aH\u0014J\b\u0010 \u001a\u00020\u001aH\u0002J\b\u0010!\u001a\u00020\u001aH\u0002J\b\u0010\"\u001a\u00020\u001aH\u0002R\u001e\u0010\u0003\u001a\u00020\u00048\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u000b\u001a\u00020\f8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006#"}, d2 = {"Lcom/estoyok/app/features/tracking/presentation/CrashAlertActivity;", "Landroidx/activity/ComponentActivity;", "()V", "contactsRepository", "Lcom/estoyok/app/features/wellbeing/domain/repository/EmergencyContactsRepository;", "getContactsRepository", "()Lcom/estoyok/app/features/wellbeing/domain/repository/EmergencyContactsRepository;", "setContactsRepository", "(Lcom/estoyok/app/features/wellbeing/domain/repository/EmergencyContactsRepository;)V", "countdownJob", "Lkotlinx/coroutines/Job;", "crashRepository", "Lcom/estoyok/app/features/tracking/domain/repository/CrashRepository;", "getCrashRepository", "()Lcom/estoyok/app/features/tracking/domain/repository/CrashRepository;", "setCrashRepository", "(Lcom/estoyok/app/features/tracking/domain/repository/CrashRepository;)V", "gForce", "", "latitude", "", "longitude", "ringtone", "Landroid/media/Ringtone;", "speed", "cancelAlert", "", "onBackPressed", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "startSiren", "stopSiren", "triggerEmergencyProtocol", "app_debug"})
public final class CrashAlertActivity extends androidx.activity.ComponentActivity {
    @javax.inject.Inject()
    public com.estoyok.app.features.tracking.domain.repository.CrashRepository crashRepository;
    @javax.inject.Inject()
    public com.estoyok.app.features.wellbeing.domain.repository.EmergencyContactsRepository contactsRepository;
    @org.jetbrains.annotations.Nullable()
    private android.media.Ringtone ringtone;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job countdownJob;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private float speed = 0.0F;
    private float gForce = 0.0F;
    
    public CrashAlertActivity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.estoyok.app.features.tracking.domain.repository.CrashRepository getCrashRepository() {
        return null;
    }
    
    public final void setCrashRepository(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.domain.repository.CrashRepository p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.estoyok.app.features.wellbeing.domain.repository.EmergencyContactsRepository getContactsRepository() {
        return null;
    }
    
    public final void setContactsRepository(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.domain.repository.EmergencyContactsRepository p0) {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void startSiren() {
    }
    
    private final void stopSiren() {
    }
    
    private final void cancelAlert() {
    }
    
    private final void triggerEmergencyProtocol() {
    }
    
    @java.lang.Override()
    public void onBackPressed() {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
}
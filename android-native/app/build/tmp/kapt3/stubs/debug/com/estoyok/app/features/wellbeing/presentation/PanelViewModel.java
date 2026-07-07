package com.estoyok.app.features.wellbeing.presentation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\b\u00106\u001a\u000207H\u0002J\u000e\u00108\u001a\u000207H\u0082@\u00a2\u0006\u0002\u00109J\u000e\u0010:\u001a\u000207H\u0082@\u00a2\u0006\u0002\u00109J\u0006\u0010;\u001a\u000207J\u0006\u0010<\u001a\u000207J\u0018\u0010=\u001a\u0002072\u0006\u0010>\u001a\u00020?2\u0006\u0010@\u001a\u00020\u0015H\u0002J\u0010\u0010A\u001a\u0002072\u0006\u0010>\u001a\u00020?H\u0002J\u000e\u0010B\u001a\u0002072\u0006\u0010>\u001a\u00020?R7\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\r0\f2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0013\u0010\u0014\u001a\u0004\b\u000f\u0010\u0010\"\u0004\b\u0011\u0010\u0012R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R/\u0010\u0016\u001a\u0004\u0018\u00010\u00152\b\u0010\u000b\u001a\u0004\u0018\u00010\u00158F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001b\u0010\u0014\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u001aR+\u0010\u001d\u001a\u00020\u001c2\u0006\u0010\u000b\u001a\u00020\u001c8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b!\u0010\u0014\u001a\u0004\b\u001d\u0010\u001e\"\u0004\b\u001f\u0010 R+\u0010\"\u001a\u00020\u001c2\u0006\u0010\u000b\u001a\u00020\u001c8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b$\u0010\u0014\u001a\u0004\b\"\u0010\u001e\"\u0004\b#\u0010 R+\u0010%\u001a\u00020\u001c2\u0006\u0010\u000b\u001a\u00020\u001c8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\'\u0010\u0014\u001a\u0004\b%\u0010\u001e\"\u0004\b&\u0010 R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R+\u0010)\u001a\u00020(2\u0006\u0010\u000b\u001a\u00020(8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b.\u0010\u0014\u001a\u0004\b*\u0010+\"\u0004\b,\u0010-R/\u00100\u001a\u0004\u0018\u00010/2\b\u0010\u000b\u001a\u0004\u0018\u00010/8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b5\u0010\u0014\u001a\u0004\b1\u00102\"\u0004\b3\u00104\u00a8\u0006C"}, d2 = {"Lcom/estoyok/app/features/wellbeing/presentation/PanelViewModel;", "Landroidx/lifecycle/ViewModel;", "checkInRepository", "Lcom/estoyok/app/features/wellbeing/domain/repository/CheckInRepository;", "settingsRepository", "Lcom/estoyok/app/features/wellbeing/domain/repository/SettingsRepository;", "sosRepository", "Lcom/estoyok/app/features/tracking/domain/repository/SosRepository;", "contactsRepository", "Lcom/estoyok/app/features/wellbeing/domain/repository/EmergencyContactsRepository;", "(Lcom/estoyok/app/features/wellbeing/domain/repository/CheckInRepository;Lcom/estoyok/app/features/wellbeing/domain/repository/SettingsRepository;Lcom/estoyok/app/features/tracking/domain/repository/SosRepository;Lcom/estoyok/app/features/wellbeing/domain/repository/EmergencyContactsRepository;)V", "<set-?>", "", "Lcom/estoyok/app/features/wellbeing/data/model/CheckInDto;", "checkInHistory", "getCheckInHistory", "()Ljava/util/List;", "setCheckInHistory", "(Ljava/util/List;)V", "checkInHistory$delegate", "Landroidx/compose/runtime/MutableState;", "", "errorMessage", "getErrorMessage", "()Ljava/lang/String;", "setErrorMessage", "(Ljava/lang/String;)V", "errorMessage$delegate", "", "isCheckingIn", "()Z", "setCheckingIn", "(Z)V", "isCheckingIn$delegate", "isRefreshing", "setRefreshing", "isRefreshing$delegate", "isSosTriggered", "setSosTriggered", "isSosTriggered$delegate", "Lcom/estoyok/app/features/wellbeing/presentation/WellbeingStatus;", "status", "getStatus", "()Lcom/estoyok/app/features/wellbeing/presentation/WellbeingStatus;", "setStatus", "(Lcom/estoyok/app/features/wellbeing/presentation/WellbeingStatus;)V", "status$delegate", "Lcom/estoyok/app/features/auth/data/model/UserDto;", "user", "getUser", "()Lcom/estoyok/app/features/auth/data/model/UserDto;", "setUser", "(Lcom/estoyok/app/features/auth/data/model/UserDto;)V", "user$delegate", "calculateStatus", "", "fetchCheckInHistory", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchUserProfile", "performCheckIn", "refreshDashboard", "startSosTrackingAndRecording", "context", "Landroid/content/Context;", "alertId", "triggerSmsFallback", "triggerSos", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class PanelViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.wellbeing.domain.repository.CheckInRepository checkInRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository settingsRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.tracking.domain.repository.SosRepository sosRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.wellbeing.domain.repository.EmergencyContactsRepository contactsRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState user$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState checkInHistory$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isCheckingIn$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isRefreshing$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isSosTriggered$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState status$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState errorMessage$delegate = null;
    
    @javax.inject.Inject()
    public PanelViewModel(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.domain.repository.CheckInRepository checkInRepository, @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository settingsRepository, @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.domain.repository.SosRepository sosRepository, @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.domain.repository.EmergencyContactsRepository contactsRepository) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.estoyok.app.features.auth.data.model.UserDto getUser() {
        return null;
    }
    
    private final void setUser(com.estoyok.app.features.auth.data.model.UserDto p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.estoyok.app.features.wellbeing.data.model.CheckInDto> getCheckInHistory() {
        return null;
    }
    
    private final void setCheckInHistory(java.util.List<com.estoyok.app.features.wellbeing.data.model.CheckInDto> p0) {
    }
    
    public final boolean isCheckingIn() {
        return false;
    }
    
    private final void setCheckingIn(boolean p0) {
    }
    
    public final boolean isRefreshing() {
        return false;
    }
    
    private final void setRefreshing(boolean p0) {
    }
    
    public final boolean isSosTriggered() {
        return false;
    }
    
    private final void setSosTriggered(boolean p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.estoyok.app.features.wellbeing.presentation.WellbeingStatus getStatus() {
        return null;
    }
    
    private final void setStatus(com.estoyok.app.features.wellbeing.presentation.WellbeingStatus p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    private final void setErrorMessage(java.lang.String p0) {
    }
    
    public final void refreshDashboard() {
    }
    
    private final java.lang.Object fetchUserProfile(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object fetchCheckInHistory(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void performCheckIn() {
    }
    
    public final void triggerSos(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    private final void startSosTrackingAndRecording(android.content.Context context, java.lang.String alertId) {
    }
    
    private final void triggerSmsFallback(android.content.Context context) {
    }
    
    private final void calculateStatus() {
    }
}
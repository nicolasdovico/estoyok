package com.estoyok.app.features.wellbeing.data.repository;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u000e\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0014\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006H\u0016J\u0014\u0010\t\u001a\u00020\n2\n\u0010\u000b\u001a\u0006\u0012\u0002\b\u00030\fH\u0002JC\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u000e0\u00070\u0006\"\u0004\b\u0000\u0010\u000e2\"\u0010\u000f\u001a\u001e\b\u0001\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u000e0\f0\u0011\u0012\u0006\u0012\u0004\u0018\u00010\u00120\u0010H\u0002\u00a2\u0006\u0002\u0010\u0013J.\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00070\u00062\u0006\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\n2\u0006\u0010\u0019\u001a\u00020\u0017H\u0016J\u001c\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00070\u00062\u0006\u0010\u001b\u001a\u00020\u001cH\u0016J$\u0010\u001d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00070\u00062\u0006\u0010\u001e\u001a\u00020\u00172\u0006\u0010\u001f\u001a\u00020\u001cH\u0016J-\u0010 \u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00070\u00062\b\u0010!\u001a\u0004\u0018\u00010\u00172\b\u0010\"\u001a\u0004\u0018\u00010\u0017H\u0016\u00a2\u0006\u0002\u0010#J\u001c\u0010$\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00070\u00062\u0006\u0010\u001e\u001a\u00020\u0017H\u0016J4\u0010%\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00070\u00062\u0006\u0010\u001e\u001a\u00020\u00172\u0006\u0010&\u001a\u00020\n2\u0006\u0010\'\u001a\u00020\n2\u0006\u0010(\u001a\u00020\nH\u0016J\u001c\u0010)\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00070\u00062\u0006\u0010\u001e\u001a\u00020\u0017H\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006*"}, d2 = {"Lcom/estoyok/app/features/wellbeing/data/repository/SettingsRepositoryImpl;", "Lcom/estoyok/app/features/wellbeing/domain/repository/SettingsRepository;", "apiService", "Lcom/estoyok/app/features/wellbeing/data/remote/SettingsApiService;", "(Lcom/estoyok/app/features/wellbeing/data/remote/SettingsApiService;)V", "getUserProfile", "Lkotlinx/coroutines/flow/Flow;", "Lcom/estoyok/app/core/util/Resource;", "Lcom/estoyok/app/features/auth/data/model/UserDto;", "parseErrorMessage", "", "response", "Lretrofit2/Response;", "safeApiCall", "T", "apiCall", "Lkotlin/Function1;", "Lkotlin/coroutines/Continuation;", "", "(Lkotlin/jvm/functions/Function1;)Lkotlinx/coroutines/flow/Flow;", "updateAutomation", "Lcom/estoyok/app/features/auth/data/model/MessageResponse;", "wifiEnabled", "", "ssid", "sensorEnabled", "updateCheckinInterval", "hours", "", "updateEscalation", "enabled", "intervalMinutes", "updatePrivacy", "shareContactResponses", "lowBatteryAlertsEnabled", "(Ljava/lang/Boolean;Ljava/lang/Boolean;)Lkotlinx/coroutines/flow/Flow;", "updateProximityAlerts", "updateQuietHours", "start", "end", "timezone", "updateSmsWhatsappCheckin", "app_debug"})
public final class SettingsRepositoryImpl implements com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.wellbeing.data.remote.SettingsApiService apiService = null;
    
    @javax.inject.Inject()
    public SettingsRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.remote.SettingsApiService apiService) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.UserDto>> getUserProfile() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> updateCheckinInterval(int hours) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> updateQuietHours(boolean enabled, @org.jetbrains.annotations.NotNull()
    java.lang.String start, @org.jetbrains.annotations.NotNull()
    java.lang.String end, @org.jetbrains.annotations.NotNull()
    java.lang.String timezone) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> updateSmsWhatsappCheckin(boolean enabled) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> updateEscalation(boolean enabled, int intervalMinutes) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> updatePrivacy(@org.jetbrains.annotations.Nullable()
    java.lang.Boolean shareContactResponses, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean lowBatteryAlertsEnabled) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> updateAutomation(boolean wifiEnabled, @org.jetbrains.annotations.Nullable()
    java.lang.String ssid, boolean sensorEnabled) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> updateProximityAlerts(boolean enabled) {
        return null;
    }
    
    private final <T extends java.lang.Object>kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<T>> safeApiCall(kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super retrofit2.Response<T>>, ? extends java.lang.Object> apiCall) {
        return null;
    }
    
    private final java.lang.String parseErrorMessage(retrofit2.Response<?> response) {
        return null;
    }
}
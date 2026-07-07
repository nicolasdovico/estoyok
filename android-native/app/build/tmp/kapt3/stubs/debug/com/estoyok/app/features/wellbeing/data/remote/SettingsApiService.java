package com.estoyok.app.features.wellbeing.data.remote;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u0014\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0005J\u001e\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00070\u00032\b\b\u0001\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u001e\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00070\u00032\b\b\u0001\u0010\b\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u001e\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00070\u00032\b\b\u0001\u0010\b\u001a\u00020\u000fH\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u001e\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00070\u00032\b\b\u0001\u0010\b\u001a\u00020\u0012H\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u001e\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00070\u00032\b\b\u0001\u0010\b\u001a\u00020\u0015H\u00a7@\u00a2\u0006\u0002\u0010\u0016J\u001e\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00070\u00032\b\b\u0001\u0010\b\u001a\u00020\u0018H\u00a7@\u00a2\u0006\u0002\u0010\u0019J\u001e\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00070\u00032\b\b\u0001\u0010\b\u001a\u00020\u001bH\u00a7@\u00a2\u0006\u0002\u0010\u001c\u00a8\u0006\u001d"}, d2 = {"Lcom/estoyok/app/features/wellbeing/data/remote/SettingsApiService;", "", "getUserProfile", "Lretrofit2/Response;", "Lcom/estoyok/app/features/auth/data/model/UserDto;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateAutomation", "Lcom/estoyok/app/features/auth/data/model/MessageResponse;", "request", "Lcom/estoyok/app/features/wellbeing/data/model/AutomationRequest;", "(Lcom/estoyok/app/features/wellbeing/data/model/AutomationRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateCheckinInterval", "Lcom/estoyok/app/features/wellbeing/data/model/CheckinIntervalRequest;", "(Lcom/estoyok/app/features/wellbeing/data/model/CheckinIntervalRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateEscalation", "Lcom/estoyok/app/features/wellbeing/data/model/EscalationRequest;", "(Lcom/estoyok/app/features/wellbeing/data/model/EscalationRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updatePrivacy", "Lcom/estoyok/app/features/wellbeing/data/model/PrivacyRequest;", "(Lcom/estoyok/app/features/wellbeing/data/model/PrivacyRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateProximityAlerts", "Lcom/estoyok/app/features/wellbeing/data/model/ProximityAlertsRequest;", "(Lcom/estoyok/app/features/wellbeing/data/model/ProximityAlertsRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateQuietHours", "Lcom/estoyok/app/features/wellbeing/data/model/QuietHoursRequest;", "(Lcom/estoyok/app/features/wellbeing/data/model/QuietHoursRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateSmsWhatsappCheckin", "Lcom/estoyok/app/features/wellbeing/data/model/SmsWhatsappCheckinRequest;", "(Lcom/estoyok/app/features/wellbeing/data/model/SmsWhatsappCheckinRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface SettingsApiService {
    
    @retrofit2.http.GET(value = "user")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUserProfile(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.UserDto>> $completion);
    
    @retrofit2.http.PUT(value = "settings/checkin-interval")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateCheckinInterval(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.CheckinIntervalRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.MessageResponse>> $completion);
    
    @retrofit2.http.PUT(value = "settings/quiet-hours")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateQuietHours(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.QuietHoursRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.MessageResponse>> $completion);
    
    @retrofit2.http.PUT(value = "settings/sms-whatsapp-checkin")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateSmsWhatsappCheckin(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.SmsWhatsappCheckinRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.MessageResponse>> $completion);
    
    @retrofit2.http.PUT(value = "settings/escalation")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateEscalation(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.EscalationRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.MessageResponse>> $completion);
    
    @retrofit2.http.PUT(value = "settings/privacy")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updatePrivacy(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.PrivacyRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.MessageResponse>> $completion);
    
    @retrofit2.http.PUT(value = "settings/automation")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateAutomation(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.AutomationRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.MessageResponse>> $completion);
    
    @retrofit2.http.PUT(value = "settings/proximity-alerts")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateProximityAlerts(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.ProximityAlertsRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.MessageResponse>> $completion);
}
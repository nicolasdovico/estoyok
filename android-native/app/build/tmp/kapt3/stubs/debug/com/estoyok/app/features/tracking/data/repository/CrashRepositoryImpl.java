package com.estoyok.app.features.tracking.data.repository;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001c\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\t\u001a\u00020\nH\u0016J\u0014\u0010\u000b\u001a\u00020\f2\n\u0010\r\u001a\u0006\u0012\u0002\b\u00030\u000eH\u0002J4\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u00070\u00062\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00122\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0015H\u0016JC\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00180\u00070\u0006\"\u0004\b\u0000\u0010\u00182\"\u0010\u0019\u001a\u001e\b\u0001\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00180\u000e0\u001b\u0012\u0006\u0012\u0004\u0018\u00010\u001c0\u001aH\u0002\u00a2\u0006\u0002\u0010\u001dR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001e"}, d2 = {"Lcom/estoyok/app/features/tracking/data/repository/CrashRepositoryImpl;", "Lcom/estoyok/app/features/tracking/domain/repository/CrashRepository;", "apiService", "Lcom/estoyok/app/features/tracking/data/remote/CrashApiService;", "(Lcom/estoyok/app/features/tracking/data/remote/CrashApiService;)V", "markFalseAlarm", "Lkotlinx/coroutines/flow/Flow;", "Lcom/estoyok/app/core/util/Resource;", "Lcom/estoyok/app/features/auth/data/model/MessageResponse;", "crashEventId", "", "parseErrorMessage", "", "response", "Lretrofit2/Response;", "reportCrash", "Lcom/estoyok/app/features/tracking/data/model/CrashAlertResponse;", "latitude", "", "longitude", "speedAtImpact", "", "gForce", "safeApiCall", "T", "apiCall", "Lkotlin/Function1;", "Lkotlin/coroutines/Continuation;", "", "(Lkotlin/jvm/functions/Function1;)Lkotlinx/coroutines/flow/Flow;", "app_debug"})
public final class CrashRepositoryImpl implements com.estoyok.app.features.tracking.domain.repository.CrashRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.tracking.data.remote.CrashApiService apiService = null;
    
    @javax.inject.Inject()
    public CrashRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.remote.CrashApiService apiService) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.tracking.data.model.CrashAlertResponse>> reportCrash(double latitude, double longitude, float speedAtImpact, float gForce) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.auth.data.model.MessageResponse>> markFalseAlarm(int crashEventId) {
        return null;
    }
    
    private final <T extends java.lang.Object>kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<T>> safeApiCall(kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super retrofit2.Response<T>>, ? extends java.lang.Object> apiCall) {
        return null;
    }
    
    private final java.lang.String parseErrorMessage(retrofit2.Response<?> response) {
        return null;
    }
}
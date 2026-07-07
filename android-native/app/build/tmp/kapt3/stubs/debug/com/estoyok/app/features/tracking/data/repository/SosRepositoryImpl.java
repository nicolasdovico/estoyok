package com.estoyok.app.features.tracking.data.repository;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0014\u0010\u0005\u001a\u00020\u00062\n\u0010\u0007\u001a\u0006\u0012\u0002\b\u00030\bH\u0002JC\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\f0\u000b0\n\"\u0004\b\u0000\u0010\f2\"\u0010\r\u001a\u001e\b\u0001\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\f0\b0\u000f\u0012\u0006\u0012\u0004\u0018\u00010\u00100\u000eH\u0002\u00a2\u0006\u0002\u0010\u0011J\u0014\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00130\u000b0\nH\u0016J$\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u000b0\n2\u0006\u0010\u0016\u001a\u00020\u00062\u0006\u0010\u0017\u001a\u00020\u0018H\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/estoyok/app/features/tracking/data/repository/SosRepositoryImpl;", "Lcom/estoyok/app/features/tracking/domain/repository/SosRepository;", "apiService", "Lcom/estoyok/app/features/tracking/data/remote/SosApiService;", "(Lcom/estoyok/app/features/tracking/data/remote/SosApiService;)V", "parseErrorMessage", "", "response", "Lretrofit2/Response;", "safeApiCall", "Lkotlinx/coroutines/flow/Flow;", "Lcom/estoyok/app/core/util/Resource;", "T", "apiCall", "Lkotlin/Function1;", "Lkotlin/coroutines/Continuation;", "", "(Lkotlin/jvm/functions/Function1;)Lkotlinx/coroutines/flow/Flow;", "triggerSos", "Lcom/estoyok/app/features/tracking/data/model/SosAlertResponse;", "uploadAudio", "Lcom/estoyok/app/features/tracking/data/model/AudioUploadResponse;", "alertId", "audioFile", "Ljava/io/File;", "app_debug"})
public final class SosRepositoryImpl implements com.estoyok.app.features.tracking.domain.repository.SosRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.tracking.data.remote.SosApiService apiService = null;
    
    @javax.inject.Inject()
    public SosRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.remote.SosApiService apiService) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.tracking.data.model.SosAlertResponse>> triggerSos() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.tracking.data.model.AudioUploadResponse>> uploadAudio(@org.jetbrains.annotations.NotNull()
    java.lang.String alertId, @org.jetbrains.annotations.NotNull()
    java.io.File audioFile) {
        return null;
    }
    
    private final <T extends java.lang.Object>kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<T>> safeApiCall(kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super retrofit2.Response<T>>, ? extends java.lang.Object> apiCall) {
        return null;
    }
    
    private final java.lang.String parseErrorMessage(retrofit2.Response<?> response) {
        return null;
    }
}
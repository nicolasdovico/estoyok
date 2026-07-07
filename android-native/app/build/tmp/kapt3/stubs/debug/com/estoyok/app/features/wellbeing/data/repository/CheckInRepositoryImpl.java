package com.estoyok.app.features.wellbeing.data.repository;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001c\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\t\u001a\u00020\nH\u0016J\u001a\u0010\u000b\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u00070\u0006H\u0016J\u0014\u0010\u000e\u001a\u00020\n2\n\u0010\u000f\u001a\u0006\u0012\u0002\b\u00030\u0010H\u0002JC\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00120\u00070\u0006\"\u0004\b\u0000\u0010\u00122\"\u0010\u0013\u001a\u001e\b\u0001\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00120\u00100\u0015\u0012\u0006\u0012\u0004\u0018\u00010\u00160\u0014H\u0002\u00a2\u0006\u0002\u0010\u0017R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lcom/estoyok/app/features/wellbeing/data/repository/CheckInRepositoryImpl;", "Lcom/estoyok/app/features/wellbeing/domain/repository/CheckInRepository;", "apiService", "Lcom/estoyok/app/features/wellbeing/data/remote/CheckInApiService;", "(Lcom/estoyok/app/features/wellbeing/data/remote/CheckInApiService;)V", "checkIn", "Lkotlinx/coroutines/flow/Flow;", "Lcom/estoyok/app/core/util/Resource;", "Lcom/estoyok/app/features/wellbeing/data/model/CheckInResponse;", "source", "", "getCheckIns", "", "Lcom/estoyok/app/features/wellbeing/data/model/CheckInDto;", "parseErrorMessage", "response", "Lretrofit2/Response;", "safeApiCall", "T", "apiCall", "Lkotlin/Function1;", "Lkotlin/coroutines/Continuation;", "", "(Lkotlin/jvm/functions/Function1;)Lkotlinx/coroutines/flow/Flow;", "app_debug"})
public final class CheckInRepositoryImpl implements com.estoyok.app.features.wellbeing.domain.repository.CheckInRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.wellbeing.data.remote.CheckInApiService apiService = null;
    
    @javax.inject.Inject()
    public CheckInRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.remote.CheckInApiService apiService) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.wellbeing.data.model.CheckInResponse>> checkIn(@org.jetbrains.annotations.NotNull()
    java.lang.String source) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<java.util.List<com.estoyok.app.features.wellbeing.data.model.CheckInDto>>> getCheckIns() {
        return null;
    }
    
    private final <T extends java.lang.Object>kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<T>> safeApiCall(kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super retrofit2.Response<T>>, ? extends java.lang.Object> apiCall) {
        return null;
    }
    
    private final java.lang.String parseErrorMessage(retrofit2.Response<?> response) {
        return null;
    }
}
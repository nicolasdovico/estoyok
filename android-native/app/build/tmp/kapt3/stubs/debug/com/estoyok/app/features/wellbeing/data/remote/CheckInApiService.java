package com.estoyok.app.features.wellbeing.data.remote;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0003\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u001a\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u000b\u00a8\u0006\f"}, d2 = {"Lcom/estoyok/app/features/wellbeing/data/remote/CheckInApiService;", "", "checkIn", "Lretrofit2/Response;", "Lcom/estoyok/app/features/wellbeing/data/model/CheckInResponse;", "request", "Lcom/estoyok/app/features/wellbeing/data/model/CheckInRequest;", "(Lcom/estoyok/app/features/wellbeing/data/model/CheckInRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCheckIns", "", "Lcom/estoyok/app/features/wellbeing/data/model/CheckInDto;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface CheckInApiService {
    
    @retrofit2.http.POST(value = "check-in")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object checkIn(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.wellbeing.data.model.CheckInRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.wellbeing.data.model.CheckInResponse>> $completion);
    
    @retrofit2.http.GET(value = "check-ins")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCheckIns(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.estoyok.app.features.wellbeing.data.model.CheckInDto>>> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}
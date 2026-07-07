package com.estoyok.app.features.tracking.data.remote;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u001e\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\n\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\fJ\u001a\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040\u000e0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u000fJ\u001e\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0011H\u00a7@\u00a2\u0006\u0002\u0010\u0012J(\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\n\u001a\u00020\u000b2\b\b\u0001\u0010\u0014\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\u0015\u00a8\u0006\u0016"}, d2 = {"Lcom/estoyok/app/features/tracking/data/remote/CircleApiService;", "", "createCircle", "Lretrofit2/Response;", "Lcom/estoyok/app/features/tracking/data/model/CircleDto;", "request", "Lcom/estoyok/app/features/tracking/data/model/CreateCircleRequest;", "(Lcom/estoyok/app/features/tracking/data/model/CreateCircleRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteCircle", "Lcom/estoyok/app/features/auth/data/model/MessageResponse;", "circleId", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCircles", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "joinCircle", "Lcom/estoyok/app/features/tracking/data/model/JoinCircleRequest;", "(Lcom/estoyok/app/features/tracking/data/model/JoinCircleRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "removeMember", "memberId", "(IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface CircleApiService {
    
    @retrofit2.http.GET(value = "circles")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCircles(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.estoyok.app.features.tracking.data.model.CircleDto>>> $completion);
    
    @retrofit2.http.POST(value = "circles")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createCircle(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.model.CreateCircleRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.tracking.data.model.CircleDto>> $completion);
    
    @retrofit2.http.POST(value = "circles/join")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object joinCircle(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.model.JoinCircleRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.tracking.data.model.CircleDto>> $completion);
    
    @retrofit2.http.DELETE(value = "circles/{circleId}/members/{memberId}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object removeMember(@retrofit2.http.Path(value = "circleId")
    int circleId, @retrofit2.http.Path(value = "memberId")
    int memberId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.MessageResponse>> $completion);
    
    @retrofit2.http.DELETE(value = "circles/{circleId}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteCircle(@retrofit2.http.Path(value = "circleId")
    int circleId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.auth.data.model.MessageResponse>> $completion);
}
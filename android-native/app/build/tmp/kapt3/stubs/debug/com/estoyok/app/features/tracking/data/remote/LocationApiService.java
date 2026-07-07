package com.estoyok.app.features.tracking.data.remote;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u001e\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u000b\u00a8\u0006\f"}, d2 = {"Lcom/estoyok/app/features/tracking/data/remote/LocationApiService;", "", "updateLocation", "Lretrofit2/Response;", "Lcom/estoyok/app/features/tracking/data/model/LocationUpdateResponse;", "request", "Lcom/estoyok/app/features/tracking/data/model/LocationUpdateRequest;", "(Lcom/estoyok/app/features/tracking/data/model/LocationUpdateRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateSensorStatus", "Lcom/estoyok/app/features/tracking/data/model/SensorStatusResponse;", "Lcom/estoyok/app/features/tracking/data/model/SensorStatusRequest;", "(Lcom/estoyok/app/features/tracking/data/model/SensorStatusRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface LocationApiService {
    
    @retrofit2.http.POST(value = "locations/update")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateLocation(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.model.LocationUpdateRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.tracking.data.model.LocationUpdateResponse>> $completion);
    
    @retrofit2.http.PUT(value = "locations/sensor-status")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateSensorStatus(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.model.SensorStatusRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.estoyok.app.features.tracking.data.model.SensorStatusResponse>> $completion);
}
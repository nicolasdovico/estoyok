package com.estoyok.app.features.tracking.domain.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\u0014\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0003H&J$\u0010\u0006\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u00040\u00032\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH&J\u001c\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\u00040\u00032\u0006\u0010\b\u001a\u00020\u000eH&\u00a8\u0006\u000f"}, d2 = {"Lcom/estoyok/app/features/tracking/domain/repository/LocationRepository;", "", "flushOfflineQueue", "Lkotlinx/coroutines/flow/Flow;", "Lcom/estoyok/app/core/util/Resource;", "", "updateLocation", "Lcom/estoyok/app/features/tracking/data/model/LocationUpdateResponse;", "request", "Lcom/estoyok/app/features/tracking/data/model/LocationUpdateRequest;", "isOnline", "", "updateSensorStatus", "Lcom/estoyok/app/features/tracking/data/model/SensorStatusResponse;", "Lcom/estoyok/app/features/tracking/data/model/SensorStatusRequest;", "app_debug"})
public abstract interface LocationRepository {
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.tracking.data.model.LocationUpdateResponse>> updateLocation(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.model.LocationUpdateRequest request, boolean isOnline);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.tracking.data.model.SensorStatusResponse>> updateSensorStatus(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.model.SensorStatusRequest request);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<java.lang.Integer>> flushOfflineQueue();
}
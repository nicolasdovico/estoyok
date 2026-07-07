package com.estoyok.app.features.tracking.data.repository;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bH\u0016J\u0016\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0082@\u00a2\u0006\u0002\u0010\u000fJ$\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\t0\b2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0012\u001a\u00020\u0013H\u0016J\u001c\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\t0\b2\u0006\u0010\r\u001a\u00020\u0016H\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/estoyok/app/features/tracking/data/repository/LocationRepositoryImpl;", "Lcom/estoyok/app/features/tracking/domain/repository/LocationRepository;", "apiService", "Lcom/estoyok/app/features/tracking/data/remote/LocationApiService;", "offlineLocationDao", "Lcom/estoyok/app/features/tracking/data/local/dao/OfflineLocationDao;", "(Lcom/estoyok/app/features/tracking/data/remote/LocationApiService;Lcom/estoyok/app/features/tracking/data/local/dao/OfflineLocationDao;)V", "flushOfflineQueue", "Lkotlinx/coroutines/flow/Flow;", "Lcom/estoyok/app/core/util/Resource;", "", "saveOffline", "", "request", "Lcom/estoyok/app/features/tracking/data/model/LocationUpdateRequest;", "(Lcom/estoyok/app/features/tracking/data/model/LocationUpdateRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateLocation", "Lcom/estoyok/app/features/tracking/data/model/LocationUpdateResponse;", "isOnline", "", "updateSensorStatus", "Lcom/estoyok/app/features/tracking/data/model/SensorStatusResponse;", "Lcom/estoyok/app/features/tracking/data/model/SensorStatusRequest;", "app_debug"})
public final class LocationRepositoryImpl implements com.estoyok.app.features.tracking.domain.repository.LocationRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.tracking.data.remote.LocationApiService apiService = null;
    @org.jetbrains.annotations.NotNull()
    private final com.estoyok.app.features.tracking.data.local.dao.OfflineLocationDao offlineLocationDao = null;
    
    @javax.inject.Inject()
    public LocationRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.remote.LocationApiService apiService, @org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.local.dao.OfflineLocationDao offlineLocationDao) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.tracking.data.model.LocationUpdateResponse>> updateLocation(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.model.LocationUpdateRequest request, boolean isOnline) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<com.estoyok.app.features.tracking.data.model.SensorStatusResponse>> updateSensorStatus(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.model.SensorStatusRequest request) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.estoyok.app.core.util.Resource<java.lang.Integer>> flushOfflineQueue() {
        return null;
    }
    
    private final java.lang.Object saveOffline(com.estoyok.app.features.tracking.data.model.LocationUpdateRequest request, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}
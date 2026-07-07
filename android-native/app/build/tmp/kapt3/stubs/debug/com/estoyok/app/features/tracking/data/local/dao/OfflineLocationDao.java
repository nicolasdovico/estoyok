package com.estoyok.app.features.tracking.data.local.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0006\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u001c\u0010\u0005\u001a\u00020\u00032\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u00a7@\u00a2\u0006\u0002\u0010\tJ\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u000b\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\r\u00a8\u0006\u000e"}, d2 = {"Lcom/estoyok/app/features/tracking/data/local/dao/OfflineLocationDao;", "", "deleteAll", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteLocations", "locations", "", "Lcom/estoyok/app/features/tracking/data/local/entity/OfflineLocationEntity;", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllLocations", "insertLocation", "location", "(Lcom/estoyok/app/features/tracking/data/local/entity/OfflineLocationEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao()
public abstract interface OfflineLocationDao {
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertLocation(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.data.local.entity.OfflineLocationEntity location, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM offline_locations ORDER BY recordedAt ASC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllLocations(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.estoyok.app.features.tracking.data.local.entity.OfflineLocationEntity>> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteLocations(@org.jetbrains.annotations.NotNull()
    java.util.List<com.estoyok.app.features.tracking.data.local.entity.OfflineLocationEntity> locations, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM offline_locations")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}
package com.estoyok.app.core.data.local;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002R\u0012\u0010\u0003\u001a\u00020\u0004X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/estoyok/app/core/data/local/EstoyOkDatabase;", "Landroidx/room/RoomDatabase;", "()V", "offlineLocationDao", "Lcom/estoyok/app/features/tracking/data/local/dao/OfflineLocationDao;", "getOfflineLocationDao", "()Lcom/estoyok/app/features/tracking/data/local/dao/OfflineLocationDao;", "app_debug"})
@androidx.room.Database(entities = {com.estoyok.app.features.tracking.data.local.entity.OfflineLocationEntity.class}, version = 1, exportSchema = false)
public abstract class EstoyOkDatabase extends androidx.room.RoomDatabase {
    
    public EstoyOkDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.estoyok.app.features.tracking.data.local.dao.OfflineLocationDao getOfflineLocationDao();
}
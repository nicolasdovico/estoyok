package com.estoyok.app.features.tracking.data.local.entity;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b#\b\u0087\b\u0018\u00002\u00020\u0001Bc\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\b\u0010\t\u001a\u0004\u0018\u00010\b\u0012\b\u0010\n\u001a\u0004\u0018\u00010\u000b\u0012\b\u0010\f\u001a\u0004\u0018\u00010\u000b\u0012\u0006\u0010\r\u001a\u00020\u000e\u0012\b\u0010\u000f\u001a\u0004\u0018\u00010\b\u0012\b\u0010\u0010\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\u0002\u0010\u0011J\t\u0010!\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010\"\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0017J\t\u0010#\u001a\u00020\u0005H\u00c6\u0003J\t\u0010$\u001a\u00020\u0005H\u00c6\u0003J\u0010\u0010%\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0013J\u0010\u0010&\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0013J\u0010\u0010\'\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0017J\u0010\u0010(\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0017J\t\u0010)\u001a\u00020\u000eH\u00c6\u0003J\u0010\u0010*\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0013J~\u0010+\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u000b2\b\b\u0002\u0010\r\u001a\u00020\u000e2\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u000bH\u00c6\u0001\u00a2\u0006\u0002\u0010,J\u0013\u0010-\u001a\u00020\u000b2\b\u0010.\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010/\u001a\u00020\u0003H\u00d6\u0001J\t\u00100\u001a\u00020\u000eH\u00d6\u0001R\u0015\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u0010\u0014\u001a\u0004\b\u0012\u0010\u0013R\u0015\u0010\t\u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u0010\u0014\u001a\u0004\b\u0015\u0010\u0013R\u0015\u0010\f\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\n\n\u0002\u0010\u0018\u001a\u0004\b\u0016\u0010\u0017R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0015\u0010\u0010\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\n\n\u0002\u0010\u0018\u001a\u0004\b\u0010\u0010\u0017R\u0015\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\n\n\u0002\u0010\u0018\u001a\u0004\b\n\u0010\u0017R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001cR\u0011\u0010\r\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0015\u0010\u000f\u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u0010\u0014\u001a\u0004\b \u0010\u0013\u00a8\u00061"}, d2 = {"Lcom/estoyok/app/features/tracking/data/local/entity/OfflineLocationEntity;", "", "id", "", "latitude", "", "longitude", "accuracy", "", "batteryLevel", "isTrackingActive", "", "gpsEnabled", "recordedAt", "", "speed", "isDriving", "(IDDLjava/lang/Float;Ljava/lang/Float;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Float;Ljava/lang/Boolean;)V", "getAccuracy", "()Ljava/lang/Float;", "Ljava/lang/Float;", "getBatteryLevel", "getGpsEnabled", "()Ljava/lang/Boolean;", "Ljava/lang/Boolean;", "getId", "()I", "getLatitude", "()D", "getLongitude", "getRecordedAt", "()Ljava/lang/String;", "getSpeed", "component1", "component10", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(IDDLjava/lang/Float;Ljava/lang/Float;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Float;Ljava/lang/Boolean;)Lcom/estoyok/app/features/tracking/data/local/entity/OfflineLocationEntity;", "equals", "other", "hashCode", "toString", "app_debug"})
@androidx.room.Entity(tableName = "offline_locations")
public final class OfflineLocationEntity {
    @androidx.room.PrimaryKey(autoGenerate = true)
    private final int id = 0;
    private final double latitude = 0.0;
    private final double longitude = 0.0;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Float accuracy = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Float batteryLevel = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean isTrackingActive = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean gpsEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String recordedAt = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Float speed = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean isDriving = null;
    
    public OfflineLocationEntity(int id, double latitude, double longitude, @org.jetbrains.annotations.Nullable()
    java.lang.Float accuracy, @org.jetbrains.annotations.Nullable()
    java.lang.Float batteryLevel, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean isTrackingActive, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean gpsEnabled, @org.jetbrains.annotations.NotNull()
    java.lang.String recordedAt, @org.jetbrains.annotations.Nullable()
    java.lang.Float speed, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean isDriving) {
        super();
    }
    
    public final int getId() {
        return 0;
    }
    
    public final double getLatitude() {
        return 0.0;
    }
    
    public final double getLongitude() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float getAccuracy() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float getBatteryLevel() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean isTrackingActive() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean getGpsEnabled() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRecordedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float getSpeed() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean isDriving() {
        return null;
    }
    
    public final int component1() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component10() {
        return null;
    }
    
    public final double component2() {
        return 0.0;
    }
    
    public final double component3() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.estoyok.app.features.tracking.data.local.entity.OfflineLocationEntity copy(int id, double latitude, double longitude, @org.jetbrains.annotations.Nullable()
    java.lang.Float accuracy, @org.jetbrains.annotations.Nullable()
    java.lang.Float batteryLevel, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean isTrackingActive, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean gpsEnabled, @org.jetbrains.annotations.NotNull()
    java.lang.String recordedAt, @org.jetbrains.annotations.Nullable()
    java.lang.Float speed, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean isDriving) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}
package com.estoyok.app.features.tracking.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u001e\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001Bi\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\t\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\t\u00a2\u0006\u0002\u0010\u000fJ\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010\u001f\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0011J\u0010\u0010 \u001a\u0004\u0018\u00010\u0006H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0011J\u0010\u0010!\u001a\u0004\u0018\u00010\tH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0015J\u0010\u0010\"\u001a\u0004\u0018\u00010\tH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0015J\u000b\u0010#\u001a\u0004\u0018\u00010\fH\u00c6\u0003J\u0010\u0010$\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0011J\u0010\u0010%\u001a\u0004\u0018\u00010\tH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0015Jv\u0010&\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\tH\u00c6\u0001\u00a2\u0006\u0002\u0010\'J\u0013\u0010(\u001a\u00020\t2\b\u0010)\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010*\u001a\u00020+H\u00d6\u0001J\t\u0010,\u001a\u00020\fH\u00d6\u0001R\u001a\u0010\u0005\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\n\n\u0002\u0010\u0012\u001a\u0004\b\u0010\u0010\u0011R\u001a\u0010\u0007\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\n\n\u0002\u0010\u0012\u001a\u0004\b\u0013\u0010\u0011R\u001a\u0010\n\u001a\u0004\u0018\u00010\t8\u0006X\u0087\u0004\u00a2\u0006\n\n\u0002\u0010\u0016\u001a\u0004\b\u0014\u0010\u0015R\u001a\u0010\u000e\u001a\u0004\u0018\u00010\t8\u0006X\u0087\u0004\u00a2\u0006\n\n\u0002\u0010\u0016\u001a\u0004\b\u000e\u0010\u0015R\u001a\u0010\b\u001a\u0004\u0018\u00010\t8\u0006X\u0087\u0004\u00a2\u0006\n\n\u0002\u0010\u0016\u001a\u0004\b\b\u0010\u0015R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0016\u0010\u0004\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0018R\u0018\u0010\u000b\u001a\u0004\u0018\u00010\f8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u001a\u0010\r\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\n\n\u0002\u0010\u0012\u001a\u0004\b\u001c\u0010\u0011\u00a8\u0006-"}, d2 = {"Lcom/estoyok/app/features/tracking/data/model/LocationUpdateRequest;", "", "latitude", "", "longitude", "accuracy", "", "batteryLevel", "isTrackingActive", "", "gpsEnabled", "recordedAt", "", "speed", "isDriving", "(DDLjava/lang/Float;Ljava/lang/Float;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Float;Ljava/lang/Boolean;)V", "getAccuracy", "()Ljava/lang/Float;", "Ljava/lang/Float;", "getBatteryLevel", "getGpsEnabled", "()Ljava/lang/Boolean;", "Ljava/lang/Boolean;", "getLatitude", "()D", "getLongitude", "getRecordedAt", "()Ljava/lang/String;", "getSpeed", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(DDLjava/lang/Float;Ljava/lang/Float;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Float;Ljava/lang/Boolean;)Lcom/estoyok/app/features/tracking/data/model/LocationUpdateRequest;", "equals", "other", "hashCode", "", "toString", "app_debug"})
public final class LocationUpdateRequest {
    @com.google.gson.annotations.SerializedName(value = "latitude")
    private final double latitude = 0.0;
    @com.google.gson.annotations.SerializedName(value = "longitude")
    private final double longitude = 0.0;
    @com.google.gson.annotations.SerializedName(value = "accuracy")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Float accuracy = null;
    @com.google.gson.annotations.SerializedName(value = "battery_level")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Float batteryLevel = null;
    @com.google.gson.annotations.SerializedName(value = "is_tracking_active")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean isTrackingActive = null;
    @com.google.gson.annotations.SerializedName(value = "gps_enabled")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean gpsEnabled = null;
    @com.google.gson.annotations.SerializedName(value = "recorded_at")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String recordedAt = null;
    @com.google.gson.annotations.SerializedName(value = "speed")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Float speed = null;
    @com.google.gson.annotations.SerializedName(value = "is_driving")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean isDriving = null;
    
    public LocationUpdateRequest(double latitude, double longitude, @org.jetbrains.annotations.Nullable()
    java.lang.Float accuracy, @org.jetbrains.annotations.Nullable()
    java.lang.Float batteryLevel, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean isTrackingActive, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean gpsEnabled, @org.jetbrains.annotations.Nullable()
    java.lang.String recordedAt, @org.jetbrains.annotations.Nullable()
    java.lang.Float speed, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean isDriving) {
        super();
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
    
    @org.jetbrains.annotations.Nullable()
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
    
    public final double component1() {
        return 0.0;
    }
    
    public final double component2() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.estoyok.app.features.tracking.data.model.LocationUpdateRequest copy(double latitude, double longitude, @org.jetbrains.annotations.Nullable()
    java.lang.Float accuracy, @org.jetbrains.annotations.Nullable()
    java.lang.Float batteryLevel, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean isTrackingActive, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean gpsEnabled, @org.jetbrains.annotations.Nullable()
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
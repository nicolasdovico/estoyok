package com.estoyok.app.services;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00a2\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0010\u0014\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u000b\b\u0007\u0018\u0000 P2\u00020\u00012\u00020\u0002:\u0001PB\u0005\u00a2\u0006\u0002\u0010\u0003J\b\u0010)\u001a\u00020\u0010H\u0002J\b\u0010*\u001a\u00020+H\u0002J\b\u0010,\u001a\u00020-H\u0002J\b\u0010.\u001a\u00020-H\u0002J\b\u0010/\u001a\u00020\u0010H\u0002J\u0018\u00100\u001a\u0012\u0012\u0006\u0012\u0004\u0018\u00010\u0005\u0012\u0006\u0012\u0004\u0018\u00010\u001001H\u0002J\u0010\u00102\u001a\u00020-2\u0006\u00103\u001a\u00020\u0005H\u0002J\u001a\u00104\u001a\u00020-2\b\u00105\u001a\u0004\u0018\u00010\t2\u0006\u00106\u001a\u000207H\u0016J\u0014\u00108\u001a\u0004\u0018\u0001092\b\u0010:\u001a\u0004\u0018\u00010;H\u0016J\b\u0010<\u001a\u00020-H\u0016J\b\u0010=\u001a\u00020-H\u0016J\u0012\u0010>\u001a\u00020-2\b\u0010?\u001a\u0004\u0018\u00010@H\u0016J\"\u0010A\u001a\u0002072\b\u0010:\u001a\u0004\u0018\u00010;2\u0006\u0010B\u001a\u0002072\u0006\u0010C\u001a\u000207H\u0016J\u0010\u0010D\u001a\u00020-2\u0006\u0010E\u001a\u00020FH\u0002J\b\u0010G\u001a\u00020-H\u0002J\b\u0010H\u001a\u00020-H\u0002J\u0010\u0010I\u001a\u00020-2\u0006\u0010J\u001a\u00020\u0007H\u0002J\b\u0010K\u001a\u00020-H\u0002J\b\u0010L\u001a\u00020-H\u0002J\b\u0010M\u001a\u00020-H\u0002J\u0010\u0010N\u001a\u00020-2\u0006\u0010O\u001a\u00020\u0007H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0018\u001a\u0004\u0018\u00010\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u001a\u001a\u00020\u001b8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001c\u0010\u001d\"\u0004\b\u001e\u0010\u001fR\u000e\u0010 \u001a\u00020!X\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010\"\u001a\b\u0012\u0004\u0012\u00020$0#X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010%\u001a\u00020&X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\'\u001a\u00020(X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006Q"}, d2 = {"Lcom/estoyok/app/services/TrackingService;", "Landroid/app/Service;", "Landroid/hardware/SensorEventListener;", "()V", "GRAVITY", "", "aboveThresholdStartTime", "", "accelerometer", "Landroid/hardware/Sensor;", "belowThresholdStartTime", "currentIntervalMs", "fusedLocationClient", "Lcom/google/android/gms/location/FusedLocationProviderClient;", "impactTime", "isDriving", "", "isPostImpactMonitoring", "isTrackingActive", "lastAccuracy", "lastLatitude", "", "lastLongitude", "lastSpeedMps", "locationCallback", "Lcom/google/android/gms/location/LocationCallback;", "locationRepository", "Lcom/estoyok/app/features/tracking/domain/repository/LocationRepository;", "getLocationRepository", "()Lcom/estoyok/app/features/tracking/domain/repository/LocationRepository;", "setLocationRepository", "(Lcom/estoyok/app/features/tracking/domain/repository/LocationRepository;)V", "sensorManager", "Landroid/hardware/SensorManager;", "sensorSamples", "", "", "serviceJob", "Lkotlinx/coroutines/CompletableJob;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "checkInternetConnection", "createNotification", "Landroid/app/Notification;", "createNotificationChannel", "", "evaluateDrivingHysteresis", "evaluateImmobilityVariance", "getBatteryStatus", "Lkotlin/Pair;", "launchCrashAlertCountdown", "gForce", "onAccuracyChanged", "sensor", "accuracy", "", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onCreate", "onDestroy", "onSensorChanged", "event", "Landroid/hardware/SensorEvent;", "onStartCommand", "flags", "startId", "processLocationUpdate", "location", "Landroid/location/Location;", "registerAccelerometer", "startLocationUpdates", "startTracking", "interval", "stopLocationUpdates", "stopTracking", "unregisterAccelerometer", "updateInterval", "newInterval", "Companion", "app_debug"})
public final class TrackingService extends android.app.Service implements android.hardware.SensorEventListener {
    @javax.inject.Inject()
    public com.estoyok.app.features.tracking.domain.repository.LocationRepository locationRepository;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CompletableJob serviceJob = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    private com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;
    @org.jetbrains.annotations.Nullable()
    private com.google.android.gms.location.LocationCallback locationCallback;
    private android.hardware.SensorManager sensorManager;
    @org.jetbrains.annotations.Nullable()
    private android.hardware.Sensor accelerometer;
    private long currentIntervalMs = 30000L;
    private boolean isTrackingActive = false;
    private boolean isDriving = false;
    private float lastSpeedMps = 0.0F;
    private long aboveThresholdStartTime = 0L;
    private long belowThresholdStartTime = 0L;
    private double lastLatitude = 0.0;
    private double lastLongitude = 0.0;
    private float lastAccuracy = 0.0F;
    private boolean isPostImpactMonitoring = false;
    private long impactTime = 0L;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<float[]> sensorSamples = null;
    private final float GRAVITY = 9.81F;
    private static boolean isRunning = false;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_ID = "tracking_service_channel";
    public static final int NOTIFICATION_ID = 101;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_START = "ACTION_START";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_STOP = "ACTION_STOP";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_UPDATE_INTERVAL = "ACTION_UPDATE_INTERVAL";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_INTERVAL = "EXTRA_INTERVAL";
    @org.jetbrains.annotations.NotNull()
    public static final com.estoyok.app.services.TrackingService.Companion Companion = null;
    
    public TrackingService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.estoyok.app.features.tracking.domain.repository.LocationRepository getLocationRepository() {
        return null;
    }
    
    public final void setLocationRepository(@org.jetbrains.annotations.NotNull()
    com.estoyok.app.features.tracking.domain.repository.LocationRepository p0) {
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    private final void startTracking(long interval) {
    }
    
    private final void updateInterval(long newInterval) {
    }
    
    private final void stopTracking() {
    }
    
    private final void startLocationUpdates() {
    }
    
    private final void stopLocationUpdates() {
    }
    
    private final void processLocationUpdate(android.location.Location location) {
    }
    
    private final void evaluateDrivingHysteresis() {
    }
    
    private final void registerAccelerometer() {
    }
    
    private final void unregisterAccelerometer() {
    }
    
    @java.lang.Override()
    public void onSensorChanged(@org.jetbrains.annotations.Nullable()
    android.hardware.SensorEvent event) {
    }
    
    private final boolean evaluateImmobilityVariance() {
        return false;
    }
    
    private final void launchCrashAlertCountdown(float gForce) {
    }
    
    @java.lang.Override()
    public void onAccuracyChanged(@org.jetbrains.annotations.Nullable()
    android.hardware.Sensor sensor, int accuracy) {
    }
    
    private final kotlin.Pair<java.lang.Float, java.lang.Boolean> getBatteryStatus() {
        return null;
    }
    
    private final boolean checkInternetConnection() {
        return false;
    }
    
    private final android.app.Notification createNotification() {
        return null;
    }
    
    private final void createNotificationChannel() {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0086T\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000b\u001a\u00020\fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\r\"\u0004\b\u000e\u0010\u000f\u00a8\u0006\u0010"}, d2 = {"Lcom/estoyok/app/services/TrackingService$Companion;", "", "()V", "ACTION_START", "", "ACTION_STOP", "ACTION_UPDATE_INTERVAL", "CHANNEL_ID", "EXTRA_INTERVAL", "NOTIFICATION_ID", "", "isRunning", "", "()Z", "setRunning", "(Z)V", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final boolean isRunning() {
            return false;
        }
        
        public final void setRunning(boolean p0) {
        }
    }
}
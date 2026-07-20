package com.estoyok.app.services

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.TriggerEvent
import android.hardware.TriggerEventListener
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.estoyok.app.BuildConfig
import com.estoyok.app.MainActivity
import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.tracking.data.model.LocationUpdateRequest
import com.estoyok.app.features.tracking.domain.repository.LocationRepository
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.sqrt

@AndroidEntryPoint
class TrackingService : Service(), SensorEventListener {

    @Inject
    lateinit var locationRepository: LocationRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    // Sensor manager for crash detection
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var currentIntervalMs = 30000L // Default 30s
    private var currentMinDistance = 15f   // Default 15m
    private var stationaryStartTime: Long = 0L
    private var isTrackingActive = false

    // Geofencing Client for dynamic stay geofences
    private lateinit var geofencingClient: com.google.android.gms.location.GeofencingClient
    private var isStayGeofenceRegistered = false
    private var isEmergencyMode = false

    // Driving Hysteresis States
    private var isDriving = false
    private var lastSpeedMps = 0.0f
    private var aboveThresholdStartTime: Long = 0
    private var belowThresholdStartTime: Long = 0

    // Coordinates cache for crash reporting
    private var lastLatitude = 0.0
    private var lastLongitude = 0.0
    private var lastAccuracy = 0.0f
    private var lastSentLocation: Location? = null

    // Significant Motion Sensor for low-power wake-up (Activity Recognition alternative)
    private var significantMotionSensor: Sensor? = null
    private var isSignificantMotionRegistered = false
    private val triggerEventListener = object : TriggerEventListener() {
        override fun onTrigger(event: TriggerEvent?) {
            android.util.Log.d("TrackingService", "Significant motion trigger fired!")
            stationaryStartTime = 0L
            isSignificantMotionRegistered = false
            updateInterval(30000L) // Switch back to active walking mode immediately (30s)
        }
    }

    // Crash algorithm states
    private var isPostImpactMonitoring = false
    private var impactTime: Long = 0
    private val sensorSamples = mutableListOf<FloatArray>()
    private val GRAVITY = 9.81f

    companion object {
        var isRunning = false
        const val CHANNEL_ID = "tracking_service_channel"
        const val NOTIFICATION_ID = 101
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_UPDATE_INTERVAL = "ACTION_UPDATE_INTERVAL"
        const val EXTRA_INTERVAL = "EXTRA_INTERVAL"
        const val EXTRA_EMERGENCY = "EXTRA_EMERGENCY"
    }

    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("TrackingService", "onCreate called")
        isRunning = true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        significantMotionSensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)
        geofencingClient = LocationServices.getGeofencingClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d("TrackingService", "onStartCommand called with action: ${intent?.action}")
        when (intent?.action) {
            ACTION_START -> {
                val interval = intent.getLongExtra(EXTRA_INTERVAL, 30000L)
                isEmergencyMode = intent.getBooleanExtra(EXTRA_EMERGENCY, false)
                startTracking(interval)
            }
            ACTION_STOP -> {
                isEmergencyMode = false
                stopTracking()
            }
            ACTION_UPDATE_INTERVAL -> {
                val interval = intent.getLongExtra(EXTRA_INTERVAL, 30000L)
                isEmergencyMode = intent.getBooleanExtra(EXTRA_EMERGENCY, false)
                if (!isTrackingActive) {
                    startTracking(interval)
                } else {
                    updateInterval(interval)
                }
            }
        }
        return START_STICKY
    }

    private fun startTracking(interval: Long) {
        android.util.Log.d("TrackingService", "startTracking called. isTrackingActive: $isTrackingActive")
        if (isTrackingActive) return
        isTrackingActive = true
        currentIntervalMs = interval
        lastSentLocation = null

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        startLocationUpdates()
    }

    private fun updateInterval(newInterval: Long) {
        android.util.Log.d("TrackingService", "updateInterval called. newInterval: $newInterval")
        if (currentIntervalMs == newInterval) return
        currentIntervalMs = newInterval
        // If it's an emergency or active checking, disable distance filter. Otherwise use default 15m.
        currentMinDistance = if (newInterval <= 5000L) 0f else 15f
        
        // If leaving stationary mode, remove stay geofence immediately
        if (newInterval <= 30000L) {
            unregisterStayGeofence()
        }

        if (isTrackingActive) {
            stopLocationUpdates()
            startLocationUpdates()
        }
    }

    private fun stopTracking() {
        android.util.Log.d("TrackingService", "stopTracking called")
        isTrackingActive = false
        stopLocationUpdates()
        unregisterAccelerometer()
        unregisterSignificantMotion()
        unregisterStayGeofence()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startLocationUpdates() {
        android.util.Log.d("TrackingService", "startLocationUpdates called. Interval: $currentIntervalMs, MinDistance: $currentMinDistance")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            android.util.Log.e("TrackingService", "Location permissions NOT GRANTED. Stopping tracking service.")
            stopTracking()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            currentIntervalMs
        ).apply {
            setMinUpdateIntervalMillis(currentIntervalMs / 2)
            setMinUpdateDistanceMeters(currentMinDistance)
            setWaitForAccurateLocation(false)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                android.util.Log.d("TrackingService", "onLocationResult received ${locationResult.locations.size} locations")
                for (location in locationResult.locations) {
                    processLocationUpdate(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        android.util.Log.d("TrackingService", "stopLocationUpdates called")
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
    }

    private fun processLocationUpdate(location: Location) {
        android.util.Log.d("TrackingService", "processLocationUpdate: Lat: ${location.latitude}, Lng: ${location.longitude}, Speed: ${location.speed}, Accuracy: ${location.accuracy}")
        lastLatitude = location.latitude
        lastLongitude = location.longitude
        lastAccuracy = location.accuracy
        lastSpeedMps = if (location.hasSpeed()) location.speed else 0.0f

        evaluateDrivingHysteresis()
        adjustTrackingMode(lastSpeedMps, location)

        // 1. Accuracy Filter: discard noisy updates (accuracy > 40m) if not in emergency mode
        if (location.hasAccuracy() && location.accuracy > 40f && !isEmergencyMode) {
            android.util.Log.d("TrackingService", "Discarding location update due to poor accuracy: ${location.accuracy}m")
            return
        }

        // 2. GPS Drift Filter: if the user is stationary (speed < 3 km/h) and displacement is small (< 15m), discard it (if not in emergency/driving)
        lastSentLocation?.let { lastLoc ->
            val distance = lastLoc.distanceTo(location)
            val speedKmh = lastSpeedMps * 3.6f
            
            if (distance < 15f && speedKmh < 3.0f && !isEmergencyMode && !isDriving) {
                android.util.Log.d("TrackingService", "Discarding location update as drift: distance=$distance m, speed=$speedKmh km/h")
                return
            }
        }

        lastSentLocation = location

        serviceScope.launch {
            val batteryStatus = getBatteryStatus()
            val isOnline = checkInternetConnection()

            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            val request = LocationUpdateRequest(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy,
                batteryLevel = batteryStatus.first,
                isTrackingActive = true,
                gpsEnabled = true,
                recordedAt = isoFormat.format(Date(location.time)),
                speed = lastSpeedMps,
                isDriving = isDriving
            )

            android.util.Log.d("TrackingService", "Calling locationRepository.updateLocation. isOnline: $isOnline")
            locationRepository.updateLocation(request, isOnline).collectLatest { resource ->
                android.util.Log.d("TrackingService", "updateLocation resource: ${resource.javaClass.simpleName}")
                if (resource is Resource.Success) {
                    val data = resource.data
                    android.util.Log.d("TrackingService", "updateLocation Success! Message: ${data?.message}, activeGeofence: ${data?.activeDynamicGeofence}")
                    
                    // Flush offline queue if we are online and successfully sent the current location
                    if (isOnline) {
                        serviceScope.launch {
                            locationRepository.flushOfflineQueue().collectLatest { syncResource ->
                                if (syncResource is Resource.Success) {
                                    android.util.Log.d("TrackingService", "Synced ${syncResource.data} offline locations from Room.")
                                } else if (syncResource is Resource.Error) {
                                    android.util.Log.e("TrackingService", "Failed to sync offline locations: ${syncResource.message}")
                                }
                            }
                        }
                    }

                    if (data?.activeDynamicGeofence == true) {
                        updateInterval(5000L) // GPS high fidelity for relative geofencing
                    }
                } else if (resource is Resource.Error) {
                    android.util.Log.e("TrackingService", "updateLocation Error: ${resource.message}")
                }
            }
        }
    }

    private fun adjustTrackingMode(speedMps: Float, currentLoc: Location) {
        val speedKmh = speedMps * 3.6f
        val now = System.currentTimeMillis()

        val (targetInterval, targetDistance) = when {
            // Vehicle: Speed > 15 km/h
            speedKmh > 15.0f -> {
                stationaryStartTime = 0L
                unregisterSignificantMotion()
                unregisterStayGeofence()
                Pair(5000L, 15f)
            }
            // Walking: Speed between 1.5 and 15 km/h
            speedKmh > 1.5f -> {
                stationaryStartTime = 0L
                unregisterSignificantMotion()
                unregisterStayGeofence()
                Pair(30000L, 15f)
            }
            // Stationary candidate: Speed < 1.5 km/h
            else -> {
                if (stationaryStartTime == 0L) {
                    stationaryStartTime = now
                }
                
                // Only enter Stationary low-power mode after 2 minutes of zero activity
                if (now - stationaryStartTime >= 120000L) {
                    registerSignificantMotion()
                    registerStayGeofence(currentLoc.latitude, currentLoc.longitude)
                    Pair(300000L, 20f)
                } else {
                    // Stay in the last active mode (default to Walking if unknown, or keep current)
                    if (currentIntervalMs <= 5000L) Pair(5000L, 15f)
                    else if (currentIntervalMs <= 10000L) Pair(10000L, 40f)
                    else Pair(30000L, 15f)
                }
            }
        }

        // If emergency or check-in alert is active, force maximum high-frequency mode (5s updates)
        if (currentIntervalMs <= 5000L && targetInterval > 5000L) {
            return
        }

        if (currentIntervalMs != targetInterval || currentMinDistance != targetDistance) {
            currentIntervalMs = targetInterval
            currentMinDistance = targetDistance
            if (isTrackingActive) {
                stopLocationUpdates()
                startLocationUpdates()
            }
        }
    }

    // --- Driving State Hysteresis ---
    private fun evaluateDrivingHysteresis() {
        val speedKmh = lastSpeedMps * 3.6f
        val now = System.currentTimeMillis()
        val isDebug = BuildConfig.DEBUG

        // Dev mode triggers driving changes faster to simplify testing
        val timeToStart = if (isDebug) 5000L else 60000L // 5s vs 1m
        val timeToStop = if (isDebug) 10000L else 120000L // 10s vs 2m

        if (speedKmh > 25.0f) {
            belowThresholdStartTime = 0
            if (aboveThresholdStartTime == 0L) {
                aboveThresholdStartTime = now
            } else if (now - aboveThresholdStartTime >= timeToStart && !isDriving) {
                isDriving = true
                registerAccelerometer()
            }
        } else {
            aboveThresholdStartTime = 0
            if (isDriving) {
                if (belowThresholdStartTime == 0L) {
                    belowThresholdStartTime = now
                } else if (now - belowThresholdStartTime >= timeToStop) {
                    isDriving = false
                    unregisterAccelerometer()
                }
            }
        }
    }

    private fun registerAccelerometer() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun unregisterAccelerometer() {
        sensorManager.unregisterListener(this)
        isPostImpactMonitoring = false
    }

    // --- Accelerometer Detections ---
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val totalAcc = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        if (!isPostImpactMonitoring) {
            // PATTERN 1: Net acceleration >= 4.5G (g-force threshold)
            val gForce = totalAcc / GRAVITY
            if (gForce >= 4.5f) {
                isPostImpactMonitoring = true
                impactTime = System.currentTimeMillis()
                sensorSamples.clear()
            }
        } else {
            val now = System.currentTimeMillis()
            if (now - impactTime <= 3000L) {
                // Collect samples for 3 seconds post impact
                sensorSamples.add(floatArrayOf(x, y, z))
            } else {
                // PATTERN 2: Evaluate 3s variance for immobility verification (< 0.15G variance)
                isPostImpactMonitoring = false
                val isStill = evaluateImmobilityVariance()
                if (isStill) {
                    // CRASH PATTERN CONFIRMED! Launch countdown overlay
                    launchCrashAlertCountdown(totalAcc / GRAVITY)
                }
            }
        }
    }

    private fun evaluateImmobilityVariance(): Boolean {
        if (sensorSamples.isEmpty()) return false
        
        var meanX = 0f
        var meanY = 0f
        var meanZ = 0f
        for (s in sensorSamples) {
            meanX += s[0]
            meanY += s[1]
            meanZ += s[2]
        }
        meanX /= sensorSamples.size
        meanY /= sensorSamples.size
        meanZ /= sensorSamples.size

        var varSum = 0f
        for (s in sensorSamples) {
            val devX = s[0] - meanX
            val devY = s[1] - meanY
            val devZ = s[2] - meanZ
            val totalDev = sqrt((devX * devX + devY * devY + devZ * devZ).toDouble()).toFloat()
            varSum += totalDev * totalDev
        }
        val variance = varSum / sensorSamples.size
        
        // Max variance tolerance: 0.15G variance (approx 1.47 m/s2)
        val tolerance = 0.15f * GRAVITY
        return variance < (tolerance * tolerance)
    }

    private fun launchCrashAlertCountdown(gForce: Float) {
        val intent = Intent(this, com.estoyok.app.features.tracking.presentation.CrashAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("latitude", lastLatitude)
            putExtra("longitude", lastLongitude)
            putExtra("speed", lastSpeedMps * 3.6f)
            putExtra("g_force", gForce)
        }
        startActivity(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun getBatteryStatus(): Pair<Float?, Boolean?> {
        val batteryStatusIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) level / scale.toFloat() else null
        val isLow = if (batteryPct != null) batteryPct < 0.15f else null
        return Pair(batteryPct, isLow)
    }

    private fun checkInternetConnection(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Estoy Ok Activo")
            .setContentText("Tu ubicación y seguridad están siendo monitoreadas en segundo plano.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rastreo de Seguridad",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificación persistente para garantizar el monitoreo del GPS."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        android.util.Log.d("TrackingService", "onTaskRemoved called. isTrackingActive: $isTrackingActive")
        
        if (isTrackingActive) {
            val restartServiceIntent = Intent(applicationContext, this.javaClass).apply {
                action = ACTION_START
                setPackage(packageName)
            }
            
            val restartServicePendingIntent = PendingIntent.getService(
                applicationContext,
                1,
                restartServiceIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmService.set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 5000L,
                restartServicePendingIntent
            )
        }
    }

    private fun registerSignificantMotion() {
        if (isSignificantMotionRegistered) return
        significantMotionSensor?.let { sensor ->
            val success = sensorManager.requestTriggerSensor(triggerEventListener, sensor)
            isSignificantMotionRegistered = success
            android.util.Log.d("TrackingService", "SignificantMotion sensor trigger requested: $success")
        }
    }

    private fun unregisterSignificantMotion() {
        if (!isSignificantMotionRegistered) return
        significantMotionSensor?.let { sensor ->
            sensorManager.cancelTriggerSensor(triggerEventListener, sensor)
            isSignificantMotionRegistered = false
            android.util.Log.d("TrackingService", "SignificantMotion sensor trigger cancelled")
        }
    }

    private fun registerStayGeofence(latitude: Double, longitude: Double) {
        if (isStayGeofenceRegistered) return
        
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            android.util.Log.w("TrackingService", "Cannot register stay geofence: Permission denied.")
            return
        }

        val geofence = com.google.android.gms.location.Geofence.Builder()
            .setRequestId("dynamic_stay_geofence")
            .setCircularRegion(latitude, longitude, 100f) // 100 meters radius
            .setExpirationDuration(com.google.android.gms.location.Geofence.NEVER_EXPIRE)
            .setTransitionTypes(com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val request = com.google.android.gms.location.GeofencingRequest.Builder()
            .setInitialTrigger(com.google.android.gms.location.GeofencingRequest.INITIAL_TRIGGER_EXIT)
            .addGeofence(geofence)
            .build()

        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            this,
            202,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
        )

        geofencingClient.addGeofences(request, pendingIntent)
            .addOnSuccessListener {
                isStayGeofenceRegistered = true
                android.util.Log.d("TrackingService", "Successfully registered dynamic stay geofence at ($latitude, $longitude)")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("TrackingService", "Failed to register stay geofence: ${e.message}", e)
            }
    }

    private fun unregisterStayGeofence() {
        if (!isStayGeofenceRegistered) return
        
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            this,
            202,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
        )

        geofencingClient.removeGeofences(pendingIntent)
            .addOnSuccessListener {
                isStayGeofenceRegistered = false
                android.util.Log.d("TrackingService", "Successfully unregistered dynamic stay geofence")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("TrackingService", "Failed to unregister stay geofence: ${e.message}", e)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        unregisterAccelerometer()
        unregisterSignificantMotion()
        unregisterStayGeofence()
        serviceJob.cancel()
    }
}

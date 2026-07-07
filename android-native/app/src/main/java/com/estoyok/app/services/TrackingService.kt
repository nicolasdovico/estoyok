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
    private var isTrackingActive = false

    // Driving Hysteresis States
    private var isDriving = false
    private var lastSpeedMps = 0.0f
    private var aboveThresholdStartTime: Long = 0
    private var belowThresholdStartTime: Long = 0

    // Coordinates cache for crash reporting
    private var lastLatitude = 0.0
    private var lastLongitude = 0.0
    private var lastAccuracy = 0.0f

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
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val interval = intent.getLongExtra(EXTRA_INTERVAL, 30000L)
                startTracking(interval)
            }
            ACTION_STOP -> {
                stopTracking()
            }
            ACTION_UPDATE_INTERVAL -> {
                val interval = intent.getLongExtra(EXTRA_INTERVAL, 30000L)
                updateInterval(interval)
            }
        }
        return START_STICKY
    }

    private fun startTracking(interval: Long) {
        if (isTrackingActive) return
        isTrackingActive = true
        currentIntervalMs = interval

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        startLocationUpdates()
    }

    private fun updateInterval(newInterval: Long) {
        if (currentIntervalMs == newInterval) return
        currentIntervalMs = newInterval
        if (isTrackingActive) {
            stopLocationUpdates()
            startLocationUpdates()
        }
    }

    private fun stopTracking() {
        isTrackingActive = false
        stopLocationUpdates()
        unregisterAccelerometer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopTracking()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            currentIntervalMs
        ).apply {
            setMinUpdateIntervalMillis(currentIntervalMs / 2)
            setWaitForAccurateLocation(false)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
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
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
    }

    private fun processLocationUpdate(location: Location) {
        lastLatitude = location.latitude
        lastLongitude = location.longitude
        lastAccuracy = location.accuracy
        lastSpeedMps = if (location.hasSpeed()) location.speed else 0.0f

        evaluateDrivingHysteresis()

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

            locationRepository.updateLocation(request, isOnline).collectLatest { resource ->
                if (resource is Resource.Success) {
                    val data = resource.data
                    if (data?.activeDynamicGeofence == true) {
                        updateInterval(5000L) // GPS high fidelity for relative geofencing
                    }
                }
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

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        unregisterAccelerometer()
        serviceJob.cancel()
    }
}

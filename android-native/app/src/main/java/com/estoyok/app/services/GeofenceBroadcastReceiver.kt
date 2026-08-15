package com.estoyok.app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e("GeofenceReceiver", "GeofencingEvent is null")
            return
        }

        if (geofencingEvent.hasError()) {
            Log.e("GeofenceReceiver", "GeofencingEvent error code: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        Log.d("GeofenceReceiver", "onReceive triggered. Transition: $geofenceTransition")

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            Log.d("GeofenceReceiver", "Geofence GEOFENCE_TRANSITION_EXIT triggered for: ${triggeringGeofences?.map { it.requestId }}")

            val hasStayGeofence = triggeringGeofences?.any { it.requestId == "dynamic_stay_geofence" } ?: false
            if (hasStayGeofence) {
                Log.d("GeofenceReceiver", "User left dynamic stay geofence. Waking up TrackingService.")

                // 1. Enqueue WorkManager one-time request for guaranteed background execution on Android 12+
                try {
                    val oneTimeSync = OneTimeWorkRequestBuilder<LocationSyncWorker>().build()
                    WorkManager.getInstance(context).enqueue(oneTimeSync)
                } catch (e: Exception) {
                    Log.e("GeofenceReceiver", "Error enqueuing WorkManager on geofence exit: ${e.message}")
                }

                // 2. Direct wake up attempt of TrackingService
                try {
                    val serviceIntent = Intent(context, TrackingService::class.java).apply {
                        action = TrackingService.ACTION_UPDATE_INTERVAL
                        putExtra(TrackingService.EXTRA_INTERVAL, 30000L) // Switch back to active walking mode
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ContextCompat.startForegroundService(context, serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (e: Exception) {
                    Log.w("GeofenceReceiver", "Direct startForegroundService restricted in background (Android 12+): ${e.message}. Handled safely via WorkManager.")
                }
            }
        }
    }
}

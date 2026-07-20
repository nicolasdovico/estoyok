package com.estoyok.app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
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
                
                // Wake up TrackingService by sending an ACTION_UPDATE_INTERVAL with 30s interval
                val serviceIntent = Intent(context, TrackingService::class.java).apply {
                    action = TrackingService.ACTION_UPDATE_INTERVAL
                    putExtra(TrackingService.EXTRA_INTERVAL, 30000L) // Switch back to active walking mode
                }
                ContextCompat.startForegroundService(context, serviceIntent)
            }
        }
    }
}

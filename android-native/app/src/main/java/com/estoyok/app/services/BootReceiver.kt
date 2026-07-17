package com.estoyok.app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.estoyok.app.core.data.local.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        android.util.Log.d("BootReceiver", "Received action: $action")
        
        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == Intent.ACTION_MY_PACKAGE_REPLACED || 
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val token = sessionManager.authTokenFlow.firstOrNull()
                    val isTrackingEnabled = sessionManager.isTrackingEnabledFlow.firstOrNull() ?: false
                    android.util.Log.d("BootReceiver", "Boot processed. Token present: ${token != null}, trackingEnabled: $isTrackingEnabled")
                    
                    if (token != null && isTrackingEnabled) {
                        val serviceIntent = Intent(context, TrackingService::class.java).apply {
                            this.action = TrackingService.ACTION_START
                        }
                        ContextCompat.startForegroundService(context, serviceIntent)
                        android.util.Log.d("BootReceiver", "TrackingService auto-started successfully on boot/update.")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BootReceiver", "Error auto-starting TrackingService: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

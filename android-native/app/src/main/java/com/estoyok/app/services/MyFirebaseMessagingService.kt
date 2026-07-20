package com.estoyok.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.estoyok.app.MainActivity
import com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService", "onNewToken: $token")
        
        // Send token to backend if the user is authenticated
        serviceScope.launch {
            settingsRepository.updatePushToken(token).collectLatest { resource ->
                Log.d("FCMService", "Sending token to backend result: $resource")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCMService", "onMessageReceived from: ${message.from}")

        // Check for Silent Push "wake_up" action
        if (message.data.isNotEmpty()) {
            Log.d("FCMService", "Message data payload: ${message.data}")
            val action = message.data["action"]
            if (action == "wake_up") {
                Log.d("FCMService", "Received wake-up silent push. Waking up TrackingService.")
                
                // Wake up or restart TrackingService to active tracking mode (30s interval)
                val serviceIntent = Intent(this, TrackingService::class.java).apply {
                    this.action = TrackingService.ACTION_UPDATE_INTERVAL
                    putExtra(TrackingService.EXTRA_INTERVAL, 30000L)
                }
                ContextCompat.startForegroundService(this, serviceIntent)
                return
            }
        }

        // Check for normal Notification payload
        message.notification?.let { notification ->
            Log.d("FCMService", "Message Notification Title: ${notification.title}, Body: ${notification.body}")
            showNotification(notification.title ?: "Estoy Ok", notification.body ?: "")
        }
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "fcm_default_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas y Notificaciones",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de Estoy Ok"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}

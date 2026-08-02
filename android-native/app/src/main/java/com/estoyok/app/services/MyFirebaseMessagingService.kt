package com.estoyok.app.services

import android.app.Notification
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
import com.estoyok.app.core.data.local.SessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var sessionManager: SessionManager

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

        // 1. Check for Silent Push actions (logout or wake_up)
        if (message.data.isNotEmpty()) {
            Log.d("FCMService", "Message data payload: ${message.data}")
            val action = message.data["action"]
            
            if (action == "logout") {
                Log.d("FCMService", "Received silent push logout command. Shutting down session.")
                try {
                    val serviceIntent = Intent(this, TrackingService::class.java).apply {
                        this.action = TrackingService.ACTION_STOP
                    }
                    stopService(serviceIntent)
                } catch (e: Exception) {
                    Log.e("FCMService", "Error stopping TrackingService: ${e.message}")
                }
                
                try {
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().deleteToken()
                } catch (e: Exception) {
                    // Ignore FCM token deletion errors
                }

                runBlocking {
                    sessionManager.clearSession()
                }

                showNotification("Sesión Finalizada", "Tu sesión se cerró porque ingresaste desde otro dispositivo.")
                return
            }

            if (action == "wake_up") {
                val token = runBlocking { sessionManager.authTokenFlow.firstOrNull() }
                if (token.isNullOrEmpty()) {
                    Log.d("FCMService", "Suppressing wake_up because no session is active.")
                    return
                }

                Log.d("FCMService", "Received wake-up silent push. Waking up TrackingService.")
                val serviceIntent = Intent(this, TrackingService::class.java).apply {
                    this.action = TrackingService.ACTION_UPDATE_INTERVAL
                    putExtra(TrackingService.EXTRA_INTERVAL, 30000L)
                }
                ContextCompat.startForegroundService(this, serviceIntent)
                return
            }
        }

        // 2. Client-side Session Guard: do not show notifications if user is logged out
        val currentToken = runBlocking { sessionManager.authTokenFlow.firstOrNull() }
        if (currentToken.isNullOrEmpty()) {
            Log.d("FCMService", "Suppressing push notification because user is logged out.")
            return
        }

        // 3. Check for normal Notification payload
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
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
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
            .setSmallIcon(com.estoyok.app.R.drawable.ic_stat_notification)
            .setLargeIcon(android.graphics.BitmapFactory.decodeResource(resources, com.estoyok.app.R.mipmap.ic_launcher))
            .setColor(ContextCompat.getColor(this, com.estoyok.app.R.color.primary_emerald))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_EVENT)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}

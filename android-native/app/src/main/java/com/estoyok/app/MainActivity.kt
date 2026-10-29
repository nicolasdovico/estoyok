package com.estoyok.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.estoyok.app.core.data.local.SessionManager
import com.estoyok.app.core.navigation.MainScreen
import com.estoyok.app.core.theme.EstoyOkTheme
import com.estoyok.app.services.TrackingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import android.widget.Toast

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    private val deepLinkFlow = MutableStateFlow<String?>(null)
    private var appUpdateManager: AppUpdateManager? = null
    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Toast.makeText(
                this,
                "Actualización lista para instalar. Reiniciando...",
                Toast.LENGTH_LONG
            ).show()
            appUpdateManager?.completeUpdate()
        }
    }

    companion object {
        private const val REQUEST_CODE_FLEXIBLE_UPDATE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
        checkForInAppUpdates()

        setContent {
            EstoyOkTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val deepLinkUrl by deepLinkFlow.collectAsState()
                    MainScreen(
                        deepLinkUrl = deepLinkUrl,
                        onDeepLinkHandled = { deepLinkFlow.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.dataString
        if (!data.isNullOrEmpty() && (data.contains("estoyok") || data.contains("subscription"))) {
            deepLinkFlow.value = data
        }
    }

    override fun onResume() {
        super.onResume()
        autoStartTrackingServiceIfAuthenticated()
        syncSubscriptionOnResume()
        checkPendingUpdateOnResume()
    }

    private fun checkForInAppUpdates() {
        try {
            val manager = AppUpdateManagerFactory.create(this)
            appUpdateManager = manager
            manager.registerListener(installStateUpdatedListener)

            val appUpdateInfoTask = manager.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    if (appUpdateInfo.isImmediateUpdateAllowed) {
                        try {
                            manager.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                this,
                                REQUEST_CODE_FLEXIBLE_UPDATE
                            )
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error starting immediate update: ${e.message}")
                        }
                    } else if (appUpdateInfo.isFlexibleUpdateAllowed) {
                        try {
                            manager.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.FLEXIBLE,
                                this,
                                REQUEST_CODE_FLEXIBLE_UPDATE
                            )
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error starting flexible update: ${e.message}")
                        }
                    }
                }
            }.addOnFailureListener { e ->
                Log.d("MainActivity", "In-App Update check skipped or failed: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing In-App Update manager: ${e.message}")
        }
    }

    private fun checkPendingUpdateOnResume() {
        try {
            appUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    appUpdateManager?.completeUpdate()
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    // Si una actualización inmediata fue interrumpida, reanudar
                    try {
                        appUpdateManager?.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            REQUEST_CODE_FLEXIBLE_UPDATE
                        )
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error resuming in-app update: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking pending update on resume: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            appUpdateManager?.unregisterListener(installStateUpdatedListener)
        } catch (_: Exception) { }
    }

    @Inject
    lateinit var settingsRepository: com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository

    @Inject
    lateinit var playBillingManager: com.estoyok.app.core.billing.PlayBillingManager

    @Inject
    lateinit var subscriptionRepository: com.estoyok.app.features.tracking.domain.repository.SubscriptionRepository

    private fun syncSubscriptionOnResume() {
        lifecycleScope.launch {
            val token = sessionManager.authTokenFlow.firstOrNull()
            if (!token.isNullOrEmpty()) {
                playBillingManager.initialize()
                val isReady = playBillingManager.isReady.firstOrNull { it }
                if (isReady == true) {
                    playBillingManager.queryActivePurchases { activePurchase ->
                        if (activePurchase != null) {
                            lifecycleScope.launch {
                                subscriptionRepository.syncGooglePlay(
                                    hasActiveSubscription = true,
                                    purchaseToken = activePurchase.purchaseToken,
                                    productId = com.estoyok.app.core.billing.PlayBillingManager.PRODUCT_ID_PREMIUM
                                ).collect { }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun autoStartTrackingServiceIfAuthenticated() {
        lifecycleScope.launch {
            val token = sessionManager.authTokenFlow.firstOrNull()
            val isTrackingEnabled = sessionManager.isTrackingEnabledFlow.firstOrNull() ?: true

            if (!token.isNullOrEmpty()) {
                // Sync FCM Token with backend on resume
                try {
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val fcmToken = task.result
                            lifecycleScope.launch {
                                val uuid = sessionManager.getOrCreateDeviceUuid()
                                settingsRepository.updatePushToken(fcmToken, uuid).collect { }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error syncing FCM token on resume: ${e.message}")
                }

                if (isTrackingEnabled && !TrackingService.isRunning) {
                    val hasLocationPermission = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasLocationPermission) {
                        try {
                            val intent = Intent(this@MainActivity, TrackingService::class.java).apply {
                                action = TrackingService.ACTION_START
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(intent)
                            } else {
                                startService(intent)
                            }
                            Log.d("MainActivity", "TrackingService auto-started on app resume.")
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error starting TrackingService on resume: ${e.message}", e)
                        }
                    }
                }
            }
        }
    }
}

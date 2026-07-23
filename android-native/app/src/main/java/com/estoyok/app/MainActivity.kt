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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EstoyOkTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        autoStartTrackingServiceIfAuthenticated()
    }

    private fun autoStartTrackingServiceIfAuthenticated() {
        lifecycleScope.launch {
            val token = sessionManager.authTokenFlow.firstOrNull()
            val isTrackingEnabled = sessionManager.isTrackingEnabledFlow.firstOrNull() ?: true

            if (!token.isNullOrEmpty() && isTrackingEnabled && !TrackingService.isRunning) {
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

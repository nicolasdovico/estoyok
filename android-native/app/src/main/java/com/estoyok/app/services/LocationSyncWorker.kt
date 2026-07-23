package com.estoyok.app.services

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.estoyok.app.core.data.local.SessionManager
import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.tracking.domain.repository.LocationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class LocationSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val locationRepository: LocationRepository,
    private val sessionManager: SessionManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        var success = true

        // 1. Check if user is authenticated
        val token = sessionManager.authTokenFlow.firstOrNull()
        if (!token.isNullOrEmpty()) {
            // 2. Auto-heal TrackingService if it was killed by Android OS
            if (!TrackingService.isRunning) {
                val hasLocationPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (hasLocationPermission) {
                    try {
                        val intent = Intent(context, TrackingService::class.java).apply {
                            action = TrackingService.ACTION_START
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }
                        Log.d("LocationSyncWorker", "TrackingService auto-healed and restarted by WorkManager.")
                    } catch (e: Exception) {
                        Log.e("LocationSyncWorker", "Error auto-healing TrackingService: ${e.message}", e)
                    }
                }
            }

            // 3. Flush offline location queue
            locationRepository.flushOfflineQueue().collectLatest { resource ->
                if (resource is Resource.Error) {
                    success = false
                }
            }
        }

        return if (success) Result.success() else Result.retry()
    }
}

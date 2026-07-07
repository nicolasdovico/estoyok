package com.estoyok.app.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.tracking.domain.repository.LocationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collectLatest

@HiltWorker
class LocationSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val locationRepository: LocationRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        var success = true
        locationRepository.flushOfflineQueue().collectLatest { resource ->
            if (resource is Resource.Error) {
                success = false
            }
        }
        return if (success) Result.success() else Result.retry()
    }
}

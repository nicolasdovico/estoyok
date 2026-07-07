package com.estoyok.app.features.tracking.domain.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.tracking.data.model.*
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    
    fun updateLocation(
        request: LocationUpdateRequest,
        isOnline: Boolean
    ): Flow<Resource<LocationUpdateResponse>>

    fun updateSensorStatus(
        request: SensorStatusRequest
    ): Flow<Resource<SensorStatusResponse>>

    fun flushOfflineQueue(): Flow<Resource<Int>> // returns number of synced points
}

package com.estoyok.app.features.tracking.domain.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.MessageResponse
import com.estoyok.app.features.tracking.data.model.CrashAlertResponse
import kotlinx.coroutines.flow.Flow

interface CrashRepository {
    
    fun reportCrash(
        latitude: Double,
        longitude: Double,
        speedAtImpact: Float,
        gForce: Float
    ): Flow<Resource<CrashAlertResponse>>

    fun markFalseAlarm(
        crashEventId: Int
    ): Flow<Resource<MessageResponse>>
}

package com.estoyok.app.features.tracking.domain.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.tracking.data.model.CheckoutResponse
import com.estoyok.app.features.tracking.data.model.StartTrialResponse
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    
    fun checkout(
        provider: String
    ): Flow<Resource<CheckoutResponse>>

    fun startTrial(
        provider: String
    ): Flow<Resource<StartTrialResponse>>
}

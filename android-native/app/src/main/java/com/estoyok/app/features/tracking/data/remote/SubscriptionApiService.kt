package com.estoyok.app.features.tracking.data.remote

import com.estoyok.app.features.tracking.data.model.CheckoutRequest
import com.estoyok.app.features.tracking.data.model.CheckoutResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SubscriptionApiService {

    @POST("subscriptions/checkout")
    suspend fun checkout(
        @Body request: CheckoutRequest
    ): Response<CheckoutResponse>
}

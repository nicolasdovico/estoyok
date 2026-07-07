package com.estoyok.app.features.tracking.data.remote

import com.estoyok.app.features.tracking.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface LocationApiService {

    @POST("locations/update")
    suspend fun updateLocation(
        @Body request: LocationUpdateRequest
    ): Response<LocationUpdateResponse>

    @PUT("locations/sensor-status")
    suspend fun updateSensorStatus(
        @Body request: SensorStatusRequest
    ): Response<SensorStatusResponse>
}

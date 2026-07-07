package com.estoyok.app.features.wellbeing.data.remote

import com.estoyok.app.features.wellbeing.data.model.CheckInDto
import com.estoyok.app.features.wellbeing.data.model.CheckInRequest
import com.estoyok.app.features.wellbeing.data.model.CheckInResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CheckInApiService {

    @POST("check-in")
    suspend fun checkIn(
        @Body request: CheckInRequest = CheckInRequest()
    ): Response<CheckInResponse>

    @GET("check-ins")
    suspend fun getCheckIns(): Response<List<CheckInDto>>
}

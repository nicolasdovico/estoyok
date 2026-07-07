package com.estoyok.app.features.tracking.data.remote

import com.estoyok.app.features.auth.data.model.MessageResponse
import com.estoyok.app.features.tracking.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface CrashApiService {

    @POST("alerts/crash")
    suspend fun reportCrash(
        @Body request: CrashAlertRequest
    ): Response<CrashAlertResponse>

    @POST("alerts/crash/{id}/false-alarm")
    suspend fun markFalseAlarm(
        @Path("id") id: Int
    ): Response<MessageResponse>
}

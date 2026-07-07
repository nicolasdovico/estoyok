package com.estoyok.app.features.tracking.data.remote

import com.estoyok.app.features.tracking.data.model.AudioUploadResponse
import com.estoyok.app.features.tracking.data.model.SosAlertResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface SosApiService {

    @POST("emergency-alerts/sos")
    suspend fun triggerSos(): Response<SosAlertResponse>

    @Multipart
    @POST("emergency-alerts/{id}/audio")
    suspend fun uploadAudio(
        @Path("id") id: String,
        @Part audio: MultipartBody.Part
    ): Response<AudioUploadResponse>
}

package com.estoyok.app.features.tracking.data.model

import com.google.gson.annotations.SerializedName

data class SosAlertResponse(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("type") val type: String,
    @SerializedName("status") val status: String,
    @SerializedName("expires_at") val expiresAt: String
)

data class AudioUploadResponse(
    @SerializedName("message") val message: String,
    @SerializedName("audio_url") val audioUrl: String?
)

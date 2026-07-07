package com.estoyok.app.features.wellbeing.data.model

import com.google.gson.annotations.SerializedName

data class CheckInDto(
    @SerializedName("id") val id: Int,
    @SerializedName("source") val source: String,
    @SerializedName("created_at") val createdAt: String
)

data class CheckInRequest(
    @SerializedName("source") val source: String = "manual"
)

data class CheckInResponse(
    @SerializedName("message") val message: String,
    @SerializedName("last_check_in_at") val lastCheckInAt: String
)

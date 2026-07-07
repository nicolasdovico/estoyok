package com.estoyok.app.features.tracking.data.model

import com.google.gson.annotations.SerializedName

data class CrashEventDto(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("speed_at_impact") val speedAtImpact: Float,
    @SerializedName("g_force") val gForce: Float,
    @SerializedName("status") val status: String
)

data class CrashAlertRequest(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("speed_at_impact") val speedAtImpact: Float,
    @SerializedName("g_force") val gForce: Float
)

data class CrashAlertResponse(
    @SerializedName("message") val message: String,
    @SerializedName("crash_event") val crashEvent: CrashEventDto
)

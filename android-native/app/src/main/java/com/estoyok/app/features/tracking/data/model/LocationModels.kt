package com.estoyok.app.features.tracking.data.model

import com.google.gson.annotations.SerializedName

data class LocationUpdateRequest(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("accuracy") val accuracy: Float? = null,
    @SerializedName("battery_level") val batteryLevel: Float? = null, // between 0.0 and 1.0
    @SerializedName("is_tracking_active") val isTrackingActive: Boolean? = null,
    @SerializedName("gps_enabled") val gpsEnabled: Boolean? = null,
    @SerializedName("recorded_at") val recordedAt: String? = null,
    @SerializedName("speed") val speed: Float? = null, // in m/s
    @SerializedName("is_driving") val isDriving: Boolean? = null
)

data class LocationUpdateResponse(
    @SerializedName("message") val message: String,
    @SerializedName("active_dynamic_geofence") val activeDynamicGeofence: Boolean
)

data class SensorStatusRequest(
    @SerializedName("is_tracking_active") val isTrackingActive: Boolean? = null,
    @SerializedName("gps_enabled") val gpsEnabled: Boolean? = null
)

data class SensorStatusResponse(
    @SerializedName("message") val message: String
)

data class LocationHistoryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("accuracy") val accuracy: Double?,
    @SerializedName("recorded_at") val recordedAt: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

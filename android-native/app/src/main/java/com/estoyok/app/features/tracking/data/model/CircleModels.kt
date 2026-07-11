package com.estoyok.app.features.tracking.data.model

import com.google.gson.annotations.SerializedName

data class MemberLocationDto(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("accuracy") val accuracy: Float?,
    @SerializedName("battery_level") val batteryLevel: Float?,
    @SerializedName("is_battery_low") val isBatteryLow: Boolean?,
    @SerializedName("is_tracking_active") val isTrackingActive: Boolean?,
    @SerializedName("gps_enabled") val gpsEnabled: Boolean?,
    @SerializedName("recorded_at") val recordedAt: String?,
    @SerializedName("speed") val speed: Float?, // in km/h from backend
    @SerializedName("is_driving") val isDriving: Boolean?,
    @SerializedName("is_offline") val isOffline: Boolean?,
    @SerializedName("last_seen_at") val lastSeenAt: String?
)

data class CircleMemberDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("current_location") val currentLocation: MemberLocationDto?
)

data class GeofenceDto(
    @SerializedName("id") val id: Int,
    @SerializedName("circle_id") val circleId: Int,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("radius") val radius: Double,
    @SerializedName("type") val type: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

data class CircleDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("invite_code") val inviteCode: String,
    @SerializedName("owner_id") val ownerId: Int,
    @SerializedName("speed_limit") val speedLimit: Int?,
    @SerializedName("users") val members: List<CircleMemberDto> = emptyList(),
    @SerializedName("geofences") val geofences: List<GeofenceDto> = emptyList()
)

data class CreateCircleRequest(
    @SerializedName("name") val name: String
)

data class JoinCircleRequest(
    @SerializedName("invite_code") val inviteCode: String
)

data class CreateGeofenceRequest(
    @SerializedName("name") val name: String,
    @SerializedName("radius") val radius: Double,
    @SerializedName("type") val type: String = "entry_exit",
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("user_id") val userId: Int? = null
)

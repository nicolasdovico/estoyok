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

data class UpdateGeofenceRequest(
    @SerializedName("name") val name: String,
    @SerializedName("radius") val radius: Double,
    @SerializedName("type") val type: String = "entry_exit",
    @SerializedName("user_id") val userId: Int? = null
)

data class DriveTelemetryEvent(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("speed_drop") val speedDrop: Double? = null,
    @SerializedName("speed_gain") val speedGain: Double? = null,
    @SerializedName("speed") val speed: Double? = null,
    @SerializedName("limit") val limit: Int? = null,
    @SerializedName("duration_seconds") val durationSeconds: Int? = null
)

data class DriveTelemetryEventsContainer(
    @SerializedName("hard_brakes") val hardBrakes: List<DriveTelemetryEvent> = emptyList(),
    @SerializedName("rapid_accelerations") val rapidAccelerations: List<DriveTelemetryEvent> = emptyList(),
    @SerializedName("speeding") val speeding: List<DriveTelemetryEvent> = emptyList(),
    @SerializedName("phone_distractions") val phoneDistractions: List<DriveTelemetryEvent> = emptyList()
)

data class DriveRoutePointDto(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("speed") val speed: Double,
    @SerializedName("recorded_at") val recordedAt: String
)

data class MemberDriveEventDto(
    @SerializedName("id") val id: Int,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("duration_seconds") val durationSeconds: Long,
    @SerializedName("distance_km") val distanceKm: Double,
    @SerializedName("max_speed") val maxSpeed: Double,
    @SerializedName("exceeded_speed_limit") val exceededSpeedLimit: Boolean,
    @SerializedName("safety_score") val safetyScore: Int,
    @SerializedName("route_points") val routePoints: List<DriveRoutePointDto> = emptyList(),
    @SerializedName("events") val events: DriveTelemetryEventsContainer = DriveTelemetryEventsContainer()
)

data class MemberDrivesResponse(
    @SerializedName("is_premium") val isPremium: Boolean,
    @SerializedName("drives") val drives: List<MemberDriveEventDto> = emptyList()
)

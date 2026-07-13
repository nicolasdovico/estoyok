package com.estoyok.app.features.tracking.data.remote

import com.estoyok.app.features.auth.data.model.MessageResponse
import com.estoyok.app.features.tracking.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface CircleApiService {

    @GET("circles")
    suspend fun getCircles(): Response<List<CircleDto>>

    @POST("circles")
    suspend fun createCircle(
        @Body request: CreateCircleRequest
    ): Response<CircleDto>

    @POST("circles/join")
    suspend fun joinCircle(
        @Body request: JoinCircleRequest
    ): Response<CircleDto>

    @DELETE("circles/{circleId}/members/{memberId}")
    suspend fun removeMember(
        @Path("circleId") circleId: Int,
        @Path("memberId") memberId: Int
    ): Response<MessageResponse>

    @DELETE("circles/{circleId}")
    suspend fun deleteCircle(
        @Path("circleId") circleId: Int
    ): Response<MessageResponse>

    @GET("circles/{circleId}/members/{memberId}/history")
    suspend fun getMemberHistory(
        @Path("circleId") circleId: Int,
        @Path("memberId") memberId: Int,
        @Query("date") date: String?
    ): Response<List<LocationHistoryDto>>

    @POST("circles/{circleId}/geofences")
    suspend fun createGeofence(
        @Path("circleId") circleId: Int,
        @Body request: CreateGeofenceRequest
    ): Response<GeofenceDto>

    @DELETE("geofences/{geofenceId}")
    suspend fun deleteGeofence(
        @Path("geofenceId") geofenceId: Int
    ): Response<MessageResponse>

    @PUT("geofences/{geofenceId}")
    suspend fun updateGeofence(
        @Path("geofenceId") geofenceId: Int,
        @Body request: UpdateGeofenceRequest
    ): Response<GeofenceDto>

    @GET("circles/{circleId}/members/{memberId}/drives")
    suspend fun getMemberDrives(
        @Path("circleId") circleId: Int,
        @Path("memberId") memberId: Int
    ): Response<MemberDrivesResponse>
}

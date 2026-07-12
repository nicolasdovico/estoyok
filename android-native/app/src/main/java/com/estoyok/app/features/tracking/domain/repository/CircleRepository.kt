package com.estoyok.app.features.tracking.domain.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.MessageResponse
import com.estoyok.app.features.tracking.data.model.CircleDto
import com.estoyok.app.features.tracking.data.model.GeofenceDto
import com.estoyok.app.features.tracking.data.model.LocationHistoryDto
import kotlinx.coroutines.flow.Flow

interface CircleRepository {
    fun getCircles(): Flow<Resource<List<CircleDto>>>
    fun createCircle(name: String): Flow<Resource<CircleDto>>
    fun joinCircle(inviteCode: String): Flow<Resource<CircleDto>>
    fun removeMember(circleId: Int, memberId: Int): Flow<Resource<MessageResponse>>
    fun deleteCircle(circleId: Int): Flow<Resource<MessageResponse>>
    fun getMemberHistory(circleId: Int, memberId: Int, date: String?): Flow<Resource<List<LocationHistoryDto>>>
    fun createGeofence(circleId: Int, name: String, radius: Double, latitude: Double, longitude: Double, userId: Int?): Flow<Resource<GeofenceDto>>
    fun deleteGeofence(geofenceId: Int): Flow<Resource<MessageResponse>>
    fun updateGeofence(geofenceId: Int, name: String, radius: Double, userId: Int?): Flow<Resource<GeofenceDto>>
}

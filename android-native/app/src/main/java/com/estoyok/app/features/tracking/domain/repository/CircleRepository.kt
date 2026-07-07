package com.estoyok.app.features.tracking.domain.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.MessageResponse
import com.estoyok.app.features.tracking.data.model.CircleDto
import kotlinx.coroutines.flow.Flow

interface CircleRepository {
    fun getCircles(): Flow<Resource<List<CircleDto>>>
    fun createCircle(name: String): Flow<Resource<CircleDto>>
    fun joinCircle(inviteCode: String): Flow<Resource<CircleDto>>
    fun removeMember(circleId: Int, memberId: Int): Flow<Resource<MessageResponse>>
    fun deleteCircle(circleId: Int): Flow<Resource<MessageResponse>>
}

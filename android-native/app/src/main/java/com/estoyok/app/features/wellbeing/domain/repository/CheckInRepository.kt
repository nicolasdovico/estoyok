package com.estoyok.app.features.wellbeing.domain.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.wellbeing.data.model.CheckInDto
import com.estoyok.app.features.wellbeing.data.model.CheckInResponse
import kotlinx.coroutines.flow.Flow

interface CheckInRepository {
    fun checkIn(source: String): Flow<Resource<CheckInResponse>>
    fun getCheckIns(): Flow<Resource<List<CheckInDto>>>
}

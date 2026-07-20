package com.estoyok.app.features.wellbeing.domain.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.MessageResponse
import com.estoyok.app.features.auth.data.model.UserDto
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getUserProfile(): Flow<Resource<UserDto>>
    fun updateCheckinInterval(hours: Int): Flow<Resource<MessageResponse>>
    fun updateQuietHours(enabled: Boolean, start: String, end: String, timezone: String): Flow<Resource<MessageResponse>>
    fun updateSmsWhatsappCheckin(enabled: Boolean): Flow<Resource<MessageResponse>>
    fun updateEscalation(enabled: Boolean, intervalMinutes: Int): Flow<Resource<MessageResponse>>
    fun updatePrivacy(shareContactResponses: Boolean?, lowBatteryAlertsEnabled: Boolean?): Flow<Resource<MessageResponse>>
    fun updateAutomation(wifiEnabled: Boolean, ssid: String?, sensorEnabled: Boolean): Flow<Resource<MessageResponse>>
    fun updateProximityAlerts(enabled: Boolean): Flow<Resource<MessageResponse>>
    fun updateAvatar(avatar: okhttp3.MultipartBody.Part): Flow<Resource<MessageResponse>>
    fun updatePushToken(pushToken: String): Flow<Resource<MessageResponse>>
}

package com.estoyok.app.features.wellbeing.data.remote

import com.estoyok.app.features.auth.data.model.MessageResponse
import com.estoyok.app.features.auth.data.model.UserDto
import com.estoyok.app.features.wellbeing.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface SettingsApiService {

    @GET("user")
    suspend fun getUserProfile(): Response<UserDto>

    @PUT("settings/checkin-interval")
    suspend fun updateCheckinInterval(
        @Body request: CheckinIntervalRequest
    ): Response<MessageResponse>

    @PUT("settings/quiet-hours")
    suspend fun updateQuietHours(
        @Body request: QuietHoursRequest
    ): Response<MessageResponse>

    @PUT("settings/sms-whatsapp-checkin")
    suspend fun updateSmsWhatsappCheckin(
        @Body request: SmsWhatsappCheckinRequest
    ): Response<MessageResponse>

    @PUT("settings/escalation")
    suspend fun updateEscalation(
        @Body request: EscalationRequest
    ): Response<MessageResponse>

    @PUT("settings/privacy")
    suspend fun updatePrivacy(
        @Body request: PrivacyRequest
    ): Response<MessageResponse>

    @PUT("settings/automation")
    suspend fun updateAutomation(
        @Body request: AutomationRequest
    ): Response<MessageResponse>

    @PUT("settings/proximity-alerts")
    suspend fun updateProximityAlerts(
        @Body request: ProximityAlertsRequest
    ): Response<MessageResponse>
}

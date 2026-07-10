package com.estoyok.app.features.wellbeing.data.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.MessageResponse
import com.estoyok.app.features.auth.data.model.UserDto
import com.estoyok.app.features.wellbeing.data.model.*
import com.estoyok.app.features.wellbeing.data.remote.SettingsApiService
import com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val apiService: SettingsApiService
) : SettingsRepository {

    override fun getUserProfile(): Flow<Resource<UserDto>> = safeApiCall {
        apiService.getUserProfile()
    }

    override fun updateCheckinInterval(hours: Int): Flow<Resource<MessageResponse>> = safeApiCall {
        apiService.updateCheckinInterval(CheckinIntervalRequest(hours))
    }

    override fun updateQuietHours(
        enabled: Boolean,
        start: String,
        end: String,
        timezone: String
    ): Flow<Resource<MessageResponse>> = safeApiCall {
        apiService.updateQuietHours(QuietHoursRequest(enabled, start, end, timezone))
    }

    override fun updateSmsWhatsappCheckin(enabled: Boolean): Flow<Resource<MessageResponse>> = safeApiCall {
        apiService.updateSmsWhatsappCheckin(SmsWhatsappCheckinRequest(enabled))
    }

    override fun updateEscalation(
        enabled: Boolean,
        intervalMinutes: Int
    ): Flow<Resource<MessageResponse>> = safeApiCall {
        apiService.updateEscalation(EscalationRequest(enabled, intervalMinutes))
    }

    override fun updatePrivacy(
        shareContactResponses: Boolean?,
        lowBatteryAlertsEnabled: Boolean?
    ): Flow<Resource<MessageResponse>> = safeApiCall {
        apiService.updatePrivacy(PrivacyRequest(shareContactResponses, lowBatteryAlertsEnabled))
    }

    override fun updateAutomation(
        wifiEnabled: Boolean,
        ssid: String?,
        sensorEnabled: Boolean
    ): Flow<Resource<MessageResponse>> = safeApiCall {
        apiService.updateAutomation(AutomationRequest(wifiEnabled, ssid, sensorEnabled))
    }

    override fun updateProximityAlerts(enabled: Boolean): Flow<Resource<MessageResponse>> = safeApiCall {
        apiService.updateProximityAlerts(ProximityAlertsRequest(enabled))
    }

    override fun updateAvatar(avatar: okhttp3.MultipartBody.Part): Flow<Resource<MessageResponse>> = safeApiCall {
        apiService.updateAvatar(avatar)
    }

    private fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Flow<Resource<T>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiCall()
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(parseErrorMessage(response)))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión. Revisa tu internet."))
        } catch (e: Exception) {
            emit(Resource.Error("Ocurrió un error inesperado."))
        }
    }

    private fun parseErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string() ?: return "Error desconocido"
            val jsonObject = JSONObject(errorBody)
            if (jsonObject.has("errors")) {
                val errors = jsonObject.getJSONObject("errors")
                val firstKey = errors.keys().next()
                val errorArray = errors.getJSONArray(firstKey)
                errorArray.getString(0)
            } else if (jsonObject.has("message")) {
                jsonObject.getString("message")
            } else {
                "Error en la petición: ${response.code()}"
            }
        } catch (e: Exception) {
            "Ocurrió un error al procesar la respuesta."
        }
    }
}

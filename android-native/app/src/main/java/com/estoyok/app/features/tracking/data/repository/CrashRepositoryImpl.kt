package com.estoyok.app.features.tracking.data.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.MessageResponse
import com.estoyok.app.features.tracking.data.model.*
import com.estoyok.app.features.tracking.data.remote.CrashApiService
import com.estoyok.app.features.tracking.domain.repository.CrashRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashRepositoryImpl @Inject constructor(
    private val apiService: CrashApiService
) : CrashRepository {

    override fun reportCrash(
        latitude: Double,
        longitude: Double,
        speedAtImpact: Float,
        gForce: Float
    ): Flow<Resource<CrashAlertResponse>> = safeApiCall {
        apiService.reportCrash(CrashAlertRequest(latitude, longitude, speedAtImpact, gForce))
    }

    override fun markFalseAlarm(crashEventId: Int): Flow<Resource<MessageResponse>> = safeApiCall {
        apiService.markFalseAlarm(crashEventId)
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
            if (jsonObject.has("message")) {
                jsonObject.getString("message")
            } else {
                "Error en la petición: ${response.code()}"
            }
        } catch (e: Exception) {
            "Ocurrió un error al procesar la respuesta."
        }
    }
}

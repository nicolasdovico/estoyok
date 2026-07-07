package com.estoyok.app.features.tracking.data.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.tracking.data.model.AudioUploadResponse
import com.estoyok.app.features.tracking.data.model.SosAlertResponse
import com.estoyok.app.features.tracking.data.remote.SosApiService
import com.estoyok.app.features.tracking.domain.repository.SosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SosRepositoryImpl @Inject constructor(
    private val apiService: SosApiService
) : SosRepository {

    override fun triggerSos(): Flow<Resource<SosAlertResponse>> = safeApiCall {
        apiService.triggerSos()
    }

    override fun uploadAudio(alertId: String, audioFile: File): Flow<Resource<AudioUploadResponse>> = safeApiCall {
        val requestFile = audioFile.asRequestBody("audio/mp4".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("audio", audioFile.name, requestFile)
        apiService.uploadAudio(alertId, body)
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
            emit(Resource.Error("Ocurrió un error inesperado: ${e.localizedMessage}"))
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

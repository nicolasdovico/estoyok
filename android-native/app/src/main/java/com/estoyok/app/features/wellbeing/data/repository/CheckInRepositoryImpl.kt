package com.estoyok.app.features.wellbeing.data.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.wellbeing.data.model.CheckInDto
import com.estoyok.app.features.wellbeing.data.model.CheckInRequest
import com.estoyok.app.features.wellbeing.data.model.CheckInResponse
import com.estoyok.app.features.wellbeing.data.remote.CheckInApiService
import com.estoyok.app.features.wellbeing.domain.repository.CheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckInRepositoryImpl @Inject constructor(
    private val apiService: CheckInApiService
) : CheckInRepository {

    override fun checkIn(source: String): Flow<Resource<CheckInResponse>> = safeApiCall {
        apiService.checkIn(CheckInRequest(source))
    }

    override fun getCheckIns(): Flow<Resource<List<CheckInDto>>> = safeApiCall {
        apiService.getCheckIns()
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

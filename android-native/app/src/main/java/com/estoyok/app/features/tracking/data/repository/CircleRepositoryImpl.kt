package com.estoyok.app.features.tracking.data.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.MessageResponse
import com.estoyok.app.features.tracking.data.model.*
import com.estoyok.app.features.tracking.data.remote.CircleApiService
import com.estoyok.app.features.tracking.domain.repository.CircleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CircleRepositoryImpl @Inject constructor(
    private val apiService: CircleApiService
) : CircleRepository {

    override fun getCircles(): Flow<Resource<List<CircleDto>>> = safeApiCall {
        apiService.getCircles()
    }

    override fun createCircle(name: String): Flow<Resource<CircleDto>> = safeApiCall {
        apiService.createCircle(CreateCircleRequest(name))
    }

    override fun joinCircle(inviteCode: String): Flow<Resource<CircleDto>> = safeApiCall {
        apiService.joinCircle(JoinCircleRequest(inviteCode))
    }

    override fun removeMember(circleId: Int, memberId: Int): Flow<Resource<MessageResponse>> = safeApiCall {
        apiService.removeMember(circleId, memberId)
    }

    override fun deleteCircle(circleId: Int): Flow<Resource<MessageResponse>> = safeApiCall {
        apiService.deleteCircle(circleId)
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

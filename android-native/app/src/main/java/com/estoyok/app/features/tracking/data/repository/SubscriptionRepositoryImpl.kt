package com.estoyok.app.features.tracking.data.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.tracking.data.model.CheckoutRequest
import com.estoyok.app.features.tracking.data.model.CheckoutResponse
import com.estoyok.app.features.tracking.data.remote.SubscriptionApiService
import com.estoyok.app.features.tracking.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val apiService: SubscriptionApiService
) : SubscriptionRepository {

    override fun checkout(provider: String): Flow<Resource<CheckoutResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.checkout(CheckoutRequest(provider))
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(parseErrorMessage(response)))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión. Revisa tu internet."))
        } catch (e: Exception) {
            emit(Resource.Error("Ocurrió un error inesperado al procesar el pago."))
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

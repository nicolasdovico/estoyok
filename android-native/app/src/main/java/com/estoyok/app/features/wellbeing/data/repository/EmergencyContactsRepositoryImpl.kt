package com.estoyok.app.features.wellbeing.data.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.MessageResponse
import com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto
import com.estoyok.app.features.wellbeing.data.model.ReorderContactsRequest
import com.estoyok.app.features.wellbeing.data.remote.EmergencyContactsApiService
import com.estoyok.app.features.wellbeing.domain.repository.EmergencyContactsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyContactsRepositoryImpl @Inject constructor(
    private val apiService: EmergencyContactsApiService
) : EmergencyContactsRepository {

    override fun getContacts(): Flow<Resource<List<EmergencyContactDto>>> = safeApiCall {
        apiService.getContacts()
    }

    override fun createContact(contact: EmergencyContactDto): Flow<Resource<EmergencyContactDto>> = safeApiCall {
        apiService.createContact(contact)
    }

    override fun updateContact(id: Int, contact: EmergencyContactDto): Flow<Resource<EmergencyContactDto>> = safeApiCall {
        apiService.updateContact(id, contact)
    }

    override fun deleteContact(id: Int): Flow<Resource<MessageResponse>> = safeApiCall {
        apiService.deleteContact(id)
    }

    override fun reorderContacts(ids: List<Int>): Flow<Resource<MessageResponse>> = safeApiCall {
        apiService.reorderContacts(ReorderContactsRequest(ids))
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

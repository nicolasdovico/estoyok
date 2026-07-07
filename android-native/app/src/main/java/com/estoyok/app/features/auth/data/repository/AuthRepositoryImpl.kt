package com.estoyok.app.features.auth.data.repository

import com.estoyok.app.core.data.local.SessionManager
import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.*
import com.estoyok.app.features.auth.data.remote.AuthApiService
import com.estoyok.app.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager
) : AuthRepository {

    override fun register(request: RegisterRequest): Flow<Resource<MessageResponse>> = safeApiCall {
        apiService.register(request)
    }

    override fun login(request: LoginRequest): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                // Save session token and info
                sessionManager.saveSession(
                    token = body.token,
                    name = body.user.name,
                    email = body.user.email,
                    phone = body.user.phone
                )
                emit(Resource.Success(body))
            } else {
                emit(Resource.Error(parseErrorMessage(response)))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión. Revisa tu internet."))
        } catch (e: Exception) {
            emit(Resource.Error("Ocurrió un error inesperado."))
        }
    }

    override fun verifyEmail(request: VerifyEmailRequest): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.verifyEmail(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sessionManager.saveSession(
                    token = body.token,
                    name = body.user.name,
                    email = body.user.email,
                    phone = body.user.phone
                )
                emit(Resource.Success(body))
            } else {
                emit(Resource.Error(parseErrorMessage(response)))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión. Revisa tu internet."))
        } catch (e: Exception) {
            emit(Resource.Error("Ocurrió un error inesperado."))
        }
    }

    override fun resendOtp(request: ResendOtpRequest): Flow<Resource<MessageResponse>> = safeApiCall {
        apiService.resendOtp(request)
    }

    override fun logout(): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.logout()
            if (response.isSuccessful && response.body() != null) {
                sessionManager.clearSession()
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(parseErrorMessage(response)))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión."))
        } catch (e: Exception) {
            emit(Resource.Error("Error al cerrar sesión."))
        }
    }

    override fun getAuthToken(): Flow<String?> {
        return sessionManager.authTokenFlow
    }

    override suspend fun saveSession(token: String, name: String, email: String, phone: String?) {
        sessionManager.saveSession(token, name, email, phone)
    }

    override suspend fun clearSession() {
        sessionManager.clearSession()
    }

    // Helper to wrap Retrofit requests that don't need local session manipulation
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
            
            // Laravel validation errors are nested under errors property
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

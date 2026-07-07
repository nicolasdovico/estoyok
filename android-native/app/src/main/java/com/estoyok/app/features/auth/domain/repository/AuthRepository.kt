package com.estoyok.app.features.auth.domain.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    
    fun register(
        request: RegisterRequest
    ): Flow<Resource<MessageResponse>>

    fun login(
        request: LoginRequest
    ): Flow<Resource<AuthResponse>>

    fun verifyEmail(
        request: VerifyEmailRequest
    ): Flow<Resource<AuthResponse>>

    fun resendOtp(
        request: ResendOtpRequest
    ): Flow<Resource<MessageResponse>>

    fun logout(): Flow<Resource<MessageResponse>>

    fun getAuthToken(): Flow<String?>

    suspend fun saveSession(token: String, name: String, email: String, phone: String?)

    suspend fun clearSession()
}

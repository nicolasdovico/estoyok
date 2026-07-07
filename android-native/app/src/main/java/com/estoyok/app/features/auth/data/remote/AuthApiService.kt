package com.estoyok.app.features.auth.data.remote

import com.estoyok.app.features.auth.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<MessageResponse>

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("verify-email")
    suspend fun verifyEmail(
        @Body request: VerifyEmailRequest
    ): Response<AuthResponse>

    @POST("resend-otp")
    suspend fun resendOtp(
        @Body request: ResendOtpRequest
    ): Response<MessageResponse>

    @POST("logout")
    suspend fun logout(): Response<MessageResponse>
}

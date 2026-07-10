package com.estoyok.app.features.auth.data.model

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("email_verified_at") val emailVerifiedAt: String?,
    @SerializedName("is_premium") val isPremium: Boolean,
    @SerializedName("checkin_interval_hours") val checkinIntervalHours: Int,
    @SerializedName("allow_sms_whatsapp_checkin") val allowSmsWhatsappCheckin: Boolean,
    @SerializedName("last_check_in_at") val lastCheckInAt: String?,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("created_at") val createdAt: String
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("device_name") val deviceName: String = "android"
)

data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
    @SerializedName("phone") val phone: String? = null
)

data class VerifyEmailRequest(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String
)

data class ResendOtpRequest(
    @SerializedName("email") val email: String
)

data class AuthResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserDto
)

data class MessageResponse(
    @SerializedName("message") val message: String,
    @SerializedName("email") val email: String? = null
)

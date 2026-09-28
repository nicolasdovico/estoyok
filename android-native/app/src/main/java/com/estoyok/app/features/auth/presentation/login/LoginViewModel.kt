package com.estoyok.app.features.auth.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.LoginRequest
import com.estoyok.app.features.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import com.estoyok.app.core.data.local.SessionManager
import com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.estoyok.app.BuildConfig
import com.estoyok.app.features.auth.data.model.GoogleLoginRequest

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val _loginSuccess = MutableSharedFlow<Unit>()
    val loginSuccess: SharedFlow<Unit> = _loginSuccess.asSharedFlow()

    fun onEmailChange(newValue: String) {
        email = newValue
        errorMessage = null
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
        errorMessage = null
    }

    fun login() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Por favor completa todos los campos."
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage = "Formato de correo electrónico inválido."
            return
        }

        viewModelScope.launch {
            val deviceUuid = sessionManager.getOrCreateDeviceUuid()
            val deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}".trim()
            val request = LoginRequest(
                email = email.trim(),
                password = password,
                deviceName = if (deviceName.isNotBlank()) deviceName else "Android Device",
                deviceUuid = deviceUuid,
                platform = "android"
            )
            authRepository.login(request).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isLoading = true
                        errorMessage = null
                    }
                    is Resource.Success -> {
                        isLoading = false
                        // Synchronize FCM Token immediately with the device_uuid upon successful login
                        try {
                            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val token = task.result
                                    viewModelScope.launch {
                                        val uuid = sessionManager.getOrCreateDeviceUuid()
                                        settingsRepository.updatePushToken(token, uuid).collectLatest { }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("LoginViewModel", "Error fetching FCM token on login: ${e.message}")
                        }
                        _loginSuccess.emit(Unit)
                    }
                    is Resource.Error -> {
                        isLoading = false
                        errorMessage = resource.message ?: "Error al iniciar sesión."
                    }
                }
            }
        }
    }

    fun loginWithGoogle(context: Context) {
        val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        if (webClientId.isBlank() || webClientId == "your_google_web_client_id_here") {
            errorMessage = "Google Web Client ID no configurado. Revisa tu archivo local.properties o .env."
            return
        }

        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    val deviceUuid = sessionManager.getOrCreateDeviceUuid()
                    val deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}".trim()
                    val googleRequest = GoogleLoginRequest(
                        idToken = idToken,
                        deviceName = if (deviceName.isNotBlank()) deviceName else "Android Device",
                        deviceUuid = deviceUuid,
                        platform = "android"
                    )

                    authRepository.loginWithGoogle(googleRequest).collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                isLoading = true
                                errorMessage = null
                            }
                            is Resource.Success -> {
                                isLoading = false
                                try {
                                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val token = task.result
                                            viewModelScope.launch {
                                                val uuid = sessionManager.getOrCreateDeviceUuid()
                                                settingsRepository.updatePushToken(token, uuid).collectLatest { }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("LoginViewModel", "Error fetching FCM token on Google login: ${e.message}")
                                }
                                _loginSuccess.emit(Unit)
                            }
                            is Resource.Error -> {
                                isLoading = false
                                errorMessage = resource.message ?: "Error al autenticar con Google."
                            }
                        }
                    }
                } else {
                    isLoading = false
                    errorMessage = "Credencial de Google no reconocida."
                }
            } catch (e: GetCredentialCancellationException) {
                isLoading = false
                // User cancelled the prompt, keep silent
            } catch (e: GetCredentialException) {
                isLoading = false
                errorMessage = "Error de autenticación con Google: ${e.message}"
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Error inesperado con Google: ${e.message}"
            }
        }
    }
}

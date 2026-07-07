package com.estoyok.app.features.auth.presentation.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.RegisterRequest
import com.estoyok.app.features.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var name by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var phone by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val _registerSuccess = MutableSharedFlow<String>() // Emits email on success
    val registerSuccess: SharedFlow<String> = _registerSuccess.asSharedFlow()

    fun onNameChange(newValue: String) {
        name = newValue
        errorMessage = null
    }

    fun onEmailChange(newValue: String) {
        email = newValue
        errorMessage = null
    }

    fun onPhoneChange(newValue: String) {
        phone = newValue
        errorMessage = null
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
        errorMessage = null
    }

    fun onConfirmPasswordChange(newValue: String) {
        confirmPassword = newValue
        errorMessage = null
    }

    fun register() {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            errorMessage = "Por favor completa todos los campos requeridos."
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage = "El formato del email es incorrecto."
            return
        }

        if (password.length < 8) {
            errorMessage = "La contraseña debe tener al menos 8 caracteres."
            return
        }

        if (password != confirmPassword) {
            errorMessage = "Las contraseñas no coinciden."
            return
        }

        val formattedPhone = phone.trim()
        if (formattedPhone.isNotEmpty() && !formattedPhone.startsWith("+")) {
            errorMessage = "El teléfono debe comenzar con el prefijo '+' (ej. +54911...) y formato E.164."
            return
        }

        viewModelScope.launch {
            val request = RegisterRequest(
                name = name.trim(),
                email = email.trim(),
                password = password,
                passwordConfirmation = confirmPassword,
                phone = formattedPhone.ifEmpty { null }
            )

            authRepository.register(request).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isLoading = true
                        errorMessage = null
                    }
                    is Resource.Success -> {
                        isLoading = false
                        _registerSuccess.emit(email.trim())
                    }
                    is Resource.Error -> {
                        isLoading = false
                        errorMessage = resource.message ?: "Error al registrarse."
                    }
                }
            }
        }
    }
}

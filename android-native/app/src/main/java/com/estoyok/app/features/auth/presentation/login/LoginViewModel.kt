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
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
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
            authRepository.login(LoginRequest(email.trim(), password)).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isLoading = true
                        errorMessage = null
                    }
                    is Resource.Success -> {
                        isLoading = false
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
}

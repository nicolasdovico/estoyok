package com.estoyok.app.features.auth.presentation.verify

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.ResendOtpRequest
import com.estoyok.app.features.auth.data.model.VerifyEmailRequest
import com.estoyok.app.features.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve email passed through navigation arguments
    val email: String = savedStateHandle.get<String>("email") ?: ""

    var code by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isResending by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    private val _verificationSuccess = MutableSharedFlow<Unit>()
    val verificationSuccess: SharedFlow<Unit> = _verificationSuccess.asSharedFlow()

    fun onCodeChange(newValue: String) {
        if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
            code = newValue
            errorMessage = null
            successMessage = null
        }
    }

    fun verify() {
        if (code.length != 6) {
            errorMessage = "El código debe tener exactamente 6 dígitos."
            return
        }

        viewModelScope.launch {
            authRepository.verifyEmail(VerifyEmailRequest(email, code)).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isLoading = true
                        errorMessage = null
                        successMessage = null
                    }
                    is Resource.Success -> {
                        isLoading = false
                        _verificationSuccess.emit(Unit)
                    }
                    is Resource.Error -> {
                        isLoading = false
                        errorMessage = resource.message ?: "Código incorrecto o vencido."
                    }
                }
            }
        }
    }

    fun resendOtp() {
        viewModelScope.launch {
            authRepository.resendOtp(ResendOtpRequest(email)).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isResending = true
                        errorMessage = null
                        successMessage = null
                    }
                    is Resource.Success -> {
                        isResending = false
                        successMessage = "Se ha reenviado un nuevo código."
                    }
                    is Resource.Error -> {
                        isResending = false
                        errorMessage = resource.message ?: "Error al reenviar el código."
                    }
                }
            }
        }
    }
}

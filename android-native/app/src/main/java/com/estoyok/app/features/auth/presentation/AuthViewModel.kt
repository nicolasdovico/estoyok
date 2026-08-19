package com.estoyok.app.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estoyok.app.core.data.local.SessionManager
import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.domain.repository.AuthRepository
import com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val isAuthenticated: StateFlow<Boolean> = authRepository.getAuthToken()
        .map { token -> !token.isNullOrEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isDisclaimerAccepted: StateFlow<Boolean?> = sessionManager.isDisclaimerAcceptedFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            authRepository.getAuthToken().collectLatest { token ->
                if (!token.isNullOrBlank()) {
                    // Sync user profile with server to ensure accurate disclaimer state
                    settingsRepository.getUserProfile().collectLatest { }
                }
            }
        }
    }

    fun acceptDisclaimer() {
        viewModelScope.launch {
            settingsRepository.acceptDisclaimer().collectLatest { resource ->
                if (resource is Resource.Success) {
                    sessionManager.saveDisclaimerAccepted(true)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout().collect { /* Handle side effects if needed */ }
        }
    }
}

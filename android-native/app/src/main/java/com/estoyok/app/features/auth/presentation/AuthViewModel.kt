package com.estoyok.app.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estoyok.app.core.data.local.SessionManager
import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.domain.repository.AuthRepository
import com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository
import com.estoyok.app.features.wellbeing.domain.repository.EmergencyContactsRepository
import com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    private val contactsRepository: EmergencyContactsRepository,
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

    val isOnboardingCompleted: StateFlow<Boolean?> = sessionManager.isOnboardingCompletedFlow
        .map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val isWelcomePaywallShown: StateFlow<Boolean?> = sessionManager.isWelcomePaywallShownFlow
        .map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    var savedOnboardingContactName by mutableStateOf<String?>(null)
        private set

    fun saveOnboardingContact(
        name: String,
        phone: String,
        email: String,
        relationship: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val contactDto = EmergencyContactDto(
                id = null,
                name = name,
                phone = phone,
                email = email.ifBlank { null },
                relationship = relationship.ifBlank { "Familiar" },
                isActive = true,
                priority = 0
            )
            contactsRepository.createContact(contactDto).collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val created = resource.data
                        savedOnboardingContactName = name
                        // Disparar WhatsApp de prueba en background de inmediato
                        created?.id?.let { contactId ->
                            contactsRepository.sendTestWhatsApp(contactId).collectLatest { }
                        }
                        onSuccess(name)
                    }
                    is Resource.Error -> {
                        onError(resource.message ?: "No se pudo guardar el contacto.")
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            sessionManager.saveOnboardingCompleted(true)
        }
    }

    fun completeWelcomePaywall() {
        viewModelScope.launch {
            sessionManager.saveWelcomePaywallShown(true)
        }
    }

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

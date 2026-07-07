package com.estoyok.app.features.tracking.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.UserDto
import com.estoyok.app.features.tracking.data.model.CircleDto
import com.estoyok.app.features.tracking.domain.repository.CircleRepository
import com.estoyok.app.features.tracking.domain.repository.SubscriptionRepository
import com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamiliaViewModel @Inject constructor(
    private val circleRepository: CircleRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    var user by mutableStateOf<UserDto?>(null)
        private set

    var circles by mutableStateOf<List<CircleDto>>(emptyList())
        private set

    var selectedCircle by mutableStateOf<CircleDto?>(null)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var isActionInProgress by mutableStateOf(false)
        private set

    var checkoutLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            isRefreshing = true
            errorMessage = null
            
            // Concurrently fetch profile and circles
            launch { fetchUserProfile() }
            launch { fetchCircles() }
        }
    }

    private suspend fun fetchUserProfile() {
        settingsRepository.getUserProfile().collectLatest { resource ->
            if (resource is Resource.Success) {
                user = resource.data
            }
        }
    }

    private suspend fun fetchCircles() {
        circleRepository.getCircles().collectLatest { resource ->
            when (resource) {
                is Resource.Loading -> {
                    isRefreshing = true
                }
                is Resource.Success -> {
                    isRefreshing = false
                    circles = resource.data ?: emptyList()
                    
                    // Maintain previous selected circle or default to first
                    if (selectedCircle == null && circles.isNotEmpty()) {
                        selectedCircle = circles.first()
                    } else if (selectedCircle != null) {
                        selectedCircle = circles.find { it.id == selectedCircle!!.id } ?: circles.firstOrNull()
                    }
                }
                is Resource.Error -> {
                    isRefreshing = false
                    errorMessage = resource.message ?: "No se pudieron cargar los núcleos."
                }
            }
        }
    }

    fun selectCircle(circle: CircleDto) {
        selectedCircle = circle
    }

    fun createCircle(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            circleRepository.createCircle(name).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isActionInProgress = true
                    }
                    is Resource.Success -> {
                        isActionInProgress = false
                        refreshData()
                    }
                    is Resource.Error -> {
                        isActionInProgress = false
                        errorMessage = resource.message ?: "Error al crear el círculo."
                    }
                }
            }
        }
    }

    fun joinCircle(inviteCode: String) {
        if (inviteCode.isBlank() || inviteCode.length != 10) {
            errorMessage = "El código de invitación debe tener exactamente 10 caracteres."
            return
        }
        viewModelScope.launch {
            circleRepository.joinCircle(inviteCode).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isActionInProgress = true
                    }
                    is Resource.Success -> {
                        isActionInProgress = false
                        refreshData()
                    }
                    is Resource.Error -> {
                        isActionInProgress = false
                        errorMessage = resource.message ?: "Error al unirse al círculo."
                    }
                }
            }
        }
    }

    fun removeMember(circleId: Int, memberId: Int) {
        viewModelScope.launch {
            circleRepository.removeMember(circleId, memberId).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isActionInProgress = true
                    }
                    is Resource.Success -> {
                        isActionInProgress = false
                        refreshData()
                    }
                    is Resource.Error -> {
                        isActionInProgress = false
                        errorMessage = resource.message ?: "No se pudo expulsar al miembro."
                    }
                }
            }
        }
    }

    fun deleteCircle(circleId: Int) {
        viewModelScope.launch {
            circleRepository.deleteCircle(circleId).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isActionInProgress = true
                    }
                    is Resource.Success -> {
                        isActionInProgress = false
                        selectedCircle = null
                        refreshData()
                    }
                    is Resource.Error -> {
                        isActionInProgress = false
                        errorMessage = resource.message ?: "No se pudo eliminar el círculo."
                    }
                }
            }
        }
    }

    fun checkoutSubscription(provider: String, onUrlReceived: (String) -> Unit) {
        viewModelScope.launch {
            subscriptionRepository.checkout(provider).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        checkoutLoading = true
                    }
                    is Resource.Success -> {
                        checkoutLoading = false
                        resource.data?.checkoutUrl?.let { onUrlReceived(it) }
                    }
                    is Resource.Error -> {
                        checkoutLoading = false
                        errorMessage = resource.message ?: "Error al generar enlace de suscripción."
                    }
                }
            }
        }
    }
}

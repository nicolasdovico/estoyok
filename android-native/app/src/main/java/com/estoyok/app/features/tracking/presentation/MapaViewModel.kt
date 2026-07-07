package com.estoyok.app.features.tracking.presentation

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.tracking.data.model.CircleDto
import com.estoyok.app.features.tracking.data.model.CircleMemberDto
import com.estoyok.app.features.tracking.domain.repository.CircleRepository
import com.estoyok.app.services.TrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapaViewModel @Inject constructor(
    private val circleRepository: CircleRepository
) : ViewModel() {

    var circles by mutableStateOf<List<CircleDto>>(emptyList())
        private set

    var selectedCircle by mutableStateOf<CircleDto?>(null)
        private set

    var selectedCircleMembers by mutableStateOf<List<CircleMemberDto>>(emptyList())
        private set

    var isServiceRunning by mutableStateOf(false)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var pollingJob: Job? = null

    init {
        isServiceRunning = TrackingService.isRunning
        refreshCircles()
        startPolling()
    }

    fun refreshCircles() {
        viewModelScope.launch {
            circleRepository.getCircles().collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isRefreshing = true
                    }
                    is Resource.Success -> {
                        isRefreshing = false
                        circles = resource.data ?: emptyList()
                        
                        // Default to the first circle or preserve active selection
                        if (selectedCircle == null && circles.isNotEmpty()) {
                            selectCircle(circles.first())
                        } else if (selectedCircle != null) {
                            val updated = circles.find { it.id == selectedCircle!!.id }
                            if (updated != null) {
                                selectCircle(updated)
                            }
                        }
                    }
                    is Resource.Error -> {
                        isRefreshing = false
                        errorMessage = resource.message ?: "No se pudieron cargar los núcleos."
                    }
                }
            }
        }
    }

    fun selectCircle(circle: CircleDto) {
        selectedCircle = circle
        selectedCircleMembers = circle.members
    }

    private fun startPolling() {
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(10000L) // Poll every 10 seconds
                circleRepository.getCircles().collectLatest { resource ->
                    if (resource is Resource.Success) {
                        circles = resource.data ?: emptyList()
                        val updated = circles.find { it.id == selectedCircle?.id }
                        if (updated != null) {
                            selectedCircle = updated
                            selectedCircleMembers = updated.members
                        }
                    }
                }
            }
        }
    }

    fun toggleTrackingService(context: Context) {
        if (isServiceRunning) {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = TrackingService.ACTION_STOP
            }
            context.stopService(intent)
            isServiceRunning = false
        } else {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = TrackingService.ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
            isServiceRunning = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}

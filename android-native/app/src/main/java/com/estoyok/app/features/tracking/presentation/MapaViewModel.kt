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
import com.estoyok.app.features.tracking.data.model.LocationHistoryDto
import com.estoyok.app.features.tracking.data.model.MemberDriveEventDto
import com.estoyok.app.features.tracking.data.model.MemberDrivesResponse
import com.estoyok.app.features.tracking.domain.repository.CircleRepository
import com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository
import com.estoyok.app.features.auth.data.model.UserDto
import com.estoyok.app.services.TrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.estoyok.app.core.data.local.SessionManager

@HiltViewModel
class MapaViewModel @Inject constructor(
    private val circleRepository: CircleRepository,
    private val settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager
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

    // Logged in user profile
    var currentUserProfile by mutableStateOf<UserDto?>(null)
        private set

    // Selected member for expanded details
    var selectedMember by mutableStateOf<CircleMemberDto?>(null)

    // History points for selected member and date
    var historyPoints by mutableStateOf<List<LocationHistoryDto>>(emptyList())
        private set

    var isHistoryLoading by mutableStateOf(false)
        private set

    var historyDate by mutableStateOf<String?>(null)

    // Selected trip index for Option A segment highlight
    var selectedTripIndex by mutableStateOf<Int?>(null)
 
    var isTrackingPersistedEnabled by mutableStateOf(true)
        private set

    // Driving/Vehiculo events and summary state
    var memberDrives by mutableStateOf<List<MemberDriveEventDto>>(emptyList())
        private set

    var allMembersDrives by mutableStateOf<Map<Int, List<MemberDriveEventDto>>>(emptyMap())
        private set

    var isDrivesLoading by mutableStateOf(false)
        private set

    var isPremiumDrives by mutableStateOf(false)
        private set

    var drivesErrorMessage by mutableStateOf<String?>(null)

    var isUploadingAvatar by mutableStateOf(false)
        private set

    var avatarVersion by mutableStateOf(0)
        private set

    var uploadAvatarSuccessMessage by mutableStateOf<String?>(null)
    var uploadAvatarErrorMessage by mutableStateOf<String?>(null)

    private var pollingJob: Job? = null

    init {
        isServiceRunning = TrackingService.isRunning
        refreshCircles()
        loadUserProfile()
        startPolling()
        viewModelScope.launch {
            sessionManager.isTrackingEnabledFlow.collectLatest { enabled ->
                isTrackingPersistedEnabled = enabled
            }
        }
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
            viewModelScope.launch {
                sessionManager.saveTrackingEnabled(false)
            }
        } else {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = TrackingService.ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
            isServiceRunning = true
            viewModelScope.launch {
                sessionManager.saveTrackingEnabled(true)
            }
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            settingsRepository.getUserProfile().collectLatest { resource ->
                if (resource is Resource.Success) {
                    currentUserProfile = resource.data
                    
                    // Sync FCM push token with the server
                    try {
                        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result
                                viewModelScope.launch {
                                    settingsRepository.updatePushToken(token).collectLatest { res ->
                                        android.util.Log.d("MapaViewModel", "FCM Token sync result: $res")
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MapaViewModel", "Error fetching FCM token: ${e.message}", e)
                    }
                }
            }
        }
    }

    fun loadMemberHistory(memberId: Int, date: String) {
        val circleId = selectedCircle?.id ?: return
        historyDate = date
        selectedTripIndex = null
        viewModelScope.launch {
            circleRepository.getMemberHistory(circleId, memberId, date).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isHistoryLoading = true
                    }
                    is Resource.Success -> {
                        isHistoryLoading = false
                        historyPoints = resource.data ?: emptyList()
                    }
                    is Resource.Error -> {
                        isHistoryLoading = false
                        historyPoints = emptyList()
                        errorMessage = resource.message ?: "Error al obtener historial."
                    }
                }
            }
        }
    }

    fun loadMemberDrives(memberId: Int) {
        val circleId = selectedCircle?.id ?: return
        viewModelScope.launch {
            circleRepository.getMemberDrives(circleId, memberId).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isDrivesLoading = true
                        drivesErrorMessage = null
                    }
                    is Resource.Success -> {
                        isDrivesLoading = false
                        val drivesList = resource.data?.drives ?: emptyList()
                        memberDrives = drivesList
                        isPremiumDrives = resource.data?.isPremium ?: false
                        allMembersDrives = allMembersDrives.toMutableMap().apply {
                            put(memberId, drivesList)
                        }
                    }
                    is Resource.Error -> {
                        isDrivesLoading = false
                        memberDrives = emptyList()
                        drivesErrorMessage = resource.message ?: "Error al obtener historial de conducción."
                    }
                }
            }
        }
    }

    fun loadAllMembersDrives(circleId: Int, members: List<CircleMemberDto>) {
        viewModelScope.launch {
            isDrivesLoading = true
            drivesErrorMessage = null
            val tempMap = mutableMapOf<Int, List<MemberDriveEventDto>>()
            var hasPremium = false

            val deferredDrives = members.map { member ->
                async {
                    var drivesList = emptyList<MemberDriveEventDto>()
                    try {
                        circleRepository.getMemberDrives(circleId, member.id).collect { resource ->
                            if (resource is Resource.Success) {
                                drivesList = resource.data?.drives ?: emptyList()
                                hasPremium = hasPremium || (resource.data?.isPremium ?: false)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MapaViewModel", "Error loading drives for member ${member.id}", e)
                    }
                    member.id to drivesList
                }
            }

            deferredDrives.awaitAll().forEach { (memberId, drivesList) ->
                tempMap[memberId] = drivesList
            }

            allMembersDrives = tempMap
            isPremiumDrives = hasPremium
            isDrivesLoading = false

            selectedMember?.id?.let { selId ->
                memberDrives = tempMap[selId] ?: emptyList()
            }
        }
    }

    fun clearHistory() {
        historyPoints = emptyList()
        historyDate = null
        selectedTripIndex = null
    }

    fun uploadAvatar(avatarPart: okhttp3.MultipartBody.Part) {
        viewModelScope.launch {
            settingsRepository.updateAvatar(avatarPart).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isUploadingAvatar = true
                        uploadAvatarErrorMessage = null
                        uploadAvatarSuccessMessage = null
                    }
                    is Resource.Success -> {
                        isUploadingAvatar = false
                        avatarVersion++
                        uploadAvatarSuccessMessage = "Foto de perfil actualizada exitosamente."
                        loadUserProfile()
                        refreshCircles()
                    }
                    is Resource.Error -> {
                        isUploadingAvatar = false
                        uploadAvatarErrorMessage = resource.message ?: "Error al subir la foto de perfil."
                    }
                }
            }
        }
    }

    fun clearUploadMessages() {
        uploadAvatarSuccessMessage = null
        uploadAvatarErrorMessage = null
    }

    var isGeofenceLoading by mutableStateOf(false)
        private set
    var geofenceSuccessMessage by mutableStateOf<String?>(null)
    var geofenceErrorMessage by mutableStateOf<String?>(null)

    fun createGeofence(name: String, radius: Double, latitude: Double, longitude: Double, userId: Int?) {
        val circleId = selectedCircle?.id ?: return
        viewModelScope.launch {
            circleRepository.createGeofence(circleId, name, radius, latitude, longitude, userId).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isGeofenceLoading = true
                        geofenceErrorMessage = null
                        geofenceSuccessMessage = null
                    }
                    is Resource.Success -> {
                        isGeofenceLoading = false
                        geofenceSuccessMessage = "Zona Segura creada exitosamente."
                        refreshCircles()
                    }
                    is Resource.Error -> {
                        isGeofenceLoading = false
                        geofenceErrorMessage = resource.message ?: "Error al crear la Zona Segura."
                    }
                }
            }
        }
    }

    fun deleteGeofence(geofenceId: Int) {
        viewModelScope.launch {
            circleRepository.deleteGeofence(geofenceId).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isGeofenceLoading = true
                        geofenceErrorMessage = null
                        geofenceSuccessMessage = null
                    }
                    is Resource.Success -> {
                        isGeofenceLoading = false
                        geofenceSuccessMessage = "Zona Segura eliminada exitosamente."
                        refreshCircles()
                    }
                    is Resource.Error -> {
                        isGeofenceLoading = false
                        geofenceErrorMessage = resource.message ?: "Error al eliminar la Zona Segura."
                    }
                }
            }
        }
    }

    fun updateGeofence(geofenceId: Int, name: String, radius: Double, userId: Int?) {
        viewModelScope.launch {
            circleRepository.updateGeofence(geofenceId, name, radius, userId).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isGeofenceLoading = true
                        geofenceErrorMessage = null
                        geofenceSuccessMessage = null
                    }
                    is Resource.Success -> {
                        isGeofenceLoading = false
                        geofenceSuccessMessage = "Zona Segura actualizada exitosamente."
                        refreshCircles()
                    }
                    is Resource.Error -> {
                        isGeofenceLoading = false
                        geofenceErrorMessage = resource.message ?: "Error al actualizar la Zona Segura."
                    }
                }
            }
        }
    }

    fun clearGeofenceMessages() {
        geofenceSuccessMessage = null
        geofenceErrorMessage = null
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}

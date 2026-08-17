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
import kotlinx.coroutines.flow.firstOrNull
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

    var activeDynamicGeofences by mutableStateOf<List<com.estoyok.app.features.tracking.data.model.DynamicGeofenceDto>>(emptyList())
        private set

    var isRadarLoading by mutableStateOf(false)
        private set

    var radarMessage by mutableStateOf<String?>(null)

    private var pollingJob: Job? = null

    init {
        isServiceRunning = TrackingService.isRunning
        refreshCircles()
        fetchActiveDynamicGeofences()
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
            val savedCircleId = sessionManager.selectedCircleIdFlow.firstOrNull()
            circleRepository.getCircles().collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isRefreshing = true
                    }
                    is Resource.Success -> {
                        isRefreshing = false
                        circles = resource.data ?: emptyList()
                        
                        // Preserve active selection or load persisted selection or fallback to first circle
                        val targetCircle = if (selectedCircle != null) {
                            circles.find { it.id == selectedCircle!!.id }
                        } else if (savedCircleId != null) {
                            circles.find { it.id == savedCircleId }
                        } else {
                            null
                        } ?: circles.firstOrNull()

                        if (targetCircle != null) {
                            selectCircle(targetCircle)
                        } else {
                            selectedCircle = null
                            selectedCircleMembers = emptyList()
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
        viewModelScope.launch {
            sessionManager.saveSelectedCircleId(circle.id)
        }
    }

    private fun startPolling() {
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(10000L) // Poll every 10 seconds
                fetchActiveDynamicGeofences()
                val currentSavedId = sessionManager.selectedCircleIdFlow.firstOrNull()
                circleRepository.getCircles().collectLatest { resource ->
                    if (resource is Resource.Success) {
                        circles = resource.data ?: emptyList()
                        val targetId = selectedCircle?.id ?: currentSavedId
                        val updated = (if (targetId != null) circles.find { it.id == targetId } else null) ?: circles.firstOrNull()
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
                }
            }
        }

        // Dedicated coroutine for FCM push token synchronization
        viewModelScope.launch {
            try {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        viewModelScope.launch {
                            val uuid = sessionManager.getOrCreateDeviceUuid()
                            settingsRepository.updatePushToken(token, uuid).collectLatest { res ->
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

    fun loadMemberDrives(memberId: Int, startDate: String? = null, endDate: String? = null) {
        val circleId = selectedCircle?.id ?: return
        viewModelScope.launch {
            circleRepository.getMemberDrives(circleId, memberId, startDate, endDate).collectLatest { resource ->
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

    fun loadAllMembersDrives(circleId: Int, members: List<CircleMemberDto>, startDate: String? = null, endDate: String? = null) {
        viewModelScope.launch {
            isDrivesLoading = true
            drivesErrorMessage = null
            val tempMap = mutableMapOf<Int, List<MemberDriveEventDto>>()
            var hasPremium = false

            val deferredDrives = members.map { member ->
                async {
                    var drivesList = emptyList<MemberDriveEventDto>()
                    try {
                        circleRepository.getMemberDrives(circleId, member.id, startDate, endDate).collect { resource ->
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

    fun fetchActiveDynamicGeofences() {
        viewModelScope.launch {
            circleRepository.getActiveDynamicGeofences().collectLatest { resource ->
                if (resource is Resource.Success) {
                    activeDynamicGeofences = resource.data ?: emptyList()
                }
            }
        }
    }

    fun createDynamicGeofence(targetId: Int, radiusMeters: Int) {
        viewModelScope.launch {
            circleRepository.createDynamicGeofence(targetId, radiusMeters).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isRadarLoading = true
                        radarMessage = null
                    }
                    is Resource.Success -> {
                        isRadarLoading = false
                        radarMessage = "📡 Radar de Proximidad activado (${radiusMeters}m)."
                        fetchActiveDynamicGeofences()
                    }
                    is Resource.Error -> {
                        isRadarLoading = false
                        radarMessage = resource.message ?: "No se pudo activar el radar de proximidad."
                    }
                }
            }
        }
    }

    fun deactivateDynamicGeofence(id: Int) {
        viewModelScope.launch {
            circleRepository.deactivateDynamicGeofence(id).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isRadarLoading = true
                        radarMessage = null
                    }
                    is Resource.Success -> {
                        isRadarLoading = false
                        radarMessage = "📡 Radar de Proximidad desactivado."
                        fetchActiveDynamicGeofences()
                    }
                    is Resource.Error -> {
                        isRadarLoading = false
                        radarMessage = resource.message ?: "No se pudo desactivar el radar."
                    }
                }
            }
        }
    }

    fun clearRadarMessage() {
        radarMessage = null
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}

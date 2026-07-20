package com.estoyok.app.features.wellbeing.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estoyok.app.BuildConfig
import com.estoyok.app.core.util.AudioRecorder
import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.UserDto
import com.estoyok.app.features.wellbeing.data.model.CheckInDto
import com.estoyok.app.features.wellbeing.domain.repository.CheckInRepository
import com.estoyok.app.features.wellbeing.domain.repository.EmergencyContactsRepository
import com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository
import com.estoyok.app.features.tracking.domain.repository.SosRepository
import com.estoyok.app.services.TrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

sealed interface WellbeingStatus {
    object NoReports : WellbeingStatus
    data class Safe(val nextReportAt: String) : WellbeingStatus
    object Expired : WellbeingStatus
}

@HiltViewModel
class PanelViewModel @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val settingsRepository: SettingsRepository,
    private val sosRepository: SosRepository,
    private val contactsRepository: EmergencyContactsRepository
) : ViewModel() {

    var user by mutableStateOf<UserDto?>(null)
        private set

    var checkInHistory by mutableStateOf<List<CheckInDto>>(emptyList())
        private set

    var isCheckingIn by mutableStateOf(false)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var isSosTriggered by mutableStateOf(false)
        private set

    var status by mutableStateOf<WellbeingStatus>(WellbeingStatus.NoReports)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            isRefreshing = true
            errorMessage = null
            
            launch { fetchUserProfile() }
            launch { fetchCheckInHistory() }
        }
    }

    private suspend fun fetchUserProfile() {
        settingsRepository.getUserProfile().collectLatest { resource ->
            when (resource) {
                is Resource.Success -> {
                    user = resource.data
                    calculateStatus()
                    isRefreshing = false
                }
                is Resource.Error -> {
                    errorMessage = resource.message ?: "Error al cargar datos del perfil."
                    isRefreshing = false
                }
                is Resource.Loading -> {}
            }
        }
    }

    private suspend fun fetchCheckInHistory() {
        checkInRepository.getCheckIns().collectLatest { resource ->
            when (resource) {
                is Resource.Success -> {
                    checkInHistory = resource.data ?: emptyList()
                }
                is Resource.Error -> {
                    // Fail silently or log
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun performCheckIn() {
        viewModelScope.launch {
            checkInRepository.checkIn("manual").collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isCheckingIn = true
                    }
                    is Resource.Success -> {
                        isCheckingIn = false
                        refreshDashboard()
                    }
                    is Resource.Error -> {
                        isCheckingIn = false
                        errorMessage = resource.message ?: "No se pudo registrar el check-in."
                    }
                }
            }
        }
    }

    fun triggerSos(context: Context) {
        viewModelScope.launch {
            sosRepository.triggerSos().collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isSosTriggered = true
                    }
                    is Resource.Success -> {
                        val alert = resource.data
                        if (alert != null) {
                            startSosTrackingAndRecording(context, alert.id)
                        }
                    }
                    is Resource.Error -> {
                        isSosTriggered = false
                        // Fallback SMS to contacts
                        triggerSmsFallback(context)
                    }
                }
            }
        }
    }

    private fun startSosTrackingAndRecording(context: Context, alertId: String) {
        // 1. Accelerate GPS to 5s
        val trackingIntent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_UPDATE_INTERVAL
            putExtra(TrackingService.EXTRA_INTERVAL, 5000L)
            putExtra(TrackingService.EXTRA_EMERGENCY, true)
        }
        ContextCompat.startForegroundService(context, trackingIntent)

        // 2. Start Audio Recording in background
        val audioRecorder = AudioRecorder(context)
        val recordedFile = audioRecorder.startRecording()

        if (recordedFile != null) {
            viewModelScope.launch {
                delay(15000L) // Record for 15 seconds
                audioRecorder.stopRecording()
                sosRepository.uploadAudio(alertId, recordedFile).collectLatest { uploadResource ->
                    isSosTriggered = false
                }
            }
        } else {
            isSosTriggered = false
        }
    }

    private fun triggerSmsFallback(context: Context) {
        viewModelScope.launch {
            contactsRepository.getContacts().collectLatest { resource ->
                if (resource is Resource.Success) {
                    val contacts = resource.data ?: emptyList()
                    val smsText = "[Estoy Ok] ¡EMERGENCIA CRÍTICA ACTIVA! He activado un SOS silencioso. Por favor contáctame o llama a emergencias de inmediato."
                    
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.SEND_SMS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val smsManager = SmsManager.getDefault()
                        for (contact in contacts) {
                            if (contact.isActive) {
                                try {
                                    smsManager.sendTextMessage(contact.phone, null, smsText, null, null)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun calculateStatus() {
        val targetUser = user ?: return
        val lastCheckInAt = targetUser.lastCheckInAt ?: run {
            status = WellbeingStatus.NoReports
            return
        }

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val sdfFallback = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            val lastCheckInDate = try {
                sdf.parse(lastCheckInAt)
            } catch (e: Exception) {
                sdfFallback.parse(lastCheckInAt)
            }

            if (lastCheckInDate == null) {
                status = WellbeingStatus.NoReports
                return
            }

            val lastCheckInTime = lastCheckInDate.time
            
            val isDebug = BuildConfig.DEBUG
            val intervalMs = targetUser.checkinIntervalHours * if (isDebug) 60L * 1000L else 60L * 60L * 1000L
            val nextCheckInTime = lastCheckInTime + intervalMs
            val now = System.currentTimeMillis()

            if (nextCheckInTime > now) {
                val nextReportDate = Date(nextCheckInTime)
                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                status = WellbeingStatus.Safe(outputFormat.format(nextReportDate))
            } else {
                status = WellbeingStatus.Expired
            }
        } catch (e: Exception) {
            status = WellbeingStatus.NoReports
        }
    }
}

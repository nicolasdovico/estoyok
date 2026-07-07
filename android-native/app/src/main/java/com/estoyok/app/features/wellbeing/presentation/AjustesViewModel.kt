package com.estoyok.app.features.wellbeing.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.UserDto
import com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto
import com.estoyok.app.features.wellbeing.domain.repository.EmergencyContactsRepository
import com.estoyok.app.features.wellbeing.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class AjustesViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val contactsRepository: EmergencyContactsRepository
) : ViewModel() {

    // User settings states
    var userProfile by mutableStateOf<UserDto?>(null)
        private set

    var checkinIntervalHours by mutableIntStateOf(24)
        private set

    var quietHoursEnabled by mutableStateOf(false)
        private set

    var quietHoursStart by mutableStateOf("22:00")
        private set

    var quietHoursEnd by mutableStateOf("08:00")
        private set

    var allowSmsWhatsappCheckin by mutableStateOf(false)
        private set

    var wifiCheckinEnabled by mutableStateOf(false)
        private set

    var safeWifiSsid by mutableStateOf("")
        private set

    var sensorCheckinEnabled by mutableStateOf(false)
        private set

    // Emergency Contacts list state
    var contacts by mutableStateOf<List<EmergencyContactDto>>(emptyList())
        private set

    // Contact creation inputs
    var newContactName by mutableStateOf("")
    var newContactPhone by mutableStateOf("")
    var newContactEmail by mutableStateOf("")
    var newContactRelationship by mutableStateOf("")

    // Screen-wide state UI
    var isLoading by mutableStateOf(false)
        private set

    var messageSuccess by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadSettings()
        loadContacts()
    }

    fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getUserProfile().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val data = resource.data
                        if (data != null) {
                            userProfile = data
                            checkinIntervalHours = data.checkinIntervalHours
                            allowSmsWhatsappCheckin = data.allowSmsWhatsappCheckin
                            // Note: Quiet hours, WiFi settings are populated below from endpoint, or default to mock/db
                            // To map with userDto quiet hours properties if defined, we retrieve them
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun loadContacts() {
        viewModelScope.launch {
            contactsRepository.getContacts().collectLatest { resource ->
                if (resource is Resource.Success) {
                    contacts = resource.data?.sortedBy { it.priority } ?: emptyList()
                }
            }
        }
    }

    fun saveCheckinInterval(hours: Int) {
        viewModelScope.launch {
            settingsRepository.updateCheckinInterval(hours).collectLatest { resource ->
                if (resource is Resource.Success) {
                    checkinIntervalHours = hours
                    messageSuccess = "Intervalo de reporte actualizado."
                }
            }
        }
    }

    fun saveQuietHoursSettings(enabled: Boolean, start: String, end: String) {
        val canonicalTimezone = TimeZone.getDefault().id
        viewModelScope.launch {
            settingsRepository.updateQuietHours(enabled, start, end, canonicalTimezone).collectLatest { resource ->
                if (resource is Resource.Success) {
                    quietHoursEnabled = enabled
                    quietHoursStart = start
                    quietHoursEnd = end
                    messageSuccess = "Modo Sueño actualizado."
                } else if (resource is Resource.Error) {
                    errorMessage = resource.message ?: "No se pudo guardar Modo Sueño."
                }
            }
        }
    }

    fun toggleSmsWhatsapp(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSmsWhatsappCheckin(enabled).collectLatest { resource ->
                if (resource is Resource.Success) {
                    allowSmsWhatsappCheckin = enabled
                    messageSuccess = "Ajustes de Twilio actualizados."
                }
            }
        }
    }

    fun saveAutomationSettings(wifiEnabled: Boolean, ssid: String, sensorEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateAutomation(wifiEnabled, ssid, sensorEnabled).collectLatest { resource ->
                if (resource is Resource.Success) {
                    wifiCheckinEnabled = wifiEnabled
                    safeWifiSsid = ssid
                    sensorCheckinEnabled = sensorEnabled
                    messageSuccess = "Auto-check-in pasivo guardado."
                }
            }
        }
    }

    // --- Emergency Contacts Operations ---

    fun addContact() {
        if (newContactName.isBlank() || newContactPhone.isBlank()) {
            errorMessage = "Completa nombre y teléfono del contacto."
            return
        }

        val phoneFormatted = newContactPhone.trim()
        if (!phoneFormatted.startsWith("+")) {
            errorMessage = "El teléfono del contacto debe iniciar con '+' (E.164)."
            return
        }

        if (newContactEmail.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(newContactEmail).matches()) {
            errorMessage = "El correo del contacto no tiene formato válido."
            return
        }

        viewModelScope.launch {
            val newContact = EmergencyContactDto(
                id = null,
                name = newContactName.trim(),
                phone = phoneFormatted,
                email = newContactEmail.trim().ifEmpty { null },
                relationship = newContactRelationship.trim().ifEmpty { null }
            )

            contactsRepository.createContact(newContact).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isLoading = true
                        errorMessage = null
                    }
                    is Resource.Success -> {
                        isLoading = false
                        newContactName = ""
                        newContactPhone = ""
                        newContactEmail = ""
                        newContactRelationship = ""
                        messageSuccess = "Contacto agregado exitosamente."
                        loadContacts()
                    }
                    is Resource.Error -> {
                        isLoading = false
                        errorMessage = resource.message ?: "Error al agregar contacto."
                    }
                }
            }
        }
    }

    fun deleteContact(id: Int) {
        viewModelScope.launch {
            contactsRepository.deleteContact(id).collectLatest { resource ->
                if (resource is Resource.Success) {
                    messageSuccess = "Contacto eliminado."
                    loadContacts()
                }
            }
        }
    }

    fun moveContactUp(index: Int) {
        if (index <= 0) return
        val list = contacts.toMutableList()
        val temp = list[index]
        list[index] = list[index - 1]
        list[index - 1] = temp
        updatePriorityOrder(list)
    }

    fun moveContactDown(index: Int) {
        if (index >= contacts.size - 1) return
        val list = contacts.toMutableList()
        val temp = list[index]
        list[index] = list[index + 1]
        list[index + 1] = temp
        updatePriorityOrder(list)
    }

    private fun updatePriorityOrder(newList: List<EmergencyContactDto>) {
        contacts = newList
        val ids = newList.mapNotNull { it.id }
        viewModelScope.launch {
            contactsRepository.reorderContacts(ids).collectLatest { resource ->
                if (resource is Resource.Success) {
                    loadContacts()
                }
            }
        }
    }

    fun clearMessages() {
        messageSuccess = null
        errorMessage = null
    }
}

package com.estoyok.app.features.wellbeing.domain.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.auth.data.model.MessageResponse
import com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto
import kotlinx.coroutines.flow.Flow

interface EmergencyContactsRepository {
    fun getContacts(): Flow<Resource<List<EmergencyContactDto>>>
    fun createContact(contact: EmergencyContactDto): Flow<Resource<EmergencyContactDto>>
    fun updateContact(id: Int, contact: EmergencyContactDto): Flow<Resource<EmergencyContactDto>>
    fun deleteContact(id: Int): Flow<Resource<MessageResponse>>
    fun reorderContacts(ids: List<Int>): Flow<Resource<MessageResponse>>
}

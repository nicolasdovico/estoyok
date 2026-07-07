package com.estoyok.app.features.wellbeing.data.remote

import com.estoyok.app.features.auth.data.model.MessageResponse
import com.estoyok.app.features.wellbeing.data.model.EmergencyContactDto
import com.estoyok.app.features.wellbeing.data.model.ReorderContactsRequest
import retrofit2.Response
import retrofit2.http.*

interface EmergencyContactsApiService {

    @GET("emergency-contacts")
    suspend fun getContacts(): Response<List<EmergencyContactDto>>

    @POST("emergency-contacts")
    suspend fun createContact(
        @Body contact: EmergencyContactDto
    ): Response<EmergencyContactDto>

    @PUT("emergency-contacts/{id}")
    suspend fun updateContact(
        @Path("id") id: Int,
        @Body contact: EmergencyContactDto
    ): Response<EmergencyContactDto>

    @DELETE("emergency-contacts/{id}")
    suspend fun deleteContact(
        @Path("id") id: Int
    ): Response<MessageResponse>

    @POST("emergency-contacts/reorder")
    suspend fun reorderContacts(
        @Body request: ReorderContactsRequest
    ): Response<MessageResponse>
}

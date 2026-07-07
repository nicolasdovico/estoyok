package com.estoyok.app.features.wellbeing.data.model

import com.google.gson.annotations.SerializedName

data class EmergencyContactDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String?,
    @SerializedName("relationship") val relationship: String?,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("priority") val priority: Int = 0
)

data class ReorderContactsRequest(
    @SerializedName("ids") val ids: List<Int>
)

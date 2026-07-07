package com.estoyok.app.features.tracking.domain.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.tracking.data.model.AudioUploadResponse
import com.estoyok.app.features.tracking.data.model.SosAlertResponse
import kotlinx.coroutines.flow.Flow
import java.io.File

interface SosRepository {
    
    fun triggerSos(): Flow<Resource<SosAlertResponse>>

    fun uploadAudio(
        alertId: String,
        audioFile: File
    ): Flow<Resource<AudioUploadResponse>>
}

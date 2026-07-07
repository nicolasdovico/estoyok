package com.estoyok.app.features.tracking.data.repository

import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.tracking.data.local.dao.OfflineLocationDao
import com.estoyok.app.features.tracking.data.local.entity.OfflineLocationEntity
import com.estoyok.app.features.tracking.data.model.*
import com.estoyok.app.features.tracking.data.remote.LocationApiService
import com.estoyok.app.features.tracking.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val apiService: LocationApiService,
    private val offlineLocationDao: OfflineLocationDao
) : LocationRepository {

    override fun updateLocation(
        request: LocationUpdateRequest,
        isOnline: Boolean
    ): Flow<Resource<LocationUpdateResponse>> = flow {
        emit(Resource.Loading())
        
        if (!isOnline) {
            // Save to offline Room database
            saveOffline(request)
            emit(Resource.Success(LocationUpdateResponse("Ubicación guardada localmente (Sin Conexión).", false)))
            return@flow
        }

        try {
            val response = apiService.updateLocation(request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                // If it fails with server error (e.g. 500), write offline to avoid data loss
                saveOffline(request)
                emit(Resource.Success(LocationUpdateResponse("Error de servidor. Guardado localmente.", false)))
            }
        } catch (e: IOException) {
            // Network failure during execution, fallback offline
            saveOffline(request)
            emit(Resource.Success(LocationUpdateResponse("Error de red. Guardado localmente.", false)))
        } catch (e: Exception) {
            emit(Resource.Error("Ocurrió un error inesperado al enviar ubicación."))
        }
    }

    override fun updateSensorStatus(request: SensorStatusRequest): Flow<Resource<SensorStatusResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.updateSensorStatus(request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error("Error al actualizar sensores."))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Sin conexión a internet."))
        } catch (e: Exception) {
            emit(Resource.Error("Error al enviar estado de sensores."))
        }
    }

    override fun flushOfflineQueue(): Flow<Resource<Int>> = flow {
        emit(Resource.Loading())
        try {
            val offlineList = offlineLocationDao.getAllLocations()
            if (offlineList.isEmpty()) {
                emit(Resource.Success(0))
                return@flow
            }

            var syncCount = 0
            val successfullySynced = mutableListOf<OfflineLocationEntity>()

            for (entity in offlineList) {
                val req = LocationUpdateRequest(
                    latitude = entity.latitude,
                    longitude = entity.longitude,
                    accuracy = entity.accuracy,
                    batteryLevel = entity.batteryLevel,
                    isTrackingActive = entity.isTrackingActive,
                    gpsEnabled = entity.gpsEnabled,
                    recordedAt = entity.recordedAt,
                    speed = entity.speed,
                    isDriving = entity.isDriving
                )

                try {
                    val response = apiService.updateLocation(req)
                    if (response.isSuccessful) {
                        successfullySynced.add(entity)
                        syncCount++
                    } else {
                        // Stop sync if server returns error to preserve order and avoid spamming
                        break
                    }
                } catch (e: IOException) {
                    // Stop sync if internet fails mid-way
                    break
                }
            }

            if (successfullySynced.isNotEmpty()) {
                offlineLocationDao.deleteLocations(successfullySynced)
            }

            emit(Resource.Success(syncCount))
        } catch (e: Exception) {
            emit(Resource.Error("Error al sincronizar cola local: ${e.localizedMessage}"))
        }
    }

    private suspend fun saveOffline(request: LocationUpdateRequest) {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val timestamp = request.recordedAt ?: isoFormat.format(Date())

        offlineLocationDao.insertLocation(
            OfflineLocationEntity(
                latitude = request.latitude,
                longitude = request.longitude,
                accuracy = request.accuracy,
                batteryLevel = request.batteryLevel,
                isTrackingActive = request.isTrackingActive,
                gpsEnabled = request.gpsEnabled,
                recordedAt = timestamp,
                speed = request.speed,
                isDriving = request.isDriving
            )
        )
    }
}

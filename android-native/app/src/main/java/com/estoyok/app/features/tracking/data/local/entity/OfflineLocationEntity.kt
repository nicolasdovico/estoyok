package com.estoyok.app.features.tracking.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_locations")
data class OfflineLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float?,
    val batteryLevel: Float?,
    val isTrackingActive: Boolean?,
    val gpsEnabled: Boolean?,
    val recordedAt: String,
    val speed: Float?,
    val isDriving: Boolean?
)

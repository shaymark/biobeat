package com.biobeat.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ecg_sessions")
data class EcgSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceMac: String,
    val startTimeMs: Long,
    val endTimeMs: Long?,
    val sampleCount: Int,
    val sampleRateHz: Int,
)

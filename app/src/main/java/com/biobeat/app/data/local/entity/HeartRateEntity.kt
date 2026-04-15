package com.biobeat.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heart_rate_readings")
data class HeartRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bpm: Int,
    val sensorContact: Boolean,
    val rrIntervals: String, // Stored as comma-separated values
    val deviceMac: String,
    val timestampMs: Long,
)

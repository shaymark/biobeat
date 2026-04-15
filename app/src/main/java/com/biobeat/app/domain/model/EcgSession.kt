package com.biobeat.app.domain.model

/**
 * A recorded ECG session with metadata.
 */
data class EcgSession(
    val id: Long = 0,
    val deviceMac: String,
    val startTimeMs: Long,
    val endTimeMs: Long?,
    val sampleCount: Int,
    val sampleRateHz: Int,
)

package com.biobeat.app.domain.model

/**
 * App-level heart rate record, enriched beyond the SDK model with persistence ID.
 */
data class HeartRateRecord(
    val id: Long = 0,
    val bpm: Int,
    val sensorContact: Boolean,
    val rrIntervals: List<Int>,
    val deviceMac: String,
    val timestampMs: Long,
)

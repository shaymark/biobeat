package com.biobeat.sdk.model

/**
 * A single heart rate measurement from the device.
 *
 * @property bpm Heart rate in beats per minute.
 * @property sensorContact Whether the sensor has reliable skin contact.
 * @property rrIntervals R-R intervals in milliseconds (time between heartbeats).
 * @property timestampMs Measurement timestamp ([System.currentTimeMillis]).
 */
public data class HeartRateReading(
    val bpm: Int,
    val sensorContact: Boolean,
    val rrIntervals: List<Int>,
    val timestampMs: Long,
)

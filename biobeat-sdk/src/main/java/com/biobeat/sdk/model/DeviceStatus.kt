package com.biobeat.sdk.model

/**
 * Current status of the connected device.
 *
 * @property batteryPercent Battery level (0–100).
 * @property isCharging Whether the device is currently charging.
 * @property memoryUsedBytes Used internal memory in bytes.
 * @property memoryTotalBytes Total internal memory in bytes.
 * @property firmwareVersion Device firmware version string.
 * @property timestampMs When this status was read ([System.currentTimeMillis]).
 */
public data class DeviceStatus(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val memoryUsedBytes: Long,
    val memoryTotalBytes: Long,
    val firmwareVersion: String,
    val timestampMs: Long,
)

package com.biobeat.sdk.model

/**
 * Information about a discovered BioBeat device during scanning.
 *
 * @property name Advertised device name, or null if not available.
 * @property macAddress Bluetooth MAC address (e.g., "AA:BB:CC:DD:EE:FF").
 * @property rssi Received signal strength indicator in dBm.
 */
public data class DeviceInfo(
    val name: String?,
    val macAddress: String,
    val rssi: Int,
)

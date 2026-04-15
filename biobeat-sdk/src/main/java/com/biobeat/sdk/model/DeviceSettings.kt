package com.biobeat.sdk.model

/**
 * Configurable settings on the device.
 *
 * Read current settings via [com.biobeat.sdk.connection.DeviceConnection.readSettings]
 * and write changes via [com.biobeat.sdk.connection.DeviceConnection.writeSettings].
 *
 * @property ecgEnabled Whether ECG recording is active.
 * @property heartRateInterval Seconds between heart rate measurements.
 * @property notificationsEnabled Whether device-side notifications are enabled.
 * @property deviceName User-facing device name.
 */
public data class DeviceSettings(
    val ecgEnabled: Boolean,
    val heartRateInterval: Int,
    val notificationsEnabled: Boolean,
    val deviceName: String,
)

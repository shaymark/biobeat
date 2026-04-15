package com.biobeat.sdk.internal.gatt

/**
 * Parses the standard Bluetooth Battery Level characteristic (0x2A19).
 *
 * The value is a single byte representing the battery percentage (0–100).
 */
internal object BatteryParser {

    fun parse(value: ByteArray): Int {
        if (value.isEmpty()) return 0
        return (value[0].toInt() and 0xFF).coerceIn(0, 100)
    }
}

package com.biobeat.sdk.internal.gatt

import com.biobeat.sdk.model.DeviceStatus
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Parses device status from the custom BioBeat status characteristic + standard battery level.
 *
 * Custom status characteristic format:
 * - Byte 0: Flags (bit 0=isCharging)
 * - Bytes 1-4: memoryUsedBytes (uint32, little-endian)
 * - Bytes 5-8: memoryTotalBytes (uint32, little-endian)
 * - Byte 9: firmwareVersion major
 * - Byte 10: firmwareVersion minor
 * - Byte 11: firmwareVersion patch
 */
internal object DeviceStatusParser {

    private const val STATUS_MIN_SIZE = 12

    fun parse(statusBytes: ByteArray, batteryLevel: Int): DeviceStatus {
        if (statusBytes.size < STATUS_MIN_SIZE) {
            return DeviceStatus(
                batteryPercent = batteryLevel,
                isCharging = false,
                memoryUsedBytes = 0,
                memoryTotalBytes = 0,
                firmwareVersion = "0.0.0",
                timestampMs = System.currentTimeMillis(),
            )
        }

        val flags = statusBytes[0].toInt() and 0xFF
        val buffer = ByteBuffer.wrap(statusBytes).order(ByteOrder.LITTLE_ENDIAN)
        val memoryUsed = buffer.getInt(1).toLong() and 0xFFFFFFFFL
        val memoryTotal = buffer.getInt(5).toLong() and 0xFFFFFFFFL
        val fwMajor = statusBytes[9].toInt() and 0xFF
        val fwMinor = statusBytes[10].toInt() and 0xFF
        val fwPatch = statusBytes[11].toInt() and 0xFF

        return DeviceStatus(
            batteryPercent = batteryLevel,
            isCharging = (flags and 0x01) != 0,
            memoryUsedBytes = memoryUsed,
            memoryTotalBytes = memoryTotal,
            firmwareVersion = "$fwMajor.$fwMinor.$fwPatch",
            timestampMs = System.currentTimeMillis(),
        )
    }
}

package com.biobeat.sdk.internal.gatt

import com.biobeat.sdk.model.HeartRateReading

/**
 * Parses the standard Bluetooth Heart Rate Measurement characteristic (0x2A37).
 *
 * Format per Bluetooth SIG specification:
 * - Byte 0: Flags (bit 0 = HR format, bit 1-2 = sensor contact, bit 4 = RR present)
 * - Byte 1 (or 1-2): Heart rate value
 * - Remaining: Optional RR intervals (1/1024 sec resolution)
 */
internal object HeartRateParser {

    fun parse(value: ByteArray): HeartRateReading {
        if (value.isEmpty()) {
            return HeartRateReading(bpm = 0, sensorContact = false, rrIntervals = emptyList(), timestampMs = System.currentTimeMillis())
        }

        val flags = value[0].toInt() and 0xFF
        val isFormat16Bit = (flags and 0x01) != 0
        val sensorContact = (flags and 0x06) == 0x06
        val rrPresent = (flags and 0x10) != 0

        var offset = 1
        val bpm: Int
        if (isFormat16Bit) {
            bpm = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
            offset += 2
        } else {
            bpm = value[offset].toInt() and 0xFF
            offset += 1
        }

        // Skip Energy Expended if present (bit 3)
        if ((flags and 0x08) != 0) {
            offset += 2
        }

        val rrIntervals = mutableListOf<Int>()
        if (rrPresent) {
            while (offset + 1 < value.size) {
                val rrRaw = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8)
                // Convert from 1/1024 sec to milliseconds
                val rrMs = (rrRaw * 1000) / 1024
                rrIntervals.add(rrMs)
                offset += 2
            }
        }

        return HeartRateReading(
            bpm = bpm,
            sensorContact = sensorContact,
            rrIntervals = rrIntervals,
            timestampMs = System.currentTimeMillis(),
        )
    }
}

package com.biobeat.sdk.internal.gatt

import com.biobeat.sdk.model.EcgSample
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Parses custom BioBeat ECG characteristic data.
 *
 * Expected format:
 * - Bytes 0-3: Sequence number (uint32, little-endian)
 * - Bytes 4-5: Sample rate in Hz (uint16, little-endian)
 * - Bytes 6+: Packed 16-bit signed samples (little-endian), each representing millivolts * 1000
 */
internal object EcgParser {

    private const val HEADER_SIZE = 6
    private const val SAMPLE_SIZE = 2
    private const val MV_SCALE = 1000f

    fun parse(value: ByteArray): EcgSample {
        if (value.size < HEADER_SIZE) {
            return EcgSample(
                sequenceNumber = 0,
                millivolts = floatArrayOf(),
                sampleRateHz = 0,
                timestampMs = System.currentTimeMillis(),
            )
        }

        val buffer = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN)
        val sequenceNumber = buffer.getInt(0).toLong() and 0xFFFFFFFFL
        val sampleRateHz = buffer.getShort(4).toInt() and 0xFFFF

        val sampleCount = (value.size - HEADER_SIZE) / SAMPLE_SIZE
        val millivolts = FloatArray(sampleCount) { i ->
            val offset = HEADER_SIZE + i * SAMPLE_SIZE
            buffer.getShort(offset).toFloat() / MV_SCALE
        }

        return EcgSample(
            sequenceNumber = sequenceNumber,
            millivolts = millivolts,
            sampleRateHz = sampleRateHz,
            timestampMs = System.currentTimeMillis(),
        )
    }
}

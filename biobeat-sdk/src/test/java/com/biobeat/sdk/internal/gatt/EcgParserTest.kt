package com.biobeat.sdk.internal.gatt

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class EcgParserTest {

    @Test
    fun `parse valid ECG packet`() {
        // Header: seq=42, sampleRate=250
        // Samples: [100, -200, 500] as int16 (millivolts * 1000)
        val buffer = ByteBuffer.allocate(6 + 6).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(42)            // sequence
        buffer.putShort(250)         // sample rate
        buffer.putShort(100)         // sample 1: 0.1 mV
        buffer.putShort((-200).toShort())  // sample 2: -0.2 mV
        buffer.putShort(500)         // sample 3: 0.5 mV

        val result = EcgParser.parse(buffer.array())

        assertEquals(42L, result.sequenceNumber)
        assertEquals(250, result.sampleRateHz)
        assertEquals(3, result.millivolts.size)
        assertEquals(0.1f, result.millivolts[0], 0.001f)
        assertEquals(-0.2f, result.millivolts[1], 0.001f)
        assertEquals(0.5f, result.millivolts[2], 0.001f)
    }

    @Test
    fun `parse packet with no samples`() {
        val buffer = ByteBuffer.allocate(6).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(1)
        buffer.putShort(250)

        val result = EcgParser.parse(buffer.array())

        assertEquals(1L, result.sequenceNumber)
        assertEquals(250, result.sampleRateHz)
        assertEquals(0, result.millivolts.size)
    }

    @Test
    fun `parse too-short packet returns defaults`() {
        val result = EcgParser.parse(byteArrayOf(0x01, 0x02))

        assertEquals(0L, result.sequenceNumber)
        assertEquals(0, result.sampleRateHz)
        assertEquals(0, result.millivolts.size)
    }
}

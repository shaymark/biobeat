package com.biobeat.sdk.internal.gatt

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HeartRateParserTest {

    @Test
    fun `parse 8-bit HR with no RR intervals`() {
        // Flags: 0x00 (8-bit format, no sensor contact info, no RR)
        // HR: 72
        val data = byteArrayOf(0x00, 72)
        val result = HeartRateParser.parse(data)

        assertEquals(72, result.bpm)
        assertFalse(result.sensorContact)
        assertTrue(result.rrIntervals.isEmpty())
    }

    @Test
    fun `parse 16-bit HR value`() {
        // Flags: 0x01 (16-bit format)
        // HR: 260 = 0x0104 (little-endian: 0x04, 0x01)
        val data = byteArrayOf(0x01, 0x04, 0x01)
        val result = HeartRateParser.parse(data)

        assertEquals(260, result.bpm)
    }

    @Test
    fun `parse with sensor contact detected`() {
        // Flags: 0x06 (sensor contact supported and detected)
        // HR: 80
        val data = byteArrayOf(0x06, 80)
        val result = HeartRateParser.parse(data)

        assertEquals(80, result.bpm)
        assertTrue(result.sensorContact)
    }

    @Test
    fun `parse with RR intervals`() {
        // Flags: 0x10 (RR present)
        // HR: 75
        // RR: 800ms = 819 in 1/1024 sec (0x0333 LE: 0x33, 0x03)
        val data = byteArrayOf(0x10, 75, 0x33, 0x03)
        val result = HeartRateParser.parse(data)

        assertEquals(75, result.bpm)
        assertEquals(1, result.rrIntervals.size)
        // 0x0333 = 819 → 819 * 1000 / 1024 = 799
        assertEquals(799, result.rrIntervals[0])
    }

    @Test
    fun `parse with energy expended and RR`() {
        // Flags: 0x18 (energy expended present + RR present)
        // HR: 65
        // Energy: 2 bytes (skipped)
        // RR: 1024 in 1/1024 sec = 1000ms (0x0400 LE: 0x00, 0x04)
        val data = byteArrayOf(0x18, 65, 0x00, 0x00, 0x00, 0x04)
        val result = HeartRateParser.parse(data)

        assertEquals(65, result.bpm)
        assertEquals(1, result.rrIntervals.size)
        assertEquals(1000, result.rrIntervals[0])
    }

    @Test
    fun `parse empty data returns zero`() {
        val result = HeartRateParser.parse(byteArrayOf())
        assertEquals(0, result.bpm)
    }
}

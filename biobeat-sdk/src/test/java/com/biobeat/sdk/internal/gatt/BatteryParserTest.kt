package com.biobeat.sdk.internal.gatt

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryParserTest {

    @Test
    fun `parse normal battery level`() {
        assertEquals(75, BatteryParser.parse(byteArrayOf(75)))
    }

    @Test
    fun `parse full battery`() {
        assertEquals(100, BatteryParser.parse(byteArrayOf(100)))
    }

    @Test
    fun `parse zero battery`() {
        assertEquals(0, BatteryParser.parse(byteArrayOf(0)))
    }

    @Test
    fun `parse empty data returns zero`() {
        assertEquals(0, BatteryParser.parse(byteArrayOf()))
    }

    @Test
    fun `parse clamps to 100`() {
        assertEquals(100, BatteryParser.parse(byteArrayOf(120)))
    }
}

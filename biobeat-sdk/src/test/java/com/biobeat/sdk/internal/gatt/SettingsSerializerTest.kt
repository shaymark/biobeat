package com.biobeat.sdk.internal.gatt

import com.biobeat.sdk.model.DeviceSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsSerializerTest {

    @Test
    fun `round-trip serialize and deserialize`() {
        val original = DeviceSettings(
            ecgEnabled = true,
            heartRateInterval = 5,
            notificationsEnabled = true,
            deviceName = "TestDevice",
        )

        val bytes = SettingsSerializer.serialize(original)
        val deserialized = SettingsSerializer.deserialize(bytes)

        assertEquals(original, deserialized)
    }

    @Test
    fun `serialize ecg disabled`() {
        val settings = DeviceSettings(
            ecgEnabled = false,
            heartRateInterval = 10,
            notificationsEnabled = true,
            deviceName = "BB",
        )

        val bytes = SettingsSerializer.serialize(settings)
        val result = SettingsSerializer.deserialize(bytes)

        assertFalse(result.ecgEnabled)
        assertTrue(result.notificationsEnabled)
        assertEquals(10, result.heartRateInterval)
        assertEquals("BB", result.deviceName)
    }

    @Test
    fun `deserialize short data returns defaults`() {
        val result = SettingsSerializer.deserialize(byteArrayOf(0x01))

        assertFalse(result.ecgEnabled)
        assertEquals(1, result.heartRateInterval)
        assertEquals("BioBeat", result.deviceName)
    }

    @Test
    fun `serialize empty device name`() {
        val settings = DeviceSettings(
            ecgEnabled = false,
            heartRateInterval = 1,
            notificationsEnabled = false,
            deviceName = "",
        )

        val bytes = SettingsSerializer.serialize(settings)
        val result = SettingsSerializer.deserialize(bytes)

        assertEquals("", result.deviceName)
    }
}

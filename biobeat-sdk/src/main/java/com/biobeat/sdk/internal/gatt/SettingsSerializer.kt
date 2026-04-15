package com.biobeat.sdk.internal.gatt

import com.biobeat.sdk.model.DeviceSettings
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Serializes and deserializes [DeviceSettings] for the custom BioBeat settings characteristic.
 *
 * Format:
 * - Byte 0: Flags (bit 0=ecgEnabled, bit 1=notificationsEnabled)
 * - Bytes 1-2: heartRateInterval (uint16, little-endian, seconds)
 * - Byte 3: deviceName length (uint8)
 * - Bytes 4+: deviceName (UTF-8)
 */
internal object SettingsSerializer {

    fun deserialize(value: ByteArray): DeviceSettings {
        if (value.size < 4) {
            return DeviceSettings(
                ecgEnabled = false,
                heartRateInterval = 1,
                notificationsEnabled = true,
                deviceName = "BioBeat",
            )
        }

        val flags = value[0].toInt() and 0xFF
        val buffer = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN)
        val heartRateInterval = buffer.getShort(1).toInt() and 0xFFFF
        val nameLength = (value[3].toInt() and 0xFF).coerceAtMost(value.size - 4)
        val deviceName = if (nameLength > 0) {
            String(value, 4, nameLength, Charsets.UTF_8)
        } else {
            ""
        }

        return DeviceSettings(
            ecgEnabled = (flags and 0x01) != 0,
            heartRateInterval = heartRateInterval,
            notificationsEnabled = (flags and 0x02) != 0,
            deviceName = deviceName,
        )
    }

    fun serialize(settings: DeviceSettings): ByteArray {
        val nameBytes = settings.deviceName.toByteArray(Charsets.UTF_8)
        val buffer = ByteBuffer.allocate(4 + nameBytes.size).order(ByteOrder.LITTLE_ENDIAN)

        var flags = 0
        if (settings.ecgEnabled) flags = flags or 0x01
        if (settings.notificationsEnabled) flags = flags or 0x02

        buffer.put(flags.toByte())
        buffer.putShort(settings.heartRateInterval.toShort())
        buffer.put(nameBytes.size.toByte())
        buffer.put(nameBytes)

        return buffer.array()
    }
}

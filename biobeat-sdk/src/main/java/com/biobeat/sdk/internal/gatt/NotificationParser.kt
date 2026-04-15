package com.biobeat.sdk.internal.gatt

import com.biobeat.sdk.model.DeviceNotification
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Parses custom BioBeat device notification characteristic data.
 *
 * Expected format:
 * - Bytes 0-1: Notification ID (uint16, little-endian)
 * - Byte 2: Type (0=ALERT, 1=REMINDER, 2=SYSTEM)
 * - Byte 3: Severity (0=LOW, 1=MEDIUM, 2=HIGH, 3=CRITICAL)
 * - Bytes 4+: UTF-8 message string
 */
internal object NotificationParser {

    private const val HEADER_SIZE = 4

    fun parse(value: ByteArray): DeviceNotification {
        if (value.size < HEADER_SIZE) {
            return DeviceNotification(
                id = 0,
                type = DeviceNotification.NotificationType.UNKNOWN,
                message = "",
                severity = DeviceNotification.Severity.LOW,
                timestampMs = System.currentTimeMillis(),
            )
        }

        val buffer = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN)
        val id = buffer.getShort(0).toInt() and 0xFFFF
        val typeOrdinal = (value[2].toInt() and 0xFF)
        val severityOrdinal = (value[3].toInt() and 0xFF)

        val type = DeviceNotification.NotificationType.entries.getOrElse(typeOrdinal) {
            DeviceNotification.NotificationType.UNKNOWN
        }

        val severity = DeviceNotification.Severity.entries.getOrElse(severityOrdinal) {
            DeviceNotification.Severity.LOW
        }

        val message = if (value.size > HEADER_SIZE) {
            String(value, HEADER_SIZE, value.size - HEADER_SIZE, Charsets.UTF_8)
        } else {
            ""
        }

        return DeviceNotification(
            id = id,
            type = type,
            message = message,
            severity = severity,
            timestampMs = System.currentTimeMillis(),
        )
    }
}

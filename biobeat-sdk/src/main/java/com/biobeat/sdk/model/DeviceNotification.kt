package com.biobeat.sdk.model

/**
 * A notification or alert pushed from the device.
 *
 * @property id Unique notification identifier.
 * @property type Category of the notification.
 * @property message Human-readable notification message.
 * @property severity Urgency level.
 * @property timestampMs When the notification was received ([System.currentTimeMillis]).
 */
public data class DeviceNotification(
    val id: Int,
    val type: NotificationType,
    val message: String,
    val severity: Severity,
    val timestampMs: Long,
) {
    public enum class NotificationType {
        ALERT,
        REMINDER,
        SYSTEM,
        UNKNOWN,
    }

    public enum class Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL,
    }
}

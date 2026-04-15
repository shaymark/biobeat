package com.biobeat.sdk.internal.ble

import java.util.UUID

/**
 * Represents a characteristic notification received from the device.
 */
internal data class CharacteristicEvent(
    val uuid: UUID,
    val value: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CharacteristicEvent) return false
        return uuid == other.uuid && value.contentEquals(other.value)
    }

    override fun hashCode(): Int = 31 * uuid.hashCode() + value.contentHashCode()
}

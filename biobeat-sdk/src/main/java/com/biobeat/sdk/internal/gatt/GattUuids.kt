package com.biobeat.sdk.internal.gatt

import java.util.UUID

/**
 * GATT service and characteristic UUIDs used by the BioBeat device.
 *
 * Standard profiles use the Bluetooth SIG base UUID.
 * Custom BioBeat services use a proprietary base UUID — replace placeholder
 * UUIDs below with the actual firmware-defined values.
 */
internal object GattUuids {

    // --- Standard Bluetooth SIG profiles ---

    val HEART_RATE_SERVICE: UUID = uuid16(0x180D)
    val HEART_RATE_MEASUREMENT: UUID = uuid16(0x2A37)

    val BATTERY_SERVICE: UUID = uuid16(0x180F)
    val BATTERY_LEVEL: UUID = uuid16(0x2A19)

    val CCC_DESCRIPTOR: UUID = uuid16(0x2902)

    // --- Custom BioBeat services (replace with actual firmware UUIDs) ---

    val ECG_SERVICE: UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    val ECG_DATA_CHAR: UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")

    val NOTIFICATION_SERVICE: UUID = UUID.fromString("6e400010-b5a3-f393-e0a9-e50e24dcca9e")
    val NOTIFICATION_CHAR: UUID = UUID.fromString("6e400011-b5a3-f393-e0a9-e50e24dcca9e")

    val SETTINGS_SERVICE: UUID = UUID.fromString("6e400020-b5a3-f393-e0a9-e50e24dcca9e")
    val SETTINGS_CHAR: UUID = UUID.fromString("6e400021-b5a3-f393-e0a9-e50e24dcca9e")
    val DEVICE_STATUS_CHAR: UUID = UUID.fromString("6e400022-b5a3-f393-e0a9-e50e24dcca9e")

    /**
     * Converts a 16-bit UUID to the full 128-bit Bluetooth SIG format.
     */
    private fun uuid16(shortUuid: Int): UUID =
        UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", shortUuid))
}

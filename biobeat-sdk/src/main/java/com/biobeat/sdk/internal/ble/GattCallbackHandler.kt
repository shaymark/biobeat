package com.biobeat.sdk.internal.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Bridges Android's callback-based BluetoothGattCallback into coroutines.
 *
 * - Connection state changes → [connectionEvents] channel
 * - Service discovery results → [serviceDiscoveredChannel] channel
 * - Characteristic notifications → [characteristicChanged] shared flow
 * - Read/write results → [readWriteResults] channel
 */
internal class GattCallbackHandler : BluetoothGattCallback() {

    val connectionEvents = Channel<GattConnectionEvent>(Channel.BUFFERED)
    val serviceDiscoveredChannel = Channel<Int>(Channel.BUFFERED)
    val characteristicChanged = MutableSharedFlow<CharacteristicEvent>(
        extraBufferCapacity = 256,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val readWriteResults = Channel<GattReadWriteResult>(Channel.BUFFERED)
    val descriptorWriteResults = Channel<Int>(Channel.BUFFERED)

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        connectionEvents.trySend(
            GattConnectionEvent(
                status = status,
                connected = newState == BluetoothProfile.STATE_CONNECTED,
            )
        )
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        serviceDiscoveredChannel.trySend(status)
    }

    @Suppress("DEPRECATION")
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
    ) {
        // Deprecated overload kept for API 26–32 compatibility
        characteristicChanged.tryEmit(
            CharacteristicEvent(characteristic.uuid, characteristic.value ?: byteArrayOf())
        )
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
    ) {
        characteristicChanged.tryEmit(
            CharacteristicEvent(characteristic.uuid, value)
        )
    }

    @Suppress("DEPRECATION")
    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int,
    ) {
        readWriteResults.trySend(
            GattReadWriteResult(characteristic.uuid, characteristic.value ?: byteArrayOf(), status)
        )
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int,
    ) {
        readWriteResults.trySend(
            GattReadWriteResult(characteristic.uuid, value, status)
        )
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int,
    ) {
        readWriteResults.trySend(
            GattReadWriteResult(characteristic.uuid, byteArrayOf(), status)
        )
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int,
    ) {
        descriptorWriteResults.trySend(status)
    }
}

internal data class GattConnectionEvent(
    val status: Int,
    val connected: Boolean,
)

internal data class GattReadWriteResult(
    val uuid: java.util.UUID,
    val value: ByteArray,
    val status: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GattReadWriteResult) return false
        return uuid == other.uuid && value.contentEquals(other.value) && status == other.status
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + value.contentHashCode()
        result = 31 * result + status
        return result
    }
}

package com.biobeat.sdk.internal.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.content.Context
import com.biobeat.sdk.BioBeatSdkConfig
import com.biobeat.sdk.connection.ConnectionState
import com.biobeat.sdk.exception.BioBeatException
import com.biobeat.sdk.internal.coroutines.BleDispatcher
import com.biobeat.sdk.internal.gatt.GattUuids
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.UUID

/**
 * Manages the BluetoothGatt lifecycle: connect, disconnect, read/write characteristics,
 * enable notifications, and handle automatic reconnection.
 */
@SuppressLint("MissingPermission")
internal class BleConnectionManager(
    private val context: Context,
    private val config: BioBeatSdkConfig,
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val callbackHandler = GattCallbackHandler()
    val characteristicChanged: SharedFlow<CharacteristicEvent> = callbackHandler.characteristicChanged

    private var gatt: BluetoothGatt? = null
    private var scope = CoroutineScope(BleDispatcher.dispatcher + SupervisorJob())
    private var reconnectJob: Job? = null
    private val reconnectionPolicy = ReconnectionPolicy(config)

    companion object {
        private const val CONNECT_TIMEOUT_MS = 15_000L
        private const val OPERATION_TIMEOUT_MS = 5_000L
    }

    /**
     * Establishes a BLE connection to the device, discovers services,
     * and enables notifications on all relevant characteristics.
     */
    suspend fun connect(macAddress: String) = withContext(BleDispatcher.dispatcher) {
        val adapter = bluetoothAdapter ?: throw BioBeatException.BluetoothDisabled()
        if (!adapter.isEnabled) throw BioBeatException.BluetoothDisabled()

        _connectionState.value = ConnectionState.Connecting
        reconnectJob?.cancel()

        try {
            val device = adapter.getRemoteDevice(macAddress)

            withTimeout(CONNECT_TIMEOUT_MS) {
                gatt = device.connectGatt(context, false, callbackHandler, BluetoothDevice.TRANSPORT_LE)

                // Wait for connection
                val event = callbackHandler.connectionEvents.receive()
                if (!event.connected || event.status != BluetoothGatt.GATT_SUCCESS) {
                    throw BioBeatException.ConnectionFailed()
                }

                // Discover services
                gatt?.discoverServices()
                val discoveryStatus = callbackHandler.serviceDiscoveredChannel.receive()
                if (discoveryStatus != BluetoothGatt.GATT_SUCCESS) {
                    throw BioBeatException.GattError(discoveryStatus)
                }
            }

            // Enable notifications on all relevant characteristics
            enableNotification(GattUuids.HEART_RATE_SERVICE, GattUuids.HEART_RATE_MEASUREMENT)
            enableNotification(GattUuids.ECG_SERVICE, GattUuids.ECG_DATA_CHAR)
            enableNotification(GattUuids.NOTIFICATION_SERVICE, GattUuids.NOTIFICATION_CHAR)

            _connectionState.value = ConnectionState.Connected

            // Start monitoring for unexpected disconnects
            startDisconnectMonitor(macAddress)
        } catch (e: BioBeatException) {
            closeGatt()
            _connectionState.value = ConnectionState.Failed(e)
            throw e
        } catch (e: Exception) {
            closeGatt()
            val wrapped = BioBeatException.ConnectionFailed(e)
            _connectionState.value = ConnectionState.Failed(wrapped)
            throw wrapped
        }
    }

    /**
     * Gracefully disconnects and releases all BLE resources.
     */
    suspend fun disconnect() = withContext(BleDispatcher.dispatcher) {
        reconnectJob?.cancel()
        reconnectJob = null
        closeGatt()
        _connectionState.value = ConnectionState.Disconnected
    }

    /**
     * Reads a characteristic value from the device.
     */
    suspend fun readCharacteristic(serviceUuid: UUID, charUuid: UUID): ByteArray =
        withContext(BleDispatcher.dispatcher) {
            val g = gatt ?: throw BioBeatException.NotConnected()
            val service = g.getService(serviceUuid) ?: throw BioBeatException.GattError(-1)
            val char = service.getCharacteristic(charUuid) ?: throw BioBeatException.GattError(-1)

            g.readCharacteristic(char)

            withTimeout(OPERATION_TIMEOUT_MS) {
                val result = callbackHandler.readWriteResults.receive()
                if (result.status != BluetoothGatt.GATT_SUCCESS) {
                    throw BioBeatException.GattError(result.status)
                }
                result.value
            }
        }

    /**
     * Writes a value to a characteristic on the device.
     */
    suspend fun writeCharacteristic(serviceUuid: UUID, charUuid: UUID, value: ByteArray) =
        withContext(BleDispatcher.dispatcher) {
            val g = gatt ?: throw BioBeatException.NotConnected()
            val service = g.getService(serviceUuid) ?: throw BioBeatException.GattError(-1)
            val char = service.getCharacteristic(charUuid) ?: throw BioBeatException.GattError(-1)

            g.writeCharacteristic(
                char,
                value,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
            )

            withTimeout(OPERATION_TIMEOUT_MS) {
                val result = callbackHandler.readWriteResults.receive()
                if (result.status != BluetoothGatt.GATT_SUCCESS) {
                    throw BioBeatException.WriteFailed("GATT status ${result.status}")
                }
            }
        }

    /**
     * Enables BLE notifications for a characteristic by writing the CCC descriptor.
     */
    private suspend fun enableNotification(serviceUuid: UUID, charUuid: UUID) {
        val g = gatt ?: return
        val service = g.getService(serviceUuid) ?: return
        val char = service.getCharacteristic(charUuid) ?: return

        g.setCharacteristicNotification(char, true)

        val descriptor = char.getDescriptor(GattUuids.CCC_DESCRIPTOR) ?: return
        g.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)

        withTimeout(OPERATION_TIMEOUT_MS) {
            callbackHandler.descriptorWriteResults.receive()
        }
    }

    /**
     * Monitors for unexpected disconnection events and triggers reconnection.
     */
    private fun startDisconnectMonitor(macAddress: String) {
        scope.launch {
            for (event in callbackHandler.connectionEvents) {
                if (!event.connected && _connectionState.value is ConnectionState.Connected) {
                    closeGatt()
                    attemptReconnection(macAddress)
                }
            }
        }
    }

    /**
     * Attempts automatic reconnection with exponential backoff.
     */
    private fun attemptReconnection(macAddress: String) {
        reconnectJob = scope.launch {
            var attempt = 0
            while (reconnectionPolicy.shouldReconnect(attempt)) {
                _connectionState.value = ConnectionState.Reconnecting(
                    attempt = attempt + 1,
                    maxAttempts = config.maxReconnectAttempts,
                )
                val delayMs = reconnectionPolicy.delayForAttempt(attempt)
                delay(delayMs)
                try {
                    connect(macAddress)
                    return@launch // Successful reconnection
                } catch (_: Exception) {
                    attempt++
                }
            }
            _connectionState.value = ConnectionState.Failed(BioBeatException.ConnectionFailed())
        }
    }

    private fun closeGatt() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }
}

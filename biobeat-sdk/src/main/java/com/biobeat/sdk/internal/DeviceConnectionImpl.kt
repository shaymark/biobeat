package com.biobeat.sdk.internal

import com.biobeat.sdk.connection.ConnectionState
import com.biobeat.sdk.connection.DeviceConnection
import com.biobeat.sdk.exception.BioBeatException
import com.biobeat.sdk.internal.ble.BleConnectionManager
import com.biobeat.sdk.internal.coroutines.BleDispatcher
import com.biobeat.sdk.internal.gatt.BatteryParser
import com.biobeat.sdk.internal.gatt.DeviceStatusParser
import com.biobeat.sdk.internal.gatt.EcgParser
import com.biobeat.sdk.internal.gatt.GattUuids
import com.biobeat.sdk.internal.gatt.HeartRateParser
import com.biobeat.sdk.internal.gatt.NotificationParser
import com.biobeat.sdk.internal.gatt.SettingsSerializer
import com.biobeat.sdk.model.DeviceNotification
import com.biobeat.sdk.model.DeviceSettings
import com.biobeat.sdk.model.DeviceStatus
import com.biobeat.sdk.model.EcgSample
import com.biobeat.sdk.model.HeartRateReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Internal implementation of [DeviceConnection].
 *
 * Wires the [BleConnectionManager]'s raw characteristic events through
 * stateless parsers to produce typed public model flows.
 */
internal class DeviceConnectionImpl(
    private val macAddress: String,
    private val connectionManager: BleConnectionManager,
) : DeviceConnection {

    override val connectionState: StateFlow<ConnectionState>
        get() = connectionManager.connectionState

    override val heartRate: Flow<HeartRateReading>
        get() = connectionManager.characteristicChanged
            .filter { it.uuid == GattUuids.HEART_RATE_MEASUREMENT }
            .map { HeartRateParser.parse(it.value) }
            .flowOn(BleDispatcher.dispatcher)

    override val ecgData: Flow<EcgSample>
        get() = connectionManager.characteristicChanged
            .filter { it.uuid == GattUuids.ECG_DATA_CHAR }
            .map { EcgParser.parse(it.value) }
            .flowOn(BleDispatcher.dispatcher)

    override val notifications: Flow<DeviceNotification>
        get() = connectionManager.characteristicChanged
            .filter { it.uuid == GattUuids.NOTIFICATION_CHAR }
            .map { NotificationParser.parse(it.value) }
            .flowOn(BleDispatcher.dispatcher)

    private val _deviceStatus = MutableStateFlow<DeviceStatus?>(null)
    override val deviceStatus: StateFlow<DeviceStatus?> = _deviceStatus.asStateFlow()

    override suspend fun connect() {
        connectionManager.connect(macAddress)
        refreshStatus()
    }

    override suspend fun disconnect() {
        connectionManager.disconnect()
    }

    override suspend fun readSettings(): DeviceSettings {
        ensureConnected()
        val bytes = connectionManager.readCharacteristic(
            GattUuids.SETTINGS_SERVICE,
            GattUuids.SETTINGS_CHAR,
        )
        return SettingsSerializer.deserialize(bytes)
    }

    override suspend fun writeSettings(settings: DeviceSettings) {
        ensureConnected()
        connectionManager.writeCharacteristic(
            GattUuids.SETTINGS_SERVICE,
            GattUuids.SETTINGS_CHAR,
            SettingsSerializer.serialize(settings),
        )
    }

    override suspend fun refreshStatus() {
        ensureConnected()
        val statusBytes = connectionManager.readCharacteristic(
            GattUuids.SETTINGS_SERVICE,
            GattUuids.DEVICE_STATUS_CHAR,
        )
        val batteryBytes = connectionManager.readCharacteristic(
            GattUuids.BATTERY_SERVICE,
            GattUuids.BATTERY_LEVEL,
        )
        val batteryLevel = BatteryParser.parse(batteryBytes)
        _deviceStatus.value = DeviceStatusParser.parse(statusBytes, batteryLevel)
    }

    private fun ensureConnected() {
        if (connectionState.value !is ConnectionState.Connected) {
            throw BioBeatException.NotConnected()
        }
    }
}

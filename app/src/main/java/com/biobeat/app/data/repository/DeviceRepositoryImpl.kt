package com.biobeat.app.data.repository

import com.biobeat.app.domain.repository.DeviceRepository
import com.biobeat.sdk.BioBeatSdk
import com.biobeat.sdk.connection.ConnectionState
import com.biobeat.sdk.connection.DeviceConnection
import com.biobeat.sdk.exception.BioBeatException
import com.biobeat.sdk.model.DeviceNotification
import com.biobeat.sdk.model.DeviceSettings
import com.biobeat.sdk.model.DeviceStatus
import com.biobeat.sdk.model.EcgSample
import com.biobeat.sdk.model.HeartRateReading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepositoryImpl @Inject constructor() : DeviceRepository {

    private var connection: DeviceConnection? = null
    private var forwardingScope: CoroutineScope? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    override val heartRate: Flow<HeartRateReading>
        get() = connection?.heartRate ?: emptyFlow()

    override val ecgData: Flow<EcgSample>
        get() = connection?.ecgData ?: emptyFlow()

    override val notifications: Flow<DeviceNotification>
        get() = connection?.notifications ?: emptyFlow()

    private val _deviceStatus = MutableStateFlow<DeviceStatus?>(null)
    override val deviceStatus: StateFlow<DeviceStatus?> = _deviceStatus.asStateFlow()

    override suspend fun connect(macAddress: String) {
        val conn = BioBeatSdk.device(macAddress)
        connection = conn
        conn.connect()

        // Forward SDK state to our state
        forwardingScope?.cancel()
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        forwardingScope = scope

        scope.launch {
            conn.connectionState.collect { _connectionState.value = it }
        }
        scope.launch {
            conn.deviceStatus.collect { _deviceStatus.value = it }
        }
    }

    override suspend fun disconnect() {
        forwardingScope?.cancel()
        forwardingScope = null
        connection?.disconnect()
        connection = null
        _connectionState.value = ConnectionState.Disconnected
        _deviceStatus.value = null
    }

    override suspend fun readSettings(): DeviceSettings {
        return connection?.readSettings() ?: throw BioBeatException.NotConnected()
    }

    override suspend fun writeSettings(settings: DeviceSettings) {
        connection?.writeSettings(settings) ?: throw BioBeatException.NotConnected()
    }

    override suspend fun refreshStatus() {
        connection?.refreshStatus() ?: throw BioBeatException.NotConnected()
    }
}

package com.biobeat.app.domain.repository

import com.biobeat.sdk.connection.ConnectionState
import com.biobeat.sdk.model.DeviceNotification
import com.biobeat.sdk.model.DeviceSettings
import com.biobeat.sdk.model.DeviceStatus
import com.biobeat.sdk.model.EcgSample
import com.biobeat.sdk.model.HeartRateReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstracts device communication from the SDK.
 * The data layer wraps [com.biobeat.sdk.connection.DeviceConnection].
 */
interface DeviceRepository {
    val connectionState: StateFlow<ConnectionState>
    val heartRate: Flow<HeartRateReading>
    val ecgData: Flow<EcgSample>
    val notifications: Flow<DeviceNotification>
    val deviceStatus: StateFlow<DeviceStatus?>

    suspend fun connect(macAddress: String)
    suspend fun disconnect()
    suspend fun readSettings(): DeviceSettings
    suspend fun writeSettings(settings: DeviceSettings)
    suspend fun refreshStatus()
}

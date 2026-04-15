package com.biobeat.sdk.connection

import com.biobeat.sdk.exception.BioBeatException
import com.biobeat.sdk.model.DeviceNotification
import com.biobeat.sdk.model.DeviceSettings
import com.biobeat.sdk.model.DeviceStatus
import com.biobeat.sdk.model.EcgSample
import com.biobeat.sdk.model.HeartRateReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents a connection to a single BioBeat wearable device.
 *
 * All [Flow] properties are cold — they begin emitting only when collected,
 * and collection is safe from any coroutine context (dispatching is handled
 * internally by the SDK).
 *
 * Lifecycle: call [connect] to initiate, [disconnect] to tear down.
 * The instance is reusable — [connect] can be called again after [disconnect].
 */
public interface DeviceConnection {

    /** Observe connection state changes. Replays the latest state to new collectors. */
    public val connectionState: StateFlow<ConnectionState>

    /** Live heart rate readings. Emits only while connected and HR service is available. */
    public val heartRate: Flow<HeartRateReading>

    /** Live ECG waveform samples. High-frequency stream (~250 Hz). */
    public val ecgData: Flow<EcgSample>

    /** Device notifications/alerts pushed from the wearable. */
    public val notifications: Flow<DeviceNotification>

    /** Device status (battery, memory). Updated periodically and on-demand via [refreshStatus]. */
    public val deviceStatus: StateFlow<DeviceStatus?>

    /**
     * Initiates the BLE connection, GATT discovery, and notification subscriptions.
     *
     * Suspends until the connection is established or fails.
     * @throws BioBeatException.ConnectionFailed if the connection cannot be established.
     * @throws BioBeatException.BluetoothDisabled if the adapter is off.
     * @throws BioBeatException.PermissionMissing if BLE permissions are not granted.
     */
    public suspend fun connect()

    /**
     * Gracefully disconnects from the device and releases BLE resources.
     */
    public suspend fun disconnect()

    /**
     * Reads current device settings from the wearable.
     * @throws BioBeatException.NotConnected if not currently connected.
     */
    public suspend fun readSettings(): DeviceSettings

    /**
     * Writes device settings to the wearable.
     * @throws BioBeatException.NotConnected if not currently connected.
     * @throws BioBeatException.WriteFailed if the write was not acknowledged.
     */
    public suspend fun writeSettings(settings: DeviceSettings)

    /**
     * Forces an immediate refresh of [deviceStatus].
     * @throws BioBeatException.NotConnected if not currently connected.
     */
    public suspend fun refreshStatus()
}

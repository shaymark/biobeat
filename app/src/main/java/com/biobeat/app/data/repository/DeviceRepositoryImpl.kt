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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.random.Random

@Singleton
class DeviceRepositoryImpl @Inject constructor() : DeviceRepository {

    private var connection: DeviceConnection? = null
    private var forwardingScope: CoroutineScope? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    override val heartRate: Flow<HeartRateReading>
        get() = connection?.heartRate ?: emptyFlow()

    override val ecgData: Flow<EcgSample>
        // Real device data:
        // get() = connection?.ecgData ?: emptyFlow()
        get() = dummyEcgFlow()

    /**
     * Generates a realistic-looking ECG waveform (PQRST complex) for UI testing.
     * 250 Hz sample rate, ~75 BPM (200 samples per beat cycle).
     * Emits batches of 10 samples every 40 ms to mimic real BLE packet timing.
     */
    private fun dummyEcgFlow(): Flow<EcgSample> = flow {
        val template = generateEcgTemplate()
        var templatePos = 0
        var sequenceNumber = 0L
        val batchSize = 10

        while (true) {
            val batch = FloatArray(batchSize) { i ->
                val sample = template[(templatePos + i) % template.size]
                sample + (Random.nextFloat() - 0.5f) * 0.015f // slight baseline noise
            }
            templatePos = (templatePos + batchSize) % template.size

            emit(
                EcgSample(
                    sequenceNumber = sequenceNumber++,
                    millivolts = batch,
                    sampleRateHz = 250,
                    timestampMs = System.currentTimeMillis(),
                )
            )
            delay(40) // 10 samples / 250 Hz = 40 ms
        }
    }

    private fun generateEcgTemplate(): FloatArray {
        val cycleLength = 200 // 200 samples at 250 Hz = 0.8 s per beat ≈ 75 BPM

        fun gaussian(x: Float, center: Float, width: Float, amplitude: Float): Float {
            val t = (x - center) / width
            return amplitude * exp((-t * t) / 2f)
        }

        return FloatArray(cycleLength) { i ->
            val t = i.toFloat()
            gaussian(t, 35f, 6f, 0.15f) +      // P wave
                gaussian(t, 60f, 1.5f, -0.1f) + // Q wave
                gaussian(t, 65f, 2.5f, 1.2f) +  // R wave
                gaussian(t, 70f, 2f, -0.25f) +  // S wave
                gaussian(t, 110f, 10f, 0.3f)    // T wave
        }
    }

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

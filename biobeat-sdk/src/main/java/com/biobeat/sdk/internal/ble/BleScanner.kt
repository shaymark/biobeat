package com.biobeat.sdk.internal.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import com.biobeat.sdk.exception.BioBeatException
import com.biobeat.sdk.model.DeviceInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

/**
 * Scans for nearby BLE devices and emits discovered devices as a Flow.
 */
@SuppressLint("MissingPermission")
internal class BleScanner(private val context: Context) {

    /**
     * Starts a BLE scan that emits [DeviceInfo] for each discovered device.
     * The flow completes after [timeoutMs] or when cancelled.
     */
    fun scan(timeoutMs: Long): Flow<DeviceInfo> = callbackFlow {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter ?: throw BioBeatException.BluetoothDisabled()
        val scanner = adapter.bluetoothLeScanner ?: throw BioBeatException.BluetoothDisabled()

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val deviceInfo = DeviceInfo(
                    name = result.device.name,
                    macAddress = result.device.address,
                    rssi = result.rssi,
                )
                trySend(deviceInfo)
            }

            override fun onScanFailed(errorCode: Int) {
                close(BioBeatException.ConnectionFailed())
            }
        }

        scanner.startScan(null, scanSettings, callback)

        // Auto-stop after timeout
        launch {
            delay(timeoutMs)
            scanner.stopScan(callback)
            close()
        }

        awaitClose {
            scanner.stopScan(callback)
        }
    }
}

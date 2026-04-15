package com.biobeat.sdk.internal.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

/**
 * Single-threaded dispatcher for all BLE operations.
 *
 * Android's BluetoothGatt is not thread-safe — concurrent operations
 * (e.g., writing two characteristics simultaneously) can corrupt state.
 * Serializing all BLE work on one thread eliminates this class of bugs.
 */
internal object BleDispatcher {
    val dispatcher: CoroutineDispatcher =
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "biobeat-ble").apply { isDaemon = true }
        }.asCoroutineDispatcher()
}

package com.biobeat.sdk

import android.content.Context
import com.biobeat.sdk.connection.DeviceConnection
import com.biobeat.sdk.exception.BioBeatException
import com.biobeat.sdk.internal.DeviceConnectionImpl
import com.biobeat.sdk.internal.ble.BleConnectionManager
import com.biobeat.sdk.internal.ble.BleScanner
import com.biobeat.sdk.model.DeviceInfo
import kotlinx.coroutines.flow.Flow

/**
 * Main entry point for the BioBeat SDK.
 *
 * Initialize once per application process, typically in [android.app.Application.onCreate].
 * The SDK does not hold a strong reference to [Context] — it extracts
 * [Context.getApplicationContext] internally.
 *
 * Thread safety: all methods are safe to call from any thread.
 */
public object BioBeatSdk {

    @Volatile
    private var appContext: Context? = null

    @Volatile
    private var config: BioBeatSdkConfig? = null

    /**
     * Initializes the SDK. Must be called before [device] or [scan].
     * Calling multiple times with the same config is a no-op.
     *
     * @param context Any context; applicationContext is extracted internally.
     * @param config Optional configuration overrides.
     * @throws BioBeatException.AlreadyInitialized if called again with a different config.
     */
    public fun init(context: Context, config: BioBeatSdkConfig = BioBeatSdkConfig()) {
        synchronized(this) {
            val existing = this.config
            if (existing != null) {
                if (existing != config) throw BioBeatException.AlreadyInitialized()
                return
            }
            this.appContext = context.applicationContext
            this.config = config
        }
    }

    /**
     * Returns the SDK version string (semver).
     */
    public fun version(): String = BuildConfig.SDK_VERSION

    /**
     * Creates a connection handle for a device identified by its MAC address.
     *
     * The connection is not established until [DeviceConnection.connect] is called.
     *
     * @param macAddress Bluetooth MAC address (e.g., "AA:BB:CC:DD:EE:FF").
     * @return A [DeviceConnection] handle for the device.
     * @throws BioBeatException.NotInitialized if [init] was not called.
     * @throws BioBeatException.InvalidAddress if the address format is invalid.
     */
    public fun device(macAddress: String): DeviceConnection {
        val ctx = appContext ?: throw BioBeatException.NotInitialized()
        val cfg = config ?: throw BioBeatException.NotInitialized()

        val macRegex = Regex("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$")
        if (!macRegex.matches(macAddress)) throw BioBeatException.InvalidAddress(macAddress)

        val connectionManager = BleConnectionManager(ctx, cfg)
        return DeviceConnectionImpl(macAddress, connectionManager)
    }

    /**
     * Scans for nearby BioBeat devices.
     *
     * @param timeoutMs Scan duration in milliseconds.
     * @return A [Flow] that emits discovered [DeviceInfo] objects
     *         and completes when the timeout expires or the [Flow] is cancelled.
     * @throws BioBeatException.NotInitialized if [init] was not called.
     */
    public fun scan(timeoutMs: Long = 10_000L): Flow<DeviceInfo> {
        val ctx = appContext ?: throw BioBeatException.NotInitialized()
        return BleScanner(ctx).scan(timeoutMs)
    }

    /**
     * Releases all SDK resources. After this call, [init] must be called again.
     */
    public fun shutdown() {
        synchronized(this) {
            appContext = null
            config = null
        }
    }
}

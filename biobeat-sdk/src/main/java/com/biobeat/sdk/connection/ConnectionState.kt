package com.biobeat.sdk.connection

import com.biobeat.sdk.exception.BioBeatException

/**
 * Represents the current state of a BLE connection to a device.
 */
public sealed class ConnectionState {

    /** No connection attempt in progress. */
    public data object Disconnected : ConnectionState()

    /** BLE connection is being established. */
    public data object Connecting : ConnectionState()

    /** Connected and GATT services discovered. Ready for data. */
    public data object Connected : ConnectionState()

    /** Connection lost unexpectedly. SDK is attempting to reconnect. */
    public data class Reconnecting(
        val attempt: Int,
        val maxAttempts: Int,
    ) : ConnectionState()

    /** A terminal failure occurred. */
    public data class Failed(val error: BioBeatException) : ConnectionState()
}

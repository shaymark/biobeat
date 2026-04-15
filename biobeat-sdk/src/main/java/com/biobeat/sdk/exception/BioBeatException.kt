package com.biobeat.sdk.exception

/**
 * Base exception for all BioBeat SDK errors.
 */
public sealed class BioBeatException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {

    /** [com.biobeat.sdk.BioBeatSdk.init] was not called before using the SDK. */
    public class NotInitialized : BioBeatException("BioBeatSdk.init() was not called")

    /** SDK was already initialized with a different configuration. */
    public class AlreadyInitialized : BioBeatException("SDK already initialized with different config")

    /** The provided MAC address is not valid. */
    public class InvalidAddress(address: String) : BioBeatException("Invalid MAC address: $address")

    /** The Bluetooth adapter is disabled. */
    public class BluetoothDisabled : BioBeatException("Bluetooth adapter is disabled")

    /** A required BLE permission has not been granted. */
    public class PermissionMissing(
        public val permission: String,
    ) : BioBeatException("Missing permission: $permission")

    /** The BLE connection could not be established. */
    public class ConnectionFailed(cause: Throwable? = null) : BioBeatException("Connection failed", cause)

    /** Operation requires an active connection but the device is not connected. */
    public class NotConnected : BioBeatException("Device is not connected")

    /** A GATT write operation was not acknowledged by the device. */
    public class WriteFailed(detail: String) : BioBeatException("Write failed: $detail")

    /** A GATT operation returned an error status code. */
    public class GattError(
        public val status: Int,
    ) : BioBeatException("GATT error status=$status")
}

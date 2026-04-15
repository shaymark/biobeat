package com.biobeat.sdk

/**
 * Configuration for [BioBeatSdk] initialization.
 *
 * @property autoReconnect Whether to automatically reconnect on unexpected disconnection.
 * @property reconnectDelayMs Base delay between reconnection attempts (exponential backoff).
 * @property maxReconnectAttempts Maximum reconnect attempts before giving up. 0 = infinite.
 * @property logLevel Logging verbosity.
 */
public data class BioBeatSdkConfig(
    val autoReconnect: Boolean = true,
    val reconnectDelayMs: Long = 2_000L,
    val maxReconnectAttempts: Int = 5,
    val logLevel: LogLevel = LogLevel.WARN,
) {
    public enum class LogLevel {
        NONE,
        ERROR,
        WARN,
        INFO,
        DEBUG,
    }
}

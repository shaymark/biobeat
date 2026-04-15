package com.biobeat.sdk.internal.ble

import com.biobeat.sdk.BioBeatSdkConfig
import kotlin.math.min
import kotlin.math.pow

/**
 * Determines whether and when to attempt reconnection after an unexpected disconnect.
 */
internal class ReconnectionPolicy(private val config: BioBeatSdkConfig) {

    /**
     * Returns true if another reconnection attempt should be made.
     *
     * @param attempt Zero-based attempt number.
     */
    fun shouldReconnect(attempt: Int): Boolean =
        config.autoReconnect &&
            (config.maxReconnectAttempts == 0 || attempt < config.maxReconnectAttempts)

    /**
     * Returns the delay in milliseconds before the given attempt.
     * Uses exponential backoff capped at ~64x the base delay.
     */
    fun delayForAttempt(attempt: Int): Long {
        val exponent = min(attempt, 6)
        return (config.reconnectDelayMs * 2.0.pow(exponent)).toLong()
    }
}

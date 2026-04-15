package com.biobeat.sdk.internal.ble

import com.biobeat.sdk.BioBeatSdkConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReconnectionPolicyTest {

    @Test
    fun `shouldReconnect returns true within max attempts`() {
        val policy = ReconnectionPolicy(
            BioBeatSdkConfig(maxReconnectAttempts = 5)
        )

        assertTrue(policy.shouldReconnect(0))
        assertTrue(policy.shouldReconnect(4))
        assertFalse(policy.shouldReconnect(5))
    }

    @Test
    fun `shouldReconnect returns false when autoReconnect is disabled`() {
        val policy = ReconnectionPolicy(
            BioBeatSdkConfig(autoReconnect = false)
        )

        assertFalse(policy.shouldReconnect(0))
    }

    @Test
    fun `shouldReconnect with infinite attempts`() {
        val policy = ReconnectionPolicy(
            BioBeatSdkConfig(maxReconnectAttempts = 0) // 0 = infinite
        )

        assertTrue(policy.shouldReconnect(0))
        assertTrue(policy.shouldReconnect(100))
        assertTrue(policy.shouldReconnect(999))
    }

    @Test
    fun `delayForAttempt uses exponential backoff`() {
        val policy = ReconnectionPolicy(
            BioBeatSdkConfig(reconnectDelayMs = 1000)
        )

        assertEquals(1000L, policy.delayForAttempt(0))  // 1000 * 2^0
        assertEquals(2000L, policy.delayForAttempt(1))  // 1000 * 2^1
        assertEquals(4000L, policy.delayForAttempt(2))  // 1000 * 2^2
        assertEquals(8000L, policy.delayForAttempt(3))  // 1000 * 2^3
    }

    @Test
    fun `delayForAttempt caps at 2 power 6`() {
        val policy = ReconnectionPolicy(
            BioBeatSdkConfig(reconnectDelayMs = 1000)
        )

        val delay6 = policy.delayForAttempt(6)   // 1000 * 2^6 = 64000
        val delay10 = policy.delayForAttempt(10)  // capped at 2^6

        assertEquals(64000L, delay6)
        assertEquals(64000L, delay10) // Same as attempt 6 due to cap
    }
}

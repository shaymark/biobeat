package com.biobeat.app.domain.usecase

import com.biobeat.app.domain.model.HealthAlert
import com.biobeat.sdk.model.HeartRateReading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CheckHealthAlertUseCaseTest {

    private val useCase = CheckHealthAlertUseCase()

    @Test
    fun `no alert for normal heart rate`() {
        val reading = HeartRateReading(bpm = 75, sensorContact = true, rrIntervals = emptyList(), timestampMs = 0)
        assertNull(useCase(reading))
    }

    @Test
    fun `high heart rate triggers alert`() {
        val reading = HeartRateReading(bpm = 190, sensorContact = true, rrIntervals = emptyList(), timestampMs = 0)
        val alert = useCase(reading)

        assertNotNull(alert)
        assertEquals(HealthAlert.AlertType.HIGH_HEART_RATE, alert!!.type)
        assertEquals(190, alert.bpm)
    }

    @Test
    fun `low heart rate with sensor contact triggers alert`() {
        val reading = HeartRateReading(bpm = 35, sensorContact = true, rrIntervals = emptyList(), timestampMs = 0)
        val alert = useCase(reading)

        assertNotNull(alert)
        assertEquals(HealthAlert.AlertType.LOW_HEART_RATE, alert!!.type)
    }

    @Test
    fun `low heart rate without sensor contact does not trigger alert`() {
        val reading = HeartRateReading(bpm = 35, sensorContact = false, rrIntervals = emptyList(), timestampMs = 0)
        assertNull(useCase(reading))
    }

    @Test
    fun `boundary value 180 does not trigger high alert`() {
        val reading = HeartRateReading(bpm = 180, sensorContact = true, rrIntervals = emptyList(), timestampMs = 0)
        assertNull(useCase(reading))
    }

    @Test
    fun `boundary value 40 does not trigger low alert`() {
        val reading = HeartRateReading(bpm = 40, sensorContact = true, rrIntervals = emptyList(), timestampMs = 0)
        assertNull(useCase(reading))
    }
}

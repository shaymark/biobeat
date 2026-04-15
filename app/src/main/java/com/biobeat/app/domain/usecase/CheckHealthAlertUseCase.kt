package com.biobeat.app.domain.usecase

import com.biobeat.app.domain.model.HealthAlert
import com.biobeat.sdk.model.HeartRateReading
import javax.inject.Inject

/**
 * Checks a heart rate reading against health thresholds and returns an alert if triggered.
 */
class CheckHealthAlertUseCase @Inject constructor() {

    companion object {
        private const val HIGH_HR_THRESHOLD = 180
        private const val LOW_HR_THRESHOLD = 40
    }

    operator fun invoke(reading: HeartRateReading): HealthAlert? {
        return when {
            reading.bpm > HIGH_HR_THRESHOLD -> HealthAlert(
                type = HealthAlert.AlertType.HIGH_HEART_RATE,
                message = "Heart rate is dangerously high: ${reading.bpm} BPM",
                bpm = reading.bpm,
                timestampMs = reading.timestampMs,
            )

            reading.bpm < LOW_HR_THRESHOLD && reading.sensorContact -> HealthAlert(
                type = HealthAlert.AlertType.LOW_HEART_RATE,
                message = "Heart rate is critically low: ${reading.bpm} BPM",
                bpm = reading.bpm,
                timestampMs = reading.timestampMs,
            )

            else -> null
        }
    }
}

package com.biobeat.app.domain.model

/**
 * An app-generated health alert based on analysis of device data.
 */
data class HealthAlert(
    val type: AlertType,
    val message: String,
    val bpm: Int,
    val timestampMs: Long,
) {
    enum class AlertType {
        HIGH_HEART_RATE,
        LOW_HEART_RATE,
        IRREGULAR_RHYTHM,
    }
}

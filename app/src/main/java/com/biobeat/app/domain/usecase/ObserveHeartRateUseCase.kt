package com.biobeat.app.domain.usecase

import com.biobeat.app.domain.model.HeartRateRecord
import com.biobeat.app.domain.repository.DeviceRepository
import com.biobeat.app.domain.repository.ReadingRepository
import com.biobeat.sdk.model.HeartRateReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Observes live heart rate from the device and persists each reading to the database.
 */
class ObserveHeartRateUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val readingRepository: ReadingRepository,
) {
    operator fun invoke(deviceMac: String): Flow<HeartRateReading> =
        deviceRepository.heartRate.onEach { reading ->
            readingRepository.saveHeartRate(
                HeartRateRecord(
                    bpm = reading.bpm,
                    sensorContact = reading.sensorContact,
                    rrIntervals = reading.rrIntervals,
                    deviceMac = deviceMac,
                    timestampMs = reading.timestampMs,
                )
            )
        }
}

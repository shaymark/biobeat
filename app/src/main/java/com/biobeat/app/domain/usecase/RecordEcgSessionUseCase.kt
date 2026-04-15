package com.biobeat.app.domain.usecase

import com.biobeat.app.domain.repository.DeviceRepository
import com.biobeat.app.domain.repository.ReadingRepository
import com.biobeat.sdk.model.EcgSample
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

/**
 * Streams ECG data from the device while managing a recording session in the database.
 */
class RecordEcgSessionUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val readingRepository: ReadingRepository,
) {
    private var currentSessionId: Long? = null

    operator fun invoke(deviceMac: String, sampleRateHz: Int = 250): Flow<EcgSample> =
        deviceRepository.ecgData
            .onStart {
                currentSessionId = readingRepository.startEcgSession(deviceMac, sampleRateHz)
            }
            .onCompletion {
                currentSessionId?.let { readingRepository.endEcgSession(it) }
                currentSessionId = null
            }
}

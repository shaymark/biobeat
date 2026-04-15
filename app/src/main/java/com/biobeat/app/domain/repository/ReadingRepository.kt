package com.biobeat.app.domain.repository

import com.biobeat.app.domain.model.EcgSession
import com.biobeat.app.domain.model.HeartRateRecord
import kotlinx.coroutines.flow.Flow

/**
 * Abstracts local storage of health readings.
 */
interface ReadingRepository {
    fun getHeartRateHistory(deviceMac: String): Flow<List<HeartRateRecord>>
    suspend fun saveHeartRate(record: HeartRateRecord)

    fun getEcgSessions(deviceMac: String): Flow<List<EcgSession>>
    suspend fun startEcgSession(deviceMac: String, sampleRateHz: Int): Long
    suspend fun endEcgSession(sessionId: Long)
}

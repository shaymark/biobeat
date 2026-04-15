package com.biobeat.app.data.repository

import com.biobeat.app.data.local.dao.EcgSessionDao
import com.biobeat.app.data.local.dao.HeartRateDao
import com.biobeat.app.data.local.entity.EcgSessionEntity
import com.biobeat.app.data.mapper.toDomain
import com.biobeat.app.data.mapper.toEntity
import com.biobeat.app.domain.model.EcgSession
import com.biobeat.app.domain.model.HeartRateRecord
import com.biobeat.app.domain.repository.ReadingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingRepositoryImpl @Inject constructor(
    private val heartRateDao: HeartRateDao,
    private val ecgSessionDao: EcgSessionDao,
) : ReadingRepository {

    override fun getHeartRateHistory(deviceMac: String): Flow<List<HeartRateRecord>> =
        heartRateDao.getByDevice(deviceMac).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun saveHeartRate(record: HeartRateRecord) {
        heartRateDao.insert(record.toEntity())
    }

    override fun getEcgSessions(deviceMac: String): Flow<List<EcgSession>> =
        ecgSessionDao.getByDevice(deviceMac).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun startEcgSession(deviceMac: String, sampleRateHz: Int): Long =
        ecgSessionDao.insert(
            EcgSessionEntity(
                deviceMac = deviceMac,
                startTimeMs = System.currentTimeMillis(),
                endTimeMs = null,
                sampleCount = 0,
                sampleRateHz = sampleRateHz,
            )
        )

    override suspend fun endEcgSession(sessionId: Long) {
        ecgSessionDao.endSession(sessionId, System.currentTimeMillis())
    }
}

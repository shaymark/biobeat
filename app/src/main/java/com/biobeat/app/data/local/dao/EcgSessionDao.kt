package com.biobeat.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.biobeat.app.data.local.entity.EcgSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EcgSessionDao {

    @Insert
    suspend fun insert(entity: EcgSessionEntity): Long

    @Query("UPDATE ecg_sessions SET endTimeMs = :endTimeMs WHERE id = :sessionId")
    suspend fun endSession(sessionId: Long, endTimeMs: Long)

    @Query("SELECT * FROM ecg_sessions WHERE deviceMac = :deviceMac ORDER BY startTimeMs DESC")
    fun getByDevice(deviceMac: String): Flow<List<EcgSessionEntity>>
}

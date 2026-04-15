package com.biobeat.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.biobeat.app.data.local.entity.HeartRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartRateDao {

    @Insert
    suspend fun insert(entity: HeartRateEntity)

    @Query("SELECT * FROM heart_rate_readings WHERE deviceMac = :deviceMac ORDER BY timestampMs DESC")
    fun getByDevice(deviceMac: String): Flow<List<HeartRateEntity>>

    @Query("SELECT * FROM heart_rate_readings WHERE deviceMac = :deviceMac ORDER BY timestampMs DESC LIMIT :limit")
    fun getRecent(deviceMac: String, limit: Int): Flow<List<HeartRateEntity>>
}

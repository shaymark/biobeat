package com.biobeat.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.biobeat.app.data.local.dao.EcgSessionDao
import com.biobeat.app.data.local.dao.HeartRateDao
import com.biobeat.app.data.local.entity.EcgSessionEntity
import com.biobeat.app.data.local.entity.HeartRateEntity

@Database(
    entities = [HeartRateEntity::class, EcgSessionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class BioBeatDatabase : RoomDatabase() {
    abstract fun heartRateDao(): HeartRateDao
    abstract fun ecgSessionDao(): EcgSessionDao
}

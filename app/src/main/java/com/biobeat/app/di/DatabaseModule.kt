package com.biobeat.app.di

import android.content.Context
import androidx.room.Room
import com.biobeat.app.data.local.BioBeatDatabase
import com.biobeat.app.data.local.dao.EcgSessionDao
import com.biobeat.app.data.local.dao.HeartRateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BioBeatDatabase =
        Room.databaseBuilder(
            context,
            BioBeatDatabase::class.java,
            "biobeat.db",
        ).build()

    @Provides
    fun provideHeartRateDao(db: BioBeatDatabase): HeartRateDao = db.heartRateDao()

    @Provides
    fun provideEcgSessionDao(db: BioBeatDatabase): EcgSessionDao = db.ecgSessionDao()
}

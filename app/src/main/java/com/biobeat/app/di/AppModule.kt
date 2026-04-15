package com.biobeat.app.di

import com.biobeat.app.data.repository.DeviceRepositoryImpl
import com.biobeat.app.data.repository.ReadingRepositoryImpl
import com.biobeat.app.domain.repository.DeviceRepository
import com.biobeat.app.domain.repository.ReadingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(impl: DeviceRepositoryImpl): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindReadingRepository(impl: ReadingRepositoryImpl): ReadingRepository
}

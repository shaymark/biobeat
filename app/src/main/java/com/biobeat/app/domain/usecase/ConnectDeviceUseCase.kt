package com.biobeat.app.domain.usecase

import com.biobeat.app.domain.repository.DeviceRepository
import javax.inject.Inject

class ConnectDeviceUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
) {
    suspend operator fun invoke(macAddress: String) {
        deviceRepository.connect(macAddress)
    }
}

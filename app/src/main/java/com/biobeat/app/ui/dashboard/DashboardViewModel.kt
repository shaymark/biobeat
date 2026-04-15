package com.biobeat.app.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobeat.app.domain.model.HealthAlert
import com.biobeat.app.domain.repository.DeviceRepository
import com.biobeat.app.domain.usecase.CheckHealthAlertUseCase
import com.biobeat.app.domain.usecase.ConnectDeviceUseCase
import com.biobeat.app.domain.usecase.ObserveHeartRateUseCase
import com.biobeat.sdk.connection.ConnectionState
import com.biobeat.sdk.model.DeviceStatus
import com.biobeat.sdk.model.HeartRateReading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val connectDevice: ConnectDeviceUseCase,
    private val observeHeartRate: ObserveHeartRateUseCase,
    private val checkHealthAlert: CheckHealthAlertUseCase,
    private val deviceRepository: DeviceRepository,
) : ViewModel() {

    val macAddress: String = savedStateHandle["macAddress"]!!

    val connectionState: StateFlow<ConnectionState> = deviceRepository.connectionState
    val deviceStatus: StateFlow<DeviceStatus?> = deviceRepository.deviceStatus

    private val _latestHeartRate = MutableStateFlow<HeartRateReading?>(null)
    val latestHeartRate: StateFlow<HeartRateReading?> = _latestHeartRate.asStateFlow()

    private val _latestAlert = MutableStateFlow<HealthAlert?>(null)
    val latestAlert: StateFlow<HealthAlert?> = _latestAlert.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        connect()
    }

    fun connect() {
        viewModelScope.launch {
            try {
                _error.value = null
                connectDevice(macAddress)
                observeHeartRateStream()
            } catch (e: Exception) {
                _error.value = e.message ?: "Connection failed"
            }
        }
    }

    private fun observeHeartRateStream() {
        viewModelScope.launch {
            observeHeartRate(macAddress).collect { reading ->
                _latestHeartRate.value = reading
                _latestAlert.value = checkHealthAlert(reading)
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            deviceRepository.disconnect()
        }
    }
}

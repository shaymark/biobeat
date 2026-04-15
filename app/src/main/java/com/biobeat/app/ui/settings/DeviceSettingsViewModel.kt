package com.biobeat.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobeat.app.domain.repository.DeviceRepository
import com.biobeat.sdk.model.DeviceSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceSettingsViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
) : ViewModel() {

    private val _settings = MutableStateFlow<DeviceSettings?>(null)
    val settings: StateFlow<DeviceSettings?> = _settings.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            try {
                _settings.value = deviceRepository.readSettings()
            } catch (e: Exception) {
                _message.value = "Failed to read settings: ${e.message}"
            }
        }
    }

    fun updateSettings(settings: DeviceSettings) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                deviceRepository.writeSettings(settings)
                _settings.value = settings
                _message.value = "Settings saved"
            } catch (e: Exception) {
                _message.value = "Failed to save: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

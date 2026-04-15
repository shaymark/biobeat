package com.biobeat.app.ui.ecg

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobeat.app.domain.repository.DeviceRepository
import com.biobeat.app.domain.usecase.RecordEcgSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EcgViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recordEcgSession: RecordEcgSessionUseCase,
    private val deviceRepository: DeviceRepository,
) : ViewModel() {

    private val macAddress: String = savedStateHandle["macAddress"]!!

    companion object {
        private const val BUFFER_SIZE = 1000 // ~4 seconds at 250Hz
    }

    private val _waveformBuffer = MutableStateFlow(FloatArray(BUFFER_SIZE))
    val waveformBuffer: StateFlow<FloatArray> = _waveformBuffer.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    init {
        startStreaming()
    }

    private fun startStreaming() {
        viewModelScope.launch {
            _isRecording.value = true
            recordEcgSession(macAddress).collect { sample ->
                _waveformBuffer.update { buffer ->
                    val new = buffer.copyOf()
                    val shift = sample.millivolts.size.coerceAtMost(new.size)
                    System.arraycopy(new, shift, new, 0, new.size - shift)
                    sample.millivolts.copyInto(
                        new,
                        new.size - shift,
                        0,
                        shift,
                    )
                    new
                }
            }
        }
    }
}

package com.biobeat.app.ui.ecg

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobeat.app.domain.usecase.RecordEcgSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EcgWaveformState(
    val buffer: FloatArray = FloatArray(0),
    val writePosition: Int = 0,
    val samplesWritten: Int = 0,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EcgWaveformState) return false
        return buffer.contentEquals(other.buffer) &&
            writePosition == other.writePosition &&
            samplesWritten == other.samplesWritten
    }

    override fun hashCode(): Int {
        var result = buffer.contentHashCode()
        result = 31 * result + writePosition
        result = 31 * result + samplesWritten
        return result
    }
}

@HiltViewModel
class EcgViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recordEcgSession: RecordEcgSessionUseCase,
) : ViewModel() {

    private val macAddress: String = savedStateHandle["macAddress"]!!

    companion object {
        const val BUFFER_SIZE = 1000 // ~4 seconds at 250Hz
    }

    private val _waveformState = MutableStateFlow(
        EcgWaveformState(buffer = FloatArray(BUFFER_SIZE))
    )
    val waveformState: StateFlow<EcgWaveformState> = _waveformState.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    init {
        startStreaming()
    }

    private fun startStreaming() {
        viewModelScope.launch {
            _isRecording.value = true
            recordEcgSession(macAddress).collect { sample ->
                _waveformState.update { state ->
                    val buf = state.buffer.copyOf()
                    var pos = state.writePosition
                    var written = state.samplesWritten
                    for (mv in sample.millivolts) {
                        buf[pos] = mv
                        pos = (pos + 1) % BUFFER_SIZE
                        if (written < BUFFER_SIZE) written++
                    }
                    EcgWaveformState(buf, pos, written)
                }
            }
        }
    }
}

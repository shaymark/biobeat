package com.biobeat.app.ui.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.biobeat.app.domain.model.EcgSession
import com.biobeat.app.domain.model.HeartRateRecord
import com.biobeat.app.domain.repository.ReadingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    readingRepository: ReadingRepository,
) : ViewModel() {

    private val macAddress: String = savedStateHandle["macAddress"]!!

    val heartRateHistory: Flow<List<HeartRateRecord>> =
        readingRepository.getHeartRateHistory(macAddress)

    val ecgSessions: Flow<List<EcgSession>> =
        readingRepository.getEcgSessions(macAddress)
}

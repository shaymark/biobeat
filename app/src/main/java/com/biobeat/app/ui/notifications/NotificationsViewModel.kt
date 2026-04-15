package com.biobeat.app.ui.notifications

import androidx.lifecycle.ViewModel
import com.biobeat.app.domain.repository.DeviceRepository
import com.biobeat.sdk.model.DeviceNotification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<DeviceNotification>>(emptyList())
    val notifications: StateFlow<List<DeviceNotification>> = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            deviceRepository.notifications.collect { notification ->
                _notifications.update { list -> listOf(notification) + list }
            }
        }
    }
}

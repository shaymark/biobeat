package com.biobeat.app.ui.scan

import android.Manifest
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobeat.sdk.BioBeatSdk
import com.biobeat.sdk.model.DeviceInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor() : ViewModel() {

    private val _devices = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val devices: StateFlow<List<DeviceInfo>> = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _permissionDenied = MutableStateFlow(false)
    val permissionDenied: StateFlow<Boolean> = _permissionDenied.asStateFlow()

    fun startScan() {
        if (_isScanning.value) return
        _devices.value = emptyList()
        _permissionDenied.value = false

        viewModelScope.launch {
            _isScanning.value = true
            BioBeatSdk.scan(timeoutMs = 15_000L)
                .onCompletion { _isScanning.value = false }
                .collect { device ->
                    _devices.update { list ->
                        val filtered = list.filter { it.macAddress != device.macAddress }
                        (filtered + device).sortedByDescending { it.rssi }
                    }
                }
        }
    }

    fun onPermissionDenied() {
        _permissionDenied.value = true
    }

    companion object {
        val requiredPermissions: Array<String> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
            } else {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            }
    }
}

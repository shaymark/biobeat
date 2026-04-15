package com.biobeat.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biobeat.app.ui.components.BatteryIndicator
import com.biobeat.app.ui.components.ConnectionStatusBar
import com.biobeat.app.ui.components.HeartRateCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToEcg: (String) -> Unit,
    onNavigateToHistory: (String) -> Unit,
    onNavigateToNotifications: (String) -> Unit,
    onNavigateToSettings: (String) -> Unit,
    onDisconnect: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val deviceStatus by viewModel.deviceStatus.collectAsStateWithLifecycle()
    val heartRate by viewModel.latestHeartRate.collectAsStateWithLifecycle()
    val alert by viewModel.latestAlert.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { onNavigateToNotifications(viewModel.macAddress) }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { onNavigateToSettings(viewModel.macAddress) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ConnectionStatusBar(state = connectionState)

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Heart Rate
                HeartRateCard(
                    bpm = heartRate?.bpm,
                    sensorContact = heartRate?.sensorContact ?: false,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Health Alert
                alert?.let { healthAlert ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                    ) {
                        Text(
                            text = healthAlert.message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                // Device Status
                deviceStatus?.let { status ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Device Status", style = MaterialTheme.typography.titleMedium)
                                BatteryIndicator(percent = status.batteryPercent, isCharging = status.isCharging)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Firmware: ${status.firmwareVersion}", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            val memoryUsedMb = status.memoryUsedBytes / (1024 * 1024)
                            val memoryTotalMb = status.memoryTotalBytes / (1024 * 1024)
                            Text("Memory: ${memoryUsedMb}MB / ${memoryTotalMb}MB", style = MaterialTheme.typography.bodySmall)
                            if (status.memoryTotalBytes > 0) {
                                LinearProgressIndicator(
                                    progress = { status.memoryUsedBytes.toFloat() / status.memoryTotalBytes },
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }

                // Quick Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    TextButton(onClick = { onNavigateToEcg(viewModel.macAddress) }) {
                        Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null)
                        Text("  ECG")
                    }
                    TextButton(onClick = { onNavigateToHistory(viewModel.macAddress) }) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Text("  History")
                    }
                }

                // Error
                error?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    TextButton(onClick = { viewModel.connect() }) {
                        Text("Retry")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = {
                        viewModel.disconnect()
                        onDisconnect()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("Disconnect")
                }
            }
        }
    }
}

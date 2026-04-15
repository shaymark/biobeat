package com.biobeat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.biobeat.sdk.connection.ConnectionState

@Composable
fun ConnectionStatusBar(
    state: ConnectionState,
    modifier: Modifier = Modifier,
) {
    val (text, bgColor) = when (state) {
        is ConnectionState.Connected -> "Connected" to Color(0xFF2E7D32)
        is ConnectionState.Connecting -> "Connecting..." to Color(0xFFF57F17)
        is ConnectionState.Reconnecting -> "Reconnecting (${state.attempt}/${state.maxAttempts})..." to Color(0xFFE65100)
        is ConnectionState.Disconnected -> "Disconnected" to Color(0xFF757575)
        is ConnectionState.Failed -> "Connection Failed" to MaterialTheme.colorScheme.error
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

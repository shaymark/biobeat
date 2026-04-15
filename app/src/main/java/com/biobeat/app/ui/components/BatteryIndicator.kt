package com.biobeat.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery0Bar
import androidx.compose.material.icons.filled.Battery2Bar
import androidx.compose.material.icons.filled.Battery4Bar
import androidx.compose.material.icons.filled.Battery6Bar
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BatteryIndicator(
    percent: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier,
) {
    val icon = when {
        isCharging -> Icons.Default.BatteryChargingFull
        percent > 85 -> Icons.Default.BatteryFull
        percent > 60 -> Icons.Default.Battery6Bar
        percent > 35 -> Icons.Default.Battery4Bar
        percent > 15 -> Icons.Default.Battery2Bar
        else -> Icons.Default.Battery0Bar
    }

    val color = when {
        isCharging -> Color.Green
        percent > 20 -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.error
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = "Battery", tint = color)
        Text(
            text = "$percent%",
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = color,
        )
    }
}

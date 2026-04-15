package com.biobeat.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biobeat.app.ui.theme.HeartRed

@Composable
fun HeartRateCard(
    bpm: Int?,
    sensorContact: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = "Heart Rate",
                tint = HeartRed,
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = if (bpm != null) "$bpm" else "--",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp),
                )
                Text(
                    text = if (sensorContact) "BPM" else "No contact",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

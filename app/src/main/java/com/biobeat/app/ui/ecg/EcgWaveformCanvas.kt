package com.biobeat.app.ui.ecg

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.biobeat.app.ui.theme.EcgGreen

@Composable
fun EcgWaveformCanvas(
    waveformData: FloatArray,
    modifier: Modifier = Modifier,
    lineColor: Color = EcgGreen,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
    ) {
        if (waveformData.isEmpty()) return@Canvas

        val stepX = size.width / waveformData.size
        val centerY = size.height / 2f
        val scaleY = size.height * 0.4f

        // Draw grid lines
        val gridColor = Color.DarkGray.copy(alpha = 0.3f)
        for (i in 1..4) {
            val y = size.height * i / 5f
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.5f)
        }
        // Center line
        drawLine(gridColor.copy(alpha = 0.5f), Offset(0f, centerY), Offset(size.width, centerY), strokeWidth = 1f)

        // Draw waveform
        val path = Path()
        waveformData.forEachIndexed { index, mv ->
            val x = index * stepX
            val y = centerY - (mv * scaleY)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(path, color = lineColor, style = Stroke(width = 2f))
    }
}

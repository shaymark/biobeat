package com.biobeat.app.ui.ecg

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.biobeat.app.ui.theme.EcgGreen

@Composable
fun EcgWaveformCanvas(
    waveformState: EcgWaveformState,
    modifier: Modifier = Modifier,
    lineColor: Color = EcgGreen,
) {
    val buffer = waveformState.buffer
    val writePos = waveformState.writePosition
    val samplesWritten = waveformState.samplesWritten

    Canvas(modifier = modifier.fillMaxSize()) {
        val bufferSize = buffer.size
        if (bufferSize == 0) return@Canvas

        val fadeLength = bufferSize / 2 // 500 samples = 2 seconds = 50% of screen
        val stepX = size.width / bufferSize
        val centerY = size.height / 2f
        val scaleY = size.height * 0.4f

        // --- Grid ---
        val gridColor = Color.DarkGray.copy(alpha = 0.25f)
        val gridColorBright = Color.DarkGray.copy(alpha = 0.4f)

        // Horizontal grid lines (voltage reference)
        for (i in 1..4) {
            val y = size.height * i / 5f
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.5f)
        }
        // Center line
        drawLine(gridColorBright, Offset(0f, centerY), Offset(size.width, centerY), strokeWidth = 0.5f)

        // Vertical grid lines: every 250 samples (1 second) = bright, every 50 samples (0.2s) = dim
        for (i in 1 until bufferSize) {
            if (i % 50 == 0) {
                val x = i * stepX
                val color = if (i % 250 == 0) gridColorBright else gridColor
                val width = if (i % 250 == 0) 0.5f else 0.3f
                drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = width)
            }
        }

        if (samplesWritten == 0) return@Canvas

        // --- Waveform with fade ---
        fun alphaAt(index: Int): Float {
            val age = (writePos - 1 - index + bufferSize * 2) % bufferSize
            if (age >= samplesWritten) return 0f
            return if (age < fadeLength) 1f - age.toFloat() / fadeLength else 0f
        }

        // Draw line segments between consecutive screen positions
        for (i in 0 until bufferSize - 1) {
            val a1 = alphaAt(i)
            val a2 = alphaAt(i + 1)
            if (a1 > 0f && a2 > 0f) {
                val segmentAlpha = maxOf(a1, a2)
                drawLine(
                    color = lineColor.copy(alpha = segmentAlpha),
                    start = Offset(i * stepX, centerY - buffer[i] * scaleY),
                    end = Offset((i + 1) * stepX, centerY - buffer[i + 1] * scaleY),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round,
                )
            }
        }

        // --- Big dot at latest sample (phosphor glow) ---
        val latestIdx = (writePos - 1 + bufferSize) % bufferSize
        val dotX = latestIdx * stepX
        val dotY = centerY - buffer[latestIdx] * scaleY
        val dotCenter = Offset(dotX, dotY)

        // Outer glow
        drawCircle(color = lineColor.copy(alpha = 0.25f), radius = 14f, center = dotCenter)
        // Core
        drawCircle(color = lineColor, radius = 6f, center = dotCenter)
        // Bright center
        drawCircle(color = Color.White, radius = 2.5f, center = dotCenter)
    }
}

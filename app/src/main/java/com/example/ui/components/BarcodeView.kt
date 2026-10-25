package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/**
 * Renders a stylized, clean visual barcode representation for any barcode string.
 */
@Composable
fun BarcodeView(
    barcode: String,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF0F172A),
    backgroundColor: Color = Color.White,
    showText: Boolean = true
) {
    // Generate deterministic bar widths based on characters in barcode
    val barPattern = remember(barcode) {
        val pattern = mutableListOf<Float>()
        val clean = if (barcode.isEmpty()) "0000000000" else barcode
        // Guard patterns at ends (thin, thin, thin)
        pattern.add(1.5f); pattern.add(1f); pattern.add(1.5f); pattern.add(1f)
        for (ch in clean) {
            val v = abs(ch.code) % 5 + 1
            when (v) {
                1 -> { pattern.add(1.2f); pattern.add(1.0f); pattern.add(2.4f); pattern.add(1.2f) }
                2 -> { pattern.add(2.4f); pattern.add(1.2f); pattern.add(1.2f); pattern.add(1.8f) }
                3 -> { pattern.add(1.8f); pattern.add(2.2f); pattern.add(1.0f); pattern.add(1.5f) }
                4 -> { pattern.add(3.0f); pattern.add(1.0f); pattern.add(1.5f); pattern.add(1.2f) }
                else -> { pattern.add(1.5f); pattern.add(2.0f); pattern.add(2.5f); pattern.add(1.0f) }
            }
        }
        // End guard
        pattern.add(1.5f); pattern.add(1f); pattern.add(1.5f)
        pattern
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                val totalRelativeUnits = barPattern.sum()
                val scale = size.width / totalRelativeUnits
                var currentX = 0f
                var isBar = true

                for (unit in barPattern) {
                    val barWidth = unit * scale
                    if (isBar) {
                        drawLine(
                            color = barColor,
                            start = Offset(currentX + barWidth / 2, 0f),
                            end = Offset(currentX + barWidth / 2, size.height),
                            strokeWidth = barWidth
                        )
                    }
                    currentX += barWidth
                    isBar = !isBar
                }
            }

            if (showText && barcode.isNotEmpty()) {
                Text(
                    text = barcode,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = barColor.copy(alpha = 0.85f),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

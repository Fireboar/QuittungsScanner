package com.example.quittungsscanner.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun SimplePieChart(
    entries: List<PieChartDataEntry>,
    modifier: Modifier = Modifier
) {
    val total = entries.sumOf { it.value.toDouble() }.toFloat()
    val proportions = entries.map { it.value / total }
    val sweepAngles = proportions.map { 360 * it }

    Canvas(modifier = modifier) {
        var startAngle = -90f
        for ((index, angle) in sweepAngles.withIndex()) {
            drawArc(
                color = entries[index].color,
                startAngle = startAngle,
                sweepAngle = angle,
                useCenter = true
            )
            startAngle += angle
        }
    }
}

data class PieChartDataEntry(
    val value: Float,
    val color: Color,
    val label: String
)
package com.example.quittungsscanner.ui.components

import androidx.compose.ui.graphics.Color

object ChartColors {
    // Base colors for the chart
    private val baseColors = listOf(
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFFFC107), // Amber
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF00BCD4), // Cyan
        Color(0xFFFF9800), // Orange
        Color(0xFF795548), // Brown
        Color(0xFF607D8B), // Blue Grey
        Color(0xFF8BC34A)  // Light Green
    )

    // Generate colors for the chart based on the number of categories
    fun generateColors(count: Int): List<Color> {
        if (count <= 0) return emptyList()
        
        // If we have fewer categories than base colors, use a subset
        if (count <= baseColors.size) {
            return baseColors.take(count)
        }
        
        // If we need more colors than base colors, generate variations
        return List(count) { index ->
            val baseColor = baseColors[index % baseColors.size]
            val variation = (index / baseColors.size) * 0.2f // Increase variation for each cycle
            
            // Create a variation of the base color
            Color(
                red = (baseColor.red * (1 - variation)).coerceIn(0f, 1f),
                green = (baseColor.green * (1 - variation)).coerceIn(0f, 1f),
                blue = (baseColor.blue * (1 - variation)).coerceIn(0f, 1f),
                alpha = baseColor.alpha
            )
        }
    }
} 
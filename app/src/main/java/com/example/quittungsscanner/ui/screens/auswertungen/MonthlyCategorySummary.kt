package com.example.quittungsscanner.ui.screens.auswertungen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quittungsscanner.data.scanner.ReceiptViewModel
import com.example.quittungsscanner.ui.components.ChartColors
import com.example.quittungsscanner.ui.theme.PieChartDataEntry
import com.example.quittungsscanner.ui.theme.SimplePieChart

@Composable
fun MonthlyCategorySummary(
    year: Int,
    month: Int,
    viewModel: ReceiptViewModel = hiltViewModel()
) {
    // Produkte für den angegebenen Monat und Jahr abrufen
    viewModel.getProductsFromYearMonth(year, month)
    val products by viewModel.productsWithCat.collectAsState()

    // Die Produkte nach Kategorien gruppieren und die Preissumme berechnen
    val categorySummaries = products
        .groupBy { it.category }
        .map { entry ->
            val category = entry.key
            val totalPrice = entry.value.sumOf { it.price.toDoubleOrNull() ?: 0.0 }
            category to totalPrice
        }

    // Generate dynamic colors based on the number of categories
    val colors = ChartColors.generateColors(categorySummaries.size)

    val pieEntries = categorySummaries.mapIndexed { index, (category, total) ->
        PieChartDataEntry(
            value = total.toFloat(),
            color = colors[index],
            label = category
        )
    }

    Column() {
        // Jede Kategorie und die zugehörige Preissumme anzeigen
        categorySummaries.forEach { (category, totalPrice) ->
            Text(text = "$category, %.2f CHF".format(totalPrice))
        }

        Row (modifier = Modifier.fillMaxWidth()
            .padding(12.dp),
            horizontalArrangement = Arrangement.Center){
            // Pie-Chart anzeigen
            SimplePieChart(
                entries = pieEntries,
                modifier = Modifier
                    .width(250.dp)
                    .height(250.dp)
                    .aspectRatio(1f),
            )
        }

        // Legende anzeigen
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            pieEntries.forEach { entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Farbiger Kreis für die Legende
                    Canvas(modifier = Modifier
                        .size(16.dp)
                        .padding(end = 8.dp)) {
                        drawCircle(color = entry.color)
                    }
                    Text(text = entry.label)
                }
            }
        }
    }
}
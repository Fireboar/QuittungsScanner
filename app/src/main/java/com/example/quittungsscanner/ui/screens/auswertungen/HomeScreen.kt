package com.example.quittungsscanner.ui.screens.auswertungen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quittungsscanner.data.scanner.ReceiptViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun HomeScreen(viewModel: ReceiptViewModel = hiltViewModel()) {
    // Aktuelles Jahr und Monat abrufen
    val currentDate = remember { Calendar.getInstance() }
    val year = currentDate.get(Calendar.YEAR)
    val month = currentDate.get(Calendar.MONTH)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header: Jahr und Monat anzeigen
        Text(text = "${monthName(month)}, $year", modifier = Modifier.padding(bottom = 16.dp), style = MaterialTheme.typography.bodyLarge)

        // Monatliche Auswertungen anzeigen
        MonthlyCategorySummary(
            year = year,
            month = month,
            viewModel = viewModel
        )
    }
}

private fun monthName(month: Int): String {
    return SimpleDateFormat("MMMM", Locale.GERMAN).format(
        Calendar.getInstance().apply {
            set(Calendar.MONTH, month - 1) // Monatsnummer ist 0-basiert, daher -1
        }.time
    ).replaceFirstChar { it.uppercase() }
}


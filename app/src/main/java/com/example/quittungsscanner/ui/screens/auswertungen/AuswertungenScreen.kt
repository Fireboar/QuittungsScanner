package com.example.quittungsscanner.ui.screens.auswertungen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quittungsscanner.data.scanner.ReceiptViewModel
import com.example.quittungsscanner.ui.theme.DropdownSelector
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AuswertungenScreen(viewModel: ReceiptViewModel = hiltViewModel()) {

    viewModel.loadAvailableYearMonthPairs()
    val currentDate = remember { Calendar.getInstance() }
    val yearMonthPairs by viewModel.availableYearMonthPairs.collectAsState()

    val selectedYear = remember { mutableIntStateOf(currentDate.get(Calendar.YEAR)) }
    val selectedMonth = remember { mutableIntStateOf(currentDate.get(Calendar.MONTH)) }

    val availableYears = yearMonthPairs.map { it.first }.distinct()
    val availableMonths = yearMonthPairs.filter { it.first == selectedYear.intValue }.map { it.second }.distinct()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            DropdownSelector(
                label = "Jahr",
                options = availableYears.map { it.toString() to it.toString() },
                selectedOption = selectedYear.intValue.toString(),
                onOptionSelected = {
                    if (it != null) {
                        selectedYear.intValue = it.toInt()
                    }
                }
            )
            DropdownSelector(
                label = "Monat",
                options = availableMonths.map { it.toString() to monthName(it) },
                selectedOption = selectedMonth.intValue.toString(),
                onOptionSelected = {
                    if (it != null) {
                        selectedMonth.intValue = it.toInt()
                    }
                }
            )
        }
        Row (Modifier.padding(top = 12.dp)) {
            MonthlyCategorySummary(
                year = selectedYear.intValue,
                month = selectedMonth.intValue,
                viewModel = viewModel
            )
        }


    }
}

fun monthName(month: Int): String {
    return SimpleDateFormat("MMMM", Locale.GERMAN).format(
        Calendar.getInstance().apply {
            set(Calendar.MONTH, month)
        }.time
    ).replaceFirstChar { it.uppercase() }
}





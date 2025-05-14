package com.example.quittungsscanner.ui.screens.auswertungen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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
    val currentDate = remember { Calendar.getInstance() }
    val yearOptions = (2022..currentDate.get(Calendar.YEAR)).toList().reversed()
    val monthOptions = (1..12).toList()

    val selectedYear = remember { mutableIntStateOf(currentDate.get(Calendar.YEAR)) }
    val selectedMonth = remember { mutableIntStateOf(currentDate.get(Calendar.MONTH)) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            DropdownSelector(
                label = "Jahr",
                options = yearOptions.map { it.toString() to it.toString() },
                selectedOption = selectedYear.intValue.toString(),
                onOptionSelected = {
                    if (it != null) {
                        selectedYear.intValue = it.toInt()
                    }
                }
            )
            DropdownSelector(
                label = "Monat",
                options = monthOptions.map { it.toString() to monthName(it) },
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

private fun monthName(month: Int): String {
    return SimpleDateFormat("MMMM", Locale.GERMAN).format(
        Calendar.getInstance().apply {
            set(Calendar.MONTH, month)
        }.time
    ).replaceFirstChar { it.uppercase() }
}





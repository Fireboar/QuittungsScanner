package com.example.quittungsscanner.ui.screens.receipts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.quittungsscanner.data.database.ReceiptWithProducts
import com.example.quittungsscanner.data.scanner.ReceiptViewModel
import com.example.quittungsscanner.ui.theme.DropdownSelector
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ReceiptScreen(
    navController: NavController,
    viewModel: ReceiptViewModel = hiltViewModel()
) {
    val receipts by viewModel.receipts.collectAsState()

    //Dropdown
    val selectedYear = remember { mutableStateOf<String?>(null) }
    val selectedMonth = remember { mutableStateOf<String?>(null) }

    // Zustand für das Popup
    val showDeleteDialog = remember { mutableStateOf(false) }
    val receiptToDelete = remember { mutableStateOf<ReceiptWithProducts?>(null) }

    // Lade Belege beim ersten Öffnen
    LaunchedEffect(receipts) {
        viewModel.loadReceipts()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        if (receipts.isEmpty()) {
            Text("Noch keine Belege gespeichert.")
        } else {
            val years = receipts.map {
                SimpleDateFormat("yyyy", Locale.getDefault()).format(it.receipt.dateCreated)
            }.distinct().map { it to it }
            val months = listOf(
                "01" to "Januar", "02" to "Februar", "03" to "März", "04" to "April",
                "05" to "Mai", "06" to "Juni", "07" to "Juli", "08" to "August",
                "09" to "September", "10" to "Oktober", "11" to "November", "12" to "Dezember"
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DropdownSelector(
                    label = "Jahr",
                    options = years,
                    selectedOption = selectedYear.value,
                    onOptionSelected = { selectedYear.value = it }
                )
                DropdownSelector(
                    label = "Monat",
                    options = months.map { it.first to it.second },
                    selectedOption = selectedMonth.value,
                    onOptionSelected = { selectedMonth.value = it }
                )
            }
            LazyColumn {
                val filteredReceipts = receipts.filter {
                    val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(it.receipt.dateCreated)
                    val month = SimpleDateFormat("MM", Locale.getDefault()).format(it.receipt.dateCreated)
                    (selectedYear.value == null || selectedYear.value == year) &&
                            (selectedMonth.value == null || selectedMonth.value == month)
                }
                items(filteredReceipts.reversed(), key = { it.receipt.id }) { receiptWithProducts ->

                    // Row für die Card und die Icons nebeneinander
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Linke Spalte: ReceiptCard
                        Column(modifier = Modifier.weight(1f)) {
                            ReceiptCard(navController,receiptWithProducts)
                        }

                        // Rechte Spalte: Icons untereinander
                        Column(
                            Modifier.fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Löschen Icon
                            IconButton(
                                onClick = {
                                    // Zeige das Dialogfenster und setze das zu löschende Element
                                    receiptToDelete.value = receiptWithProducts
                                    showDeleteDialog.value = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Löschen",
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp)) // Abstand zwischen den Zeilen
                }
            }
        }
    }

    // Dialog für Bestätigung des Löschens
    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog.value = false // Schließe den Dialog, wenn außerhalb geklickt wird
            },
            title = {
                Text(text = "Beleg löschen?")
            },
            text = {
                Text("Bist du sicher, dass du diesen Beleg löschen möchtest?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Hier führst du die Löschaktion aus
                        val receipt = receiptToDelete.value
                        receipt?.let {
                            viewModel.deleteReceipt(it.receipt.id)
                        }
                        showDeleteDialog.value = false // Schließe den Dialog
                    }
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDeleteDialog.value = false // Schließe den Dialog
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
}


@Composable
fun ReceiptCard(navController: NavController, receiptWithProducts: ReceiptWithProducts) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
            .clickable {
                // Navigation zur Edit-Seite mit der ID des Belegs
                navController.navigate("editReceipt/${receiptWithProducts.receipt.id}")
            }
    ) {
        // Column für die gesamte Card
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
        ) {
            // Zeile für Geschäftsname (links) und Datum (rechts)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(receiptWithProducts.receipt.storeName, style = MaterialTheme.typography.titleMedium)
                val dateFormat = SimpleDateFormat("dd.MM.yy, HH:mm", Locale.getDefault())
                val formattedDate = dateFormat.format(receiptWithProducts.receipt.dateCreated)
                Text(formattedDate, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp)) // Abstand zwischen Geschäftsname und Produkte

            // Zeile für Produkte
            Column {
                //Text("Produkte:", style = MaterialTheme.typography.bodyMedium)
                receiptWithProducts.products.take(2).forEach { product ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "%.2f CHF".format(product.price),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                if (receiptWithProducts.products.size > 2) {
                    Text("...", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Flexibler Platz, der den Rest des Platzes einnimmt

            // Zeile für Summe am unteren Rand
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Summe", style = MaterialTheme.typography.titleMedium)
                val totalPrice = receiptWithProducts.products.sumOf { it.price }
                Text("%.2f CHF".format(totalPrice), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}


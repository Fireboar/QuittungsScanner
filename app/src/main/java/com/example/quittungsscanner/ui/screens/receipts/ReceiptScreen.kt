package com.example.quittungsscanner.ui.screens.receipts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.quittungsscanner.data.database.ReceiptWithProducts
import com.example.quittungsscanner.data.scanner.ReceiptViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReceiptScreen(
    navController: NavController,
    viewModel: ReceiptViewModel = hiltViewModel()
) {
    val receipts by viewModel.receipts.collectAsState()

    // Lade Belege beim ersten Öffnen
    LaunchedEffect(Unit) {
        viewModel.loadReceipts()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        if (receipts.isEmpty()) {
            Text("Noch keine Belege gespeichert.")
        } else {
            LazyColumn {
                items(receipts.reversed()) { receiptWithProducts ->
                    ReceiptCard(navController, receiptWithProducts)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ReceiptCard(
    navController: NavController,
    receiptWithProducts: ReceiptWithProducts
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Linke Seite: Store, Datum, Produkte
            Column(modifier = Modifier.weight(1.5f)) {
                val dateFormat = SimpleDateFormat("dd.MM.yy, HH:mm", Locale.getDefault())
                val formattedDate = dateFormat.format(receiptWithProducts.receipt.dateCreated)

                Text(receiptWithProducts.receipt.storeName, style = MaterialTheme.typography.titleMedium)
                Text("Datum: $formattedDate", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Produkte:", style = MaterialTheme.typography.bodyMedium)
                receiptWithProducts.products.take(2).forEach { product ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyMedium,
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

            Spacer(modifier = Modifier.width(16.dp))

            // Rechte Seite: Summe und Button
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                // Oben: Summe
                Text("Summe:", style = MaterialTheme.typography.bodyMedium)
                val totalPrice = receiptWithProducts.products.sumOf { it.price }
                Text("%.2f CHF".format(totalPrice), style = MaterialTheme.typography.titleMedium)

                // Unten: Button
                Button(onClick = {
                    navController.navigate("editReceipt/${receiptWithProducts.receipt.id}")
                }) {
                    Text("bearbeiten")
                }

            }
        }
    }
}



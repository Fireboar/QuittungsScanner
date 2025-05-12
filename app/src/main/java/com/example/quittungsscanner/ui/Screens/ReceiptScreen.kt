package com.example.quittungsscanner.ui.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quittungsscanner.data.database.ReceiptWithProducts
import com.example.quittungsscanner.data.receipt.ReceiptViewModel

@Composable
fun ReceiptScreen(
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
        Text(
            text = "Gespeicherte Belege",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (receipts.isEmpty()) {
            Text("Noch keine Belege gespeichert.")
        } else {
            LazyColumn {
                items(receipts) { receiptWithProducts ->
                    ReceiptCard(receiptWithProducts)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ReceiptCard(receiptWithProducts: ReceiptWithProducts) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Beleg vom: ${receiptWithProducts.receipt.dateCreated}")
            Spacer(modifier = Modifier.height(8.dp))

            receiptWithProducts.products.forEach { product ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(product.name)
                    Text("%.2f CHF".format(product.price))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val total = receiptWithProducts.products.sumOf { it.price }
            Text("Gesamt: %.2f CHF".format(total), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
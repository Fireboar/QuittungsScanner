package com.example.quittungsscanner.ui.Screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.quittungsscanner.data.receipt.ReceiptViewModel
import com.example.quittungsscanner.data.database.Product
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EditReceiptScreen(
    receiptId: Long,
    viewModel: ReceiptViewModel = hiltViewModel(),
    navController: NavController
) {
    val receiptWithProducts by viewModel.receiptWithProducts.collectAsState()

    LaunchedEffect(receiptId) {
        viewModel.getReceiptWithProducts(receiptId)
    }

    if (receiptWithProducts == null) {
        Text("Beleg wird geladen...")
        return
    }

    // Liste für die bearbeiteten Produkte mit nameState und priceState
    val editedProducts = receiptWithProducts!!.products.map { product ->
        val nameState = remember { mutableStateOf(product.name) }
        val priceState = remember { mutableStateOf(product.price.toString()) }
        ProductWithState(product, nameState, priceState)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(receiptWithProducts?.receipt?.storeName ?: "Unbekannt")
        val dateFormat = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(receiptWithProducts!!.receipt.dateCreated)

        Text("Datum: $formattedDate", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Produkte anzeigen und bearbeiten
        editedProducts.forEach { editedProduct ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                // Produktname
                TextField(
                    value = editedProduct.nameState.value,
                    onValueChange = { editedProduct.nameState.value = it },
                    label = { Text("Name") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                // Preis
                TextField(
                    value = editedProduct.priceState.value,
                    onValueChange = { editedProduct.priceState.value = it },
                    label = { Text("Preis") },
                    modifier = Modifier
                        .width(100.dp)
                )
            }
        }

        // Button zum Speichern der Änderungen
        Button(onClick = {
            // Speichern der bearbeiteten Produkte
            val updatedProducts = editedProducts.map { editedProduct ->
                editedProduct.product.copy(
                    name = editedProduct.nameState.value,
                    price = editedProduct.priceState.value.toDoubleOrNull() ?: 0.0
                )
            }
            updatedProducts.forEach { updatedProduct ->
                viewModel.updateProduct(updatedProduct)
            }
            navController.popBackStack() // Zurück zur vorherigen Seite
        }) {
            Text("Änderungen speichern")
        }
    }
}


// Hilfsklasse zum Speichern der bearbeiteten Produktdaten
data class ProductWithState(
    val product: Product,
    val nameState: MutableState<String>,
    val priceState: MutableState<String>
)

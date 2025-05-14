package com.example.quittungsscanner.ui.screens.receipts

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.quittungsscanner.data.scanner.ReceiptViewModel
import com.example.quittungsscanner.data.database.Product
import com.example.quittungsscanner.ui.screens.Screens
import com.example.quittungsscanner.ui.theme.CustomTextField
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditReceiptScreen(
    receiptId: Long,
    viewModel: ReceiptViewModel = hiltViewModel(),
    navController: NavController
) {

    LaunchedEffect(receiptId) {
        viewModel.getReceiptWithProducts(receiptId)
    }

    val receiptWithProducts by viewModel.receiptWithProducts.collectAsState()

    var storeName by remember { mutableStateOf("") }
    LaunchedEffect(receiptWithProducts) {
        receiptWithProducts?.let {
            storeName = it.receipt.storeName
        }
    }

    if (receiptWithProducts == null) {
        Text("Beleg wird geladen...")
        return
    }

    val products by viewModel.products.collectAsState()

    // Gesamtsumme berechnen
    val total = products.sumOf { it.second.toDoubleOrNull() ?: 0.0 }

    // Zustand für die Eingabefelder
    var newName by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }

    //Datum
    val dateFormat = SimpleDateFormat("dd.MM.yy, HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(receiptWithProducts?.receipt?.dateCreated ?: Date())

    Column(modifier = Modifier.fillMaxSize()) {
        Row (modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp),
            Arrangement.SpaceBetween
        ){
            Text("Geschäftsname", style = MaterialTheme.typography.bodyMedium)
            Text(formattedDate, style = MaterialTheme.typography.bodyMedium)
        }

        CustomTextField(
            value = storeName,
            onValueChange = { storeName = it },
            label = "",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 8.dp, end = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 0.dp)
        ) {
            itemsIndexed(products) { index, product ->
                val name = product.first
                val price = product.second

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CustomTextField(
                        value = name,
                        onValueChange = {
                            viewModel.updateProduct(index, it, price)
                        },
                        label = if (index == 0) "Produktname" else "",
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    CustomTextField(
                        value = price,
                        onValueChange = {
                            viewModel.updateProduct(index, name, it)
                        },
                        label = if (index == 0) "Preis" else "",
                        modifier = Modifier.width(100.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            viewModel.deleteProduct(product)
                        }, modifier = Modifier.padding(top = if (index == 0) 18.dp else 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete, contentDescription = "Löschen",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }

        // Eingabefelder für das Hinzufügen eines Produkts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            CustomTextField(
                value = newName,
                onValueChange = { newName = it },
                label = "Produktname",
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            CustomTextField(
                value = newPrice,
                onValueChange = { newPrice = it },
                label = "Preis",
                modifier = Modifier.width(100.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    // Produkt hinzufügen, wenn beide Felder ausgefüllt sind
                    if (newName.isNotBlank() && newPrice.isNotBlank()) {
                        viewModel.addProduct(newName, newPrice)
                        // Nach dem Hinzufügen die Eingabefelder zurücksetzen
                        newName = ""
                        newPrice = ""
                    }
                },
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add, contentDescription = "Produkt hinzufügen",
                    modifier = Modifier.size(30.dp)
                )
            }

        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, end = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(text = "Summe: %.2f CHF".format(total))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)){
            Button(
                onClick = {
                    receiptWithProducts?.receipt?.let { oldReceipt ->
                        val updatedReceipt = oldReceipt.copy(storeName = storeName)


                        viewModel.updateReceipt(updatedReceipt)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Beleg speichern")
            }
        }
    }


}


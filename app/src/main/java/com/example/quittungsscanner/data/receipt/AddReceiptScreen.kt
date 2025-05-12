package com.example.quittungsscanner.data.receipt

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.example.quittungsscanner.ui.Screens.Screens

@Composable
fun AddReceiptScreen(
    navController: NavController,
    viewModel: ReceiptViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data?.getStringExtra("recognized_text") ?: ""
            viewModel.processReceiptText(text)
        }
    }

    Column {
        Button(onClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.CAMERA),
                    1001
                )
            } else {
                val intent = Intent(context, CameraScanActivity::class.java)
                launcher.launch(intent)
            }
        }) {
            Text("Scannen starten")
        }


        Spacer(modifier = Modifier.height(16.dp))

        ProductList(navController = navController)
    }
}

@Composable
fun ProductList(
    viewModel: ReceiptViewModel = hiltViewModel(),
    navController: NavController) {
    val products by viewModel.products.collectAsState()

    // Gesamtsumme berechnen
    val total = products.sumOf { it.second.toDoubleOrNull() ?: 0.0 }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(products) { index, product ->
                var name by remember { mutableStateOf(product.first) }
                var price by remember { mutableStateOf(product.second) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextField(
                        value = name,
                        onValueChange = {
                            name = it
                            viewModel.updateProduct(index, it, price)
                        },
                        label = { Text("Produkt") },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    TextField(
                        value = price,
                        onValueChange = {
                            price = it
                            viewModel.updateProduct(index, name, it)
                        },
                        label = { Text("Preis") },
                        modifier = Modifier.width(100.dp)
                    )
                }
            }
        }

        // Gesamtsumme anzeigen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(text = "Summe: %.2f CHF".format(total))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.saveReceiptToDatabase {
                    navController.navigate(Screens.savedReceipt.name)
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("Beleg speichern")
        }
    }
}

@Composable
fun ReceiptSavedScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Beleg wurde erfolgreich gespeichert!",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            navController.navigate(Screens.AddQuittung.name) {
                popUpTo("add_receipt") { inclusive = true } // verhindert Zurück zu diesem Screen
            }
        }) {
            Text("Mehr Quittungen Scannen")
        }
    }
}





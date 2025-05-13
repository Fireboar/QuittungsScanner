package com.example.quittungsscanner.ui.Screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.example.quittungsscanner.data.receipt.CameraScanActivity
import com.example.quittungsscanner.data.receipt.ReceiptViewModel
import kotlinx.coroutines.launch

@Composable
fun AddReceiptScreen(
    navController: NavController,
    viewModel: ReceiptViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var storeName by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data?.getStringExtra("recognized_text") ?: ""
            Log.d("UI_RECEIVED_TEXT", "Text empfangen: $text")  // <-- Hier prüfen
            viewModel.processReceiptText(text)
        }
    }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("Store/Laden") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
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
            }

            ProductList()

            Button(
                onClick = {
                    if (storeName.isNotEmpty()) {
                        viewModel.saveReceiptToDatabase(storeName) {
                            navController.navigate(Screens.savedReceipt.name)
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Fehler: Store-Name fehlt!")
                        }
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
}


@Composable
fun ProductList(viewModel: ReceiptViewModel = hiltViewModel()) {
    val products by viewModel.products.collectAsState()

    // Gesamtsumme berechnen
    val total = products.sumOf { it.second.toDoubleOrNull() ?: 0.0 }

    Column {
        LazyColumn {
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
                .padding(end=16.dp,top=16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(text = "Summe: %.2f CHF".format(total))
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





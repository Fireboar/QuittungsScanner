package com.example.quittungsscanner.ui.Screens

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.example.quittungsscanner.ui.scanner.CameraScanActivity
import com.example.quittungsscanner.data.receipt.ReceiptViewModel
import com.example.quittungsscanner.ui.theme.CustomTextField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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
            Log.d("UI_RECEIVED_TEXT", "Text empfangen: $text")
            viewModel.processReceiptText(text)
            storeName = viewModel.getStoreName(text)
        }
    }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) }
    ) {
        Column {
            Row (Modifier.fillMaxWidth().padding(16.dp), Arrangement.Center){
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

            CustomTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = "Store/Laden",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
            )

            ProductList(snackbarHostState,storeName,navController,coroutineScope)

        }
    }
}

@Composable
fun ProductList(snackbarHostState:SnackbarHostState,storeName:String, navController: NavController, coroutineScope: CoroutineScope, viewModel: ReceiptViewModel = hiltViewModel()) {
    val products by viewModel.products.collectAsState()

    // Gesamtsumme berechnen
    val total = products.sumOf { it.second.toDoubleOrNull() ?: 0.0 }

    // Zustand für die Eingabefelder
    var newName by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
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

                    IconButton(onClick = {
                        viewModel.deleteProduct(product)
                    }, modifier = Modifier.padding(top = if (index == 0) 18.dp else 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Löschen",
                                modifier = Modifier.size(30.dp))
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

            IconButton(onClick = {
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
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Produkt hinzufügen",
                    modifier = Modifier.size(30.dp))
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Beleg speichern")
            }
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





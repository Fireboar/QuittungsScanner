package com.example.quittungsscanner.data.receipt

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.collectAsState

@Composable
fun AddReceiptScreen(viewModel: ReceiptViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val recognizedText by viewModel.recognizedText
    val products by viewModel.products.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data?.getStringExtra("recognized_text") ?: ""
            viewModel.setRecognizedText(text)
        }
    }

    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }


    Column {
        // Zeige das Bild an, wenn es verfügbar ist
        imageBitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "Scanned Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        //Text(recognizedText)
        Log.d("ReceiptScreen", "Recognized Text: $recognizedText")

        products.forEach { (name, price) ->
            Text(text = "$name: $price CHF")
            Log.d("ReceiptScreen3", "$name: $price CHF" )
        }
    }
}


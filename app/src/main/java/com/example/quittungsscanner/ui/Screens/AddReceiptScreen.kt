package com.example.quittungsscanner.ui.Screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
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
import com.example.quittungsscanner.data.receipt.ReceiptViewModel

@Composable
fun AddReceiptScreen(viewModel: ReceiptViewModel = hiltViewModel()){
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Launcher für die Kamera, um ein Bild aufzunehmen
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            bitmap?.let {
                imageBitmap = it
                viewModel.scanReceipt(it)  // OCR-Verarbeitung
            }
        }
    )

    Column {
        // Zeige das Bild an, wenn es verfügbar ist
        imageBitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "Receipt Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button zum Starten der Kamera
        Button(onClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                val CAMERA_PERMISSION_REQUEST_CODE = 1001
                ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            } else {
                cameraLauncher.launch()
            }
        }) {
            Text("Bild mit der Kamera aufnehmen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Zeige den erkannten Text
        Text("Erkannter Text: ${viewModel.recognizedText}")
    }
}
package com.example.quittungsscanner.data.receipt

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@Composable
fun AddReceiptScreen(viewModel: ReceiptViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var recognizedText by remember { mutableStateOf("") }

    // Funktion zur kontinuierlichen Texterkennung
    fun processDocumentScan(bitmap: Bitmap) {
        val options = TextRecognizerOptions.Builder().build()
        val recognizer: TextRecognizer = TextRecognition.getClient(options)
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val newText = visionText.text
                recognizedText = newText // Update des erkannten Textes
            }
            .addOnFailureListener { e ->
                // Fehlerbehandlung, falls OCR fehlschlägt
            }
    }

    Column {
        // Zeige das Bild an, wenn es verfügbar ist
        imageBitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "Scanned Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button zum Starten der kontinuierlichen Kameraerfassung
        Button(onClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                val CAMERA_PERMISSION_REQUEST_CODE = 1001
                ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            } else {
                // Starte die CameraScanActivity
                val intent = Intent(context, CameraScanActivity::class.java)
                context.startActivity(intent)
            }
        }) {
            Text("Scannen starten")
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Zeige den erkannten Text
        Text("Erkannter Text: $recognizedText")
    }
}


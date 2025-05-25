package com.example.quittungsscanner.ui.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.quittungsscanner.data.scanner.TextProcessor.levenshtein
import com.example.quittungsscanner.ui.theme.QuittungsScannerTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraScanActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Check permissions and start camera
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted) {
                Toast.makeText(this, "Kamera-Berechtigung benötigt", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // ContentView mit Compose UI anstelle einer XML-Datei
        setContent {
            QuittungsScannerTheme {
                CameraScanScreen(
                    cameraExecutor = cameraExecutor,
                    onResult = { result ->
                        setResult(RESULT_OK, Intent().apply {
                            putExtra("recognized_text", result)
                        })
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources
        cameraExecutor.shutdown()
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraScanScreen(cameraExecutor: ExecutorService, onResult: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isScanning by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    val recognizedTexts = remember { mutableStateListOf<String>() }

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    // Initialize camera provider
    LaunchedEffect(Unit) {
        cameraProvider = ProcessCameraProvider.getInstance(context).get()
    }

    // Bind/unbind CameraX when previewView or isScanning changes
    DisposableEffect(previewView, cameraProvider, isScanning) {
        val provider = cameraProvider
        val view = previewView
        if (provider != null && view != null && isScanning) {
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(view.surfaceProvider)
            }
            val imageAnalysis = ImageAnalysis.Builder().build().also { analysis ->
                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    if (isScanning) {
                        analyzeImage(imageProxy,
                            onTextRecognized = { text ->
                                recognizedText = text
                                recognizedTexts.add(text)
                                Log.d("OCR", "Erkannter Text: $text")
                            },
                            onImageClosed = { imageProxy.close() }
                        )
                    } else {
                        imageProxy.close()
                    }
                }
            }
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        }
        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(
                if (isSystemInDarkTheme()) Color(0xFF1c1b1f) else Color.White
            )
    ) {
        // Kamera-Vorschau
        AndroidView(
            factory = { context ->
                PreviewView(context).also {
                    previewView = it
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(top = 100.dp)
        )

        // UI-Buttons unten anzeigen
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                if (!isScanning) {
                    isScanning = true
                    Toast.makeText(context, "Scan gestartet", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Start Scan")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (isScanning) {
                    isScanning = false

                    Toast.makeText(context, "Scan gestoppt", Toast.LENGTH_SHORT).show()

                    val result = getTextWithStartEndString(
                        recognizedTexts,
                        startString = "artikelbezeichung",
                        possibleEndWords = listOf("total chf", "sie sparen total", "total", "total in eur"),
                        maxStartDistance = 4,
                        maxEndDistance = 6
                    ) ?: recognizedText
                    onResult(result)
                }
            },
                colors = ButtonDefaults.buttonColors())
            {
                Text("Stop Scan")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { onResult(getDummyReceiptText()) }) {
                Text("Test-Beleg analysieren")
            }

        }
    }
}

// OCR-Analyse mit ML Kit
@OptIn(ExperimentalGetImage::class)
private fun analyzeImage(imageProxy: ImageProxy,
                         onTextRecognized: (String) -> Unit,
                         onImageClosed: () -> Unit) {
    val mediaImage = imageProxy.image
    mediaImage?.let {
        val inputImage = InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees)

        val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build())
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val recognized = visionText.text
                onTextRecognized(recognized)
                Log.d("OCR", "Erkannter Text: $recognized")
            }
            .addOnFailureListener { e ->
                Log.e("OCR", "Fehler bei der Texterkennung", e)
            }
            .addOnCompleteListener {
                onImageClosed()
            }
    } ?: imageProxy.close()
}

fun getTextWithStartEndString(
    recognizedTexts: List<String>,
    startString: String,
    possibleEndWords: List<String>,
    maxStartDistance: Int = 4,
    maxEndDistance: Int = 6
): String? {

    for (text in recognizedTexts) {
        // Lowercase the text to make the matching case insensitive
        val cleanedText = text.lowercase()

        // Perform fuzzy start matching using Levenshtein distance
        val fuzzyStartMatch = levenshtein(cleanedText, startString.lowercase()) <= maxStartDistance
        val startMatch = Regex("artikel[bsz]e?zeich(n|n?u|nu?g|ung)?", RegexOption.IGNORE_CASE).containsMatchIn(text)

        // Check if the start string has a fuzzy match
        if (fuzzyStartMatch || startMatch) {
            // Now look for a match with any of the possible end words
            for (endString in possibleEndWords) {
                val endIndex = text.indexOf(endString)
                if (endIndex != -1) {
                    // Perform fuzzy matching for the end string
                    val endSubString = text.substring(endIndex, endIndex + endString.length)
                    val endDistance = levenshtein(endString.lowercase(), endSubString.lowercase())

                    if (endDistance <= maxEndDistance) {
                        Log.d("OCR", "PASSENDER TEXT")
                        return text // Return the matching text if both start and end criteria are satisfied
                    }
                }
            }
        }
    }
    return null // Return null if no matching text is found
}

fun getDummyReceiptText(): String {
    return """
        MH Seepark
        Tel. 058 712 75 00
        Totel#
        Preis Gespart
        Menge
        Artikelbezeichnung
        4.20 1
        6.25 1
        7.65 1
        5.26 1
        6.25
        4.20
        0.94
        1
        36.00
        3.19
        Bio Erdbeeren
        Bio Heide lbeeren
        Pouletbrust Medaillon 1
        Joghurt Nature
        2
        0.01-
        0.95
        Rundungsorteil
        Sie sparen total
        23.35
        23.35
        25.94
        Total CHF
        TUINT OR
        Total in EUR
        TUINT
        16:50
        831526678•00271166/5c4cc4/0000
        XXXXXXXXXXXXXXX6069
        Buchung
        12.05.2025
        23.35
        00000028
        Total-EFT CHF:
        23.36
        542.65
        2099.60).308.781
        Punktestand per 11.05.205
        Cunulus-Nunner
        Erhaltene Punkte
        NUS
        CHE-105.784.711 MUST
        0.59
        Total
        23.36
        Satz
        2.60
        # NUST. -Nunner
        Gr
        Besten Dank für Ihren Einkauf!
    """.trimIndent()
}




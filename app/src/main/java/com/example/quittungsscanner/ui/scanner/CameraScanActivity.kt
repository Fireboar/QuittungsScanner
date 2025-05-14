package com.example.quittungsscanner.ui.scanner

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.quittungsscanner.data.receipt.TextProcessor.levenshtein
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraScanActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private lateinit var previewView: PreviewView  // PreviewView für die Kameraansicht
    private var isScanning = false  // Flag, um den Scan-Prozess zu verfolgen
    private var recognizedText by mutableStateOf("")
    private val recognizedTexts = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Berechtigungen prüfen und Kamera starten
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 1001)
        }

        // Executor für die Bildanalyse
        cameraExecutor = Executors.newSingleThreadExecutor()

        // ContentView mit Compose UI anstelle einer XML-Datei
        setContent {
            CameraScanScreen()
        }
    }

    // Startet den Kamerafeed
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Kamera-Selektor für Rückkamera
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            // Preview-Use-Case für die Kamera
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            // ImageAnalysis-Use-Case
            val imageAnalysis = ImageAnalysis.Builder()
                .build()

            // Bildanalyse für kontinuierlichen Scan
            imageAnalysis.setAnalyzer(cameraExecutor, { imageProxy ->
                if (isScanning) {  // Nur analysieren, wenn der Scan aktiv ist
                    analyzeImage(imageProxy)
                } else {
                    imageProxy.close()  // Bild schließen, wenn Scan nicht aktiv
                }
            })

            try {
                // Kamera-Use-Cases binden
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this,  // LifecycleOwner
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("CameraScan", "Fehler beim Binden der Kamera-Use-Cases", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // OCR-Analyse mit ML Kit
    @OptIn(ExperimentalGetImage::class)
    private fun analyzeImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        mediaImage?.let {
            val inputImage = InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees)

            val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build())
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val recognized = visionText.text
                    recognizedText = recognized
                    recognizedTexts.add(recognized)
                    Log.d("OCR", "Erkannter Text: $recognized")
                }
                .addOnFailureListener { e ->
                    Log.e("OCR", "Fehler bei der Texterkennung", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()  // Bild schließen
                }
        }
    }

    @Composable
    fun CameraScanScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Kamera-Vorschau
            AndroidView(
                factory = { context ->
                    PreviewView(context).apply {
                        previewView = this
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .align(Alignment.TopCenter)
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
                        startCamera()
                        Toast.makeText(this@CameraScanActivity, "Scan gestartet", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Start Scan")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    if (isScanning) {
                        isScanning = false
                        camera?.cameraControl?.enableTorch(false)

                        Toast.makeText(this@CameraScanActivity, "Scan gestoppt", Toast.LENGTH_SHORT).show()

                        val targetStart = "artikelbezeichnung"
                        val possibleEndWords = listOf("total chf", "sie sparen total", "total", "total in eur")

                        val result = getTextWithStartEndString(
                            recognizedTexts,
                            targetStart,
                            possibleEndWords,
                            maxStartDistance = 4,
                            maxEndDistance = 6
                        ) ?: recognizedText

                        val resultIntent = Intent().apply {
                            putExtra("recognized_text", result)  // ✅ Text zurückgeben!
                        }
                        setResult(RESULT_OK, resultIntent)

                        finish()
                    }
                }) {
                    Text("Stop Scan")
                }
                TestReceiptButton()

            }
        }
    }

    @Composable
    fun TestReceiptButton() {
        val dummyText = getDummyReceiptText()

        Button(
            onClick = {
                val resultIntent = Intent().apply {
                    putExtra("recognized_text", dummyText)  // ✅ Testtext zurückgeben
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text("Test-Beleg analysieren")
        }
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




    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()  // Kamera-Executor schließen
    }
}

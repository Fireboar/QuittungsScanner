package com.example.quittungsscanner.data.receipt

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraScanActivity : ComponentActivity() {

    private val viewModel: ReceiptViewModel by viewModels()
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private lateinit var previewView: PreviewView  // PreviewView für die Kameraansicht
    private var isScanning = false  // Flag, um den Scan-Prozess zu verfolgen
    private var recognizedText by mutableStateOf("")

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
                    recognizedText = recognized  // Erkannten Text aktualisieren
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

            // 🔴 Horizontale Hilfslinie in der Mitte des Kamera-Previews
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter)  // An den Kamera-Bereich anpassen
                    .padding(top = 200.dp)       // -> halbe Höhe des Kamera-Bereichs (400dp)
                    .background(androidx.compose.ui.graphics.Color.Red)
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

                        val resultIntent = Intent()
                        resultIntent.putExtra("recognized_text", recognizedText)
                        setResult(RESULT_OK, resultIntent)

                        Toast.makeText(this@CameraScanActivity, "Scan gestoppt", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }) {
                    Text("Stop Scan")
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()  // Kamera-Executor schließen
    }
}

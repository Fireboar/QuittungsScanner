package com.example.quittungsscanner.data.receipt

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraScanActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private lateinit var previewView: PreviewView  // PreviewView, um den Kamerafeed darzustellen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setze die PreviewView
        previewView = PreviewView(this)
        setContentView(previewView)

        // Kameraerlaubnis prüfen
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 1001)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Kamera-Selektor konfigurieren (nur Rückkamera)
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            // Preview-Use-Case konfigurieren
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)  // Binde die PreviewView als Ziel für das Kamerabild

            // ImageAnalysis-Use-Case konfigurieren
            val imageAnalysis = ImageAnalysis.Builder()
                .build()
            imageAnalysis.setAnalyzer(cameraExecutor, { imageProxy ->
                analyzeImage(imageProxy)
            })

            try {
                // Alle Use-Cases binden
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this,  // LifecycleOwner
                    cameraSelector,
                    preview,  // Füge die Preview-Use-Case hinzu
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("CameraScan", "Use case binding failed", e)
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
                    val recognizedText = visionText.text
                    Log.d("OCR", "Erkannter Text: $recognizedText")
                }
                .addOnFailureListener { e ->
                    Log.e("OCR", "Fehler bei der Texterkennung", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()  // Wichtig: Bild immer schließen
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

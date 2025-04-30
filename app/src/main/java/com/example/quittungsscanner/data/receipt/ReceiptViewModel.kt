package com.example.quittungsscanner.data.receipt

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ReceiptViewModel @Inject constructor() : ViewModel() {

    var recognizedText by mutableStateOf("")

    // Methode umbenennen, um den Konflikt zu vermeiden
    fun updateRecognizedText(text: String) {
        recognizedText = text
    }
}
package com.example.quittungsscanner.data.receipt

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.State


@HiltViewModel
class ReceiptViewModel @Inject constructor() : ViewModel() {

    private val _recognizedText = mutableStateOf("")
    val recognizedText: State<String> = _recognizedText

    fun setRecognizedText(text: String) {
        _recognizedText.value = text
    }
}
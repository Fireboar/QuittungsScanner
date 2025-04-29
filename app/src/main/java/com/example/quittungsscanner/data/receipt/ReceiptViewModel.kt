package com.example.quittungsscanner.data.receipt

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quittungsscanner.data.database.UserDao
import com.googlecode.tesseract.android.TessBaseAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val tessBaseAPI: TessBaseAPI
) : ViewModel() {

    var recognizedText by mutableStateOf("")
        private set

    // Beispiel für OCR-Erkennung
    fun scanReceipt(bitmap: Bitmap) {
        viewModelScope.launch {
            recognizedText = withContext(Dispatchers.IO) {
                tessBaseAPI.setImage(bitmap)
                tessBaseAPI.utF8Text
            }
        }
    }
}
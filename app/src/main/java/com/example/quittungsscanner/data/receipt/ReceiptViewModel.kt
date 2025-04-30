package com.example.quittungsscanner.data.receipt

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.State
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@HiltViewModel
class ReceiptViewModel @Inject constructor() : ViewModel() {

    private val _recognizedText = mutableStateOf("")
    val recognizedText: State<String> = _recognizedText

    private val _products = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val products: StateFlow<List<Pair<String, String>>> get() = _products

    fun setRecognizedText(text: String) {
        _recognizedText.value = text
    }

    fun setExtractedProducts(products: List<Pair<String, String>>) {
        _products.value = products
    }
}
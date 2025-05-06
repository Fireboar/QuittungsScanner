package com.example.quittungsscanner.data.receipt

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class ReceiptViewModel @Inject constructor() : ViewModel() {
    private val _products = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val products: StateFlow<List<Pair<String, String>>> get() = _products

    fun processReceiptText(text: String) {
        Log.d("ReceiptViewModel", "Verarbeite Text: $text") // <-- Log hinzufügen
        val productPairs = TextProcessor.extractProducts(text)
        _products.value = productPairs
    }

    fun updateProduct(index: Int, name: String, price: String) {
        _products.update { current ->
            current.toMutableList().apply {
                this[index] = name to price
            }
        }
    }
}
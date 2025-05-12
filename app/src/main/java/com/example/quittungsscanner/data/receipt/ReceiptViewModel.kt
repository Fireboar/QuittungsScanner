package com.example.quittungsscanner.data.receipt

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quittungsscanner.data.database.Product
import com.example.quittungsscanner.data.database.ProductDao
import com.example.quittungsscanner.data.database.Receipt
import com.example.quittungsscanner.data.database.ReceiptDao
import com.example.quittungsscanner.data.database.ReceiptWithProducts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject


@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val receiptDao: ReceiptDao,
    private val productDao: ProductDao
) : ViewModel() {
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

    fun saveReceiptToDatabase(onSaved: () -> Unit) {
        viewModelScope.launch {
            val receipt = Receipt(dateCreated = Date())
            val receiptId = receiptDao.insertReceipt(receipt)

            val productEntities = _products.value.mapNotNull { (name, priceStr) ->
                priceStr.toDoubleOrNull()?.let { price ->
                    Product(name = name, price = price, receiptId = receiptId)
                }
            }

            // Einfügen aller Produkte auf einmal, erhält eine Liste der generierten IDs (optional verwendbar)
            val insertedIds = productDao.insertProducts(*productEntities.toTypedArray())

            // Optional: Log zur Kontrolle
            Log.d("ReceiptViewModel", "Inserted receipt ID: $receiptId, product IDs: $insertedIds")
            onSaved()
        }
    }

    private val _receipts = MutableStateFlow<List<ReceiptWithProducts>>(emptyList())
    val receipts: StateFlow<List<ReceiptWithProducts>> get() = _receipts

    fun loadReceipts() {
        viewModelScope.launch {
            _receipts.value = receiptDao.getReceiptsWithProducts()
        }
    }
}
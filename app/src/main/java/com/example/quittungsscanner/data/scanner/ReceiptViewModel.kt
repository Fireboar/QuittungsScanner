package com.example.quittungsscanner.data.scanner

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

    fun getStoreName(text: String): String {
        val storeName = TextProcessor.getStoreName(text)
        return storeName
    }

    private val _products = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val products: StateFlow<List<Pair<String, String>>> get() = _products

    fun processReceiptText(text: String) {
        Log.d("ReceiptViewModel", "Verarbeite Text: $text") // <-- Log hinzufügen
        val productPairs = TextProcessor.extractProducts(text)
        Log.d("ReceiptViewModel", "Erkannte Produkte: ${productPairs.joinToString { "${it.first} - ${it.second}" }}")
        _products.value = productPairs
    }

    fun addProduct(name: String, price: String) {
        val newProduct = name to price
        _products.update { current ->
            current.toMutableList().apply {
                add(newProduct)
            }
        }
    }

    fun deleteProduct(product: Pair<String, String>) {
        _products.update { it.toMutableList().apply { remove(product) } }
    }

    fun deleteProduct(index: Int) {
        _products.update { current ->
            val updatedList = current.toMutableList()
            updatedList.removeAt(index)
            updatedList
        }
        Log.d("edit", "Deleted product at index $index")
    }

    fun updateProduct(index: Int, name: String, price: String) {
        _products.update { current ->
            current.toMutableList().apply {
                this[index] = name to price
            }
        }
    }

    fun saveReceiptToDatabase( storeName:String,onSaved: () -> Unit) {
        viewModelScope.launch {
            val receipt = Receipt(
                dateCreated = Date(),
                storeName = storeName
            )
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

    private val _receiptWithProducts = MutableStateFlow<ReceiptWithProducts?>(null)
    val receiptWithProducts: StateFlow<ReceiptWithProducts?> get() = _receiptWithProducts

    fun getReceiptWithProducts(receiptId: Long) {
        viewModelScope.launch {
            // Fetch the ReceiptWithProducts by its ID
            val result = receiptDao.getReceiptsWithProducts().find { it.receipt.id == receiptId }

            // If the result is not null, update the products list
            result?.let {
                // Map products into a Pair of name and price as Strings
                val productPairs = it.products.map { product ->
                    product.name to product.price.toString()
                }

                // Update the _products StateFlow with the new list of products
                _products.value = productPairs

                // Optionally, update _receiptWithProducts if needed
                _receiptWithProducts.value = it
            }
        }
    }

    fun updateReceipt(receipt: Receipt) {
        viewModelScope.launch {
            receiptDao.updateReceipt(receipt)
            val oldProducts = productDao.getProductsByReceipt(receipt.id)
            oldProducts.forEach { product ->
                productDao.deleteProductById(product.id)
            }
            val newProducts = _products.value.mapNotNull { (name, priceStr) ->
                priceStr.toDoubleOrNull()?.let { price ->
                    Product(name = name, price = price, receiptId = receipt.id)
                }
            }
            productDao.insertProducts(*newProducts.toTypedArray())
            loadReceipts()
        }
    }

    fun deleteReceipt(id: Long) {
        viewModelScope.launch {
            receiptDao.deleteReceiptById(id)
            loadReceipts()
        }
    }
}
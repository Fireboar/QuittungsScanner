package com.example.quittungsscanner.data.scanner

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quittungsscanner.data.categories.openFoodFactsService
import com.example.quittungsscanner.data.database.Product
import com.example.quittungsscanner.data.database.ProductDao
import com.example.quittungsscanner.data.database.Receipt
import com.example.quittungsscanner.data.database.ReceiptDao
import com.example.quittungsscanner.data.database.ReceiptWithProducts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.Calendar
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

    suspend fun getProductCategory(productName: String): String {
        return try {
            // API-Aufruf, um Produktdetails zu erhalten
            val response = openFoodFactsService.searchProduct(productName)

            // Abrufen des ersten Produkts
            val firstProduct = response.products.firstOrNull()

            // Extrahieren der Keywords
            val keywords = firstProduct?._keywords ?: emptyList()
            val categories = firstProduct?.categories?.split(",") ?: emptyList()

            // Durchsuchen der Keywords und Bestimmen der Kategorie
            var category: String = when {
                // Prüfen, ob eines der Keywords auf Lebensmittel hinweist
                keywords.any { it.contains("lebensmittel", ignoreCase = true) } -> "Lebensmittel"
                categories.any { it.contains("lebensmittel", ignoreCase = true) } -> "Lebensmittel"
                // Prüfen, ob eines der Keywords auf Kleidung hinweist
                keywords.any { it.contains("bekleidung", ignoreCase = true) } -> "Kleidung"
                categories.any { it.contains("bekleidung", ignoreCase = true) } -> "Kleidung"
                // Prüfen, ob eines der Keywords auf Elektronik hinweist
                keywords.any { it.contains("elektronik", ignoreCase = true) } -> "Elektronik"
                categories.any { it.contains("elektronik", ignoreCase = true) } -> "Elektronik"
                else -> "Unbekannt"
            }

            category // Rückgabe der Kategorie
        } catch (e: Exception) {
            Log.e("OpenFoodFacts", "Fehler beim Abrufen der Kategorie", e)
            "Unbekannt" // Rückgabe "Unbekannt" im Fehlerfall
        }
    }


    var isLoading by mutableStateOf(false)
        private set

    fun saveReceiptToDatabase(storeName: String, onSaved: () -> Unit) {
        viewModelScope.launch {
            // Die Funktion wird jetzt asynchron ausgeführt
            try {
                isLoading = true
                val receipt = Receipt(
                    dateCreated = Date(),
                    storeName = storeName
                )
                val receiptId = receiptDao.insertReceipt(receipt)

                val productEntities = _products.value.mapNotNull { (name, priceStr) ->
                    priceStr.toDoubleOrNull()?.let { price ->
                        async {
                            val category = try {
                                getProductCategory(name)  // Holen der Kategorie für das Produkt
                            } catch (e: Exception) {
                                Log.e("Product", "Fehler beim Abrufen der Kategorie für $name", e)
                                "Unbekannt"  // Falls ein Fehler auftritt, Kategorie auf "Unbekannt" setzen
                            }
                            Product(name = name, price = price, receiptId = receiptId, category = category)
                        }
                    }
                }.map { it.await() }

                // Einfügen aller Produkte auf einmal
                val insertedIds = productDao.insertProducts(*productEntities.toTypedArray())

                // Log zur Kontrolle
                Log.d("ReceiptViewModel", "Inserted receipt ID: $receiptId, product IDs: $insertedIds")

                // Rückmeldung an die UI
                onSaved()

            } catch (e: Exception) {
                // Fehlerbehandlung, wenn etwas schief geht
                Log.e("ReceiptViewModel", "Fehler beim Speichern des Belegs: ${e.message}")
            } finally {
                isLoading = false
            }
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
                    Product(name = name, price = price, receiptId = receipt.id, category = "")
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

    private val _productsWithCat = MutableStateFlow<List<ProductWithCategory>>(emptyList())
    val productsWithCat: StateFlow<List<ProductWithCategory>> get() = _productsWithCat

    fun getProductsFromYearMonth(year: Int, month: Int) {
        viewModelScope.launch {
            try {
                // Hole alle Belege aus der Datenbank
                val allReceipts = receiptDao.getReceiptsWithProducts()

                // Filtern der Belege für das angegebene Jahr und Monat
                val filteredReceipts = allReceipts.filter { receipt ->
                    val receiptDate = Calendar.getInstance().apply {
                        time = receipt.receipt.dateCreated
                    }
                    val receiptMonth = receiptDate.get(Calendar.MONTH)
                    val receiptYear = receiptDate.get(Calendar.YEAR)

                    Log.d("ReceiptFilter", "Beleg-Datum: ${receipt.receipt.dateCreated}, Monat: $receiptMonth, Jahr: $receiptYear")

                    // Vergleich des Monats und des Jahres
                    receiptMonth == month && receiptYear == year
                }

                // Extrahieren der Produkte der gefilterten Belege und deren Kategorien
                val productWithCategoryList = filteredReceipts.flatMap { receipt ->
                    receipt.products.map { product ->
                        ProductWithCategory(product.name, product.price.toString(), product.category)
                    }
                }

                // Aktualisieren des StateFlow mit den Produkten und Kategorien
                _productsWithCat.value = productWithCategoryList

            } catch (e: Exception) {
                Log.e("Product", "Fehler beim Abrufen der Produkte für Jahr $year, Monat $month", e)
            }
        }
    }

}

data class ProductWithCategory(
    val name: String,
    val price: String,
    val category: String
)
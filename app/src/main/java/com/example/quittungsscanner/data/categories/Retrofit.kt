package com.example.quittungsscanner.data.categories

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// OpenFoodFacts API Service
interface OpenFoodFactsService {
    @GET("cgi/search.pl")
    suspend fun searchProduct(@Query("search_terms") searchTerm: String, @Query("json") json: Int = 1): ProductSearchResponse
}

// Datenklasse für API-Antwort
data class ProductSearchResponse(
    val products: List<ProductDetails>
)

data class ProductDetails(
    val product_name: String,
    val categories: String,
    val _keywords: List<String> // List of keywords
)

// Retrofit-Instanz erstellen
fun createRetrofit(): Retrofit {
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)  // Timeout für die Verbindung auf 60 Sekunden setzen
        .readTimeout(60, TimeUnit.SECONDS)     // Lese-Timeout auf 60 Sekunden setzen
        .writeTimeout(60, TimeUnit.SECONDS)    // Schreib-Timeout auf 60 Sekunden setzen
        .build()

    return Retrofit.Builder()
        .baseUrl("https://world.openfoodfacts.org/")
        .client(okHttpClient)  // OkHttpClient hinzufügen
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

val openFoodFactsService = createRetrofit().create(OpenFoodFactsService::class.java)

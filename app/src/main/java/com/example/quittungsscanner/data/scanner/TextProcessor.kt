package com.example.quittungsscanner.data.scanner

import android.annotation.SuppressLint
import android.util.Log
import java.util.Locale
import kotlin.math.roundToInt

object TextProcessor {

    @SuppressLint("DefaultLocale")
    fun extractProducts(text: String): List<Pair<String, String>> {
        Log.d("TextProcessor Text", text)
        val lines = text.lines()
        val productList = mutableListOf<Pair<String, String>>()
        val prices = mutableListOf<String>()

        // Suchbegriffe
        val targetStart = "artikelbezeichnung"
        val possibleEndWords = listOf("total chf", "sie sparen total", "total", "total in eur", "rundungsvorteil")

        // Levenshtein-Toleranzen
        val maxStartDistance = 4
        val maxEndDistance = 3

        var startIndex = -1
        var endIndex = lines.size  // Standardmäßig bis zum Ende

        // 1. STARTWORT finden
        for ((i, line) in lines.withIndex()) {
            val cleanedLine = line.lowercase().replace("[^a-z]".toRegex(), "")
            val startMatch = Regex("artikel[bsz]e?zeich(n|n?u|nu?g|ung)?", RegexOption.IGNORE_CASE).containsMatchIn(line)
            val fuzzyStartMatch = levenshtein(cleanedLine, targetStart.replace(" ", "")) <= maxStartDistance

            if (startMatch || fuzzyStartMatch) {
                startIndex = i + 1  // Produkte beginnen nach der Startzeile
                break
            }
        }

        // Wenn kein Startwort → leer zurückgeben
        if (startIndex == -1) {
            Log.d("TextProcessor", "❌ Kein Startwort gefunden")
            return emptyList()
        }

        // 2. ENDWORT suchen **nach** dem Startwort
        for (i in startIndex until lines.size) {
            val originalLine = lines[i]
            val cleanedLine = originalLine.lowercase().replace("[^a-z]".toRegex(), "")

            val matchedEndWord = possibleEndWords.firstOrNull {
                val distance = levenshtein(cleanedLine, it.replace(" ", ""))
                distance <= maxEndDistance
            }

            if (matchedEndWord != null) {
                endIndex = i
                Log.d("TextProcessor", "✅ Endwort erkannt: '$matchedEndWord' in Zeile $i: '$originalLine'")
                break
            }
        }

        for (i in startIndex until endIndex) {
            val line = lines[i].trim()
            if (line.isNotBlank()) {

                // Prüfen, ob die Zeile *nur* aus Zahlen (inkl. Punkt, Komma, Leerzeichen) und einem optionalen Minuszeichen besteht
                val isOnlyNumbers = line.matches(Regex("""^[\d.,\s-]+$"""))

                if (isOnlyNumbers) {
                    prices.add(line)
                    Log.d("TextProcessor", "Prices: '$line'")
                    continue
                }

                // Bereinige den Produktnamen, indem wir die " 1" entfernen
                val cleanedProductName = line.replace(Regex("""\s\d+$"""), "")

                // Füge bereinigten Produktnamen zur Liste hinzu, aber setze den Preis noch als "0"
                productList.add(Pair(cleanedProductName, "0"))
            }
        }

        val cleanedPrices = prices
            .map { it.replace(Regex("""\s\d+$"""), "") }
            .filter { it.contains(".") } // nur Preise mit Dezimalpunkt behalten

        Log.d("TextProcessor Cleaned Prices", cleanedPrices.toString())

        for (i in 0 until productList.size) {
            val product = productList[i]
            val priceString = cleanedPrices[i]
            val priceDouble = priceString.toDoubleOrNull() ?: 0.0
            val roundedPrice = roundToNearest5Rappen(priceDouble)
            productList[i] = product.copy(second = String.format("%.2f", roundedPrice))  // falls du 2 Nachkommastellen willst
            Log.d("TextProcessor", "Produkt: '${product.first}' → Preis: '$roundedPrice'")
        }

        Log.d("TextProcessor", "✅ Erkannte Produkte: $productList")
        return productList
    }

    fun roundToNearest5Rappen(amount: Double): Double {
        return (amount / 0.05).roundToInt() * 0.05
    }

    fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j

        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[a.length][b.length]
    }

    fun getStoreName(text: String): String {
        val storeNames = listOf("migros", "cumulus", "cumulusnummer")
        val threshold = 4  // Etwas enger, da einzelne Wörter verglichen werden

        // Durchlaufe jede Zeile
        text.lines().forEach { line ->
            // Entferne Bindestriche und Punkte, aber behalte Leerzeichen
            val cleanedLine = line.replace(Regex("[-.]"), "").lowercase(Locale.ROOT)

            // Zerlege die Zeile in Wörter
            val words = cleanedLine.split(Regex("\\s+"))

            for (word in words) {
                for (store in storeNames) {
                    val distance = levenshtein(word, store)
                    //Log.d("TextProcessor StoreName", "Distanz zwischen '$word' und '$store' = $distance")

                    if (distance <= threshold) {
                        return "Migros"
                    }
                }
            }
        }

        return "Unbekannt"
    }




}

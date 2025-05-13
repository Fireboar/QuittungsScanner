package com.example.quittungsscanner.data.receipt

import android.util.Log
import kotlin.math.absoluteValue

object TextProcessor {

    fun extractProducts(text: String): List<Pair<String, String>> {
        Log.d("ReceiptScreen", text)
        val productNames = extractProductNames(text)
        val prices = extractPrices(text)
        val limitedPrices = prices.take(productNames.size)

        val productPairs = mutableListOf<Pair<String, String>>()

        for (i in productNames.indices) {
            val name = productNames[i].trim()
            val price = if (i < limitedPrices.size) limitedPrices[i] else "0.00"
            if (name.isNotEmpty()) {
                productPairs.add(Pair(name, price))
            }
        }

        Log.d("ReceiptScreen", productPairs.toString())

        return productPairs
    }

    fun extractProductNames(text: String): List<String> {
        val lines = text.lines()
        val productLines = mutableListOf<String>()

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
            Log.d("ReceiptScreen Products", "❌ Kein Startwort gefunden")
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
                Log.d("ReceiptScreen Products", "✅ Endwort erkannt: '$matchedEndWord' in Zeile $i: '$originalLine'")
                break
            }
        }

        for (i in startIndex until endIndex) {
            val line = lines[i].trim()
            if (line.isNotBlank()) {

                // Prüfen, ob die Zeile *nur* aus Zahlen (inkl. Punkt, Komma, Leerzeichen) und einem optionalen Minuszeichen besteht
                val isOnlyNumbers = line.matches(Regex("""^[\d.,\s-]+$"""))

                // Extrahiere Zahlen (inkl. möglichem Minuszeichen)
                val numberRegex = Regex("""-?\d+[.,]?\d*""")
                val foundNumbers = numberRegex.findAll(line).map { it.value.toDoubleOrNull() }.filterNotNull().toList()

                // Prüfen, ob eine Zahl kleiner als 0.1 oder negativ ist
                val hasInvalidPrice = foundNumbers.any { it.absoluteValue < 0.1 }

                if (isOnlyNumbers || hasInvalidPrice) {
                    Log.d("ReceiptScreen Products", "⏭️ Überspringe ungültige oder zu kleine Zahlenzeile: '$line'")
                    continue
                }

                productLines.add(line)

                Log.d("ReceiptScreen Products", "📦 Zeile: '$line' → Gefundene Zahlen: $foundNumbers")
            }
        }


        Log.d("ReceiptScreen Products", "✅ Erkannte Produktzeilen: $productLines")


        return productLines
    }

    fun extractPrices(text: String): List<String> {
        val lines = text.lines()
        val prices = mutableListOf<String>()

        val startTargets = listOf("preis", "gespart")
        val endTarget = "artikelbezeichnung"

        val maxStartDistance = 4
        val maxEndDistance = 6

        var startIndex = -1
        var endIndex = -1

        // Starte mit Zeilenanalyse
        for ((i, line) in lines.withIndex()) {
            val cleanedLine = line.lowercase().replace("[^a-z]".toRegex(), "")

            // Prüfe alle möglichen Startbegriffe
            if (startIndex == -1 && startTargets.any { levenshtein(cleanedLine, it) <= maxStartDistance }) {
                startIndex = i + 1
                continue
            }

            // Prüfe das Ende (Artikelbezeichnung)
            if (startIndex != -1 && levenshtein(cleanedLine, endTarget) <= maxEndDistance) {
                endIndex = i
                break
            }
        }

        if (startIndex == -1 || endIndex == -1 || endIndex <= startIndex) {
            Log.d("ReceiptScreen Prices", "Start oder Ende nicht gefunden oder ungültig")
            return emptyList()
        }

        val priceBlock = lines.subList(startIndex, endIndex)

        val priceRegex = Regex("""\d{1,3}([.:,])\d{1,2}[a-zA-Z]*""")  // erlaubt z. B. "10.35T", "2.50CHF"


        for (line in priceBlock) {
            val matches = priceRegex.findAll(line)
            for (match in matches) {
                val rawPrice = match.value
                val cleanedPrice = rawPrice
                    .replace(":", ".")              // Einheitlich auf Dezimalpunkt
                    .replace("[^\\d.]".toRegex(), "") // Entfernt Buchstaben etc., behält Ziffern und Punkt
                prices.add(cleanedPrice)
            }
        }

        Log.d("ReceiptScreen Prices", prices.toString())
        return prices
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



}

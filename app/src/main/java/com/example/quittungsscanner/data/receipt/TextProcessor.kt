package com.example.quittungsscanner.data.receipt

import android.util.Log

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

        Log.d("ReceiptScreen Products", productPairs.toString())

        return productPairs
    }

    fun extractProductNames(text: String): List<String> {
        val lines = text.lines()
        val productLines = mutableListOf<String>()

        var startIndex = -1
        var endIndex = -1

        // Suchbegriffe
        val targetStart = "artikelbezeichnung"
        val targetEnd = "total chf"
        val possibleEndWords = listOf("total chf", "sie sparen total", "total", "total in eur")

        // Levenshtein-Toleranzen
        val maxStartDistance = 4
        val maxEndDistance = 6

        // Zeile für Zeile analysieren
        for ((i, line) in lines.withIndex()) {
            val cleanedLine = line.lowercase().replace("[^a-z]".toRegex(), "")

            val startMatch = Regex("artikel[bsz]e?zeich(n|n?u|nu?g|ung)?", RegexOption.IGNORE_CASE).containsMatchIn(line)
            val fuzzyStartMatch = levenshtein(cleanedLine, targetStart) <= maxStartDistance

            if (startIndex == -1 && (startMatch || fuzzyStartMatch)) {
                startIndex = i + 1
                continue
            }

            if (startIndex != -1) {
                val isFuzzyEndMatch = possibleEndWords.any {
                    levenshtein(cleanedLine, it.replace(" ", "")) <= maxEndDistance
                }
                if (isFuzzyEndMatch) {
                    endIndex = i
                    break
                }
            }
        }

        if (startIndex == -1 || endIndex == -1 || endIndex <= startIndex) {
            Log.d("ReceiptScreen Names", "Start oder Ende nicht erkannt oder ungültig")
            return emptyList()
        }

        // Zwischen Start- und Endzeile extrahieren
        for (i in startIndex until endIndex) {
            val cleanLine = lines[i].trim()
            if (cleanLine.isNotEmpty() && cleanLine.any { it.isLetter() }) {
                val lineWithoutNumbers = cleanLine.replace(Regex("\\d+"), "")
                productLines.add(lineWithoutNumbers.trim())
            }
        }

        Log.d("ReceiptScreen Names", productLines.toString())
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

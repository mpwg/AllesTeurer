package eu.mpwg.allesteurer.domain.parsing

import eu.mpwg.allesteurer.domain.ocr.*
import kotlin.math.max

/**
 * Comprehensive German receipt parser that extracts structured data from OCR text.
 * Optimized for major German retailers and common receipt formats.
 */
class GermanReceiptParser {
    
    /**
     * Parses OCR text as a German receipt and extracts structured information.
     * 
     * @param ocrResult Raw OCR result to parse
     * @return Structured receipt data with extracted fields and validation
     */
    fun parseReceiptText(ocrResult: OCRResult): ReceiptOCRResult {
        val lines = ocrResult.text.lines().filter { it.trim().isNotEmpty() }
        
        // Extract main receipt components
        val storeName = extractStoreName(lines)
        val receiptDate = extractDate(lines)
        val receiptTime = extractTime(lines)
        val totalAmount = extractTotalAmount(lines)
        val subtotal = extractSubtotal(lines)
        val taxAmount = extractTaxAmount(lines)
        val items = extractItems(lines, ocrResult.boundingBoxes)
        
        // Validate extracted data
        val parseErrors = validateParsedData(storeName, receiptDate, totalAmount, items)
        
        // Calculate overall confidence
        val confidence = calculateOverallConfidence(ocrResult.confidence, parseErrors, items.size)
        
        return ReceiptOCRResult(
            rawText = ocrResult.text,
            storeName = storeName,
            receiptDate = receiptDate,
            receiptTime = receiptTime,
            totalAmount = totalAmount,
            subtotal = subtotal,
            taxAmount = taxAmount,
            items = items,
            confidence = confidence,
            parseErrors = parseErrors
        )
    }
    
    /**
     * Extracts store name from receipt text using German retailer patterns.
     */
    private fun extractStoreName(lines: List<String>): String? {
        // Check first 5 lines for store patterns (stores usually appear at the top)
        val searchLines = lines.take(5)
        
        for (line in searchLines) {
            val cleanLine = line.trim().uppercase()
            
            // Try exact matches first
            for (pattern in STORE_PATTERNS) {
                val match = pattern.find(cleanLine)
                if (match != null) {
                    return formatStoreName(match.value)
                }
            }
            
            // Try fuzzy matching for known retailers
            for (retailer in KNOWN_RETAILERS) {
                if (cleanLine.contains(retailer, ignoreCase = true)) {
                    return formatStoreName(retailer)
                }
            }
        }
        
        // Fallback: try to extract any business-looking name from first 3 lines
        for (line in searchLines.take(3)) {
            val match = GENERIC_BUSINESS_PATTERN.find(line.trim())
            if (match != null && match.value.length > 3) {
                return formatStoreName(match.value)
            }
        }
        
        return null
    }
    
    /**
     * Extracts receipt date using common German date formats.
     */
    private fun extractDate(lines: List<String>): String? {
        for (line in lines) {
            for (pattern in DATE_PATTERNS) {
                val match = pattern.find(line)
                if (match != null) {
                    return normalizeDate(match.value)
                }
            }
        }
        return null
    }
    
    /**
     * Extracts receipt time using German time formats.
     */
    private fun extractTime(lines: List<String>): String? {
        for (line in lines) {
            val match = TIME_PATTERN.find(line)
            if (match != null) {
                return match.value
            }
        }
        return null
    }
    
    /**
     * Extracts total amount from receipt using German currency patterns.
     */
    private fun extractTotalAmount(lines: List<String>): String? {
        // Search in reverse order as total is usually near the bottom
        val reversedLines = lines.reversed()
        
        for (line in reversedLines) {
            for (pattern in TOTAL_AMOUNT_PATTERNS) {
                val match = pattern.find(line)
                if (match != null) {
                    val amount = extractAmountFromMatch(match)
                    if (amount != null && isValidAmount(amount)) {
                        return normalizeAmount(amount)
                    }
                }
            }
        }
        
        return null
    }
    
    /**
     * Extracts subtotal amount (before tax).
     */
    private fun extractSubtotal(lines: List<String>): String? {
        val reversedLines = lines.reversed()
        
        for (line in reversedLines) {
            for (pattern in SUBTOTAL_PATTERNS) {
                val match = pattern.find(line)
                if (match != null) {
                    val amount = extractAmountFromMatch(match)
                    if (amount != null && isValidAmount(amount)) {
                        return normalizeAmount(amount)
                    }
                }
            }
        }
        
        return null
    }
    
    /**
     * Extracts tax amount from receipt.
     */
    private fun extractTaxAmount(lines: List<String>): String? {
        val reversedLines = lines.reversed()
        
        for (line in reversedLines) {
            for (pattern in TAX_PATTERNS) {
                val match = pattern.find(line)
                if (match != null) {
                    val amount = extractAmountFromMatch(match)
                    if (amount != null && isValidAmount(amount)) {
                        return normalizeAmount(amount)
                    }
                }
            }
        }
        
        return null
    }
    
    /**
     * Extracts individual line items from the receipt.
     */
    private fun extractItems(lines: List<String>, boundingBoxes: List<TextBoundingBox>): List<ReceiptLineItem> {
        val items = mutableListOf<ReceiptLineItem>()
        
        // Skip header (first few lines) and footer (last few lines)
        val contentLines = if (lines.size > 10) {
            lines.drop(3).dropLast(5)
        } else {
            lines.drop(1).dropLast(2)
        }
        
        for (line in contentLines) {
            // Skip lines that look like headers, totals, or other non-item content
            if (shouldSkipLine(line)) continue
            
            for (pattern in ITEM_PATTERNS) {
                val match = pattern.find(line)
                if (match != null) {
                    val boundingBox = findBoundingBoxForText(line, boundingBoxes)
                    val item = createLineItem(line, match, boundingBox)
                    if (item != null) {
                        items.add(item)
                        break // Stop at first successful pattern match
                    }
                }
            }
        }
        
        return items
    }
    
    /**
     * Creates a ReceiptLineItem from a regex match.
     */
    private fun createLineItem(
        originalLine: String,
        match: MatchResult,
        boundingBox: TextBoundingBox?
    ): ReceiptLineItem? {
        val groups = match.groups
        
        return when (groups.size) {
            3 -> { // Pattern: Name Price
                val name = groups[1]?.value?.trim()
                val price = groups[2]?.value?.trim()
                
                if (!name.isNullOrBlank() && !price.isNullOrBlank() && isValidAmount(price)) {
                    ReceiptLineItem(
                        text = originalLine,
                        extractedName = cleanProductName(name),
                        extractedPrice = normalizeAmount(price),
                        confidence = boundingBox?.confidence ?: 0.7f,
                        boundingBox = boundingBox ?: createDefaultBoundingBox(originalLine)
                    )
                } else null
            }
            4 -> { // Pattern: Quantity Name Price
                val quantity = groups[1]?.value?.trim()
                val name = groups[2]?.value?.trim()
                val price = groups[3]?.value?.trim()
                
                if (!name.isNullOrBlank() && !price.isNullOrBlank() && isValidAmount(price)) {
                    ReceiptLineItem(
                        text = originalLine,
                        extractedName = cleanProductName(name),
                        extractedPrice = normalizeAmount(price),
                        extractedQuantity = quantity?.takeIf { it.toIntOrNull() != null },
                        confidence = boundingBox?.confidence ?: 0.7f,
                        boundingBox = boundingBox ?: createDefaultBoundingBox(originalLine)
                    )
                } else null
            }
            else -> null
        }
    }
    
    /**
     * Validates extracted receipt data and returns list of errors.
     */
    private fun validateParsedData(
        storeName: String?,
        receiptDate: String?,
        totalAmount: String?,
        items: List<ReceiptLineItem>
    ): List<ReceiptParseError> {
        val errors = mutableListOf<ReceiptParseError>()
        
        // Check required fields
        if (storeName.isNullOrBlank()) {
            errors.add(ReceiptParseError.MissingRequiredField("storeName", "Store name could not be extracted"))
        }
        
        if (receiptDate.isNullOrBlank()) {
            errors.add(ReceiptParseError.MissingRequiredField("receiptDate", "Receipt date could not be extracted"))
        }
        
        if (totalAmount.isNullOrBlank()) {
            errors.add(ReceiptParseError.MissingRequiredField("totalAmount", "Total amount could not be extracted"))
        } else {
            // Validate amount format
            if (!isValidAmount(totalAmount)) {
                errors.add(ReceiptParseError.InvalidCurrencyFormat(totalAmount))
            }
        }
        
        // Validate items
        if (items.isEmpty()) {
            errors.add(ReceiptParseError.NoItemsFound("No recognizable items found in receipt text"))
        } else {
            // Check if item prices sum up reasonably close to total
            val itemSum = items.mapNotNull { it.priceAsDouble }.sum()
            val total = totalAmount?.replace(",", ".")?.toDoubleOrNull()
            
            if (total != null && itemSum > 0 && kotlin.math.abs(total - itemSum) > total * 0.2) {
                errors.add(ReceiptParseError.ValidationFailed(
                    "itemSum", 
                    "Item sum ($itemSum) differs significantly from total ($total)"
                ))
            }
        }
        
        // Check if store is recognized
        if (storeName != null && !isKnownGermanRetailer(storeName)) {
            errors.add(ReceiptParseError.UnknownStore(storeName))
        }
        
        return errors
    }
    
    /**
     * Calculates overall parsing confidence based on OCR confidence, errors, and extracted data quality.
     */
    private fun calculateOverallConfidence(
        ocrConfidence: Float,
        parseErrors: List<ReceiptParseError>,
        itemCount: Int
    ): Float {
        var confidence = ocrConfidence
        
        // Reduce confidence for each error
        val criticalErrors = parseErrors.count { it.isCritical }
        val minorErrors = parseErrors.size - criticalErrors
        
        confidence -= (criticalErrors * 0.25f) // -25% per critical error
        confidence -= (minorErrors * 0.1f)     // -10% per minor error
        
        // Boost confidence if we found items (indicates successful parsing)
        if (itemCount > 0) {
            confidence += 0.1f
            if (itemCount >= 3) confidence += 0.05f // Bonus for multiple items
        }
        
        return max(0.1f, confidence).coerceAtMost(1.0f)
    }
    
    // --- Helper Methods ---
    
    private fun formatStoreName(name: String): String {
        return name.trim()
            .replace(Regex("\\s+"), " ") // Normalize whitespace
            .split(" ")
            .joinToString(" ") { word ->
                if (word.length > 1 && word.all { it.isUpperCase() }) {
                    word // Keep all-caps words as-is (like REWE, ALDI)
                } else {
                    word.lowercase().replaceFirstChar { it.uppercase() }
                }
            }
    }
    
    private fun normalizeDate(dateText: String): String {
        // Try to normalize various German date formats to DD.MM.YYYY
        return dateText.replace(Regex("[/-]"), ".")
    }
    
    private fun normalizeAmount(amountText: String): String {
        return amountText
            .replace("€", "")
            .replace(" ", "")
            .replace(",", ".")
            .trim()
    }
    
    private fun extractAmountFromMatch(match: MatchResult): String? {
        // Try to find the amount in the match groups
        for (i in 1 until match.groups.size) {
            val group = match.groups[i]?.value
            if (group != null && AMOUNT_PATTERN.matches(group)) {
                return group
            }
        }
        return null
    }
    
    private fun isValidAmount(amount: String): Boolean {
        val cleaned = normalizeAmount(amount)
        return cleaned.toDoubleOrNull() != null && cleaned.toDouble() > 0
    }
    
    private fun cleanProductName(name: String): String {
        return name.trim()
            .replace(Regex("\\s+"), " ") // Normalize whitespace
            .replace(Regex("^\\d+\\s*x?\\s+"), "") // Remove leading quantity like "2x "
    }
    
    private fun shouldSkipLine(line: String): Boolean {
        val upperLine = line.uppercase()
        return SKIP_LINE_PATTERNS.any { upperLine.contains(it) }
    }
    
    private fun findBoundingBoxForText(text: String, boundingBoxes: List<TextBoundingBox>): TextBoundingBox? {
        return boundingBoxes.find { box ->
            box.text.contains(text, ignoreCase = true) || 
            text.contains(box.text, ignoreCase = true) ||
            levenshteinDistance(box.text.lowercase(), text.lowercase()) < 3
        }
    }
    
    private fun createDefaultBoundingBox(text: String): TextBoundingBox {
        return TextBoundingBox(
            text = text,
            x = 0f,
            y = 0f,
            width = 1f,
            height = 0.05f,
            confidence = 0.5f
        )
    }
    
    private fun isKnownGermanRetailer(storeName: String): Boolean {
        return KNOWN_RETAILERS.any { retailer ->
            storeName.uppercase().contains(retailer.uppercase())
        }
    }
    
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        
        return dp[s1.length][s2.length]
    }
    
    companion object {
        // --- Pattern Definitions ---
        
        /**
         * Patterns for German store/retailer recognition.
         */
        val STORE_PATTERNS = listOf(
            Regex("""(REWE\s*(?:MARKT|CENTER|CITY|GROUP)?(?:\s*GMBH)?(?:\s*&\s*CO\.?\s*KG)?)""", RegexOption.IGNORE_CASE),
            Regex("""(EDEKA\s*(?:CENTER|MARKT|NEUKAUF)?(?:\s*GMBH)?(?:\s*&\s*CO\.?\s*KG)?)""", RegexOption.IGNORE_CASE),
            Regex("""(ALDI\s*(?:SÜD|NORD)?(?:\s*GMBH)?(?:\s*&\s*CO\.?\s*KG)?)""", RegexOption.IGNORE_CASE),
            Regex("""(LIDL\s*(?:STIFTUNG)?(?:\s*&\s*CO\.?\s*KG)?(?:\s*GMBH)?)""", RegexOption.IGNORE_CASE),
            Regex("""(PENNY\s*(?:MARKT)?(?:\s*GMBH)?)""", RegexOption.IGNORE_CASE),
            Regex("""(NETTO\s*(?:MARKEN-DISCOUNT)?(?:\s*STIFTUNG)?(?:\s*&\s*CO\.?\s*KG)?)""", RegexOption.IGNORE_CASE),
            Regex("""(KAUFLAND\s*(?:WARENHANDEL)?(?:\s*GMBH)?(?:\s*&\s*CO\.?\s*KG)?)""", RegexOption.IGNORE_CASE),
            Regex("""(DM\s*(?:DROGERIE\s*MARKT)?(?:\s*GMBH)?(?:\s*&\s*CO\.?\s*KG)?)""", RegexOption.IGNORE_CASE),
            Regex("""(ROSSMANN\s*(?:DROGERIEMÄRKTE)?(?:\s*GMBH)?)""", RegexOption.IGNORE_CASE),
            Regex("""(MÜLLER\s*(?:DROGERIEMARKT|DROGERIE)?(?:\s*GMBH)?)""", RegexOption.IGNORE_CASE),
            Regex("""(SATURN\s*(?:TECH\s*MARKT)?(?:\s*GMBH)?)""", RegexOption.IGNORE_CASE),
            Regex("""(MEDIAMARKT|MEDIA\s*MARKT)""", RegexOption.IGNORE_CASE),
        )
        
        /**
         * Fallback pattern for generic business names.
         */
        val GENERIC_BUSINESS_PATTERN = Regex("""([A-ZÄÖÜ][a-zA-ZäöüÄÖÜß\s&.-]{2,30}(?:\s*GmbH|\s*KG|\s*AG)?)""")
        
        /**
         * List of known German retailers for validation.
         */
        val KNOWN_RETAILERS = listOf(
            "REWE", "EDEKA", "ALDI", "LIDL", "PENNY", "NETTO", "KAUFLAND",
            "DM", "ROSSMANN", "MÜLLER", "SATURN", "MEDIAMARKT", "REAL",
            "GLOBUS", "MARKTKAUF", "FAMILA", "COMBI", "TEGUT", "HIT"
        )
        
        /**
         * Patterns for German date formats.
         */
        val DATE_PATTERNS = listOf(
            Regex("""(\d{1,2})[.\-/](\d{1,2})[.\-/](\d{4})"""), // DD.MM.YYYY, DD-MM-YYYY, DD/MM/YYYY
            Regex("""(\d{4})[.\-/](\d{1,2})[.\-/](\d{1,2})"""), // YYYY.MM.DD, YYYY-MM-DD, YYYY/MM/DD
            Regex("""(\d{1,2})\.(\d{1,2})\.(\d{2})"""),         // DD.MM.YY
        )
        
        /**
         * Pattern for time formats.
         */
        val TIME_PATTERN = Regex("""(\d{1,2}):(\d{2})(?::(\d{2}))?""")
        
        /**
         * Patterns for total amount extraction.
         */
        val TOTAL_AMOUNT_PATTERNS = listOf(
            Regex("""(?:SUMME|GESAMT|TOTAL|BETRAG)\s*:?\s*€?\s*(\d+[,.]\d{2})\s*€?""", RegexOption.IGNORE_CASE),
            Regex("""(?:SUMME|GESAMT|TOTAL|BETRAG)\s*:?\s*(\d+[,.]\d{2})\s*€""", RegexOption.IGNORE_CASE),
            Regex("""€\s*(\d+[,.]\d{2})(?:\s*SUMME|\s*GESAMT|\s*TOTAL)?""", RegexOption.IGNORE_CASE),
            Regex("""(\d+[,.]\d{2})\s*€?\s*$"""), // Amount at end of line
        )
        
        /**
         * Patterns for subtotal extraction.
         */
        val SUBTOTAL_PATTERNS = listOf(
            Regex("""(?:ZWISCHENSUMME|NETTO|SUMME\s*NETTO)\s*:?\s*€?\s*(\d+[,.]\d{2})\s*€?""", RegexOption.IGNORE_CASE),
        )
        
        /**
         * Patterns for tax amount extraction.
         */
        val TAX_PATTERNS = listOf(
            Regex("""(?:MWST|MwSt|STEUER|USt)\s*(?:\d+(?:[.,]\d+)?%)?\s*:?\s*€?\s*(\d+[,.]\d{2})\s*€?""", RegexOption.IGNORE_CASE),
        )
        
        /**
         * Patterns for line item extraction.
         */
        val ITEM_PATTERNS = listOf(
            // Pattern 1: Name followed by price: "Milch 1L  2.49€"
            Regex("""^([A-Za-zäöüÄÖÜß\s&.-]{3,40})\s+(\d+[,.]\d{2})\s*€?\s*$"""),
            
            // Pattern 2: Quantity, name, then price: "2 Äpfel Braeburn  4.99"
            Regex("""^(\d+)\s+([A-Za-zäöüÄÖÜß\s&.-]{3,40})\s+(\d+[,.]\d{2})\s*€?\s*$"""),
            
            // Pattern 3: Name with price at end: "Brot Vollkorn 500g  €3.29"
            Regex("""^([A-Za-zäöüÄÖÜß\s&.-]{3,40})\s+€?\s*(\d+[,.]\d{2})\s*$"""),
            
            // Pattern 4: More flexible pattern with various separators
            Regex("""^([A-Za-zäöüÄÖÜß\s&.-]{3,30})\s+[A-Z*]?\s*(\d+[,.]\d{2})\s*€?\s*[A-Z]?\s*$"""),
        )
        
        /**
         * Pattern for validating amounts.
         */
        val AMOUNT_PATTERN = Regex("""\d+[,.]\d{2}""")
        
        /**
         * Patterns to skip when looking for items.
         */
        val SKIP_LINE_PATTERNS = listOf(
            "DATUM", "ZEIT", "UHRZEIT", "KASSE", "KASSIERER", "BEDIENUNG",
            "SUMME", "GESAMT", "TOTAL", "BETRAG", "MWST", "STEUER", "UST",
            "GEGEBEN", "RÜCKGELD", "KARTENZAHLUNG", "BAR", "EC-KARTE",
            "VIELEN DANK", "DANKE", "AUF WIEDERSEHEN", "BIS BALD",
            "TELEFON", "FAX", "WWW", "EMAIL", "@", "HTTP",
            "ÖFFNUNGSZEITEN", "ADRESSE", "GESCHÄFTSFÜHRER",
            "----------------------------------------",
            "========================================",
            "ST-NR", "UST-ID", "HRB", "AMTSGERICHT"
        )
    }
}
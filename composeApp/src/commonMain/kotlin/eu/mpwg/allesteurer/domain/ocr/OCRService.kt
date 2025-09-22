package eu.mpwg.allesteurer.domain.ocr

/**
 * Shared OCR service interface that platform-specific implementations will provide.
 * This uses the expect/actual pattern to enable native OCR implementations on each platform.
 */
expect class OCRService {
    /**
     * Recognizes text from image bytes and returns raw OCR result.
     * 
     * @param imageBytes Raw image data
     * @param configuration OCR processing configuration
     * @return Result containing OCR data or error
     */
    suspend fun recognizeText(
        imageBytes: ByteArray,
        configuration: OCRConfiguration = OCRConfiguration()
    ): Result<OCRResult>

    /**
     * Recognizes text from image bytes and parses it as a German receipt.
     * This method combines OCR with German receipt-specific parsing.
     * 
     * @param imageBytes Raw image data
     * @param configuration OCR processing configuration
     * @return Result containing parsed receipt data or error
     */
    suspend fun recognizeReceiptText(
        imageBytes: ByteArray,
        configuration: OCRConfiguration = OCRConfiguration()
    ): Result<ReceiptOCRResult>

    /**
     * Checks if OCR service is available on this platform.
     * 
     * @return true if OCR functionality is available
     */
    fun isAvailable(): Boolean

    /**
     * Gets list of supported languages for OCR recognition.
     * 
     * @return List of language codes (e.g., ["de", "en", "fr"])
     */
    fun getSupportedLanguages(): List<String>
}

/**
 * Configuration for OCR processing, optimized for German receipt recognition.
 * 
 * @property language Primary language for recognition (default: German)
 * @property recognitionLevel Speed vs accuracy trade-off
 * @property customWords Additional words to improve recognition accuracy
 * @property enableReceiptOptimization Enable receipt-specific preprocessing
 */
data class OCRConfiguration(
    val language: String = "de",
    val recognitionLevel: RecognitionLevel = RecognitionLevel.ACCURATE,
    val customWords: List<String> = GERMAN_RECEIPT_WORDS,
    val enableReceiptOptimization: Boolean = true
) {
    companion object {
        /**
         * Common German words found on receipts to improve OCR accuracy.
         */
        val GERMAN_RECEIPT_WORDS = listOf(
            // Common receipt terms
            "Summe", "Gesamt", "Total", "Betrag", "Datum", "Uhrzeit",
            "MwSt", "Steuer", "Mehrwertsteuer", "Netto", "Brutto",
            
            // German retailers
            "REWE", "EDEKA", "ALDI", "LIDL", "PENNY", "NETTO", "KAUFLAND",
            "dm", "ROSSMANN", "MÜLLER", "SATURN", "MediaMarkt",
            
            // Common products
            "Milch", "Brot", "Käse", "Butter", "Eier", "Fleisch", "Wurst",
            "Obst", "Gemüse", "Bananen", "Äpfel", "Kartoffeln",
            
            // Units and quantities
            "Stück", "Packung", "Liter", "Kilogramm", "Gramm", "ml", "cl",
            
            // German characters that might be problematic
            "ä", "ö", "ü", "Ä", "Ö", "Ü", "ß"
        )
    }
}

/**
 * Recognition level determines the speed vs accuracy trade-off for OCR processing.
 */
enum class RecognitionLevel {
    /**
     * Fast recognition with lower accuracy - suitable for preview or draft processing.
     */
    FAST,
    
    /**
     * Balanced speed and accuracy - recommended for most use cases.
     */
    ACCURATE,
    
    /**
     * Maximum accuracy with slower processing - use for final processing.
     */
    PRECISE
}
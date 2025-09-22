package eu.mpwg.allesteurer.domain.ocr

import kotlinx.serialization.Serializable

/**
 * Raw OCR result from platform-specific implementations.
 * Contains the unprocessed text and metadata from the OCR engine.
 * 
 * @property text Complete recognized text from the image
 * @property confidence Overall OCR confidence score (0.0 to 1.0)
 * @property boundingBoxes Location information for each recognized text element
 * @property language Detected language code (e.g., "de", "en")
 * @property processingTimeMs Time taken for OCR processing in milliseconds
 * @property errors List of non-fatal errors encountered during processing
 */
@Serializable
data class OCRResult(
    val text: String,
    val confidence: Float,
    val boundingBoxes: List<TextBoundingBox> = emptyList(),
    val language: String,
    val processingTimeMs: Long,
    val errors: List<OCRError> = emptyList()
) {
    /**
     * Returns true if OCR confidence meets minimum threshold.
     */
    val hasAcceptableConfidence: Boolean
        get() = confidence >= 0.7f
        
    /**
     * Returns true if OCR confidence is high (above 0.85).
     */
    val hasHighConfidence: Boolean
        get() = confidence >= 0.85f
}

/**
 * Receipt-specific OCR result with parsed structure.
 * Contains extracted and validated receipt information.
 * 
 * @property rawText Original OCR text
 * @property storeName Extracted store/business name
 * @property receiptDate Extracted receipt date (in German format)
 * @property receiptTime Extracted receipt time (HH:MM:SS format)
 * @property totalAmount Extracted total amount (as string to preserve formatting)
 * @property subtotal Subtotal before tax (if available)
 * @property taxAmount Tax amount (if available)
 * @property items List of extracted line items
 * @property confidence Overall parsing confidence (0.0 to 1.0)
 * @property parseErrors List of parsing errors encountered
 */
@Serializable
data class ReceiptOCRResult(
    val rawText: String,
    val storeName: String?,
    val receiptDate: String?,
    val receiptTime: String? = null,
    val totalAmount: String?,
    val subtotal: String? = null,
    val taxAmount: String? = null,
    val items: List<ReceiptLineItem> = emptyList(),
    val confidence: Float,
    val parseErrors: List<ReceiptParseError> = emptyList()
) {
    /**
     * Returns true if all required fields were successfully extracted.
     */
    val hasRequiredFields: Boolean
        get() = !storeName.isNullOrBlank() && 
                !receiptDate.isNullOrBlank() && 
                !totalAmount.isNullOrBlank()
    
    /**
     * Returns true if parsing confidence meets minimum threshold.
     */
    val hasAcceptableConfidence: Boolean
        get() = confidence >= 0.6f
        
    /**
     * Returns true if at least one item was extracted.
     */
    val hasItems: Boolean
        get() = items.isNotEmpty()
        
    /**
     * Returns the number of critical parsing errors.
     */
    val criticalErrorCount: Int
        get() = parseErrors.count { it.isCritical }
}

/**
 * Text bounding box information for precise text location.
 * 
 * @property text The recognized text within this bounding box
 * @property x Left coordinate (normalized 0.0 to 1.0)
 * @property y Top coordinate (normalized 0.0 to 1.0)
 * @property width Width of bounding box (normalized 0.0 to 1.0)
 * @property height Height of bounding box (normalized 0.0 to 1.0)
 * @property confidence Confidence score for this specific text element
 */
@Serializable
data class TextBoundingBox(
    val text: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val confidence: Float
) {
    /**
     * Returns the center X coordinate of the bounding box.
     */
    val centerX: Float
        get() = x + (width / 2f)
        
    /**
     * Returns the center Y coordinate of the bounding box.
     */
    val centerY: Float
        get() = y + (height / 2f)
}

/**
 * Individual line item extracted from a receipt.
 * 
 * @property text Original text line from OCR
 * @property extractedName Parsed product/item name
 * @property extractedPrice Parsed price (as string to preserve formatting)
 * @property extractedQuantity Parsed quantity (if available)
 * @property confidence Confidence score for this extraction
 * @property boundingBox Location information for this line item
 */
@Serializable
data class ReceiptLineItem(
    val text: String,
    val extractedName: String?,
    val extractedPrice: String?,
    val extractedQuantity: String? = null,
    val confidence: Float,
    val boundingBox: TextBoundingBox
) {
    /**
     * Returns true if both name and price were successfully extracted.
     */
    val isComplete: Boolean
        get() = !extractedName.isNullOrBlank() && !extractedPrice.isNullOrBlank()
        
    /**
     * Returns the price as a Double, or null if parsing fails.
     */
    val priceAsDouble: Double?
        get() = extractedPrice?.replace(",", ".")?.toDoubleOrNull()
        
    /**
     * Returns the quantity as an Integer, or 1 if not specified.
     */
    val quantityAsInt: Int
        get() = extractedQuantity?.toIntOrNull() ?: 1
}
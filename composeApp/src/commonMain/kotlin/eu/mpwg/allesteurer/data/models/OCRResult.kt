package eu.mpwg.allesteurer.data.models

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlin.random.Random

/**
 * OCRResult data model representing the result of OCR processing on a receipt image.
 * 
 * @property id Unique identifier for the OCR result
 * @property rawText Complete raw text extracted from the image
 * @property confidence Overall OCR confidence score (0.0 to 1.0)
 * @property detectedLanguage Detected text language (e.g., "de", "en")
 * @property extractedReceipt Parsed receipt information
 * @property extractedItems List of parsed line items
 * @property processingTimeMs Time taken to process the image in milliseconds
 * @property imageMetadata Information about the processed image
 * @property errors List of errors or issues encountered during processing
 * @property createdAt Timestamp when the OCR was performed
 */
@Serializable
data class OCRResult(
    val id: String = generateId(),
    val rawText: String,
    val confidence: Double,
    val detectedLanguage: String = "de",
    val extractedReceipt: ExtractedReceipt? = null,
    val extractedItems: List<ExtractedItem> = emptyList(),
    val processingTimeMs: Long,
    val imageMetadata: ImageMetadata? = null,
    val errors: List<String> = emptyList(),
    val createdAt: Instant = ModelUtils.now()
) {
    companion object {
        /**
         * Generate a unique ID for a new OCR result.
         */
        fun generateId(): String = "ocr_${Random.nextLong().toString(36)}_${ModelUtils.nowMillis()}"
    }
    
    /**
     * Returns true if OCR confidence is above threshold (0.8).
     */
    val hasHighConfidence: Boolean
        get() = confidence >= 0.8
    
    /**
     * Returns true if receipt information was successfully extracted.
     */
    val hasReceiptInfo: Boolean
        get() = extractedReceipt != null
    
    /**
     * Returns true if line items were successfully extracted.
     */
    val hasItems: Boolean
        get() = extractedItems.isNotEmpty()
    
    /**
     * Returns true if any errors occurred during processing.
     */
    val hasErrors: Boolean
        get() = errors.isNotEmpty()
    
    /**
     * Returns the number of successfully extracted items.
     */
    val itemCount: Int
        get() = extractedItems.size
    
    /**
     * Validates that the OCR result has valid data.
     */
    fun validate(): Result<Unit> {
        return when {
            rawText.isBlank() -> Result.failure(IllegalArgumentException("Raw text cannot be blank"))
            confidence < 0.0 || confidence > 1.0 -> 
                Result.failure(IllegalArgumentException("Confidence must be between 0.0 and 1.0"))
            processingTimeMs < 0 -> 
                Result.failure(IllegalArgumentException("Processing time cannot be negative"))
            else -> Result.success(Unit)
        }
    }
}

/**
 * Extracted receipt header information.
 */
@Serializable
data class ExtractedReceipt(
    val storeName: String? = null,
    val storeAddress: String? = null,
    val totalAmount: Double? = null,
    val taxAmount: Double? = null,
    val receiptDate: Instant? = null,
    val receiptNumber: String? = null,
    val cashier: String? = null,
    val paymentMethod: String? = null,
    val confidence: Double = 0.0
) {
    /**
     * Returns true if all essential information is available.
     */
    val isComplete: Boolean
        get() = !storeName.isNullOrBlank() && totalAmount != null && receiptDate != null
    
    /**
     * Validates the extracted receipt data.
     */
    fun validate(): Result<Unit> {
        return when {
            totalAmount != null && totalAmount < 0.0 -> 
                Result.failure(IllegalArgumentException("Total amount cannot be negative"))
            taxAmount != null && taxAmount < 0.0 -> 
                Result.failure(IllegalArgumentException("Tax amount cannot be negative"))
            confidence < 0.0 || confidence > 1.0 -> 
                Result.failure(IllegalArgumentException("Confidence must be between 0.0 and 1.0"))
            else -> Result.success(Unit)
        }
    }
}

/**
 * Extracted line item information.
 */
@Serializable
data class ExtractedItem(
    val name: String,
    val quantity: Double = 1.0,
    val unitPrice: Double? = null,
    val totalPrice: Double,
    val unitType: String = "piece",
    val lineNumber: Int? = null,
    val rawText: String? = null,
    val confidence: Double = 0.0
) {
    /**
     * Returns true if pricing information is consistent.
     */
    val hasPriceConsistency: Boolean
        get() = unitPrice == null || (unitPrice * quantity - totalPrice) < 0.01 // Allow for rounding
    
    /**
     * Calculates unit price if not provided.
     */
    val calculatedUnitPrice: Double
        get() = if (unitPrice != null) unitPrice else totalPrice / quantity
    
    /**
     * Validates the extracted item data.
     */
    fun validate(): Result<Unit> {
        return when {
            name.isBlank() -> Result.failure(IllegalArgumentException("Item name cannot be blank"))
            quantity <= 0.0 -> Result.failure(IllegalArgumentException("Quantity must be positive"))
            totalPrice < 0.0 -> Result.failure(IllegalArgumentException("Total price cannot be negative"))
            unitPrice != null && unitPrice < 0.0 -> 
                Result.failure(IllegalArgumentException("Unit price cannot be negative"))
            confidence < 0.0 || confidence > 1.0 -> 
                Result.failure(IllegalArgumentException("Confidence must be between 0.0 and 1.0"))
            lineNumber != null && lineNumber < 0 -> 
                Result.failure(IllegalArgumentException("Line number cannot be negative"))
            else -> Result.success(Unit)
        }
    }
}

/**
 * Metadata about the processed image.
 */
@Serializable
data class ImageMetadata(
    val width: Int,
    val height: Int,
    val format: String,
    val sizeBytes: Long,
    val dpi: Int? = null
) {
    /**
     * Returns the aspect ratio of the image.
     */
    val aspectRatio: Double
        get() = width.toDouble() / height.toDouble()
    
    /**
     * Returns the megapixel count.
     */
    val megapixels: Double
        get() = (width * height).toDouble() / 1_000_000.0
    
    /**
     * Validates the image metadata.
     */
    fun validate(): Result<Unit> {
        return when {
            width <= 0 -> Result.failure(IllegalArgumentException("Width must be positive"))
            height <= 0 -> Result.failure(IllegalArgumentException("Height must be positive"))
            sizeBytes < 0 -> Result.failure(IllegalArgumentException("Size cannot be negative"))
            dpi != null && dpi <= 0 -> Result.failure(IllegalArgumentException("DPI must be positive"))
            else -> Result.success(Unit)
        }
    }
}
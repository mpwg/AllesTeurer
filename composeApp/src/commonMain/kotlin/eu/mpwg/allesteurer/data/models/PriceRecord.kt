package eu.mpwg.allesteurer.data.models

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlin.random.Random

/**
 * PriceRecord data model representing an individual item on a receipt.
 * This is used for tracking price history and analytics.
 * 
 * @property id Unique identifier for the price record
 * @property receiptId Reference to the Receipt this item belongs to
 * @property productId Reference to the Product entity (if matched)
 * @property productName Product name as it appears on the receipt
 * @property normalizedProductName Normalized version for matching
 * @property quantity Quantity purchased
 * @property unitPrice Price per unit
 * @property totalPrice Total price for this line item
 * @property unitType Unit of measurement (piece, kg, g, l, ml, etc.)
 * @property brand Product brand if detected
 * @property category Product category if classified
 * @property taxRate Tax rate applied to this item (as percentage)
 * @property discountAmount Discount applied to this specific item
 * @property rawText Original OCR text for this line item
 * @property lineNumber Position of this item in the receipt
 * @property isMatched Whether the item has been matched to a Product
 * @property matchConfidence Confidence score for product matching (0.0 to 1.0)
 * @property createdAt Timestamp when the price record was created
 * @property updatedAt Timestamp when the price record was last modified
 */
@Serializable
data class PriceRecord(
    val id: String = generateId(),
    val receiptId: String,
    val productId: String? = null,
    val productName: String,
    val normalizedProductName: String = productName.normalizeForMatching(),
    val quantity: Double = 1.0,
    val unitPrice: Double,
    val totalPrice: Double,
    val unitType: String = "piece",
    val brand: String? = null,
    val category: String? = null,
    val taxRate: Double? = null,
    val discountAmount: Double = 0.0,
    val rawText: String? = null,
    val lineNumber: Int? = null,
    val isMatched: Boolean = false,
    val matchConfidence: Double? = null,
    val createdAt: Instant = ModelUtils.now(),
    val updatedAt: Instant = ModelUtils.now()
) {
    companion object {
        /**
         * Generate a unique ID for a new price record.
         */
        fun generateId(): String = "price_${Random.nextLong().toString(36)}_${ModelUtils.nowMillis()}"
    }
    
    /**
     * Returns true if this price record has been matched to a Product.
     */
    val hasProductMatch: Boolean
        get() = productId != null && isMatched
    
    /**
     * Returns true if the match confidence is above threshold (0.8).
     */
    val hasHighMatchConfidence: Boolean
        get() = matchConfidence != null && matchConfidence >= 0.8
    
    /**
     * Returns true if this item has brand information.
     */
    val hasBrand: Boolean
        get() = !brand.isNullOrBlank()
    
    /**
     * Returns true if this item has been categorized.
     */
    val isCategorized: Boolean
        get() = !category.isNullOrBlank()
    
    /**
     * Returns true if tax information is available.
     */
    val hasTaxInfo: Boolean
        get() = taxRate != null
    
    /**
     * Returns true if a discount was applied to this item.
     */
    val hasDiscount: Boolean
        get() = discountAmount > 0.0
    
    /**
     * Calculates the effective unit price after discount.
     */
    val effectiveUnitPrice: Double
        get() = if (hasDiscount && quantity > 0) {
            (totalPrice - discountAmount) / quantity
        } else unitPrice
    
    /**
     * Calculates the price per standardized unit (e.g., per kg for weight-based items).
     */
    val pricePerStandardUnit: Double?
        get() = when (unitType) {
            "g" -> unitPrice * 1000 // Convert to per kg
            "ml" -> unitPrice * 1000 // Convert to per liter
            else -> unitPrice
        }
    
    /**
     * Returns a display name for the product combining brand if available.
     */
    val displayName: String
        get() = if (hasBrand) "$brand $productName" else productName
    
    /**
     * Creates a copy of this price record with updated timestamp.
     */
    fun withUpdatedTimestamp(): PriceRecord = copy(updatedAt = ModelUtils.now())
    
    /**
     * Creates a copy with product match information.
     */
    fun withProductMatch(productId: String, confidence: Double): PriceRecord = copy(
        productId = productId,
        isMatched = true,
        matchConfidence = confidence,
        updatedAt = ModelUtils.now()
    )
    
    /**
     * Creates a copy with updated normalized name.
     */
    fun withNormalizedName(): PriceRecord = copy(normalizedProductName = productName.normalizeForMatching())
    
    /**
     * Validates that the price record has required fields and valid values.
     */
    fun validate(): Result<Unit> {
        return when {
            receiptId.isBlank() -> Result.failure(IllegalArgumentException("Receipt ID cannot be blank"))
            productName.isBlank() -> Result.failure(IllegalArgumentException("Product name cannot be blank"))
            quantity <= 0.0 -> Result.failure(IllegalArgumentException("Quantity must be positive"))
            unitPrice < 0.0 -> Result.failure(IllegalArgumentException("Unit price cannot be negative"))
            totalPrice < 0.0 -> Result.failure(IllegalArgumentException("Total price cannot be negative"))
            discountAmount < 0.0 -> Result.failure(IllegalArgumentException("Discount amount cannot be negative"))
            taxRate != null && (taxRate < 0.0 || taxRate > 100.0) -> 
                Result.failure(IllegalArgumentException("Tax rate must be between 0 and 100"))
            matchConfidence != null && (matchConfidence < 0.0 || matchConfidence > 1.0) -> 
                Result.failure(IllegalArgumentException("Match confidence must be between 0.0 and 1.0"))
            lineNumber != null && lineNumber < 0 -> 
                Result.failure(IllegalArgumentException("Line number cannot be negative"))
            else -> Result.success(Unit)
        }
    }
}
package eu.mpwg.allesteurer.data.models

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlin.random.Random

/**
 * Receipt data model representing a scanned receipt.
 * 
 * @property id Unique identifier for the receipt
 * @property storeId Reference to the Store entity
 * @property storeName Store name as fallback if storeId is null
 * @property totalAmount Total amount paid for the receipt
 * @property taxAmount Tax amount if available on the receipt
 * @property discountAmount Total discount amount applied
 * @property receiptDate Date/time when the receipt was issued by the store
 * @property scanDate Date/time when the receipt was scanned by the user
 * @property receiptNumber Receipt/transaction number if available
 * @property cashierId Cashier ID if available on the receipt
 * @property paymentMethod Payment method used (cash, card, etc.)
 * @property currency Currency code (ISO 4217), defaults to EUR
 * @property rawOcrText Original OCR text for debugging/reprocessing
 * @property ocrConfidence OCR processing confidence score (0.0 to 1.0)
 * @property imagePath Path to stored receipt image (optional)
 * @property isValidated Whether user has validated OCR results
 * @property notes User notes about the receipt
 * @property createdAt Timestamp when the receipt was created
 * @property updatedAt Timestamp when the receipt was last modified
 */
@Serializable
data class Receipt(
    val id: String = generateId(),
    val storeId: String? = null,
    val storeName: String,
    val totalAmount: Double,
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val receiptDate: Instant,
    val scanDate: Instant = ModelUtils.now(),
    val receiptNumber: String? = null,
    val cashierId: String? = null,
    val paymentMethod: String? = null,
    val currency: String = "EUR",
    val rawOcrText: String? = null,
    val ocrConfidence: Double? = null,
    val imagePath: String? = null,
    val isValidated: Boolean = false,
    val notes: String? = null,
    val createdAt: Instant = ModelUtils.now(),
    val updatedAt: Instant = ModelUtils.now()
) {
    companion object {
        /**
         * Generate a unique ID for a new receipt.
         */
        fun generateId(): String = "receipt_${Random.nextLong().toString(36)}_${ModelUtils.nowMillis()}"
        
        /**
         * Common payment methods.
         */
        val PAYMENT_METHODS = listOf("cash", "card", "credit_card", "debit_card", "contactless", "mobile")
        
        /**
         * Supported currencies.
         */
        val CURRENCIES = listOf("EUR", "USD", "GBP", "CHF")
    }
    
    /**
     * Returns true if the receipt has associated store information.
     */
    val hasStoreInfo: Boolean
        get() = storeId != null
    
    /**
     * Returns true if the receipt has detailed OCR information.
     */
    val hasOcrData: Boolean
        get() = !rawOcrText.isNullOrBlank()
    
    /**
     * Returns true if OCR confidence is above threshold (0.8).
     */
    val hasHighOcrConfidence: Boolean
        get() = ocrConfidence != null && ocrConfidence >= 0.8
    
    /**
     * Returns true if the receipt has a stored image.
     */
    val hasImage: Boolean
        get() = !imagePath.isNullOrBlank()
    
    /**
     * Returns true if tax information is available.
     */
    val hasTaxInfo: Boolean
        get() = taxAmount > 0.0
    
    /**
     * Returns true if discounts were applied.
     */
    val hasDiscount: Boolean
        get() = discountAmount > 0.0
    
    /**
     * Calculates the subtotal before tax and discounts.
     */
    val subtotal: Double
        get() = totalAmount - taxAmount + discountAmount
    
    /**
     * Returns the effective tax rate as a percentage.
     */
    val taxRate: Double?
        get() = if (hasTaxInfo && subtotal > 0.0) {
            (taxAmount / subtotal) * 100
        } else null
    
    /**
     * Creates a copy of this receipt with updated timestamp.
     */
    fun withUpdatedTimestamp(): Receipt = copy(updatedAt = ModelUtils.now())
    
    /**
     * Creates a copy with validation status updated.
     */
    fun withValidation(validated: Boolean): Receipt = copy(
        isValidated = validated,
        updatedAt = ModelUtils.now()
    )
    
    /**
     * Validates that the receipt has required fields and valid values.
     */
    fun validate(): Result<Unit> {
        return when {
            storeName.isBlank() -> Result.failure(IllegalArgumentException("Store name cannot be blank"))
            totalAmount < 0.0 -> Result.failure(IllegalArgumentException("Total amount cannot be negative"))
            taxAmount < 0.0 -> Result.failure(IllegalArgumentException("Tax amount cannot be negative"))
            discountAmount < 0.0 -> Result.failure(IllegalArgumentException("Discount amount cannot be negative"))
            currency.length != 3 -> Result.failure(IllegalArgumentException("Currency must be 3-character ISO code"))
            ocrConfidence != null && (ocrConfidence < 0.0 || ocrConfidence > 1.0) -> 
                Result.failure(IllegalArgumentException("OCR confidence must be between 0.0 and 1.0"))
            else -> Result.success(Unit)
        }
    }
}
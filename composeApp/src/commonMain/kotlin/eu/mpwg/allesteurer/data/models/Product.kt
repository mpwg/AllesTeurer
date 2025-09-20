package eu.mpwg.allesteurer.data.models

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlin.random.Random

/**
 * Product data model representing a product in the catalog.
 * 
 * @property id Unique identifier for the product
 * @property name Product name as it appears on receipts
 * @property normalizedName Normalized version of the name for fuzzy matching
 * @property category Main product category (e.g., "Lebensmittel", "Getränke")
 * @property subcategory Subcategory for more specific classification
 * @property brand Product brand name
 * @property barcode EAN/UPC barcode if available
 * @property unitType Unit type for measurements (piece, kg, g, l, ml, etc.)
 * @property createdAt Timestamp when the product was first added
 * @property updatedAt Timestamp when the product was last modified
 */
@Serializable
data class Product(
    val id: String = generateId(),
    val name: String,
    val normalizedName: String = name.normalizeForMatching(),
    val category: String? = null,
    val subcategory: String? = null,
    val brand: String? = null,
    val barcode: String? = null,
    val unitType: String = "piece",
    val createdAt: Instant = ModelUtils.now(),
    val updatedAt: Instant = ModelUtils.now()
) {
    companion object {
        /**
         * Generate a unique ID for a new product.
         */
        fun generateId(): String = "product_${Random.nextLong().toString(36)}_${ModelUtils.nowMillis()}"
        
        /**
         * Common unit types for products.
         */
        val UNIT_TYPES = listOf("piece", "kg", "g", "l", "ml", "pack", "box", "bottle", "can")
        
        /**
         * Common German product categories.
         */
        val CATEGORIES = listOf(
            "Lebensmittel", "Getränke", "Süßwaren", "Milchprodukte", "Fleisch & Wurst",
            "Obst & Gemüse", "Brot & Backwaren", "Haushaltsartikel", "Drogerie",
            "Tiefkühlkost", "Konserven", "Gewürze & Saucen"
        )
    }
    
    /**
     * Returns true if this product has barcode information.
     */
    val hasBarcode: Boolean
        get() = !barcode.isNullOrBlank()
    
    /**
     * Returns true if this product has been categorized.
     */
    val isCategorized: Boolean
        get() = !category.isNullOrBlank()
    
    /**
     * Returns true if this product has brand information.
     */
    val hasBrand: Boolean
        get() = !brand.isNullOrBlank()
    
    /**
     * Returns a display name combining brand and product name if available.
     */
    val displayName: String
        get() = if (hasBrand) "$brand $name" else name
    
    /**
     * Returns the full category path for display.
     */
    val fullCategory: String?
        get() = when {
            category != null && subcategory != null -> "$category > $subcategory"
            category != null -> category
            else -> null
        }
    
    /**
     * Creates a copy of this product with updated timestamp.
     */
    fun withUpdatedTimestamp(): Product = copy(updatedAt = ModelUtils.now())
    
    /**
     * Creates a copy with updated normalized name.
     */
    fun withNormalizedName(): Product = copy(normalizedName = name.normalizeForMatching())
    
    /**
     * Validates that the product has required fields.
     */
    fun validate(): Result<Unit> {
        return when {
            name.isBlank() -> Result.failure(IllegalArgumentException("Product name cannot be blank"))
            unitType.isBlank() -> Result.failure(IllegalArgumentException("Unit type cannot be blank"))
            barcode?.isBlank() == true -> Result.failure(IllegalArgumentException("Barcode cannot be empty string"))
            else -> Result.success(Unit)
        }
    }
}

/**
 * Extension function to normalize product names for fuzzy matching.
 * Removes special characters, converts to lowercase, normalizes whitespace.
 */
fun String.normalizeForMatching(): String {
    return this
        .lowercase()
        .replace(Regex("[^a-z0-9\\s]"), "") // Remove special characters
        .replace(Regex("\\s+"), " ") // Normalize whitespace
        .trim()
}
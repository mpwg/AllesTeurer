package eu.mpwg.allesteurer.data.models

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlin.random.Random

/**
 * Store data model representing a retail store/shop.
 * 
 * @property id Unique identifier for the store
 * @property name Store name as it appears on receipts
 * @property address Street address of the store
 * @property city City where the store is located
 * @property postalCode Postal/ZIP code
 * @property country Country code (ISO 3166-1 alpha-2), defaults to DE for Germany
 * @property createdAt Timestamp when the store was first added
 * @property updatedAt Timestamp when the store was last modified
 */
@Serializable
data class Store(
    val id: String = generateId(),
    val name: String,
    val address: String? = null,
    val city: String? = null,
    val postalCode: String? = null,
    val country: String = "DE",
    val createdAt: Instant = ModelUtils.now(),
    val updatedAt: Instant = ModelUtils.now()
) {
    companion object {
        /**
         * Generate a unique ID for a new store.
         */
        fun generateId(): String = "store_${Random.nextLong().toString(36)}_${ModelUtils.nowMillis()}"
    }
    
    /**
     * Returns true if this store has a complete address.
     */
    val hasCompleteAddress: Boolean
        get() = !address.isNullOrBlank() && !city.isNullOrBlank() && !postalCode.isNullOrBlank()
    
    /**
     * Returns a formatted address string for display.
     */
    val formattedAddress: String?
        get() = when {
            hasCompleteAddress -> "$address, $postalCode $city"
            !city.isNullOrBlank() && !postalCode.isNullOrBlank() -> "$postalCode $city"
            !city.isNullOrBlank() -> city
            else -> null
        }
    
    /**
     * Creates a copy of this store with updated timestamp.
     */
    fun withUpdatedTimestamp(): Store = copy(updatedAt = ModelUtils.now())
    
    /**
     * Validates that the store has required fields.
     */
    fun validate(): Result<Unit> {
        return when {
            name.isBlank() -> Result.failure(IllegalArgumentException("Store name cannot be blank"))
            country.length != 2 -> Result.failure(IllegalArgumentException("Country code must be 2 characters (ISO 3166-1 alpha-2)"))
            else -> Result.success(Unit)
        }
    }
}
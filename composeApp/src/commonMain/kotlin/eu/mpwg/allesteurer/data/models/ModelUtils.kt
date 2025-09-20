package eu.mpwg.allesteurer.data.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Utility functions for common data model operations.
 */
object ModelUtils {
    /**
     * Get current timestamp as Instant.
     */
    fun now(): Instant = Clock.System.now()
    
    /**
     * Get current timestamp in milliseconds.
     */
    fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
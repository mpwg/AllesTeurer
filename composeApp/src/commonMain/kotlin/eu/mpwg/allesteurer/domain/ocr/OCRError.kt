/**
 * OCR error handling definitions for AllesTeurer.
 * 
 * This file defines structured error hierarchies for OCR operations and receipt parsing.
 * All errors extend Exception to ensure proper exception handling throughout the system.
 */
package eu.mpwg.allesteurer.domain.ocr

import kotlinx.serialization.Serializable

/**
 * Base sealed class for all OCR-related errors.
 * Extends Exception to provide proper exception semantics.
 */
@Serializable
sealed class OCRError : Exception() {

    abstract val code: String
    abstract val userMessage: String
    abstract val isCritical: Boolean

    /**
     * Camera or image capture related errors
     */
    @Serializable
    data class CameraError(
        override val code: String = "CAMERA_ERROR",
        override val message: String?,
        override val userMessage: String = "Kamera Fehler aufgetreten.",
        override val isCritical: Boolean = false
    ) : OCRError()

    /**
     * Image processing errors before OCR
     */
    @Serializable
    data class ImageProcessingError(
        override val code: String = "IMAGE_PROCESSING_ERROR",
        override val message: String?,
        override val userMessage: String = "Fehler bei der Bildverarbeitung.",
        override val isCritical: Boolean = false
    ) : OCRError()

    /**
     * OCR engine/service errors
     */
    @Serializable
    data class OCRServiceError(
        override val code: String = "OCR_SERVICE_ERROR",
        override val message: String?,
        override val userMessage: String = "Texterkennung fehlgeschlagen.",
        override val isCritical: Boolean = true
    ) : OCRError()

    /**
     * Processing timeout errors
     */
    @Serializable
    data class ProcessingTimeout(
        val timeoutMs: Long,
        override val code: String = "OCR_PROCESSING_TIMEOUT",
        override val message: String? = "Processing timeout after \${timeoutMs}ms",
        override val userMessage: String = "Die Verarbeitung dauerte zu lange. Bitte versuchen Sie es mit einem einfacheren Bild erneut.",
        override val isCritical: Boolean = false
    ) : OCRError()

    /**
     * Low quality image errors
     */
    @Serializable
    data class LowQualityImage(
        val qualityScore: Float,
        override val code: String = "LOW_QUALITY_IMAGE",
        override val message: String? = "Image quality too low: \$qualityScore",
        override val userMessage: String = "Die Bildqualität ist zu niedrig. Bitte machen Sie ein schärferes Foto.",
        override val isCritical: Boolean = false
    ) : OCRError()

    /**
     * No text detected in image
     */
    @Serializable
    data class NoTextDetected(
        override val code: String = "NO_TEXT_DETECTED",
        override val message: String? = "No text found in image",
        override val userMessage: String = "Kein Text im Bild erkannt. Bitte stellen Sie sicher, dass der Kassenbon gut sichtbar ist.",
        override val isCritical: Boolean = false
    ) : OCRError()
}

/**
 * Sealed class for receipt parsing errors.
 * These occur after successful OCR when trying to parse receipt structure.
 */
@Serializable
sealed class ReceiptParseError : Exception() {

    abstract val code: String
    abstract val userMessage: String
    abstract val isCritical: Boolean

    /**
     * Missing required field error
     */
    @Serializable
    data class MissingRequiredField(
        val fieldName: String,
        val details: String,
        override val code: String = "MISSING_REQUIRED_FIELD",
        override val message: String? = "Required field '\$fieldName' missing: \$details",
        override val userMessage: String = "Pflichtfeld '\$fieldName' konnte nicht erkannt werden.",
        override val isCritical: Boolean = false
    ) : ReceiptParseError()

    /**
     * Invalid currency format error
     */
    @Serializable
    data class InvalidCurrencyFormat(
        val foundValue: String,
        override val code: String = "INVALID_CURRENCY_FORMAT",
        override val message: String? = "Invalid currency format: \$foundValue",
        override val userMessage: String = "Ungültiges Währungsformat: \$foundValue",
        override val isCritical: Boolean = false
    ) : ReceiptParseError()

    /**
     * No items found in receipt
     */
    @Serializable
    data class NoItemsFound(
        val details: String,
        override val code: String = "NO_ITEMS_FOUND",
        override val message: String? = "No purchase items found: \$details",
        override val userMessage: String = "Keine Artikel im Kassenbon gefunden.",
        override val isCritical: Boolean = true
    ) : ReceiptParseError()

    /**
     * Validation failed error
     */
    @Serializable
    data class ValidationFailed(
        val validationDetails: String,
        override val code: String = "VALIDATION_FAILED",
        override val message: String? = "Receipt validation failed: \$validationDetails",
        override val userMessage: String = "Kassenbon-Validierung fehlgeschlagen.",
        override val isCritical: Boolean = false
    ) : ReceiptParseError()

    /**
     * Unknown store error
     */
    @Serializable
    data class UnknownStore(
        val storeName: String,
        override val code: String = "UNKNOWN_STORE",
        override val message: String? = "Unknown store: \$storeName",
        override val userMessage: String = "Unbekanntes Geschäft: \$storeName",
        override val isCritical: Boolean = false
    ) : ReceiptParseError()
}

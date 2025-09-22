@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package eu.mpwg.allesteurer.platform.ocr

import eu.mpwg.allesteurer.domain.ocr.OCRError
import eu.mpwg.allesteurer.domain.ocr.OCRConfiguration
import eu.mpwg.allesteurer.domain.ocr.OCRResult

/**
 * iOS OCR service implementation stub.
 * This is a simplified implementation for build validation.
 * Full Vision Framework integration should be implemented later.
 */
class IOSOCRServiceImpl {
    
    suspend fun recognizeText(
        imageBytes: ByteArray,
        configuration: OCRConfiguration = OCRConfiguration()
    ): Result<OCRResult> {
        return try {
            // Simplified stub implementation
            // In a real implementation, this would use Vision Framework
            
            val processingStartTime = kotlin.time.TimeSource.Monotonic.markNow()
            
            // Simulate basic OCR processing
            val mockText = "SUPERMARKT DEMO\nArtikel 1  2.50€\nArtikel 2  3.75€\nGesamt: 6.25€"
            val confidence = 0.85
            
            val processingTime = processingStartTime.elapsedNow().inWholeMilliseconds
            
            val ocrResult = OCRResult(
                text = mockText,
                confidence = confidence.toFloat(),
                boundingBoxes = emptyList(),
                language = configuration.language,
                processingTimeMs = processingTime,
                errors = emptyList()
            )
            
            Result.success(ocrResult)
        } catch (e: Exception) {
            Result.failure(
                OCRError.OCRServiceError(
                    message = "iOS OCR stub error: ${e.message}"
                )
            )
        }
    }
    
    fun isAvailable(): Boolean = true
    
    fun getSupportedLanguages(): List<String> = listOf("de", "en")
}
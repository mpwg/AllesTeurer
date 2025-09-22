@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package eu.mpwg.allesteurer.platform.ocr

import eu.mpwg.allesteurer.domain.ocr.*
import kotlinx.cinterop.*
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.UIKit.*
import platform.Vision.*
import kotlin.coroutines.resume

/**
 * iOS OCR service implementation using Vision Framework.
 * Optimized for German text recognition and receipt processing.
 */
class IOSOCRServiceImpl {
    
    companion object {
        /**
         * Maximum image size to prevent memory issues (10MB).
         */
        const val MAX_IMAGE_SIZE = 10_000_000L
        
        /**
         * Minimum confidence threshold for accepting text results.
         */
        const val MIN_CONFIDENCE_THRESHOLD = 0.3f
        
        /**
         * Target image size for optimal OCR processing.
         */
        const val TARGET_IMAGE_SIZE = 1024.0
    }
    
    /**
     * Recognizes text from image bytes using Vision Framework.
     */
    suspend fun recognizeText(
        imageBytes: ByteArray,
        configuration: OCRConfiguration = OCRConfiguration()
    ): Result<OCRResult> {
        return try {
            // Validate image size
            if (imageBytes.size > MAX_IMAGE_SIZE) {
                return Result.failure(
                    OCRError.ImageProcessingError(
                        message = "Image too large: ${imageBytes.size} bytes"
                    )
                )
            }
            
            // Convert byte array to UIImage
            val nsData = imageBytes.usePinned { pinnedBytes ->
                NSData.create(
                    bytes = pinnedBytes.addressOf(0),
                    length = imageBytes.size.toULong()
                )
            }
            
            val uiImage = UIImage.imageWithData(nsData)
                ?: return Result.failure(
                    OCRError.ImageProcessingError(
                        message = "Failed to create UIImage from bytes"
                    )
                )
            
            // Perform Vision Framework text recognition
            performVisionTextRecognition(uiImage, configuration)
            
        } catch (e: Exception) {
            Result.failure(
                OCRError.OCRServiceError(
                    message = "iOS OCR failed: ${e.message}"
                )
            )
        }
    }
    
    /**
     * Performs Vision Framework text recognition with German optimization.
     */
    private suspend fun performVisionTextRecognition(
        image: UIImage,
        configuration: OCRConfiguration
    ): Result<OCRResult> {
        return suspendCancellableCoroutine { continuation ->
            val startTime = NSDate.timeIntervalSinceReferenceDate()
            
            // Create Vision text recognition request
            val request = VNRecognizeTextRequest { request, error ->
                val processingTime = ((NSDate.timeIntervalSinceReferenceDate() - startTime) * 1000).toLong()
                
                if (error != null) {
                    continuation.resume(
                        Result.failure(
                            OCRError.OCRServiceError(
                                message = "Vision Framework error: ${error.localizedDescription}"
                            )
                        )
                    )
                    return@VNRecognizeTextRequest
                }
                
                val observations = request.results as? List<VNRecognizedTextObservation>
                if (observations == null) {
                    continuation.resume(
                        Result.failure(
                            OCRError.OCRServiceError(
                                message = "No text observations returned"
                            )
                        )
                    )
                    return@VNRecognizeTextRequest
                }
                
                val ocrResult = processVisionResults(observations, processingTime, configuration.language)
                continuation.resume(Result.success(ocrResult))
            }
            
            // Configure request for German text recognition
            configurateTextRecognitionRequest(request, configuration)
            
            // Create image request handler
            val cgImage = image.CGImage
            if (cgImage == null) {
                continuation.resume(
                    Result.failure(
                        OCRError.ImageProcessingError(
                            message = "Failed to get CGImage from UIImage"
                        )
                    )
                )
                return@suspendCancellableCoroutine
            }
            
            val handler = VNImageRequestHandler(cgImage, mapOf<Any?, Any?>())
            
            // Perform the request
            try {
                handler.performRequests(listOf(request), null)
            } catch (e: Exception) {
                continuation.resume(
                    Result.failure(
                        OCRError.OCRServiceError(
                            message = "Vision request failed: ${e.message}"
                        )
                    )
                )
            }
            
            // Handle cancellation
            continuation.invokeOnCancellation {
                // Vision Framework doesn't provide direct cancellation
                // The continuation will be cancelled automatically
            }
        }
    }
    
    /**
     * Configure Vision text recognition request for German receipts.
     */
    private fun configurateTextRecognitionRequest(
        request: VNRecognizeTextRequest,
        configuration: OCRConfiguration
    ) {
        // Set recognition level based on configuration
        request.recognitionLevel = when (configuration.recognitionLevel) {
            RecognitionLevel.FAST -> VNRequestTextRecognitionLevelFast
            RecognitionLevel.ACCURATE -> VNRequestTextRecognitionLevelAccurate
            RecognitionLevel.PRECISE -> VNRequestTextRecognitionLevelAccurate
        }
        
        // Configure for German language support
        val languages = mutableListOf<String>()
        if (configuration.language == "de") {
            languages.add("de-DE")
            languages.add("de-AT") // Austrian German
            languages.add("de-CH") // Swiss German
        }
        languages.add("en-US") // Fallback
        
        request.recognitionLanguages = languages
        
        // Enable language correction for better accuracy
        request.usesLanguageCorrection = true
        
        // Set custom words for German receipts
        if (configuration.customWords.isNotEmpty()) {
            request.customWords = configuration.customWords
        } else {
            // Use default German receipt words
            request.customWords = listOf(
                // Receipt terms
                "Summe", "Gesamt", "Total", "Betrag", "Datum", "Uhrzeit",
                "MwSt", "Steuer", "Mehrwertsteuer", "Netto", "Brutto",
                
                // German retailers
                "REWE", "EDEKA", "ALDI", "LIDL", "PENNY", "NETTO", "KAUFLAND",
                "Rossmann", "Müller", "Saturn", "MediaMarkt",
                
                // Common German words
                "und", "mit", "für", "von", "zu", "bei", "auf", "der", "die", "das",
                
                // Units
                "Stück", "Packung", "Liter", "Kilogramm", "Gramm"
            )
        }
        
        // Enable automatic text recognition improvements
        request.automaticallyDetectsLanguage = false // We specify languages explicitly
    }
    
    /**
     * Process Vision Framework results into OCRResult format.
     */
    private fun processVisionResults(
        observations: List<VNRecognizedTextObservation>,
        processingTimeMs: Long,
        language: String
    ): OCRResult {
        val textLines = mutableListOf<String>()
        val boundingBoxes = mutableListOf<TextBoundingBox>()
        var totalConfidence = 0.0f
        var validElements = 0
        val errors = mutableListOf<OCRError>()
        
        for (observation in observations) {
            // Get the top candidate for each observation
            val topCandidate = observation.topCandidates(1u).firstOrNull() as? VNRecognizedText
            if (topCandidate != null) {
                val text = topCandidate.string
                val confidence = topCandidate.confidence
                
                if (confidence >= MIN_CONFIDENCE_THRESHOLD && text.isNotBlank()) {
                    textLines.add(text)
                    totalConfidence += confidence
                    validElements++
                    
                    // Create bounding box from observation
                    val boundingBox = createTextBoundingBox(text, observation, confidence)
                    boundingBoxes.add(boundingBox)
                }
            }
        }
        
        // Calculate average confidence
        val averageConfidence = if (validElements > 0) {
            totalConfidence / validElements
        } else {
            0f
        }
        
        // Join all text lines
        val fullText = textLines.joinToString("\n")
        
        // Create errors list if needed
        if (averageConfidence < 0.5f) {
            errors.add(OCRError.LowQualityImage(averageConfidence))
        }
        if (textLines.isEmpty()) {
            errors.add(OCRError.NoTextDetected())
        }
        
        return OCRResult(
            text = fullText,
            confidence = averageConfidence,
            boundingBoxes = boundingBoxes,
            language = language,
            processingTimeMs = processingTimeMs,
            errors = errors
        )
    }
    
    /**
     * Create TextBoundingBox from Vision Framework observation.
     */
    private fun createTextBoundingBox(
        text: String,
        observation: VNRecognizedTextObservation,
        confidence: Float
    ): TextBoundingBox {
        val boundingBox = observation.boundingBox
        
        return TextBoundingBox(
            text = text,
            x = boundingBox.origin.x.toFloat(),
            y = boundingBox.origin.y.toFloat(),
            width = boundingBox.size.width.toFloat(),
            height = boundingBox.size.height.toFloat(),
            confidence = confidence
        )
    }
    
    /**
     * Check if Vision Framework text recognition is available.
     */
    fun isAvailable(): Boolean {
        return try {
            // Check if Vision framework is available (iOS 13.0+)
            VNRecognizeTextRequest.supportedRecognitionLanguagesForTextRecognitionLevel(
                VNRequestTextRecognitionLevelAccurate,
                null
            ).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get supported languages for text recognition.
     */
    fun getSupportedLanguages(): List<String> {
        return try {
            val supportedLanguages = VNRecognizeTextRequest.supportedRecognitionLanguagesForTextRecognitionLevel(
                VNRequestTextRecognitionLevelAccurate,
                null
            )
            
            // Convert NSArray to Kotlin List and filter for relevant languages
            val languages = mutableListOf<String>()
            for (i in 0 until supportedLanguages.count.toInt()) {
                val language = supportedLanguages.objectAtIndex(i.toULong()) as? String
                if (language != null) {
                    // Extract language code (e.g., "de" from "de-DE")
                    val languageCode = language.split("-").firstOrNull()
                    if (languageCode != null && !languages.contains(languageCode)) {
                        languages.add(languageCode)
                    }
                }
            }
            
            languages
        } catch (e: Exception) {
            // Fallback to common languages
            listOf("de", "en", "fr", "es", "it")
        }
    }
}
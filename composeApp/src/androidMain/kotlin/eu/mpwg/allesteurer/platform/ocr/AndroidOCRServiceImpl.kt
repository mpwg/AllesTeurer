package eu.mpwg.allesteurer.platform.ocr

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import eu.mpwg.allesteurer.domain.ocr.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android-specific OCR implementation using Google ML Kit Text Recognition API.
 * Optimized for German text recognition and receipt processing.
 */
class AndroidOCRServiceImpl(private val context: Context) {
    
    companion object {
        /**
         * Maximum image size to prevent memory issues (10MB).
         */
        const val MAX_IMAGE_SIZE = 10_000_000L
        
        /**
         * Minimum confidence threshold for accepting text results.
         */
        const val MIN_CONFIDENCE_THRESHOLD = 0.3f
    }
    
    private val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.Builder().build()
    )
    
    /**
     * Recognizes text from image bytes using ML Kit.
     */
    suspend fun recognizeText(
        imageBytes: ByteArray,
        configuration: OCRConfiguration
    ): Result<OCRResult> {
        return try {
            // Validate image size
            if (imageBytes.size > MAX_IMAGE_SIZE) {
                return Result.failure(Exception("Image too large: ${imageBytes.size} bytes"))
            }
            
            // Convert byte array to Bitmap
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return Result.failure(Exception("Failed to decode image bytes"))
            
            // Create InputImage
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            
            // Perform ML Kit text recognition
            performMLKitTextRecognition(inputImage, configuration)
        } catch (e: Exception) {
            Result.failure(Exception("Android OCR failed: ${e.message}", e))
        }
    }
    
    /**
     * Performs the actual ML Kit text recognition.
     */
    private suspend fun performMLKitTextRecognition(
        inputImage: InputImage,
        configuration: OCRConfiguration
    ): Result<OCRResult> {
        return suspendCancellableCoroutine { continuation ->
            val startTime = System.currentTimeMillis()
            
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val processingTime = System.currentTimeMillis() - startTime
                    val result = processMLKitResults(visionText, processingTime, configuration.language)
                    continuation.resume(Result.success(result))
                }
                .addOnFailureListener { exception ->
                    val processingTime = System.currentTimeMillis() - startTime
                    continuation.resume(
                        Result.failure(Exception("ML Kit processing failed: ${exception.message}", exception))
                    )
                }
            
            // Set up cancellation
            continuation.invokeOnCancellation {
                // ML Kit doesn't provide direct cancellation,
                // but we can clean up resources if needed
            }
        }
    }
    
    /**
     * Processes ML Kit results into our OCRResult format.
     */
    private fun processMLKitResults(
        visionText: com.google.mlkit.vision.text.Text,
        processingTimeMs: Long,
        language: String
    ): OCRResult {
        val textLines = mutableListOf<String>()
        val boundingBoxes = mutableListOf<TextBoundingBox>()
        var totalConfidence = 0.0f
        var validElements = 0
        
        // Process text blocks
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val lineText = line.text
                
                // ML Kit doesn't provide confidence scores directly,
                // so we estimate based on text characteristics
                val estimatedConfidence = estimateConfidence(lineText)
                
                if (estimatedConfidence >= MIN_CONFIDENCE_THRESHOLD) {
                    textLines.add(lineText)
                    totalConfidence += estimatedConfidence
                    validElements++
                    
                    // Create bounding box from ML Kit rectangle
                    val boundingRect = line.boundingBox
                    if (boundingRect != null) {
                        boundingBoxes.add(createBoundingBox(lineText, boundingRect, estimatedConfidence))
                    } else {
                        // Create default bounding box if none provided
                        boundingBoxes.add(createDefaultBoundingBox(lineText, estimatedConfidence))
                    }
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
        val errors = mutableListOf<OCRError>()
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
     * Estimates confidence score for a text line based on characteristics.
     * ML Kit doesn't provide confidence scores, so we estimate based on:
     * - Text length
     * - Character variety
     * - Presence of special characters
     * - German language patterns
     */
    private fun estimateConfidence(text: String): Float {
        var confidence = 0.7f // Base confidence
        
        // Length factor
        when {
            text.length < 3 -> confidence -= 0.2f
            text.length > 20 -> confidence += 0.1f
        }
        
        // Character variety
        val hasLetters = text.any { it.isLetter() }
        val hasDigits = text.any { it.isDigit() }
        val hasSpecialChars = text.any { !it.isLetterOrDigit() && !it.isWhitespace() }
        
        if (hasLetters) confidence += 0.1f
        if (hasDigits) confidence += 0.05f
        if (hasSpecialChars) confidence += 0.05f
        
        // German characters boost confidence
        val germanChars = listOf('ä', 'ö', 'ü', 'ß', 'Ä', 'Ö', 'Ü')
        if (text.any { it in germanChars }) {
            confidence += 0.1f
        }
        
        // Receipt-like patterns
        if (text.contains("€") || text.matches(Regex(".*\\d+[,.]\\d{2}.*"))) {
            confidence += 0.15f
        }
        
        // Known German words boost confidence
        val germanWords = listOf("und", "mit", "für", "von", "zu", "bei", "auf")
        if (germanWords.any { text.lowercase().contains(it) }) {
            confidence += 0.1f
        }
        
        return confidence.coerceIn(0.0f, 1.0f)
    }
    
    /**
     * Creates a TextBoundingBox from ML Kit's Rect.
     */
    private fun createBoundingBox(text: String, rect: Rect, confidence: Float): TextBoundingBox {
        return TextBoundingBox(
            text = text,
            x = rect.left.toFloat(),
            y = rect.top.toFloat(),
            width = rect.width().toFloat(),
            height = rect.height().toFloat(),
            confidence = confidence
        )
    }
    
    /**
     * Creates a default bounding box when ML Kit doesn't provide one.
     */
    private fun createDefaultBoundingBox(text: String, confidence: Float): TextBoundingBox {
        return TextBoundingBox(
            text = text,
            x = 0f,
            y = 0f,
            width = 1f,
            height = 0.05f,
            confidence = confidence
        )
    }
    
    /**
     * Checks if ML Kit Text Recognition is available.
     */
    fun isAvailable(): Boolean {
        return try {
            // Try to create a recognizer to check availability
            TextRecognition.getClient(TextRecognizerOptions.Builder().build())
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Returns list of supported languages.
     * ML Kit's Latin script recognizer supports multiple languages automatically.
     */
    fun getSupportedLanguages(): List<String> {
        return listOf(
            "de", "en", "fr", "es", "it", "pt", "nl", "sv", "da", 
            "no", "fi", "pl", "cs", "sk", "hu", "ro", "hr", "sl"
        )
    }
}
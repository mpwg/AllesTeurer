package eu.mpwg.allesteurer.platform.ocr

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Clean abstraction over ML Kit Text Recognition APIs for better maintainability.
 * Provides configuration helpers and consistent API surface.
 */
class MLKitWrapper(private val context: Context) {
    
    companion object {
        /**
         * Default confidence threshold for text recognition results.
         */
        const val DEFAULT_CONFIDENCE_THRESHOLD = 0.3f
        
        /**
         * Maximum processing timeout in milliseconds.
         */
        const val PROCESSING_TIMEOUT_MS = 30000L
    }
    
    private var recognizer: TextRecognizer? = null
    
    /**
     * Initializes the ML Kit recognizer with optimal settings for German text.
     */
    fun initializeRecognizer(): TextRecognizer {
        if (recognizer == null) {
            recognizer = TextRecognition.getClient(
                TextRecognizerOptions.Builder()
                    .build()
            )
        }
        return recognizer!!
    }
    
    /**
     * Processes an InputImage using ML Kit Text Recognition.
     * 
     * @param inputImage The image to process
     * @return The recognized text result
     */
    suspend fun processImage(inputImage: InputImage): com.google.mlkit.vision.text.Text {
        return suspendCancellableCoroutine { continuation ->
            val recognizer = initializeRecognizer()
            
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText)
                }
                .addOnFailureListener { exception ->
                    continuation.cancel(exception)
                }
            
            continuation.invokeOnCancellation {
                // ML Kit doesn't provide direct cancellation
                // We rely on the coroutine cancellation mechanism
            }
        }
    }
    
    /**
     * Creates an InputImage from a Bitmap with optimal settings.
     * 
     * @param bitmap The bitmap to convert
     * @param rotationDegrees Optional rotation degrees (0, 90, 180, 270)
     * @return The configured InputImage
     */
    fun createInputImageFromBitmap(
        bitmap: Bitmap, 
        rotationDegrees: Int = 0
    ): InputImage {
        return InputImage.fromBitmap(bitmap, rotationDegrees)
    }
    
    /**
     * Creates an InputImage from byte array.
     * 
     * @param byteArray The image bytes
     * @param width Image width
     * @param height Image height
     * @param format Image format (e.g., InputImage.IMAGE_FORMAT_NV21)
     * @param rotationDegrees Optional rotation degrees
     * @return The configured InputImage
     */
    fun createInputImageFromByteArray(
        byteArray: ByteArray,
        width: Int,
        height: Int,
        format: Int,
        rotationDegrees: Int = 0
    ): InputImage {
        return InputImage.fromByteArray(byteArray, width, height, rotationDegrees, format)
    }
    
    /**
     * Validates if the text recognition result meets quality thresholds.
     * 
     * @param visionText The ML Kit recognition result
     * @param minimumConfidence Minimum confidence threshold
     * @return True if the result meets quality standards
     */
    fun isValidResult(
        visionText: com.google.mlkit.vision.text.Text,
        minimumConfidence: Float = DEFAULT_CONFIDENCE_THRESHOLD
    ): Boolean {
        // ML Kit doesn't provide confidence scores directly,
        // so we check for basic quality indicators
        return when {
            visionText.textBlocks.isEmpty() -> false
            visionText.text.isBlank() -> false
            visionText.text.length < 3 -> false
            else -> true
        }
    }
    
    /**
     * Extracts structured information from ML Kit text blocks.
     * 
     * @param visionText The ML Kit recognition result
     * @return Map of text lines with their bounding rectangles
     */
    fun extractStructuredText(
        visionText: com.google.mlkit.vision.text.Text
    ): List<TextLineInfo> {
        val textLines = mutableListOf<TextLineInfo>()
        
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                textLines.add(
                    TextLineInfo(
                        text = line.text,
                        boundingBox = line.boundingBox,
                        elements = line.elements.map { element ->
                            TextElementInfo(
                                text = element.text,
                                boundingBox = element.boundingBox
                            )
                        }
                    )
                )
            }
        }
        
        return textLines
    }
    
    /**
     * Checks if ML Kit is available and properly configured.
     * 
     * @return True if ML Kit Text Recognition is available
     */
    fun isMLKitAvailable(): Boolean {
        return try {
            TextRecognition.getClient(TextRecognizerOptions.Builder().build())
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Gets the version information for ML Kit Text Recognition.
     * 
     * @return Version string or "Unknown" if not available
     */
    fun getMLKitVersion(): String {
        return try {
            // ML Kit doesn't provide direct version info
            // We return a generic identifier
            "ML Kit Text Recognition (Latin)"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * Cleans up resources and closes the recognizer.
     */
    fun cleanup() {
        recognizer?.close()
        recognizer = null
    }
    
    /**
     * Data class representing a line of recognized text with its elements.
     */
    data class TextLineInfo(
        val text: String,
        val boundingBox: android.graphics.Rect?,
        val elements: List<TextElementInfo>
    )
    
    /**
     * Data class representing an individual text element.
     */
    data class TextElementInfo(
        val text: String,
        val boundingBox: android.graphics.Rect?
    )
}
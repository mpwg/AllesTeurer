package eu.mpwg.allesteurer.domain.image

/**
 * Cross-platform image preprocessing interface for optimal OCR results.
 * Uses expect/actual pattern for platform-specific implementations.
 * 
 * Platform implementations:
 * - iOS: CoreImage-based preprocessing
 * - Android: Canvas and Paint API-based preprocessing
 */
expect class ImageProcessor() {
    
    /**
     * Preprocesses image bytes for optimal OCR performance.
     * 
     * Applies the following optimizations:
     * - Resizes to optimal dimensions (typically 1024px width)
     * - Enhances contrast and brightness for better text visibility  
     * - Reduces noise through smoothing filters
     * - Sharpens text edges for improved recognition
     * 
     * @param imageBytes Original image bytes in any supported format
     * @return Result containing processed image bytes or error
     */
    fun preprocessImageForOCR(imageBytes: ByteArray): Result<ByteArray>
    
    /**
     * Analyzes image quality metrics to determine OCR suitability.
     * 
     * Provides metrics including:
     * - Resolution and aspect ratio
     * - Brightness and contrast levels
     * - Overall quality assessment for OCR
     * 
     * @param imageBytes Image bytes to analyze
     * @return ImageQualityMetrics with analysis results
     */
    fun analyzeImageQuality(imageBytes: ByteArray): ImageQualityMetrics
}

/**
 * Image quality analysis results.
 * Provides metrics to assess OCR suitability.
 */
data class ImageQualityMetrics(
    /**
     * Total pixel count (width * height).
     */
    val resolution: Int,
    
    /**
     * Width to height ratio.
     */
    val aspectRatio: Float,
    
    /**
     * Average brightness level (0-255).
     */
    val brightness: Float,
    
    /**
     * Contrast level indicating text clarity.
     */
    val contrast: Float,
    
    /**
     * Whether the analysis completed successfully.
     */
    val isValid: Boolean,
    
    /**
     * Human-readable analysis message.
     */
    val message: String
) {
    
    companion object {
        /**
         * Creates an invalid metrics instance for error cases.
         */
        fun invalid(message: String) = ImageQualityMetrics(
            resolution = 0,
            aspectRatio = 0f,
            brightness = 0f,
            contrast = 0f,
            isValid = false,
            message = message
        )
        
        /**
         * Optimal resolution range for OCR processing.
         */
        val OPTIMAL_RESOLUTION_RANGE = 100_000..4_000_000
        
        /**
         * Acceptable aspect ratio range for document images.
         */
        val ACCEPTABLE_ASPECT_RATIO_RANGE = 0.5f..2.0f
        
        /**
         * Optimal brightness range for text recognition.
         */
        val OPTIMAL_BRIGHTNESS_RANGE = 50f..200f
        
        /**
         * Minimum contrast threshold for good OCR results.
         */
        const val MIN_CONTRAST_THRESHOLD = 20f
    }
    
    /**
     * Determines if the image metrics indicate good OCR suitability.
     * 
     * Checks:
     * - Valid analysis results
     * - Sufficient resolution
     * - Reasonable aspect ratio
     * - Adequate brightness levels  
     * - Sufficient contrast
     * 
     * @return True if image is well-suited for OCR processing
     */
    fun isGoodForOCR(): Boolean {
        return isValid &&
               resolution in OPTIMAL_RESOLUTION_RANGE &&
               aspectRatio in ACCEPTABLE_ASPECT_RATIO_RANGE &&
               brightness in OPTIMAL_BRIGHTNESS_RANGE &&
               contrast >= MIN_CONTRAST_THRESHOLD
    }
    
    /**
     * Provides specific recommendations for improving OCR results.
     * 
     * @return List of actionable suggestions
     */
    fun getRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (!isValid) {
            recommendations.add("Image could not be analyzed - check format and file integrity")
            return recommendations
        }
        
        if (resolution < OPTIMAL_RESOLUTION_RANGE.first) {
            recommendations.add("Image resolution is too low - use higher quality camera settings")
        } else if (resolution > OPTIMAL_RESOLUTION_RANGE.last) {
            recommendations.add("Image resolution is very high - processing may be slower")
        }
        
        if (aspectRatio !in ACCEPTABLE_ASPECT_RATIO_RANGE) {
            recommendations.add("Unusual aspect ratio - ensure receipt is fully visible and not distorted")
        }
        
        if (brightness < OPTIMAL_BRIGHTNESS_RANGE.start) {
            recommendations.add("Image is too dark - improve lighting or increase brightness")
        } else if (brightness > OPTIMAL_BRIGHTNESS_RANGE.endInclusive) {
            recommendations.add("Image is too bright - reduce lighting or exposure")
        }
        
        if (contrast < MIN_CONTRAST_THRESHOLD) {
            recommendations.add("Low contrast detected - ensure good lighting and sharp focus")
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Image quality is good for OCR processing")
        }
        
        return recommendations
    }
    
    /**
     * Returns a quality score from 0.0 to 1.0 for the image.
     * 
     * @return Normalized quality score
     */
    fun getQualityScore(): Float {
        if (!isValid) return 0f
        
        var score = 0f
        var maxScore = 0f
        
        // Resolution score (0-0.25)
        val resolutionScore = when {
            resolution in OPTIMAL_RESOLUTION_RANGE -> 0.25f
            resolution < OPTIMAL_RESOLUTION_RANGE.first -> (resolution.toFloat() / OPTIMAL_RESOLUTION_RANGE.first) * 0.25f
            else -> 0.15f // Very high resolution gets lower score due to processing overhead
        }
        score += resolutionScore
        maxScore += 0.25f
        
        // Aspect ratio score (0-0.15)
        val aspectRatioScore = if (aspectRatio in ACCEPTABLE_ASPECT_RATIO_RANGE) 0.15f else 0f
        score += aspectRatioScore
        maxScore += 0.15f
        
        // Brightness score (0-0.3)
        val brightnessScore = when {
            brightness in OPTIMAL_BRIGHTNESS_RANGE -> 0.3f
            brightness < OPTIMAL_BRIGHTNESS_RANGE.start -> (brightness / OPTIMAL_BRIGHTNESS_RANGE.start) * 0.3f
            brightness > OPTIMAL_BRIGHTNESS_RANGE.endInclusive -> (255f - brightness) / (255f - OPTIMAL_BRIGHTNESS_RANGE.endInclusive) * 0.3f
            else -> 0f
        }
        score += brightnessScore
        maxScore += 0.3f
        
        // Contrast score (0-0.3)
        val contrastScore = minOf(contrast / 50f, 1f) * 0.3f // Scale contrast to 0-0.3
        score += contrastScore
        maxScore += 0.3f
        
        return if (maxScore > 0) score / maxScore else 0f
    }
}
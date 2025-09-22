package eu.mpwg.allesteurer.domain.image

import eu.mpwg.allesteurer.platform.image.AndroidImageProcessor

/**
 * Android actual implementation of ImageProcessor using Canvas and Paint APIs.
 */
actual class ImageProcessor actual constructor() {
    
    private val androidImageProcessor = AndroidImageProcessor()
    
    actual fun preprocessImageForOCR(imageBytes: ByteArray): Result<ByteArray> {
        return androidImageProcessor.preprocessImageForOCR(imageBytes)
    }
    
    actual fun analyzeImageQuality(imageBytes: ByteArray): ImageQualityMetrics {
        val androidMetrics = androidImageProcessor.analyzeImageQuality(imageBytes)
        
        // Convert Android-specific metrics to common format
        return ImageQualityMetrics(
            resolution = androidMetrics.resolution,
            aspectRatio = androidMetrics.aspectRatio,
            brightness = androidMetrics.brightness,
            contrast = androidMetrics.contrast,
            isValid = androidMetrics.isValid,
            message = androidMetrics.message
        )
    }
}
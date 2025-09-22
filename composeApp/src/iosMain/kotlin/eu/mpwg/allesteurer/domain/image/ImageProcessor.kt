package eu.mpwg.allesteurer.domain.image

import eu.mpwg.allesteurer.platform.image.IOSImageProcessor
import kotlinx.coroutines.runBlocking

/**
 * iOS actual implementation of ImageProcessor using CoreImage.
 */
actual class ImageProcessor actual constructor() {
    
    private val iosImageProcessor = IOSImageProcessor()
    
    actual fun preprocessImageForOCR(imageBytes: ByteArray): Result<ByteArray> {
        // Since the domain interface is synchronous but platform implementation is async,
        // we need to use runBlocking for now. In a real-world scenario, the domain interface
        // should be made suspend to properly handle async operations.
        return runBlocking {
            iosImageProcessor.preprocessForOCR(imageBytes)
        }
    }
    
    actual fun analyzeImageQuality(imageBytes: ByteArray): ImageQualityMetrics {
        // Convert iOS platform result to domain model
        return runBlocking {
            val platformResult = iosImageProcessor.analyzeQuality(imageBytes)
            
            ImageQualityMetrics(
                resolution = platformResult.resolution,
                aspectRatio = platformResult.aspectRatio,
                brightness = platformResult.brightness,
                contrast = platformResult.contrast,
                isValid = platformResult.isValid,
                message = platformResult.message
            )
        }
    }
}
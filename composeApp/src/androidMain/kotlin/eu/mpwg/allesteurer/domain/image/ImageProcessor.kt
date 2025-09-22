package eu.mpwg.allesteurer.domain.image

import android.content.Context
import eu.mpwg.allesteurer.platform.image.AndroidImageProcessor
import kotlinx.coroutines.runBlocking

/**
 * Android actual implementation of ImageProcessor using Canvas and Paint APIs.
 */
actual class ImageProcessor actual constructor() {
    
    // TODO: Inject Context properly via DI framework
    private val context: Context by lazy { 
        // This is a temporary solution - should be injected via DI
        error("Context must be injected for Android image processing")
    }
    
    private val androidImageProcessor by lazy { AndroidImageProcessor(context) }
    
    actual fun preprocessImageForOCR(imageBytes: ByteArray): Result<ByteArray> {
        return runBlocking {
            androidImageProcessor.preprocessForOCR(imageBytes)
        }
    }
    
    actual fun analyzeImageQuality(imageBytes: ByteArray): ImageQualityMetrics {
        val androidMetrics = runBlocking { 
            androidImageProcessor.analyzeQuality(imageBytes)
        }
        
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
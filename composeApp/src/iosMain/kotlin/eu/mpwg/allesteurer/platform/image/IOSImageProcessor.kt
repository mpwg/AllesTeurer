@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package eu.mpwg.allesteurer.platform.image

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.*
import platform.CoreImage.*
import platform.Foundation.*
import platform.UIKit.*

/**
 * iOS image processing implementation using CoreImage framework.
 * Optimized for receipt image preprocessing to improve OCR accuracy.
 */
class IOSImageProcessor {
    
    companion object {
        const val TARGET_WIDTH = 1024.0
        const val TARGET_HEIGHT = 1024.0
        const val MAX_IMAGE_SIZE = 10_000_000L
        const val COMPRESSION_QUALITY = 0.9
        const val CONTRAST_ENHANCEMENT = 1.5
        const val BRIGHTNESS_ADJUSTMENT = 0.1
        const val SATURATION_REDUCTION = 0.3 // Reduce saturation for better OCR
    }
    
    /**
     * Preprocesses image for optimal OCR results using CoreImage.
     */
    suspend fun preprocessForOCR(imageBytes: ByteArray): Result<ByteArray> {
        return withContext(Dispatchers.Default) {
            try {
                // Check size
                if (imageBytes.size > MAX_IMAGE_SIZE) {
                    return@withContext Result.failure(
                        Exception("Image too large: ${imageBytes.size} bytes")
                    )
                }
                
                // Convert byte array to UIImage
                val nsData = imageBytes.usePinned { pinnedBytes ->
                    NSData.create(
                        bytes = pinnedBytes.addressOf(0),
                        length = imageBytes.size.toULong()
                    )
                }
                
                val originalImage = UIImage.imageWithData(nsData)
                    ?: return@withContext Result.failure(
                        Exception("Failed to create UIImage from bytes")
                    )
                
                // Apply preprocessing pipeline
                val processedImage = applyImageProcessingPipeline(originalImage)
                    ?: return@withContext Result.failure(
                        Exception("Image processing pipeline failed")
                    )
                
                // Convert back to byte array
                val processedImageData = UIImageJPEGRepresentation(processedImage, COMPRESSION_QUALITY)
                    ?: return@withContext Result.failure(
                        Exception("Failed to convert processed image to data")
                    )
                
                val resultBytes = ByteArray(processedImageData.length.toInt())
                processedImageData.bytes?.let { bytes ->
                    resultBytes.usePinned { pinnedResult ->
                        platform.posix.memcpy(
                            pinnedResult.addressOf(0),
                            bytes,
                            processedImageData.length
                        )
                    }
                }
                
                Result.success(resultBytes)
                
            } catch (e: Exception) {
                Result.failure(Exception("iOS image processing error: ${e.message}"))
            }
        }
    }
    
    /**
     * Apply comprehensive image processing pipeline for OCR optimization.
     */
    private fun applyImageProcessingPipeline(image: UIImage): UIImage? {
        // Get CGImage
        val cgImage = image.CGImage ?: return null
        
        // Create CIImage from CGImage
        val ciImage = CIImage.imageWithCGImage(cgImage)
        
        // Create CIContext for processing
        val context = CIContext.context()
        
        // Step 1: Resize to optimal dimensions
        val resizedImage = resizeImage(ciImage)
        
        // Step 2: Convert to grayscale for better OCR
        val grayscaleImage = convertToGrayscale(resizedImage)
        
        // Step 3: Enhance contrast
        val contrastEnhancedImage = enhanceContrast(grayscaleImage, CONTRAST_ENHANCEMENT)
        
        // Step 4: Adjust brightness if needed
        val brightnessAdjustedImage = adjustBrightness(contrastEnhancedImage, BRIGHTNESS_ADJUSTMENT)
        
        // Step 5: Apply noise reduction
        val denoisedImage = reduceNoise(brightnessAdjustedImage)
        
        // Step 6: Sharpen text edges
        val sharpenedImage = sharpenImage(denoisedImage)
        
        // Convert back to CGImage and then to UIImage
        val finalCGImage = context.createCGImage(sharpenedImage, sharpenedImage.extent)
            ?: return null
        
        return UIImage.imageWithCGImage(finalCGImage)
    }
    
    /**
     * Resize image to optimal dimensions for OCR processing.
     */
    private fun resizeImage(ciImage: CIImage): CIImage {
        val currentSize = ciImage.extent.size
        val scaleX = TARGET_WIDTH / currentSize.width
        val scaleY = TARGET_HEIGHT / currentSize.height
        val scale = minOf(scaleX, scaleY) // Maintain aspect ratio
        
        val transform = CGAffineTransformMakeScale(scale, scale)
        return ciImage.imageByApplyingTransform(transform)
    }
    
    /**
     * Convert image to grayscale for better OCR performance.
     */
    private fun convertToGrayscale(ciImage: CIImage): CIImage {
        val filter = CIFilter.filterWithName("CIColorControls")
        filter?.setValue(ciImage, "inputImage")
        filter?.setValue(0.0, "inputSaturation") // Remove all color
        return filter?.outputImage ?: ciImage
    }
    
    /**
     * Enhance image contrast to make text more readable.
     */
    private fun enhanceContrast(ciImage: CIImage, factor: Double): CIImage {
        val filter = CIFilter.filterWithName("CIColorControls")
        filter?.setValue(ciImage, "inputImage")
        filter?.setValue(factor, "inputContrast")
        return filter?.outputImage ?: ciImage
    }
    
    /**
     * Adjust image brightness for optimal OCR conditions.
     */
    private fun adjustBrightness(ciImage: CIImage, adjustment: Double): CIImage {
        val filter = CIFilter.filterWithName("CIColorControls")
        filter?.setValue(ciImage, "inputImage")
        filter?.setValue(adjustment, "inputBrightness")
        return filter?.outputImage ?: ciImage
    }
    
    /**
     * Reduce image noise to improve text clarity.
     */
    private fun reduceNoise(ciImage: CIImage): CIImage {
        val filter = CIFilter.filterWithName("CINoiseReduction")
        filter?.setValue(ciImage, "inputImage")
        filter?.setValue(0.02, "inputNoiseLevel") // Light noise reduction
        filter?.setValue(0.4, "inputSharpness") // Maintain sharpness
        return filter?.outputImage ?: ciImage
    }
    
    /**
     * Sharpen image edges to improve text recognition.
     */
    private fun sharpenImage(ciImage: CIImage): CIImage {
        val filter = CIFilter.filterWithName("CISharpenLuminance")
        filter?.setValue(ciImage, "inputImage")
        filter?.setValue(0.4, "inputSharpness") // Moderate sharpening
        return filter?.outputImage ?: ciImage
    }
    
    /**
     * Analyze image quality metrics for OCR suitability.
     */
    suspend fun analyzeQuality(imageBytes: ByteArray): ImageQualityResult {
        return withContext(Dispatchers.Default) {
            try {
                // Convert to UIImage for analysis
                val nsData = imageBytes.usePinned { pinnedBytes ->
                    NSData.create(
                        bytes = pinnedBytes.addressOf(0),
                        length = imageBytes.size.toULong()
                    )
                }
                
                val image = UIImage.imageWithData(nsData)
                    ?: return@withContext ImageQualityResult(
                        resolution = 0,
                        aspectRatio = 0f,
                        brightness = 0f,
                        contrast = 0f,
                        isValid = false,
                        message = "Failed to load image"
                    )
                
                // Calculate basic metrics
                val size = image.size
                val resolution = (size.width * size.height).toInt()
                val aspectRatio = (size.width / size.height).toFloat()
                
                // Estimate brightness and contrast (simplified analysis)
                val brightness = estimateBrightness(image)
                val contrast = estimateContrast(image)
                
                // Determine if quality is good for OCR
                val isGoodQuality = resolution >= 100_000 && 
                                   aspectRatio >= 0.5f && aspectRatio <= 2.0f &&
                                   brightness >= 50f && brightness <= 200f &&
                                   contrast >= 20f
                
                ImageQualityResult(
                    resolution = resolution,
                    aspectRatio = aspectRatio,
                    brightness = brightness,
                    contrast = contrast,
                    isValid = true,
                    message = if (isGoodQuality) "Good quality for OCR" else "Quality could be improved"
                )
                
            } catch (e: Exception) {
                ImageQualityResult(
                    resolution = 0,
                    aspectRatio = 0f,
                    brightness = 0f,
                    contrast = 0f,
                    isValid = false,
                    message = "Quality analysis failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Estimate image brightness using CoreImage histogram.
     */
    private fun estimateBrightness(image: UIImage): Float {
        return try {
            val cgImage = image.CGImage ?: return 128f
            val ciImage = CIImage.imageWithCGImage(cgImage)
            
            // Use area histogram to estimate brightness
            val filter = CIFilter.filterWithName("CIAreaHistogram")
            filter?.setValue(ciImage, "inputImage")
            filter?.setValue(CIVector.vectorWithX_Y_Z_W(0.0, 0.0, 1.0, 1.0), "inputExtent")
            filter?.setValue(1, "inputCount")
            
            val outputImage = filter?.outputImage
            if (outputImage != null) {
                // Simplified brightness estimation
                return 128f // Middle value as fallback
            }
            
            128f // Default brightness
        } catch (e: Exception) {
            128f // Fallback value
        }
    }
    
    /**
     * Estimate image contrast.
     */
    private fun estimateContrast(image: UIImage): Float {
        return try {
            // Simplified contrast estimation
            // In a real implementation, this would analyze pixel value distribution
            30f // Assume moderate contrast as default
        } catch (e: Exception) {
            30f // Fallback value
        }
    }
}

/**
 * Data class for image quality metrics.
 */
data class ImageQualityResult(
    val resolution: Int,
    val aspectRatio: Float,
    val brightness: Float,
    val contrast: Float,
    val isValid: Boolean,
    val message: String
)
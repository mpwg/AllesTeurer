package eu.mpwg.allesteurer.platform.image

import android.content.Context
import android.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.*

/**
 * Android image processing implementation using Canvas and Paint APIs.
 * Optimized for receipt image preprocessing to improve OCR accuracy.
 */
class AndroidImageProcessor(private val context: Context) {
    
    companion object {
        const val TARGET_WIDTH = 1024
        const val TARGET_HEIGHT = 1024
        const val MAX_IMAGE_SIZE = 10_000_000L
        const val COMPRESSION_QUALITY = 90 // JPEG quality (0-100)
        const val CONTRAST_FACTOR = 1.5f
        const val BRIGHTNESS_ADJUSTMENT = 20
        const val SATURATION_REDUCTION = 0.3f
    }
    
    /**
     * Preprocesses image for optimal OCR results using Android graphics APIs.
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
                
                // Decode byte array to Bitmap
                val originalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ?: return@withContext Result.failure(
                        Exception("Failed to decode image bytes")
                    )
                
                // Apply preprocessing pipeline
                val processedBitmap = applyImageProcessingPipeline(originalBitmap)
                    ?: return@withContext Result.failure(
                        Exception("Image processing pipeline failed")
                    )
                
                // Convert back to byte array
                val outputStream = ByteArrayOutputStream()
                val compressResult = processedBitmap.compress(
                    Bitmap.CompressFormat.JPEG, 
                    COMPRESSION_QUALITY, 
                    outputStream
                )
                
                if (!compressResult) {
                    return@withContext Result.failure(
                        Exception("Failed to compress processed image")
                    )
                }
                
                // Clean up bitmaps
                if (!originalBitmap.isRecycled) originalBitmap.recycle()
                if (!processedBitmap.isRecycled) processedBitmap.recycle()
                
                Result.success(outputStream.toByteArray())
                
            } catch (e: Exception) {
                Result.failure(Exception("Android image processing error: ${e.message}"))
            }
        }
    }
    
    /**
     * Apply comprehensive image processing pipeline for OCR optimization.
     */
    private fun applyImageProcessingPipeline(originalBitmap: Bitmap): Bitmap? {
        try {
            // Step 1: Resize to optimal dimensions
            val resizedBitmap = resizeImage(originalBitmap)
            
            // Step 2: Convert to grayscale for better OCR
            val grayscaleBitmap = convertToGrayscale(resizedBitmap)
            
            // Step 3: Enhance contrast
            val contrastEnhancedBitmap = enhanceContrast(grayscaleBitmap, CONTRAST_FACTOR)
            
            // Step 4: Adjust brightness if needed
            val brightnessAdjustedBitmap = adjustBrightness(contrastEnhancedBitmap, BRIGHTNESS_ADJUSTMENT)
            
            // Step 5: Apply noise reduction (simple blur and sharpen)
            val denoisedBitmap = reduceNoise(brightnessAdjustedBitmap)
            
            // Step 6: Sharpen text edges
            val sharpenedBitmap = sharpenImage(denoisedBitmap)
            
            // Clean up intermediate bitmaps
            cleanupIntermediateBitmaps(
                originalBitmap, resizedBitmap, grayscaleBitmap, 
                contrastEnhancedBitmap, brightnessAdjustedBitmap, denoisedBitmap
            )
            
            return sharpenedBitmap
            
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * Clean up intermediate bitmaps to prevent memory leaks.
     */
    private fun cleanupIntermediateBitmaps(vararg bitmaps: Bitmap) {
        bitmaps.forEachIndexed { index, bitmap ->
            // Don't recycle the original bitmap or the last processed bitmap
            if (index > 0 && index < bitmaps.size - 1 && !bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }
    
    /**
     * Resize image to optimal dimensions for OCR processing while maintaining aspect ratio.
     */
    private fun resizeImage(bitmap: Bitmap): Bitmap {
        val currentWidth = bitmap.width
        val currentHeight = bitmap.height
        
        val scaleX = TARGET_WIDTH.toFloat() / currentWidth
        val scaleY = TARGET_HEIGHT.toFloat() / currentHeight
        val scale = minOf(scaleX, scaleY) // Maintain aspect ratio
        
        val newWidth = (currentWidth * scale).toInt()
        val newHeight = (currentHeight * scale).toInt()
        
        return if (scale < 1.0f || scale > 1.2f) {
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap // No resizing needed
        }
    }
    
    /**
     * Convert image to grayscale for better OCR performance.
     */
    private fun convertToGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(grayscaleBitmap)
        
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f) // Remove all color
        
        val colorFilter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = colorFilter
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return grayscaleBitmap
    }
    
    /**
     * Enhance image contrast to make text more readable.
     */
    private fun enhanceContrast(bitmap: Bitmap, factor: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val contrastBitmap = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(contrastBitmap)
        
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        
        // Create contrast adjustment matrix
        val contrast = factor
        val brightness = (1 - contrast) / 2 * 255
        
        colorMatrix.set(floatArrayOf(
            contrast, 0f, 0f, 0f, brightness,
            0f, contrast, 0f, 0f, brightness,
            0f, 0f, contrast, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        ))
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return contrastBitmap
    }
    
    /**
     * Adjust image brightness for optimal OCR conditions.
     */
    private fun adjustBrightness(bitmap: Bitmap, adjustment: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val brightnessBitmap = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(brightnessBitmap)
        
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.set(floatArrayOf(
            1f, 0f, 0f, 0f, adjustment.toFloat(),
            0f, 1f, 0f, 0f, adjustment.toFloat(),
            0f, 0f, 1f, 0f, adjustment.toFloat(),
            0f, 0f, 0f, 1f, 0f
        ))
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return brightnessBitmap
    }
    
    /**
     * Reduce image noise using simple blur technique.
     */
    private fun reduceNoise(bitmap: Bitmap): Bitmap {
        // Simple noise reduction using slight blur
        val width = bitmap.width
        val height = bitmap.height
        
        val noiseBitmap = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(noiseBitmap)
        
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        paint.isDither = true
        
        // Very light blur to reduce noise while preserving text clarity
        val blurRadius = 0.5f
        paint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return noiseBitmap
    }
    
    /**
     * Sharpen image edges to improve text recognition using convolution.
     */
    private fun sharpenImage(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val sharpenedBitmap = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        
        // Sharpening kernel (3x3)
        val kernel = floatArrayOf(
            0f, -0.25f, 0f,
            -0.25f, 2f, -0.25f,
            0f, -0.25f, 0f
        )
        
        // Apply convolution manually for sharpening
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val newPixels = IntArray(width * height)
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var r = 0f
                var g = 0f
                var b = 0f
                
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val pixel = pixels[(y + ky) * width + (x + kx)]
                        val weight = kernel[(ky + 1) * 3 + (kx + 1)]
                        
                        r += Color.red(pixel) * weight
                        g += Color.green(pixel) * weight
                        b += Color.blue(pixel) * weight
                    }
                }
                
                // Clamp values to 0-255
                val newR = r.coerceIn(0f, 255f).toInt()
                val newG = g.coerceIn(0f, 255f).toInt()
                val newB = b.coerceIn(0f, 255f).toInt()
                
                newPixels[y * width + x] = Color.rgb(newR, newG, newB)
            }
        }
        
        sharpenedBitmap.setPixels(newPixels, 0, width, 0, 0, width, height)
        
        return sharpenedBitmap
    }
    
    /**
     * Analyze image quality metrics for OCR suitability.
     */
    suspend fun analyzeQuality(imageBytes: ByteArray): ImageQualityResult {
        return withContext(Dispatchers.Default) {
            try {
                // Decode image for analysis
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ?: return@withContext ImageQualityResult(
                        resolution = 0,
                        aspectRatio = 0f,
                        brightness = 0f,
                        contrast = 0f,
                        isValid = false,
                        message = "Failed to decode image"
                    )
                
                // Calculate basic metrics
                val width = bitmap.width
                val height = bitmap.height
                val resolution = width * height
                val aspectRatio = width.toFloat() / height.toFloat()
                
                // Estimate brightness and contrast
                val brightness = estimateBrightness(bitmap)
                val contrast = estimateContrast(bitmap)
                
                // Clean up
                if (!bitmap.isRecycled) bitmap.recycle()
                
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
     * Estimate image brightness by sampling pixels.
     */
    private fun estimateBrightness(bitmap: Bitmap): Float {
        val width = bitmap.width
        val height = bitmap.height
        val sampleSize = 100 // Sample 100 pixels
        
        var totalBrightness = 0L
        var sampleCount = 0
        
        val stepX = maxOf(1, width / 10)
        val stepY = maxOf(1, height / 10)
        
        for (y in 0 until height step stepY) {
            for (x in 0 until width step stepX) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                
                // Calculate luminance using standard weights
                val brightness = (0.299 * r + 0.587 * g + 0.114 * b).toLong()
                totalBrightness += brightness
                sampleCount++
                
                if (sampleCount >= sampleSize) break
            }
            if (sampleCount >= sampleSize) break
        }
        
        return if (sampleCount > 0) {
            (totalBrightness / sampleCount).toFloat()
        } else {
            128f // Default middle brightness
        }
    }
    
    /**
     * Estimate image contrast by calculating pixel value variance.
     */
    private fun estimateContrast(bitmap: Bitmap): Float {
        val width = bitmap.width
        val height = bitmap.height
        val sampleSize = 100
        
        val brightnessList = mutableListOf<Float>()
        var sampleCount = 0
        
        val stepX = maxOf(1, width / 10)
        val stepY = maxOf(1, height / 10)
        
        for (y in 0 until height step stepY) {
            for (x in 0 until width step stepX) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                
                val brightness = 0.299f * r + 0.587f * g + 0.114f * b
                brightnessList.add(brightness)
                sampleCount++
                
                if (sampleCount >= sampleSize) break
            }
            if (sampleCount >= sampleSize) break
        }
        
        if (brightnessList.size < 2) return 30f // Default contrast
        
        val mean = brightnessList.average().toFloat()
        val variance = brightnessList.map { (it - mean).pow(2) }.average().toFloat()
        val standardDeviation = sqrt(variance)
        
        // Normalize contrast to a reasonable scale (0-100)
        return (standardDeviation / 255f * 100f).coerceIn(0f, 100f)
    }
}

/**
 * Data class for image quality analysis results.
 */
data class ImageQualityResult(
    val resolution: Int,
    val aspectRatio: Float,
    val brightness: Float,
    val contrast: Float,
    val isValid: Boolean,
    val message: String
)
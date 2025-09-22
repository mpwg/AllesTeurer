package eu.mpwg.allesteurer.platform.image

import android.graphics.*
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import kotlin.math.*

/**
 * Android-specific image preprocessing implementation for optimal OCR results.
 * Uses Android's Canvas and Paint APIs for image manipulation.
 */
class AndroidImageProcessor {
    
    companion object {
        /**
         * Optimal width for OCR processing (maintaining aspect ratio).
         */
        const val OPTIMAL_WIDTH = 1024
        
        /**
         * Maximum image dimension to prevent memory issues.
         */
        const val MAX_DIMENSION = 2048
        
        /**
         * JPEG quality for compressed output (0-100).
         */
        const val JPEG_QUALITY = 85
        
        /**
         * Contrast adjustment factor for receipt images.
         */
        const val CONTRAST_FACTOR = 1.2f
        
        /**
         * Brightness adjustment offset.
         */
        const val BRIGHTNESS_OFFSET = 10f
    }
    
    /**
     * Preprocesses image bytes for optimal OCR performance.
     * Applies contrast enhancement, noise reduction, and optimal resizing.
     * 
     * @param imageBytes Original image bytes
     * @return Processed image bytes optimized for OCR
     */
    fun preprocessImageForOCR(imageBytes: ByteArray): Result<ByteArray> {
        return try {
            // Decode original bitmap
            val originalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return Result.failure(IllegalArgumentException("Failed to decode image bytes"))
            
            // Apply preprocessing pipeline
            val processedBitmap = originalBitmap
                .let { resizeToOptimalSize(it) }
                .let { enhanceContrastAndBrightness(it) }
                .let { reduceNoise(it) }
                .let { sharpenImage(it) }
            
            // Convert back to bytes
            val processedBytes = bitmapToByteArray(processedBitmap, Bitmap.CompressFormat.JPEG, JPEG_QUALITY)
            
            // Clean up bitmaps
            if (originalBitmap != processedBitmap) {
                originalBitmap.recycle()
            }
            processedBitmap.recycle()
            
            Result.success(processedBytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Resizes image to optimal dimensions for OCR while maintaining aspect ratio.
     */
    private fun resizeToOptimalSize(bitmap: Bitmap): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        
        // Check if resizing is needed
        if (originalWidth <= OPTIMAL_WIDTH && originalHeight <= OPTIMAL_WIDTH) {
            return bitmap
        }
        
        // Calculate new dimensions maintaining aspect ratio
        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()
        val (newWidth, newHeight) = if (originalWidth > originalHeight) {
            OPTIMAL_WIDTH to (OPTIMAL_WIDTH / aspectRatio).toInt()
        } else {
            (OPTIMAL_WIDTH * aspectRatio).toInt() to OPTIMAL_WIDTH
        }
        
        // Ensure dimensions don't exceed maximum
        val finalWidth = minOf(newWidth, MAX_DIMENSION)
        val finalHeight = minOf(newHeight, MAX_DIMENSION)
        
        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }
    
    /**
     * Enhances contrast and brightness for better text recognition.
     */
    private fun enhanceContrastAndBrightness(bitmap: Bitmap): Bitmap {
        val enhancedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(enhancedBitmap)
        
        // Create color matrix for contrast and brightness adjustment
        val colorMatrix = ColorMatrix().apply {
            // Contrast adjustment
            val contrast = CONTRAST_FACTOR
            val translate = (1.0f - contrast) / 2.0f * 255.0f + BRIGHTNESS_OFFSET
            
            set(floatArrayOf(
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            ))
        }
        
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return enhancedBitmap
    }
    
    /**
     * Reduces noise in the image using a simple smoothing filter.
     */
    private fun reduceNoise(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val denoisedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val denoisedPixels = IntArray(width * height)
        
        // Apply 3x3 Gaussian blur kernel for noise reduction
        val kernel = floatArrayOf(
            1f, 2f, 1f,
            2f, 4f, 2f,
            1f, 2f, 1f
        )
        val kernelSum = 16f
        
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
                
                denoisedPixels[y * width + x] = Color.rgb(
                    (r / kernelSum).toInt().coerceIn(0, 255),
                    (g / kernelSum).toInt().coerceIn(0, 255),
                    (b / kernelSum).toInt().coerceIn(0, 255)
                )
            }
        }
        
        // Copy edge pixels as-is
        for (y in 0 until height) {
            denoisedPixels[y * width] = pixels[y * width] // Left edge
            denoisedPixels[y * width + width - 1] = pixels[y * width + width - 1] // Right edge
        }
        for (x in 0 until width) {
            denoisedPixels[x] = pixels[x] // Top edge
            denoisedPixels[(height - 1) * width + x] = pixels[(height - 1) * width + x] // Bottom edge
        }
        
        denoisedBitmap.setPixels(denoisedPixels, 0, width, 0, 0, width, height)
        return denoisedBitmap
    }
    
    /**
     * Sharpens the image to enhance text edges.
     */
    private fun sharpenImage(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val sharpenedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val sharpenedPixels = IntArray(width * height)
        
        // Sharpening kernel
        val kernel = floatArrayOf(
            0f, -1f, 0f,
            -1f, 5f, -1f,
            0f, -1f, 0f
        )
        
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
                
                sharpenedPixels[y * width + x] = Color.rgb(
                    r.toInt().coerceIn(0, 255),
                    g.toInt().coerceIn(0, 255),
                    b.toInt().coerceIn(0, 255)
                )
            }
        }
        
        // Copy edge pixels as-is
        for (y in 0 until height) {
            sharpenedPixels[y * width] = pixels[y * width] // Left edge
            sharpenedPixels[y * width + width - 1] = pixels[y * width + width - 1] // Right edge
        }
        for (x in 0 until width) {
            sharpenedPixels[x] = pixels[x] // Top edge
            sharpenedPixels[(height - 1) * width + x] = pixels[(height - 1) * width + x] // Bottom edge
        }
        
        sharpenedBitmap.setPixels(sharpenedPixels, 0, width, 0, 0, width, height)
        return sharpenedBitmap
    }
    
    /**
     * Converts a bitmap to byte array with specified format and quality.
     */
    private fun bitmapToByteArray(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        quality: Int
    ): ByteArray {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(format, quality, stream)
        return stream.toByteArray()
    }
    
    /**
     * Analyzes image quality metrics for OCR suitability.
     */
    fun analyzeImageQuality(imageBytes: ByteArray): ImageQualityMetrics {
        return try {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return ImageQualityMetrics.invalid("Failed to decode image")
            
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            
            // Calculate basic metrics
            val resolution = width * height
            val aspectRatio = width.toFloat() / height.toFloat()
            
            // Calculate brightness and contrast
            var totalBrightness = 0.0
            var brightnessVariance = 0.0
            
            for (pixel in pixels) {
                val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3.0
                totalBrightness += brightness
            }
            
            val averageBrightness = totalBrightness / pixels.size
            
            for (pixel in pixels) {
                val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3.0
                brightnessVariance += (brightness - averageBrightness).pow(2)
            }
            
            val contrast = sqrt(brightnessVariance / pixels.size)
            
            bitmap.recycle()
            
            ImageQualityMetrics(
                resolution = resolution,
                aspectRatio = aspectRatio,
                brightness = averageBrightness.toFloat(),
                contrast = contrast.toFloat(),
                isValid = true,
                message = "Image analysis complete"
            )
        } catch (e: Exception) {
            ImageQualityMetrics.invalid("Analysis failed: ${e.message}")
        }
    }
    
    /**
     * Data class containing image quality metrics.
     */
    data class ImageQualityMetrics(
        val resolution: Int,
        val aspectRatio: Float,
        val brightness: Float,
        val contrast: Float,
        val isValid: Boolean,
        val message: String
    ) {
        companion object {
            fun invalid(message: String) = ImageQualityMetrics(
                resolution = 0,
                aspectRatio = 0f,
                brightness = 0f,
                contrast = 0f,
                isValid = false,
                message = message
            )
        }
        
        /**
         * Checks if image metrics indicate good OCR suitability.
         */
        fun isGoodForOCR(): Boolean {
            return isValid &&
                   resolution >= 100_000 && // At least 100k pixels
                   aspectRatio in 0.5f..2.0f && // Reasonable aspect ratio
                   brightness in 50f..200f && // Not too dark or bright
                   contrast >= 20f // Sufficient contrast
        }
    }
}
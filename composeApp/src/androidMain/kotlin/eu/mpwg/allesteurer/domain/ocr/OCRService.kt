package eu.mpwg.allesteurer.domain.ocr

import eu.mpwg.allesteurer.platform.ocr.AndroidOCRServiceImpl
import eu.mpwg.allesteurer.domain.parsing.GermanReceiptParser
import android.content.Context

/**
 * Android-specific implementation of the OCR service using ML Kit.
 */
actual class OCRService(private val context: Context) {
    private val androidImplementation = AndroidOCRServiceImpl(context)
    private val receiptParser = GermanReceiptParser()
    
    actual suspend fun recognizeText(
        imageBytes: ByteArray,
        configuration: OCRConfiguration
    ): Result<OCRResult> {
        return androidImplementation.recognizeText(imageBytes, configuration)
    }
    
    actual suspend fun recognizeReceiptText(
        imageBytes: ByteArray,
        configuration: OCRConfiguration
    ): Result<ReceiptOCRResult> {
        return recognizeText(imageBytes, configuration).fold(
            onSuccess = { ocrResult ->
                val receiptResult = receiptParser.parseReceiptText(ocrResult)
                Result.success(receiptResult)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
    
    actual fun isAvailable(): Boolean {
        return androidImplementation.isAvailable()
    }
    
    actual fun getSupportedLanguages(): List<String> {
        return androidImplementation.getSupportedLanguages()
    }
}
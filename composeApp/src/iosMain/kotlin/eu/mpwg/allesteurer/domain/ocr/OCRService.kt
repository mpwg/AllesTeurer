package eu.mpwg.allesteurer.domain.ocr

import eu.mpwg.allesteurer.platform.ocr.IOSOCRServiceImpl
import eu.mpwg.allesteurer.domain.parsing.GermanReceiptParser

/**
 * iOS-specific implementation of the OCR service using Vision Framework.
 */
actual class OCRService {
    private val iosImplementation = IOSOCRServiceImpl()
    private val receiptParser = GermanReceiptParser()
    
    actual suspend fun recognizeText(
        imageBytes: ByteArray,
        configuration: OCRConfiguration
    ): Result<OCRResult> {
        return iosImplementation.recognizeText(imageBytes, configuration)
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
        return iosImplementation.isAvailable()
    }
    
    actual fun getSupportedLanguages(): List<String> {
        return iosImplementation.getSupportedLanguages()
    }
}
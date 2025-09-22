# AllesTeurer Phase 2 - Unit Testing Strategy

## Testing Framework Overview

### Primary Testing Framework: Kotlin Test (KMP Compatible)

**Rationale**: Kotlin Test provides excellent multiplatform support, allowing shared test logic while supporting platform-specific testing requirements.

**Dependencies Required**:

```kotlin
commonTest {
    implementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    implementation("io.mockk:mockk:1.13.12")
}

androidTest {
    implementation("io.mockk:mockk-android:1.13.12")
    implementation("org.robolectric:robolectric:4.13")
}

iosTest {
    implementation("io.mockk:mockk:1.13.12")
}
```

## 1. Shared Business Logic Testing (commonTest)

### 1.1 OCR Service Interface Testing

**File**: `composeApp/src/commonTest/kotlin/domain/ocr/OCRServiceTest.kt`

```kotlin
@Test
class OCRServiceTest {

    private val mockOCRService = mockk<OCRService>()
    private val testImageBytes = byteArrayOf(1, 2, 3, 4, 5) // Minimal test data

    @Test
    fun `recognizeText should return success result for valid input`() = runTest {
        // Arrange
        val expectedResult = OCRResult(
            text = "Test Receipt",
            confidence = 0.85f,
            boundingBoxes = emptyList(),
            language = "de",
            processingTimeMs = 1000
        )
        coEvery { mockOCRService.recognizeText(testImageBytes) } returns Result.success(expectedResult)

        // Act
        val result = mockOCRService.recognizeText(testImageBytes)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
        assertEquals("Test Receipt", result.getOrNull()?.text)
        assertEquals(0.85f, result.getOrNull()?.confidence)
    }

    @Test
    fun `recognizeText should return error for invalid input`() = runTest {
        // Arrange
        val expectedError = OCRError.ProcessingError("Invalid image format")
        coEvery { mockOCRService.recognizeText(byteArrayOf()) } returns Result.failure(expectedError)

        // Act
        val result = mockOCRService.recognizeText(byteArrayOf())

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedError, result.exceptionOrNull())
    }

    @Test
    fun `recognizeReceiptText should parse German receipt correctly`() = runTest {
        // Arrange
        val germanReceiptText = """
            REWE Markt GmbH
            Datum: 22.09.2025

            Milch 3,5%        2.49€
            Brot Vollkorn     3.29€

            SUMME:           5.78€
        """.trimIndent()

        val expectedResult = ReceiptOCRResult(
            rawText = germanReceiptText,
            storeName = "REWE Markt GmbH",
            receiptDate = "22.09.2025",
            totalAmount = "5.78",
            items = listOf(
                ReceiptLineItem("Milch 3,5%        2.49€", "Milch 3,5%", "2.49", 0.9f, TextBoundingBox("", 0f, 0f, 0f, 0f, 0.9f)),
                ReceiptLineItem("Brot Vollkorn     3.29€", "Brot Vollkorn", "3.29", 0.9f, TextBoundingBox("", 0f, 0f, 0f, 0f, 0.9f))
            ),
            confidence = 0.85f
        )

        coEvery { mockOCRService.recognizeReceiptText(testImageBytes) } returns Result.success(expectedResult)

        // Act
        val result = mockOCRService.recognizeReceiptText(testImageBytes)

        // Assert
        assertTrue(result.isSuccess)
        val receiptResult = result.getOrNull()!!
        assertEquals("REWE Markt GmbH", receiptResult.storeName)
        assertEquals("5.78", receiptResult.totalAmount)
        assertEquals(2, receiptResult.items.size)
    }
}
```

### 1.2 German Receipt Parser Testing

**File**: `composeApp/src/commonTest/kotlin/domain/parsing/GermanReceiptParserTest.kt`

```kotlin
@Test
class GermanReceiptParserTest {

    private val parser = GermanReceiptParser()

    @Test
    fun `parseReceiptText should extract store name correctly`() {
        // Test various German store formats
        val testCases = listOf(
            "REWE Markt GmbH" to "REWE Markt GmbH",
            "EDEKA Center" to "EDEKA Center",
            "ALDI SÜD" to "ALDI SÜD",
            "LIDL Stiftung & Co. KG" to "LIDL Stiftung & Co. KG"
        )

        testCases.forEach { (input, expected) ->
            val ocrResult = OCRResult(
                text = "$input\nDatum: 22.09.2025\nSUMME: 10.50€",
                confidence = 0.9f,
                boundingBoxes = emptyList(),
                language = "de",
                processingTimeMs = 1000
            )

            val result = parser.parseReceiptText(ocrResult)

            assertEquals(expected, result.storeName, "Failed for input: $input")
        }
    }

    @Test
    fun `parseReceiptText should extract German date formats correctly`() {
        val testCases = listOf(
            "22.09.2025" to "22.09.2025",
            "22-09-2025" to "22-09-2025",
            "2025.09.22" to "2025.09.22",
            "2025-09-22" to "2025-09-22"
        )

        testCases.forEach { (input, expected) ->
            val ocrResult = OCRResult(
                text = "REWE\nDatum: $input\nSUMME: 10.50€",
                confidence = 0.9f,
                boundingBoxes = emptyList(),
                language = "de",
                processingTimeMs = 1000
            )

            val result = parser.parseReceiptText(ocrResult)

            assertEquals(expected, result.receiptDate, "Failed for date: $input")
        }
    }

    @Test
    fun `parseReceiptText should extract German amounts correctly`() {
        val testCases = listOf(
            "SUMME: 10.50€" to "10.50",
            "GESAMT: €15,99" to "15.99",
            "TOTAL: 7,50 €" to "7.50",
            "BETRAG: 123.45€" to "123.45"
        )

        testCases.forEach { (input, expected) ->
            val ocrResult = OCRResult(
                text = "REWE\nDatum: 22.09.2025\n$input",
                confidence = 0.9f,
                boundingBoxes = emptyList(),
                language = "de",
                processingTimeMs = 1000
            )

            val result = parser.parseReceiptText(ocrResult)

            assertEquals(expected, result.totalAmount, "Failed for amount: $input")
        }
    }

    @Test
    fun `parseReceiptText should extract item lines with German characters`() {
        val receiptText = """
            REWE Markt GmbH
            Datum: 22.09.2025

            Müsli Früchte         4.99€
            Käse Gouda jung      3.49€
            Brötchen 6 Stück     1.89€

            SUMME:              10.37€
        """.trimIndent()

        val ocrResult = OCRResult(
            text = receiptText,
            confidence = 0.9f,
            boundingBoxes = emptyList(),
            language = "de",
            processingTimeMs = 1000
        )

        val result = parser.parseReceiptText(ocrResult)

        assertEquals(3, result.items.size)

        val items = result.items
        assertEquals("Müsli Früchte", items[0].extractedName)
        assertEquals("4.99", items[0].extractedPrice)

        assertEquals("Käse Gouda jung", items[1].extractedName)
        assertEquals("3.49", items[1].extractedPrice)

        assertEquals("Brötchen 6 Stück", items[2].extractedName)
        assertEquals("1.89", items[2].extractedPrice)
    }

    @Test
    fun `parseReceiptText should handle malformed input gracefully`() {
        val malformedText = """
            INVALID STORE FORMAT 123!@#
            INVALID DATE FORMAT
            NO TOTAL AMOUNT
        """.trimIndent()

        val ocrResult = OCRResult(
            text = malformedText,
            confidence = 0.9f,
            boundingBoxes = emptyList(),
            language = "de",
            processingTimeMs = 1000
        )

        val result = parser.parseReceiptText(ocrResult)

        // Should handle gracefully with errors
        assertTrue(result.parseErrors.isNotEmpty())
        assertTrue(result.parseErrors.any { it is ReceiptParseError.MissingRequiredField })
    }
}
```

### 1.3 Repository Testing

**File**: `composeApp/src/commonTest/kotlin/domain/repository/ReceiptRepositoryTest.kt`

```kotlin
@Test
class ReceiptRepositoryTest {

    private val mockRepository = mockk<ReceiptRepository>()

    @BeforeTest
    fun setup() {
        // Setup mock repository behavior
    }

    @Test
    fun `saveReceipt should return success with valid receipt`() = runTest {
        // Arrange
        val receipt = createTestReceipt()
        val expectedId = "receipt-123"

        coEvery { mockRepository.saveReceipt(receipt) } returns Result.success(expectedId)

        // Act
        val result = mockRepository.saveReceipt(receipt)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
    }

    @Test
    fun `getReceipt should return receipt when exists`() = runTest {
        // Arrange
        val receiptId = "receipt-123"
        val expectedReceipt = createTestReceipt().copy(id = receiptId)

        coEvery { mockRepository.getReceipt(receiptId) } returns Result.success(expectedReceipt)

        // Act
        val result = mockRepository.getReceipt(receiptId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedReceipt, result.getOrNull())
    }

    @Test
    fun `getReceipt should return null when receipt doesn't exist`() = runTest {
        // Arrange
        val receiptId = "nonexistent-123"

        coEvery { mockRepository.getReceipt(receiptId) } returns Result.success(null)

        // Act
        val result = mockRepository.getReceipt(receiptId)

        // Assert
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `searchReceipts should filter by store name`() = runTest {
        // Arrange
        val query = "REWE"
        val expectedReceipts = listOf(
            createTestReceipt().copy(id = "1", storeName = "REWE Markt"),
            createTestReceipt().copy(id = "2", storeName = "REWE Center")
        )

        coEvery { mockRepository.searchReceipts(query) } returns Result.success(expectedReceipts)

        // Act
        val result = mockRepository.searchReceipts(query)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertTrue(result.getOrNull()?.all { it.storeName.contains("REWE") } == true)
    }

    private fun createTestReceipt(): Receipt {
        return Receipt(
            id = "",
            storeName = "Test Store",
            scanDate = Clock.System.now(),
            items = listOf(
                ReceiptItem(
                    name = "Test Item",
                    unitPrice = 5.99,
                    totalPrice = 5.99,
                    quantity = 1
                )
            ),
            totalAmount = 5.99,
            ocrConfidence = 0.85f,
            rawOCRText = "Test Receipt"
        )
    }
}
```

### 1.4 ViewModel Testing

**File**: `composeApp/src/commonTest/kotlin/presentation/scanner/ReceiptScannerViewModelTest.kt`

```kotlin
@Test
class ReceiptScannerViewModelTest {

    private val mockOCRService = mockk<OCRService>()
    private val mockCameraService = mockk<CameraService>()
    private val mockImageProcessor = mockk<ImageProcessor>()
    private val mockRepository = mockk<ReceiptRepository>()

    private lateinit var viewModel: ReceiptScannerViewModel

    @BeforeTest
    fun setup() {
        viewModel = ReceiptScannerViewModel(
            mockOCRService,
            mockCameraService,
            mockImageProcessor,
            mockRepository
        )
    }

    @Test
    fun `scanReceipt should complete successfully with valid inputs`() = runTest {
        // Arrange
        val testImageBytes = byteArrayOf(1, 2, 3, 4, 5)
        val captureResult = ImageCaptureResult(
            imageBytes = testImageBytes,
            width = 1024,
            height = 1024,
            format = ImageFormat.JPEG,
            metadata = ImageMetadata(0, ImageOrientation.PORTRAIT, 1.0f, testImageBytes.size)
        )

        val ocrResult = ReceiptOCRResult(
            rawText = "REWE\nMilch 2.49€\nSUMME: 2.49€",
            storeName = "REWE",
            receiptDate = "22.09.2025",
            totalAmount = "2.49",
            items = listOf(
                ReceiptLineItem("Milch 2.49€", "Milch", "2.49", 0.9f, TextBoundingBox("", 0f, 0f, 0f, 0f, 0.9f))
            ),
            confidence = 0.85f
        )

        val savedReceiptId = "receipt-123"

        // Mock all services
        coEvery { mockCameraService.isPermissionGranted() } returns true
        coEvery { mockCameraService.captureImage() } returns Result.success(captureResult)
        coEvery { mockImageProcessor.preprocessForOCR(testImageBytes) } returns Result.success(testImageBytes)
        coEvery { mockOCRService.recognizeReceiptText(testImageBytes) } returns Result.success(ocrResult)
        coEvery { mockRepository.saveReceipt(any()) } returns Result.success(savedReceiptId)

        // Act
        viewModel.scanReceipt()

        // Assert
        val finalState = viewModel.scanState.value
        assertTrue(finalState is ReceiptScannerViewModel.ScanState.Success)

        val successState = finalState as ReceiptScannerViewModel.ScanState.Success
        assertEquals("REWE", successState.receipt.storeName)
        assertEquals(2.49, successState.receipt.totalAmount, 0.01)
        assertEquals(savedReceiptId, successState.receipt.id)
    }

    @Test
    fun `scanReceipt should handle camera permission denied`() = runTest {
        // Arrange
        coEvery { mockCameraService.isPermissionGranted() } returns false
        coEvery { mockCameraService.requestPermissions() } returns Result.success(PermissionStatus.DENIED)

        // Act
        viewModel.scanReceipt()

        // Assert
        val finalState = viewModel.scanState.value
        assertTrue(finalState is ReceiptScannerViewModel.ScanState.Error)

        val errorState = finalState as ReceiptScannerViewModel.ScanState.Error
        assertTrue(errorState.message.contains("Camera permission required"))
    }

    @Test
    fun `scanReceipt should handle OCR processing failure`() = runTest {
        // Arrange
        val testImageBytes = byteArrayOf(1, 2, 3, 4, 5)
        val captureResult = ImageCaptureResult(
            testImageBytes, 1024, 1024, ImageFormat.JPEG,
            ImageMetadata(0, ImageOrientation.PORTRAIT, 1.0f, testImageBytes.size)
        )

        coEvery { mockCameraService.isPermissionGranted() } returns true
        coEvery { mockCameraService.captureImage() } returns Result.success(captureResult)
        coEvery { mockImageProcessor.preprocessForOCR(testImageBytes) } returns Result.success(testImageBytes)
        coEvery { mockOCRService.recognizeReceiptText(testImageBytes) } returns Result.failure(
            OCRError.ProcessingError("OCR failed")
        )

        // Act
        viewModel.scanReceipt()

        // Assert
        val finalState = viewModel.scanState.value
        assertTrue(finalState is ReceiptScannerViewModel.ScanState.Error)

        val errorState = finalState as ReceiptScannerViewModel.ScanState.Error
        assertTrue(errorState.message.contains("OCR processing failed"))
    }

    @Test
    fun `resetScanState should reset to idle`() {
        // Arrange - Set state to something other than idle
        viewModel.scanState.value = ReceiptScannerViewModel.ScanState.ProcessingOCR

        // Act
        viewModel.resetScanState()

        // Assert
        assertEquals(ReceiptScannerViewModel.ScanState.Idle, viewModel.scanState.value)
    }
}
```

## 2. Platform-Specific Testing

### 2.1 iOS Vision Framework Testing

**File**: `composeApp/src/iosTest/kotlin/platform/ocr/IOSOCRServiceTest.kt`

```kotlin
@Test
class IOSOCRServiceTest {

    @Test
    fun testVisionFrameworkIntegration() {
        // Test iOS-specific Vision Framework functionality
        // Mock Vision Framework responses
        // Verify German language configuration
    }

    @Test
    fun testIOSImagePreprocessing() {
        // Test CoreImage processing pipeline
        // Verify contrast enhancement
        // Test perspective correction
    }

    @Test
    fun testIOSErrorHandling() {
        // Test Vision Framework error scenarios
        // Verify proper error mapping to shared error types
        // Test memory management under low memory conditions
    }
}
```

### 2.2 Android ML Kit Testing

**File**: `composeApp/src/androidTest/kotlin/platform/ocr/AndroidOCRServiceTest.kt`

```kotlin
@Test
class AndroidOCRServiceTest {

    @Test
    fun testMLKitIntegration() {
        // Test Android-specific ML Kit functionality
        // Mock ML Kit text recognition
        // Verify German language configuration
    }

    @Test
    fun testAndroidImagePreprocessing() {
        // Test Android image processing with OpenCV
        // Verify bitmap handling
        // Test memory efficiency
    }

    @Test
    fun testAndroidErrorHandling() {
        // Test ML Kit error scenarios
        // Verify proper error mapping to shared error types
        // Test battery optimization impacts
    }
}
```

## 3. Integration Testing Strategy

### 3.1 End-to-End Receipt Processing

**File**: `composeApp/src/commonTest/kotlin/integration/ReceiptProcessingIntegrationTest.kt`

```kotlin
@Test
class ReceiptProcessingIntegrationTest {

    @Test
    fun testCompleteReceiptScanningFlow() = runTest {
        // Test the complete flow from image capture to database storage
        // Use real test receipt images
        // Verify data consistency throughout the pipeline
        // Test error recovery at each stage
    }

    @Test
    fun testGermanReceiptVariations() = runTest {
        // Test different German receipt formats
        // Major retailers: REWE, EDEKA, ALDI, LIDL, PENNY
        // Different date formats, price formats
        // Various product name patterns
    }

    @Test
    fun testErrorRecoveryScenarios() = runTest {
        // Test recovery from network failures
        // Test recovery from low-quality images
        // Test partial OCR failures
        // Verify user experience remains smooth
    }
}
```

## 4. Performance Testing

### 4.1 OCR Performance Benchmarks

```kotlin
@Test
class OCRPerformanceTest {

    @Test
    fun testOCRProcessingTime() = runTest {
        val testImages = loadTestReceiptImages()
        val processingTimes = mutableListOf<Long>()

        testImages.forEach { imageBytes ->
            val startTime = System.currentTimeMillis()
            ocrService.recognizeText(imageBytes)
            val endTime = System.currentTimeMillis()

            processingTimes.add(endTime - startTime)
        }

        val averageTime = processingTimes.average()
        val maxTime = processingTimes.maxOrNull() ?: 0L

        // Performance assertions
        assertTrue(averageTime < 3000, "Average OCR time should be under 3 seconds")
        assertTrue(maxTime < 5000, "Max OCR time should be under 5 seconds")
    }

    @Test
    fun testMemoryUsage() = runTest {
        // Monitor memory usage during OCR processing
        // Verify no memory leaks
        // Test with large images
    }
}
```

## 5. Test Data Management

### 5.1 Test Receipt Images

Create a collection of test receipt images representing:

- Different German retailers
- Various lighting conditions
- Different paper qualities
- Crumpled/damaged receipts
- Different orientations

### 5.2 Test Data Factory

```kotlin
object TestDataFactory {

    fun createGermanReceiptText(
        store: String = "REWE Markt GmbH",
        date: String = "22.09.2025",
        items: List<Pair<String, String>> = listOf("Milch 1L" to "2.49"),
        total: String = "2.49"
    ): String {
        val itemLines = items.joinToString("\n") { "${it.first}  ${it.second}€" }
        return """
            $store
            Datum: $date

            $itemLines

            SUMME: $total€
        """.trimIndent()
    }

    fun createTestImageBytes(size: Int = 1024): ByteArray {
        // Generate test image data
        return ByteArray(size) { it.toByte() }
    }

    fun createTestOCRResult(
        text: String = createGermanReceiptText(),
        confidence: Float = 0.85f
    ): OCRResult {
        return OCRResult(
            text = text,
            confidence = confidence,
            boundingBoxes = emptyList(),
            language = "de",
            processingTimeMs = 1000
        )
    }
}
```

## 6. Test Automation and CI/CD Integration

### 6.1 Automated Test Execution

```yaml
# Add to existing CI/CD pipeline
test-phase2:
  runs-on: ubuntu-latest
  steps:
    - name: Run KMP Tests
      run: ./gradlew testDebugUnitTest

    - name: Run iOS Tests (if on macOS runner)
      run: ./gradlew iosSimulatorArm64Test

    - name: Run Android Tests
      run: ./gradlew testReleaseUnitTest

    - name: Generate Test Reports
      run: ./gradlew testReport

    - name: Upload Test Results
      uses: actions/upload-artifact@v3
      with:
        name: test-reports
        path: build/reports/tests/
```

### 6.2 Test Coverage Requirements

- **Minimum Coverage**: 85% for shared business logic
- **Critical Path Coverage**: 95% for OCR and parsing logic
- **Platform-Specific Coverage**: 80% (due to platform testing complexity)
- **Integration Test Coverage**: 70% of critical user flows

### 6.3 Test Quality Gates

- All tests must pass before merge to develop branch
- Performance tests must meet benchmarks
- Memory leak tests must show clean memory usage
- Platform-specific tests must pass on both iOS and Android

This comprehensive testing strategy ensures robust validation of the OCR implementation across all platforms while maintaining high code quality and performance standards.

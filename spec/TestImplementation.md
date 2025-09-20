# Test Implementation - Kotlin Multiplatform### Test Implementation

````typescript

## Overview// Example test suite

describe('ShoppingListOptimizer', () => {

Comprehensive testing strategy for AllesTeurer KMP application covering shared business logic, platform-specific implementations, and integration testing across iOS and Android platforms.  it('should minimize total cost across multiple stores', async () => {

    const items = [

## Testing Architecture      { productId: '1', quantity: 2 },

      { productId: '2', quantity: 1 }

```kotlin    ]

// Test Structure

commonTest/          # Shared business logic tests    const result = await optimizer.optimize(

├── kotlin/      items,

│   ├── data/        # Repository tests      OptimizationStrategy.COST_MINIMUM

│   ├── domain/      # Use case tests    )

│   └── presentation/ # ViewModel tests

    expect(result.totalCost).toBeLessThan(manualTotal)

androidUnitTest/     # Android-specific unit tests    expect(result.stores).toHaveLength(2)

├── kotlin/    expect(result.savings).toBeGreaterThan(0)

│   ├── ocr/         # ML Kit OCR tests  })

│   └── platform/    # Android utilities tests})

````

androidInstrumentedTest/ # Android integration tests

├── kotlin/## 🔄 DevOps & Deployment

│ ├── database/ # SQLDelight Android tests

│ └── ui/ # Compose UI tests### CI/CD Pipeline

````yaml

iosTest/            # iOS-specific testsname: Deploy Pipeline

├── kotlin/

│   ├── ocr/        # Vision Framework OCR testson:

│   └── platform/   # iOS utilities tests  push:

```    branches: [main, develop]



## Shared Business Logic Tests (commonTest)jobs:

  test:

### Receipt Processing Tests    runs-on: ubuntu-latest

    steps:

```kotlin      - uses: actions/checkout@v3

// commonTest/kotlin/domain/ReceiptProcessorTest.kt      - name: Install dependencies

import kotlin.test.Test        run: npm ci

import kotlin.test.assertEquals      - name: Run tests

import kotlin.test.assertTrue        run: npm test

import kotlinx.coroutines.test.runTest      - name: SonarCloud scan

        uses: SonarSource/sonarcloud-github-action@master

class ReceiptProcessorTest {

    private val mockOcrService = MockOCRService()  build:

    private val mockRepository = MockReceiptRepository()    needs: test

    private val processor = ReceiptProcessor(mockOcrService, mockRepository)    strategy:

      matrix:

    @Test        platform: [ios, android, web, backend]

    fun `should process receipt image successfully`() = runTest {    steps:

        // Given      - name: Build ${{ matrix.platform }}

        val imageData = ByteArray(100) // Mock image data        run: npm run build:${{ matrix.platform }}

        val expectedOcrText = """

            REWE  deploy:

            Bananen 1kg         €2.99    needs: build

            Milch 1L           €1.49    if: github.ref == 'refs/heads/main'

            TOTAL              €4.48    steps:

        """.trimIndent()      - name: Deploy to production

                run: |

        mockOcrService.setMockResponse(expectedOcrText)          kubectl apply -f k8s/

                  helm upgrade alles-teuer ./charts

        // When```

        val result = processor.processReceipt(imageData)

        ### Infrastructure as Code

        // Then```terraform

        assertTrue(result.isSuccess)# AWS Infrastructure

        val receipt = result.getOrNull()!!module "alles_teuer" {

        assertEquals("REWE", receipt.storeName)  source = "./modules/app"

        assertEquals(2, receipt.items.size)

        assertEquals(4.48, receipt.totalAmount, 0.01)  # RDS PostgreSQL

    }  database = {

    engine         = "postgres"

    @Test    version        = "15.4"

    fun `should handle OCR errors gracefully`() = runTest {    instance_class = "db.t3.medium"

        // Given    storage        = 100

        val imageData = ByteArray(100)  }

        mockOcrService.setError(Exception("OCR service unavailable"))

          # EKS Cluster

        // When  kubernetes = {

        val result = processor.processReceipt(imageData)    version    = "1.28"

            node_count = 3

        // Then    node_type  = "t3.large"

        assertTrue(result.isFailure)  }

    }

}  # Redis Cache

```  redis = {

    node_type = "cache.t3.micro"

### Price Analysis Tests    replicas  = 2

  }

```kotlin

// commonTest/kotlin/domain/PriceAnalyzerTest.kt  # S3 Buckets

import kotlin.test.Test  storage = {

import kotlin.test.assertEquals    receipts_bucket = "alles-teuer-receipts"

import kotlin.test.assertTrue    images_bucket   = "alles-teuer-images"

  }

class PriceAnalyzerTest {}

    private val analyzer = PriceAnalyzer()```



    @Test## 📁 Project Structure

    fun `should calculate inflation rate correctly`() {

        // Given```

        val priceHistory = listOf(alles-teuer/

            PriceRecord(productId = "1", price = 2.00, date = date("2023-01-01")),├── apps/

            PriceRecord(productId = "1", price = 2.10, date = date("2023-06-01")),│   ├── ios/                 # iOS App (Swift/SwiftUI)

            PriceRecord(productId = "1", price = 2.20, date = date("2023-12-01"))│   ├── android/              # Android App (Kotlin)

        )│   ├── web/                  # Web App (Next.js)

        │   └── backend/              # Backend Services (NestJS)

        // When├── packages/

        val inflationRate = analyzer.calculateInflationRate(priceHistory)│   ├── shared/               # Shared TypeScript code

        │   ├── ui/                   # Shared UI components

        // Then│   ├── api-client/           # Generated API client

        assertEquals(10.0, inflationRate, 0.1) // 10% inflation over the year│   └── database/             # Prisma schema & migrations

    }├── services/

│   ├── auth/                 # Authentication service

    @Test│   ├── product/              # Product management

    fun `should identify price trends`() {│   ├── price/                # Price tracking

        // Given│   ├── ocr/                  # OCR processing

        val priceHistory = listOf(│   └── optimizer/            # Optimization engine

            PriceRecord(productId = "1", price = 2.00, date = date("2023-01-01")),├── infrastructure/

            PriceRecord(productId = "1", price = 2.10, date = date("2023-02-01")),│   ├── terraform/            # IaC definitions

            PriceRecord(productId = "1", price = 2.20, date = date("2023-03-01"))│   ├── k8s/                  # Kubernetes manifests

        )│   └── docker/               # Dockerfiles

        ├── docs/

        // When│   ├── api/                  # API documentation

        val trend = analyzer.identifyTrend(priceHistory)│   ├── architecture/         # Architecture decisions

        │   └── guides/               # Development guides

        // Then└── scripts/

        assertEquals(PriceTrend.INCREASING, trend)    ├── setup.sh              # Development setup

    }    └── deploy.sh             # Deployment scripts

}```

````

## 🤖 AI Assistant Implementation Guide

### Database Tests (SQLDelight)

### Step-by-Step Instructions for AI Implementation

````kotlin

// commonTest/kotlin/data/ReceiptDatabaseTest.kt#### 1. Environment Setup

import kotlin.test.Test```bash

import kotlin.test.assertEquals# Clone and setup

import kotlin.test.assertNotNullgit clone https://github.com/mpwg/AllesTeurer.git

import app.cash.sqldelight.db.SqlDrivercd AllesTeurer

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

# Install dependencies

class ReceiptDatabaseTest {npm install

    private fun createInMemoryDatabase(): Database {

        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)# Setup environment variables

        Database.Schema.create(driver)cp .env.example .env

        return Database(driver)# Edit .env with necessary API keys and configs

    }

# Initialize database

    @Testnpm run db:setup

    fun `should insert and retrieve receipt`() {npm run db:migrate

        // Given```

        val database = createInMemoryDatabase()

        val receiptQueries = database.receiptQueries#### 2. Backend Implementation Order

        1. **Authentication Service** - Start with user management

        val receipt = Receipt(2. **Product Service** - Core domain logic

            id = "test-id",3. **Price Service** - Price tracking and comparison

            storeName = "REWE",4. **OCR Service** - Receipt processing

            scanDate = Clock.System.now(),5. **Optimizer Service** - Shopping list optimization

            items = listOf(

                ReceiptItem("Bananen", 1, 2.99, 2.99)#### 3. Frontend Implementation Order

            ),1. **Navigation & Layout** - Base structure

            totalAmount = 2.992. **Authentication Flow** - Login/Register

        )3. **Product Search** - Core feature

        4. **Shopping Lists** - List management

        // When5. **Receipt Scanning** - Camera integration

        receiptQueries.insertReceipt(6. **Analytics Dashboard** - Data visualization

            id = receipt.id,

            storeName = receipt.storeName,#### 4. Integration Points

            scanDate = receipt.scanDate.toEpochMilliseconds(),```typescript

            totalAmount = receipt.totalAmount// Key integration interfaces

        )interface PriceProvider {

          getName(): string

        // Then  searchProducts(query: string): Promise<Product[]>

        val retrieved = receiptQueries.selectById(receipt.id).executeAsOneOrNull()  getPrices(productId: string): Promise<Price[]>

        assertNotNull(retrieved)}

        assertEquals(receipt.storeName, retrieved.storeName)

    }interface OCRProvider {

  processImage(image: Buffer): Promise<string>

    @Test  parseReceipt(text: string): Promise<Receipt>

    fun `should query receipts by date range`() {}

        // Given

        val database = createInMemoryDatabase()interface OptimizationEngine {

        val receiptQueries = database.receiptQueries  optimize(items: Item[], strategy: Strategy): Promise<Result>

        }

        val startDate = Clock.System.now().minus(30.days)```

        val endDate = Clock.System.now()

        ## 📝 Additional Notes for AI Implementation

        // Insert test data

        receiptQueries.insertReceipt(### Code Generation Guidelines

            id = "receipt1",- Use TypeScript with strict mode enabled

            storeName = "REWE",- Follow Clean Architecture principles

            scanDate = startDate.plus(1.days).toEpochMilliseconds(),- Implement comprehensive error handling

            totalAmount = 10.00- Add JSDoc comments for all public APIs

        )- Write tests alongside implementation

        - Use dependency injection for testability

        // When

        val receipts = receiptQueries.selectByDateRange(### Common Pitfalls to Avoid

            startDate.toEpochMilliseconds(),- Don't hardcode API endpoints

            endDate.toEpochMilliseconds()- Avoid synchronous blocking operations

        ).executeAsList()- Don't store sensitive data in plain text

        - Avoid N+1 query problems

        // Then- Don't skip input validation

        assertEquals(1, receipts.size)

    }### Performance Considerations

}- Implement pagination for all list endpoints

```- Use database indexes strategically

- Cache frequently accessed data

## Platform-Specific Tests- Optimize images before storage

- Use CDN for static assets

### Android OCR Tests

## 🎯 Definition of Done

```kotlin

// androidUnitTest/kotlin/ocr/MLKitOCRServiceTest.ktA feature is considered complete when:

import androidx.test.ext.junit.runners.AndroidJUnit4- [ ] Code is written and reviewed

import org.junit.Test- [ ] Unit tests pass with >80% coverage

import org.junit.runner.RunWith- [ ] Integration tests are implemented

import org.mockito.kotlin.mock- [ ] API documentation is updated

import org.mockito.kotlin.whenever- [ ] UI is responsive on all platforms

import com.google.mlkit.vision.text.Text- [ ] Performance meets defined KPIs

import com.google.mlkit.vision.text.TextRecognition- [ ] Security scan passes

import kotlinx.coroutines.test.runTest- [ ] Feature is deployed to staging

- [ ] Product owner approves

@RunWith(AndroidJUnit4::class)

class MLKitOCRServiceTest {---

    private val mockTextRecognizer = mock<TextRecognizer>()

    private val ocrService = MLKitOCRService(mockTextRecognizer)**This document serves as the single source of truth for the Alles Teuer project implementation. AI assistants should reference this document when generating code and making architectural decisions.**


    @Test
    fun `should extract text from image using ML Kit`() = runTest {
        // Given
        val imageBytes = ByteArray(100)
        val mockText = mock<Text>()
        whenever(mockText.text).thenReturn("REWE\nBananen €2.99")
        whenever(mockTextRecognizer.process(any())).thenReturn(
            Tasks.forResult(mockText)
        )

        // When
        val result = ocrService.extractText(imageBytes)

        // Then
        assertEquals("REWE\nBananen €2.99", result)
    }
}
````

### iOS OCR Tests

```kotlin
// iosTest/kotlin/ocr/VisionFrameworkOCRServiceTest.kt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import platform.Vision.*
import platform.CoreGraphics.*

class VisionFrameworkOCRServiceTest {
    private val ocrService = VisionFrameworkOCRService()

    @Test
    fun `should extract text from image using Vision Framework`() = runTest {
        // Given
        val imageBytes = ByteArray(100) // Mock image data

        // When
        val result = ocrService.extractText(imageBytes)

        // Then
        // Note: Actual testing would require mock Vision Framework
        // This is a simplified example
        assertTrue(result.isNotEmpty())
    }
}
```

## Integration Tests

### Database Integration Tests (Android)

```kotlin
// androidInstrumentedTest/kotlin/database/DatabaseIntegrationTest.kt
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

@RunWith(AndroidJUnit4::class)
class DatabaseIntegrationTest {

    @Test
    fun `should perform database operations on Android`() {
        // Given
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val driver = AndroidSqliteDriver(Database.Schema, context, "test.db")
        val database = Database(driver)

        // When
        database.receiptQueries.insertReceipt(
            id = "test",
            storeName = "REWE",
            scanDate = System.currentTimeMillis(),
            totalAmount = 10.0
        )

        // Then
        val receipt = database.receiptQueries.selectById("test").executeAsOneOrNull()
        assertNotNull(receipt)
        assertEquals("REWE", receipt.storeName)
    }
}
```

### UI Tests (Compose Multiplatform)

```kotlin
// androidInstrumentedTest/kotlin/ui/ReceiptScannerScreenTest.kt
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReceiptScannerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `should display camera preview when screen loads`() {
        // Given
        composeTestRule.setContent {
            ReceiptScannerScreen()
        }

        // Then
        composeTestRule
            .onNodeWithTag("camera_preview")
            .assertIsDisplayed()
    }

    @Test
    fun `should show processing state after capture`() {
        // Given
        composeTestRule.setContent {
            ReceiptScannerScreen()
        }

        // When
        composeTestRule
            .onNodeWithTag("capture_button")
            .performClick()

        // Then
        composeTestRule
            .onNodeWithText("Processing...")
            .assertIsDisplayed()
    }
}
```

## Test Configuration

### gradle/libs.versions.toml (Testing Dependencies)

```toml
[versions]
junit = "4.13.2"
androidx-test-ext = "1.1.5"
androidx-test-espresso = "3.5.1"
mockito-kotlin = "4.1.0"
kotlinx-coroutines-test = "1.8.0"
turbine = "1.0.0"

[libraries]
# Common Test Dependencies
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines-test" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }

# Android Test Dependencies
junit = { module = "junit:junit", version.ref = "junit" }
androidx-test-ext-junit = { module = "androidx.test.ext:junit", version.ref = "androidx-test-ext" }
androidx-test-espresso-core = { module = "androidx.test.espresso:espresso-core", version.ref = "androidx-test-espresso" }
androidx-compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4", version.ref = "compose" }
mockito-kotlin = { module = "org.mockito.kotlin:mockito-kotlin", version.ref = "mockito-kotlin" }

# SQLDelight Test
sqldelight-sqlite-driver = { module = "app.cash.sqldelight:sqlite-driver", version.ref = "sqldelight" }
```

### Test Build Configuration

```kotlin
// apps/composeApp/build.gradle.kts
kotlin {
    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
                implementation(libs.sqldelight.sqlite.driver)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.mockito.kotlin)
                implementation(libs.androidx.test.ext.junit)
            }
        }

        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.androidx.test.ext.junit)
                implementation(libs.androidx.test.espresso.core)
                implementation(libs.androidx.compose.ui.test.junit4)
            }
        }
    }
}
```

## CI/CD Pipeline for Testing

### .github/workflows/tests.yml

```yaml
name: Test Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: "17"
          distribution: "temurin"

      - name: Cache Gradle packages
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
          restore-keys: |
            ${{ runner.os }}-gradle-

      - name: Run shared tests
        run: ./gradlew :apps:composeApp:testDebugUnitTest

      - name: Run Android unit tests
        run: ./gradlew :apps:composeApp:testDebugUnitTest

      - name: Generate test report
        run: ./gradlew jacocoTestReport

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          file: ./build/reports/jacoco/test/jacocoTestReport.xml

  android-instrumented-tests:
    runs-on: macos-latest
    strategy:
      matrix:
        api-level: [29, 31, 33]
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: "17"
          distribution: "temurin"

      - name: Enable KVM group perms
        run: |
          echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
          sudo udevadm control --reload-rules
          sudo udevadm trigger --name-match=kvm

      - name: Run Android instrumented tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: ${{ matrix.api-level }}
          arch: x86_64
          profile: Nexus 6
          script: ./gradlew :apps:composeApp:connectedDebugAndroidTest

  ios-tests:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: "17"
          distribution: "temurin"

      - name: Run iOS tests
        run: ./gradlew :apps:composeApp:iosSimulatorArm64Test
```

## Mock Implementations

### Mock OCR Service

```kotlin
// commonTest/kotlin/mock/MockOCRService.kt
class MockOCRService : OCRService {
    private var mockResponse: String = ""
    private var mockError: Exception? = null

    fun setMockResponse(response: String) {
        mockResponse = response
        mockError = null
    }

    fun setError(error: Exception) {
        mockError = error
        mockResponse = ""
    }

    override suspend fun extractText(imageData: ByteArray): String {
        mockError?.let { throw it }
        return mockResponse
    }
}
```

### Mock Repository

```kotlin
// commonTest/kotlin/mock/MockReceiptRepository.kt
class MockReceiptRepository : ReceiptRepository {
    private val receipts = mutableListOf<Receipt>()

    override suspend fun saveReceipt(receipt: Receipt) {
        receipts.add(receipt)
    }

    override suspend fun getReceiptById(id: String): Receipt? {
        return receipts.find { it.id == id }
    }

    override suspend fun getAllReceipts(): List<Receipt> {
        return receipts.toList()
    }

    fun clear() {
        receipts.clear()
    }
}
```

## Testing Commands

### Run All Tests

```bash
# Run all tests (common + platform-specific)
./gradlew test

# Run only shared tests
./gradlew :apps:composeApp:testDebugUnitTest

# Run Android instrumented tests
./gradlew :apps:composeApp:connectedDebugAndroidTest

# Run iOS tests
./gradlew :apps:composeApp:iosSimulatorArm64Test

# Generate test coverage report
./gradlew jacocoTestReport
```

### Test-Specific Commands

```bash
# Run specific test class
./gradlew :apps:composeApp:testDebugUnitTest --tests "*ReceiptProcessorTest"

# Run tests with continuous mode
./gradlew :apps:composeApp:testDebugUnitTest --continuous

# Run tests with detailed output
./gradlew :apps:composeApp:testDebugUnitTest --info
```

## Testing Best Practices

### KMP Testing Guidelines

1. **Shared Logic First**: Test all business logic in `commonTest`
2. **Platform-Specific Mocks**: Use expect/actual pattern for platform mocks
3. **Database Testing**: Always test SQLDelight queries with real SQLite
4. **UI Testing**: Test shared UI components, platform-specific adaptations separately
5. **Integration Tests**: Focus on OCR→Database→UI workflows

### Code Coverage Targets

- **Shared Business Logic**: 90%+ coverage required
- **Platform-Specific Code**: 70%+ coverage required
- **UI Components**: 60%+ coverage required
- **Integration Workflows**: 80%+ coverage required

### Test Naming Conventions

- Use descriptive test names: `should_[expected_behavior]_when_[condition]`
- Group related tests in inner classes
- Use `@Test` annotation consistently
- Prefer `kotlin.test` over platform-specific frameworks

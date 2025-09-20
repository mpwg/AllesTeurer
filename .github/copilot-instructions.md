# AllesTeurer - AI Agent Instructions

## Project Overview

AllesTeurer is a privacy-first mobile app that tracks product price inflation through receipt scanning and local analytics. The app uses Kotlin Multiplatform Mobile (KMP) with Compose Multiplatform for shared business logic and UI, platform-specific OCR implementations (Vision Framework for iOS, ML Kit for Android), and SQLDelight for persistence, maintaining strict on-device processing.

**Architecture**: Mobile-first development using KMP and Compose Multiplatform targeting iOS and Android.

## KMM Project Structure

```
alles-teurer/
├── gradle/                  # Gradle wrapper and version catalogs
├── apps/
│   └── composeApp/         # Main KMP application
│       ├── src/
│       │   ├── commonMain/ # Shared Kotlin code
│       │   │   ├── kotlin/
│       │   │   │   ├── data/        # Repositories and data sources
│       │   │   │   ├── domain/      # Business logic and use cases
│       │   │   │   ├── presentation/ # ViewModels and UI state
│       │   │   │   └── ui/          # Compose Multiplatform UI
│       │   │   └── sqldelight/     # Database schema and queries
│       │   ├── androidMain/ # Android-specific implementations
│       │   │   └── kotlin/
│       │   │       ├── ocr/         # ML Kit OCR implementation
│       │   │       └── platform/   # Android-specific utilities
│       │   ├── iosMain/     # iOS-specific implementations
│       │   │   └── kotlin/
│       │   │       ├── ocr/         # Vision Framework OCR
│       │   │       └── platform/   # iOS-specific utilities
│       ├── androidApp/      # Android app wrapper
│       └── iosApp/         # iOS app wrapper (Xcode project)
├── shared/                 # Additional shared modules (if needed)
├── spec/                  # Requirements and architecture docs
├── tools/                 # Development tooling and scripts
├── gradle.properties      # Gradle configuration
├── settings.gradle.kts    # Module configuration
└── build.gradle.kts       # Root build configuration
```

## Architecture & Key Patterns

### Kotlin Multiplatform Strategy

- **Shared Business Logic**: 100% business logic sharing via commonMain
- **Platform-Specific OCR**: expect/actual declarations for native OCR implementations
- **Compose Multiplatform UI**: Cross-platform UI with platform adaptations
- **Native Performance**: Direct compilation to native code on all platforms
- **Privacy-First**: All processing happens on-device, optional sync only

### MVVM + Repository Pattern

- **UI**: Compose Multiplatform with platform-aware components
- **ViewModels**: Shared ViewModels handling UI state and business logic
- **Repositories**: Data access abstraction with platform-specific implementations
- **Use Cases**: Domain logic encapsulation for complex operations
- **Local-First**: SQLDelight for type-safe, multiplatform database queries

### Multiplatform Code Organization

- **commonMain/**: Shared business logic, UI, and data models
- **androidMain/**: Android-specific implementations (ML Kit OCR, Android APIs)
- **iosMain/**: iOS-specific implementations (Vision Framework, iOS APIs)
- **Expect/Actual**: Platform-specific implementations of shared interfaces

### Critical Technical Constraints

- **SQLDelight ONLY**: Type-safe SQL database for all platforms
- **Kotlin 2.2.20+**: Modern Kotlin with K2 compiler
- **Gradle 9+**: Latest Gradle version with Kotlin DSL
- **Compose Multiplatform**: Shared UI across iOS and Android
- **Privacy-First**: No external API calls for core functionality, all data stays on device
- **Accessibility**: WCAG 2.2 Level AA compliance required for all UI components

## Essential File Structure

```
apps/composeApp/src/
├── commonMain/kotlin/
│   ├── data/
│   │   ├── local/           # SQLDelight database and DAOs
│   │   ├── repository/      # Repository implementations
│   │   └── models/          # Data models with @Serializable
│   ├── domain/
│   │   ├── models/          # Domain entities
│   │   ├── repository/      # Repository interfaces
│   │   └── usecase/         # Business logic use cases
│   ├── presentation/
│   │   ├── viewmodel/       # Shared ViewModels
│   │   └── state/           # UI state classes
│   └── ui/
│       ├── screens/         # Compose screens
│       ├── components/      # Reusable UI components
│       └── theme/           # Design system and theming
├── androidMain/kotlin/
│   ├── ocr/                 # ML Kit OCR implementation
│   ├── camera/              # Android camera integration
│   └── platform/           # Android-specific utilities
├── iosMain/kotlin/
│   ├── ocr/                 # Vision Framework OCR implementation
│   ├── camera/              # iOS camera integration
│   └── platform/           # iOS-specific utilities
└── commonMain/sqldelight/
    └── database/           # SQL schema and queries
```

│ ├── DataManager.swift # Swift Data operations
│ ├── ProductMatcher.swift # Local fuzzy matching algorithms
│ └── PriceAnalyzer.swift # Local analytics calculations
├── Views/ # SwiftUI views organized by feature
│ ├── Scanner/ # Receipt scanning interface
│ ├── Products/ # Product list and details
│ ├── Analytics/ # Price charts and insights
│ └── Settings/ # App configuration
├── Coordinators/ # Navigation flow management
└── Utils/ # Extensions and helper functions

```

### Shared Package Structure

```

packages/shared/
├── models/ # Platform-agnostic data models
│ ├── Receipt.ts # TypeScript definitions
│ ├── Product.ts # Shared product model
│ └── PriceHistory.ts # Price tracking models
├── analytics/ # Business logic algorithms
│ ├── PriceAnalyzer.ts # Price trend calculations
│ ├── InflationCalc.ts # Inflation rate algorithms
│ └── StatUtils.ts # Statistical utilities
├── matching/ # Product matching logic
│ ├── FuzzyMatcher.ts # Product name matching
│ └── Categorizer.ts # Product categorization
└── utils/ # Common utilities
├── DateUtils.ts # Date formatting and parsing
└── TextProcessor.ts # Text processing utilities

````

## Core Development Patterns

### Data Models (Kotlin Serialization)

```kotlin
@Serializable
data class Receipt(
    val id: String = UUID.randomUUID().toString(),
    val storeName: String,
    val scanDate: Instant,
    val items: List<ReceiptItem>,
    val totalAmount: Double
)

@Serializable
data class ReceiptItem(
    val name: String,
    val quantity: Int = 1,
    val unitPrice: Double,
    val totalPrice: Double
)
````

### ViewModels (Shared)

```kotlin
class ReceiptScannerViewModel(
    private val ocrService: OCRService,
    private val receiptRepository: ReceiptRepository
) : ViewModel() {
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState = _scanState.asStateFlow()

    suspend fun processReceipt(imageBytes: ByteArray) {
        _scanState.value = ScanState.Processing
        try {
            val receipt = ocrService.processReceiptImage(imageBytes)
            receiptRepository.saveReceipt(receipt)
            _scanState.value = ScanState.Success(receipt)
        } catch (e: Exception) {
            _scanState.value = ScanState.Error(e.message ?: "Unknown error")
        }
    }
}
```

### Compose Multiplatform Views

```kotlin
@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(products) { product ->
            ProductItem(
                product = product,
                onClick = { viewModel.onProductClick(product) }
            )
        }
    }
}
```

## Development Workflows

### Gradle Commands

```bash
# Build all targets
./gradlew build

# Run tests across all platforms
./gradlew test

# Format Kotlin code
./gradlew ktlintFormat

# Format code across entire monorepo
pnpm format
```

### Platform-Specific Commands

```bash
# Open iOS project (for platform-specific debugging)
open apps/composeApp/iosApp/iosApp.xcodeproj

# Run platform-specific tests
./gradlew :apps:composeApp:testDebugUnitTest        # Android
./gradlew :apps:composeApp:iosSimulatorArm64Test    # iOS

# Run on specific platforms
./gradlew :composeApp:installDebug             # Android
```

### Feature Development Approach

1. **Follow spec-driven workflow**: Reference `/spec/` directory for requirements and architecture
2. **EARS notation**: Requirements written as "WHEN [condition], THE SYSTEM SHALL [behavior]"
3. **Privacy validation**: Ensure no data leaves device except optional backend sync
4. **Accessibility first**: Use semantic markup and proper accessibility support in Compose

## Technology-Specific Conventions

### OCR Integration (Platform-Specific)

#### iOS (Vision Framework)

- Preprocess images for optimal OCR (resize to 1024x1024, enhance contrast)
- Handle German text recognition specifically
- Implement user correction flows for OCR errors
- Use async/await for all Vision operations

#### Android (ML Kit)

- Configure ML Kit for German language support
- Implement camera capture with CameraX
- Handle OCR result processing with error correction
- Use Kotlin coroutines for async operations

#### Web (Tesseract.js fallback)

- Load German language pack for better accuracy
- Implement client-side image preprocessing
- Provide manual text input as fallback
- Handle progressive enhancement gracefully

### Database Integration (SQLDelight)

- Use type-safe SQL queries across all platforms
- Implement proper schema migration strategies
- Handle complex relationships with foreign keys
- Write platform-specific database drivers when needed

### Testing Framework

- **KMP Testing**: Kotlin Test for shared business logic
- **Platform Testing**: Platform-specific UI and integration tests
- **OCR Testing**: Mock OCR services for consistent test results
- **Database Testing**: In-memory database for fast test execution

## Critical "Don'ts"

- **Never use platform-specific databases** - SQLDelight only for consistency
- **No external OCR services** - All OCR must be on-device
- **No shared UI state across platforms** - Keep platform UI independent when needed
- **Don't skip expect/actual implementations** - Always provide platform-specific code
- **Don't break accessibility** - Every UI element must be accessible

## Testing Approach

- **Unit Testing**: Test shared business logic, OCR processing, and price analytics
- **Integration Testing**: Verify OCR-to-database workflows across platforms
- **UI Testing**: Platform-specific UI testing (Compose testing, XCUITest for iOS)
- **Privacy Testing**: Ensure no network calls in core functionality

## References

- `/spec/Anforderungen.md` - Functional requirements in German
- `/spec/architecture.md` - Technical architecture decisions
- `/.github/instructions/kotlin.instructions.md` - Kotlin coding conventions
- `/.github/instructions/a11y.instructions.md` - Accessibility requirements
- `/.github/instructions/compose-multiplatform.instructions.md` - UI development patterns

### Swift Charts Implementation

- Create interactive price trend visualizations
- Support both light/dark mode appearances
- Implement drill-down functionality for detailed views
- Ensure charts are accessible with VoiceOver

### Apple Intelligence Integration

- Use Natural Language framework for product categorization
- Implement smart product matching algorithms
- Process receipt text locally without external API calls
- Handle multilingual product names (German focus)

### Testing Framework

- **Primary Framework**: Swift Testing (`import Testing`) for new tests
- **Legacy Support**: XCTest for existing compatibility
- **UI Testing**: XCUITest for end-to-end receipt scanning flows
- **Accessibility Testing**: Validate VoiceOver and Dynamic Type support

## Critical "Don'ts"

- **Never use Core Data** - Swift Data only
- **No external analytics** - All insights calculated locally
- **No third-party OCR** - Apple Vision Framework exclusively
- **No cloud processing** - Maintain privacy-first architecture
- **Don't break accessibility** - Every UI element must be accessible

## Testing Approach

- **Unit Testing**: Test OCR processing, product matching, and price analytics using Swift Testing framework
- **UI Testing**: Verify receipt scanning flow and data display accuracy with XCUITest
- **Accessibility Testing**: Validate VoiceOver and Dynamic Type support
- **Privacy Testing**: Ensure no network calls in core functionality

## References

- `/spec/Anforderungen.md` - Functional requirements in German
- `/spec/architecture.md` - Technical architecture decisions
- `/.github/instructions/swift.instructions.md` - Swift coding conventions
- `/.github/instructions/a11y.instructions.md` - Accessibility requirements
- `/.github/instructions/swiftui-observation.instructions.md` - SwiftUI Observable patterns
- `/.github/instructions/ai-agent-testing.instructions.md` - AI agent test implementation guidelines

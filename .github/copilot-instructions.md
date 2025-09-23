# AllesTeurer - AI Agent Instructions

## Project Overview

AllesTeurer is a privacy-first native iOS app that tracks product price inflation through receipt scanning and local analytics. The app uses SwiftUI for the user interface, SwiftData for persistence, Apple's Vision Framework for OCR text recognition, and Swift Charts for data visualization, maintaining strict on-device processing.

**Architecture**: iOS-first development using SwiftUI, SwiftData, and native iOS frameworks.

## iOS Project Structure

```
AllesTeurer/
├── Alles Teurer/            # Native iOS App
│   ├── Alles_TeurerApp.swift       # App Entry Point with SwiftData ModelContainer
│   ├── ContentView.swift           # Main SwiftUI View
│   ├── Item.swift                  # SwiftData Models
│   ├── Info.plist                  # App Configuration
│   ├── Alles_Teurer.entitlements  # App Capabilities
│   └── Assets.xcassets/            # App Assets and Icons
├── Alles Teurer.xcodeproj/  # Xcode Project Configuration
├── Alles TeurerTests/       # Unit Tests using Swift Testing
├── Alles TeurerUITests/     # UI Tests using XCUITest
├── spec/                    # Requirements and architecture docs
├── docs/                    # Additional documentation
├── README.md               # Project overview
└── .github/                # GitHub configuration and instructions
    ├── copilot-instructions.md     # AI agent development guidelines
    └── instructions/               # Specific coding guidelines
        ├── swift.instructions.md           # Swift coding conventions
        ├── swiftui-observation.instructions.md  # SwiftUI Observable patterns
        └── a11y.instructions.md           # Accessibility requirements
```

## Architecture & Key Patterns

### Native iOS Strategy

- **SwiftUI**: Declarative UI framework with native performance
- **SwiftData**: Type-safe data persistence with Core Data backend
- **Vision Framework**: On-device OCR for precise German text recognition
- **Swift Charts**: Native data visualization for price analytics
- **iOS-First**: Single platform focus with deep iOS integration
- **Privacy-First**: All processing happens on-device, optional sync only

### MVVM + Repository Pattern with Async/Await (MANDATORY)

**CRITICAL ARCHITECTURE RULES - ALWAYS ENFORCE:**

- **UI**: SwiftUI with Observable ViewModels and data binding
- **ViewModels**: @Observable classes handling UI state and business logic
- **Repositories**: Data access abstraction with SwiftData integration
- **Use Cases**: Domain logic encapsulation for complex operations
- **Local-First**: SwiftData for type-safe, native iOS data persistence

**CONCURRENCY REQUIREMENTS - NO EXCEPTIONS:**

- **Always use async/await pattern** - NEVER use completion handlers or @escaping closures
- **Thread Safety**: Use @ModelActor for data operations, @MainActor for UI updates
- **ViewModel Pattern**: All ViewModels must be @Observable and handle async operations properly
- **Data Operations**: All data access must go through ModelActor-based repositories
- **UI Updates**: All UI state changes must happen on @MainActor

**NEVER DO:**

- Use completion handlers instead of async/await
- Use @escaping closures for async operations
- Access SwiftData ModelContext directly from ViewModels
- Perform data operations without proper actor isolation
- Mix ObservableObject with @Observable patterns

### SwiftData Implementation

```swift
import SwiftData

@Model
class Item {
    var name: String
    var timestamp: Date

    init(name: String, timestamp: Date = Date()) {
        self.name = name
        self.timestamp = timestamp
    }
}

// In SwiftUI View
struct ContentView: View {
    @Query private var items: [Item]
    @Environment(\.modelContext) private var modelContext

    var body: some View {
        // UI code here
    }
}
```

### SwiftData Predicate Guidelines (CRITICAL)

**ALWAYS FOLLOW THESE RULES:**

- Use `#Predicate<ModelType>` with explicit type for @Query
- String filtering: `contains()`, `starts(with:)`, `localizedStandardContains()` for case-insensitive
- Boolean logic: Use `&&`/`||` in single expressions, `!condition` (NOT `== false`)
- External values: Create local copies first (`let now = Date.now`)
- Relationship queries: `collection.contains { }`, `collection.filter { }`, `!collection.isEmpty`

**PREDICATE EXAMPLES:**

```swift
// Correct patterns
#Predicate<Product> { product in product.name.contains("Milk") }
#Predicate<Receipt> { receipt in !receipt.items.isEmpty }
let today = Date.now
#Predicate<Receipt> { receipt in receipt.date > today }
```

### Critical Technical Constraints

- **SwiftData ONLY**: Native iOS data persistence
- **iOS 17.0+**: Minimum deployment target for SwiftData
- **Xcode 15.0+**: Development environment requirement
- **Privacy-First**: No external API calls for core functionality, all data stays on device
- **Accessibility**: WCAG 2.2 Level AA compliance required for all UI components

## Core Development Patterns

### SwiftData Models

```swift
@Model
class Receipt {
    let id: String
    let storeName: String
    let scanDate: Date
    var items: [ReceiptItem]
    let totalAmount: Double

    init(id: String = UUID().uuidString, storeName: String, scanDate: Date, items: [ReceiptItem], totalAmount: Double) {
        self.id = id
        self.storeName = storeName
        self.scanDate = scanDate
        self.items = items
        self.totalAmount = totalAmount
    }
}

@Model
class ReceiptItem {
    let name: String
    let quantity: Int
    let unitPrice: Double
    let totalPrice: Double

    init(name: String, quantity: Int = 1, unitPrice: Double, totalPrice: Double) {
        self.name = name
        self.quantity = quantity
        self.unitPrice = unitPrice
        self.totalPrice = totalPrice
    }
}
```

### Observable ViewModels with Async/Await

```swift
@MainActor
@Observable
class ReceiptScannerViewModel {
    private let ocrService: OCRService
    private let dataManager: DataManager

    var scanState: ScanState = .idle
    var receipts: [Receipt] = []

    init(ocrService: OCRService, dataManager: DataManager) {
        self.ocrService = ocrService
        self.dataManager = dataManager
    }

    func processReceipt(from imageData: Data) async {
        scanState = .processing
        do {
            let receipt = try await ocrService.processReceiptImage(imageData)
            try await dataManager.saveReceipt(receipt)
            await loadReceipts()
            scanState = .success(receipt)
        } catch {
            scanState = .error(error.localizedDescription)
        }
    }

    private func loadReceipts() async {
        do {
            receipts = try await dataManager.fetchAllReceipts()
        } catch {
            print("Failed to load receipts: \(error)")
        }
    }
}

enum ScanState {
    case idle
    case processing
    case success(Receipt)
    case error(String)
}
```

### SwiftUI Views with Async ViewModels

```swift
struct ReceiptScannerView: View {
    @State private var viewModel: ReceiptScannerViewModel

    init(dataManager: DataManager) {
        let ocrService = OCRService()
        _viewModel = State(wrappedValue: ReceiptScannerViewModel(
            ocrService: ocrService,
            dataManager: dataManager
        ))
    }

    var body: some View {
        NavigationStack {
            VStack {
                switch viewModel.scanState {
                case .idle:
                    CameraView { imageData in
                        Task {
                            await viewModel.processReceipt(from: imageData)
                        }
                    }
                case .processing:
                    ProgressView("Kassenbon wird verarbeitet...")
                case .success(let receipt):
                    ReceiptResultView(receipt: receipt)
                case .error(let message):
                    ErrorView(message: message) {
                        Task {
                            await viewModel.resetScanState()
                        }
                    }
                }
            }
            .navigationTitle("Kassenbon scannen")
            .task {
                await viewModel.loadInitialData()
            }
        }
    }
}
```

## Development Workflows

### MANDATORY: Use Fastlane Commands Only

**CRITICAL RULE - NEVER USE xcodebuild DIRECTLY:**

- ALWAYS use fastlane commands for all build, test, and deployment operations
- fastlane provides consistent, reliable automation with proper error handling
- Direct xcodebuild usage is prohibited to ensure consistent CI/CD workflows

### Fastlane Commands (MANDATORY)

```bash
# Build all targets (ALWAYS USE THIS)
bundle exec fastlane build

# Run all tests (ALWAYS USE THIS)
bundle exec fastlane test

# Run unit tests only
bundle exec fastlane unit_tests

# Run UI tests only
bundle exec fastlane ui_tests

# Run tests on specific simulator
bundle exec fastlane test device:"iPhone 17 Pro"

# Build and archive for App Store
bundle exec fastlane archive

# Deploy to App Store Connect
bundle exec fastlane deploy_to_app_store
```

### Development Setup Commands

```bash
# Install fastlane dependencies
bundle install

# Open iOS project (for debugging only)
open "Alles Teurer.xcodeproj"

# Check fastlane configuration
bundle exec fastlane --help
```

### NEVER USE - Prohibited Commands

```bash
# ❌ NEVER USE - Use 'bundle exec fastlane build' instead
xcodebuild build -scheme "Alles Teurer" -destination "platform=iOS Simulator,name=iPhone 17 Pro"

# ❌ NEVER USE - Use 'bundle exec fastlane test' instead
xcodebuild test -scheme "Alles Teurer" -destination "platform=iOS Simulator,name=iPhone 17 Pro"

# ❌ NEVER USE - Use 'bundle exec fastlane archive' instead
xcodebuild archive -scheme "Alles Teurer" -archivePath "AllesTeurer.xcarchive"
```

### Feature Development Approach

1. **Follow spec-driven workflow**: Reference `/spec/` directory for requirements and architecture
2. **EARS notation**: Requirements written as "WHEN [condition], THE SYSTEM SHALL [behavior]"
3. **Privacy validation**: Ensure no data leaves device except optional backend sync
4. **Accessibility first**: Use semantic markup and proper accessibility support in SwiftUI

## Technology-Specific Conventions

### OCR Integration (iOS Vision Framework)

- Preprocess images for optimal OCR (resize to 1024x1024, enhance contrast)
- Handle German text recognition specifically
- Implement user correction flows for OCR errors
- Use async/await for all Vision operations

### Swift Charts Implementation

- Create interactive price trend visualizations

```

### Feature Development Approach

1. **Follow spec-driven workflow**: Reference `/spec/` directory for requirements and architecture
2. **EARS notation**: Requirements written as "WHEN [condition], THE SYSTEM SHALL [behavior]"
3. **Privacy validation**: Ensure no data leaves device except optional backend sync
4. **Accessibility first**: Use semantic markup and proper accessibility support in SwiftUI

## Technology-Specific Conventions

### OCR Integration (iOS Vision Framework)

- Preprocess images for optimal OCR (resize to 1024x1024, enhance contrast)
- Handle German text recognition specifically
- Implement user correction flows for OCR errors
- Use async/await for all Vision operations

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
```

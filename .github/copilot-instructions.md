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

### MVVM + Repository Pattern

- **UI**: SwiftUI with Observable ViewModels and data binding
- **ViewModels**: ObservableObject classes handling UI state and business logic
- **Repositories**: Data access abstraction with SwiftData integration
- **Use Cases**: Domain logic encapsulation for complex operations
- **Local-First**: SwiftData for type-safe, native iOS data persistence

### iOS Code Organization

```
Alles Teurer/
├── Models/                 # SwiftData models and entities
├── ViewModels/            # Observable ViewModels for business logic
├── Views/                 # SwiftUI views and screens
│   ├── Scanner/           # Receipt scanning interface
│   ├── Products/          # Product list and details
│   ├── Analytics/         # Price charts and insights
│   └── Settings/          # App configuration
├── Services/              # Business logic and data processing
│   ├── OCRService.swift   # Vision Framework integration
│   ├── DataManager.swift  # Swift Data operations
│   ├── ProductMatcher.swift # Local fuzzy matching algorithms
│   └── PriceAnalyzer.swift # Local analytics calculations
├── Views/                 # SwiftUI views organized by feature
│   ├── Scanner/           # Receipt scanning interface
│   ├── Products/          # Product list and details
│   ├── Analytics/         # Price charts and insights
│   └── Settings/          # App configuration
├── Coordinators/          # Navigation flow management
└── Utils/                 # Extensions and helper functions
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

### Observable ViewModels

```swift
@MainActor
@Observable
class ReceiptScannerViewModel {
    private let ocrService: OCRService
    private let receiptRepository: ReceiptRepository

    var scanState: ScanState = .idle

    init(ocrService: OCRService, receiptRepository: ReceiptRepository) {
        self.ocrService = ocrService
        self.receiptRepository = receiptRepository
    }

    func processReceipt(from imageData: Data) async {
        scanState = .processing
        do {
            let receipt = try await ocrService.processReceiptImage(imageData)
            try await receiptRepository.save(receipt)
            scanState = .success(receipt)
        } catch {
            scanState = .error(error.localizedDescription)
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

### SwiftUI Views

```swift
struct ReceiptScannerView: View {
    @State private var viewModel = ReceiptScannerViewModel(
        ocrService: OCRService(),
        receiptRepository: ReceiptRepository()
    )

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
                    ProgressView("Processing receipt...")
                case .success(let receipt):
                    ReceiptResultView(receipt: receipt)
                case .error(let message):
                    ErrorView(message: message)
                }
            }
            .navigationTitle("Scan Receipt")
        }
    }
}
```

## Development Workflows

### Xcode Commands

```bash
# Build all targets
xcodebuild build -scheme "Alles Teurer" -destination "platform=iOS Simulator,name=iPhone 15"

# Run tests across all platforms
xcodebuild test -scheme "Alles Teurer" -destination "platform=iOS Simulator,name=iPhone 15"

# Archive for distribution
xcodebuild archive -scheme "Alles Teurer" -archivePath "AllesTeurer.xcarchive"
```

### Platform-Specific Commands

```bash
# Open iOS project (for debugging)
open "Alles Teurer.xcodeproj"

# Run on specific simulators
xcodebuild test -scheme "Alles Teurer" -destination "platform=iOS Simulator,name=iPhone 15 Pro"
xcodebuild test -scheme "Alles Teurer" -destination "platform=iOS Simulator,name=iPad Pro (12.9-inch)"

# Run on device
xcodebuild test -scheme "Alles Teurer" -destination "platform=iOS,id=[device-id]"

# Run on specific simulators
xcodebuild test -scheme "Alles Teurer" -destination "platform=iOS Simulator,name=iPhone 15 Pro"
xcodebuild test -scheme "Alles Teurer" -destination "platform=iOS Simulator,name=iPad Pro (12.9-inch)"

# Run on device
xcodebuild test -scheme "Alles Teurer" -destination "platform=iOS,id=[device-id]"
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

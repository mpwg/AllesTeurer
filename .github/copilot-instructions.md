# AllesTeurer - AI Agent Instructions

## Project Overview

AllesTeurer is a privacy-first iOS app that tracks product price inflation through receipt scanning and local analytics. The app uses Apple's Vision Framework for OCR, Swift Data for persistence, and maintains strict on-device processing.

## Architecture & Key Patterns

### MVVM + Privacy-First Design

- **Views**: SwiftUI views with declarative UI
- **ViewModels**: `@Observable` classes (NOT `ObservableObject`) handling business logic
- **Services**: Abstracted services for OCR, data management, and analytics
- **Local-First**: All processing happens on-device, optional CloudKit sync only

### Critical Technical Constraints

- **Swift Data ONLY**: Never use Core Data - this project exclusively uses Swift Data with `@Model` macro
- **iOS 17+ Target**: Leverage iOS 17+ features including enhanced Vision Framework
- **Privacy-First**: No external API calls for core functionality, all data stays on device
- **Accessibility**: WCAG 2.2 Level AA compliance required for all UI components

## Essential File Structure

```
AllesTeurer.xcodeproj/
├── Models/              # Swift Data models with @Model macro
├── ViewModels/          # @Observable classes for MVVM pattern
├── Services/            # Business logic abstraction layer
│   ├── OCRService.swift      # Vision Framework receipt processing
│   ├── DataManager.swift     # Swift Data operations
│   ├── ProductMatcher.swift  # Local fuzzy matching algorithms
│   └── PriceAnalyzer.swift   # Local analytics calculations
├── Views/               # SwiftUI views organized by feature
│   ├── Scanner/         # Receipt scanning interface
│   ├── Products/        # Product list and details
│   ├── Analytics/       # Price charts and insights
│   └── Settings/        # App configuration
└── Utils/               # Extensions and helper functions
```

## Core Development Patterns

### Data Models (Swift Data)

```swift
@Model
class Receipt {
    var storeName: String
    var scanDate: Date
    var items: [ReceiptItem] = []

    init(storeName: String, scanDate: Date = Date()) {
        self.storeName = storeName
        self.scanDate = scanDate
    }
}
```

### ViewModels (Observable)

```swift
@Observable
class ReceiptScannerViewModel {
    var isScanning = false
    var extractedText = ""

    func processReceipt(_ image: UIImage) async {
        // OCR processing using Vision Framework
    }
}
```

### SwiftUI Views with Swift Data

```swift
struct ProductListView: View {
    @Query private var products: [Product]
    @Environment(\.modelContext) private var modelContext

    var body: some View {
        // UI implementation
    }
}
```

## Development Workflows

### Build & Test Commands

```bash
# Open project
open AllesTeurer.xcodeproj

# Run tests
xcodebuild test -scheme AllesTeurer -destination 'platform=iOS Simulator,name=iPhone 15'

# Format code (if SwiftFormat configured)
swiftformat .
```

### Feature Development Approach

1. **Follow spec-driven workflow**: Reference `/spec/` directory for requirements and architecture
2. **EARS notation**: Requirements written as "WHEN [condition], THE SYSTEM SHALL [behavior]"
3. **Privacy validation**: Ensure no data leaves device except optional CloudKit sync
4. **Accessibility first**: Use semantic markup, VoiceOver labels, and Dynamic Type support

## Technology-Specific Conventions

### Vision Framework Integration

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

## Critical "Don'ts"

- **Never use Core Data** - Swift Data only
- **No external analytics** - All insights calculated locally
- **No third-party OCR** - Apple Vision Framework exclusively
- **No cloud processing** - Maintain privacy-first architecture
- **Don't break accessibility** - Every UI element must be accessible

## Testing Approach

- **UI Testing**: Verify receipt scanning flow and data display accuracy
- **Unit Testing**: Test OCR processing, product matching, and price analytics
- **Accessibility Testing**: Validate VoiceOver and Dynamic Type support
- **Privacy Testing**: Ensure no network calls in core functionality

## References

- `/spec/Anforderungen.md` - Functional requirements in German
- `/spec/architecture.md` - Technical architecture decisions
- `/.github/instructions/swift.instructions.md` - Swift coding conventions
- `/.github/instructions/a11y.instructions.md` - Accessibility requirements
- `/.github/instructions/swiftui-observation.instructions.md` - SwiftUI Observable patterns

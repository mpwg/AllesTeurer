# AllesTeurer - iOS Testing Strategy

## Overview

This document outlines the testing strategy for AllesTeurer, a native iOS application built with SwiftUI, SwiftData, and Apple's Vision Framework for receipt scanning and price tracking.

## Testing Framework Overview

### Primary Testing Framework: Swift Testing

**iOS Testing Stack**:

- **Swift Testing**: Primary framework for new tests (iOS 17+)
- **XCTest**: Legacy framework for compatibility
- **XCUITest**: UI and integration testing
- **SwiftData Testing**: Database layer testing
- **Vision Framework Testing**: OCR functionality testing

**Rationale**: Swift Testing provides modern testing capabilities for Swift applications, with excellent integration with SwiftUI, SwiftData, and iOS frameworks. For legacy compatibility, XCTest is also supported.

## Test Structure

### Unit Testing with Swift Testing

```swift
import Testing
import SwiftData
@testable import Alles_Teurer

@Suite("OCR Service Tests")
struct OCRServiceTests {
    @Test("Vision Framework recognizes German text")
    func testGermanTextRecognition() async throws {
        let ocrService = OCRService()
        let testImage = createGermanReceiptImage()
        
        let result = try await ocrService.recognizeText(from: testImage)
        
        #expect(!result.recognizedText.isEmpty)
        #expect(result.confidence > 0.8)
    }
    
    @Test("OCR handles invalid image data")
    func testInvalidImageHandling() async throws {
        let ocrService = OCRService()
        let invalidImageData = Data()
        
        await #expect(throws: OCRError.invalidImage) {
            try await ocrService.recognizeText(from: invalidImageData)
        }
    }
}

@Suite("SwiftData Repository Tests")
struct ReceiptRepositoryTests {
    @Test("Save and retrieve receipt")
    func testReceiptPersistence() async throws {
        let container = ModelContainer.inMemoryContainer()
        let repository = ReceiptRepository(modelContainer: container)
        
        let receipt = Receipt(
            storeName: "REWE",
            scanDate: Date(),
            items: [ReceiptItem(name: "Milch", unitPrice: 1.29, totalPrice: 1.29)],
            totalAmount: 1.29
        )
        
        try await repository.save(receipt)
        let savedReceipts = try await repository.fetchAll()
        
        #expect(savedReceipts.count == 1)
        #expect(savedReceipts.first?.storeName == "REWE")
    }
}
```

### UI Testing with XCUITest

```swift
import XCTest

final class ReceiptScannerUITests: XCTestCase {
    var app: XCUIApplication!
    
    override func setUp() {
        super.setUp()
        app = XCUIApplication()
        app.launch()
    }
    
    func testReceiptScanningFlow() {
        // Test camera interface accessibility
        let cameraButton = app.buttons["Scan Receipt"]
        XCTAssertTrue(cameraButton.exists)
        XCTAssertTrue(cameraButton.isEnabled)
        
        // Test accessibility labels
        XCTAssertEqual(cameraButton.label, "Scan Receipt")
        XCTAssertTrue(cameraButton.isAccessibilityElement)
    }
}
```

## Testing Areas

### Core Functionality Testing

#### Receipt Scanning (Vision Framework)
- Text recognition accuracy for German receipts
- Image preprocessing validation
- Error handling for invalid images
- Performance benchmarks for OCR processing

#### Data Persistence (SwiftData)
- Receipt CRUD operations
- Data model relationships
- Migration testing
- Query performance

#### Business Logic
- Price calculation accuracy
- Product matching algorithms
- Analytics computations
- Currency formatting

### UI Testing

#### SwiftUI Views
- View state management
- Data binding validation
- Navigation flows
- Accessibility compliance

#### User Interactions
- Receipt scanning workflow
- Product list management
- Settings configuration
- Error state handling

### Performance Testing

#### OCR Performance
```swift
@Test("OCR processing performance")
func testOCRPerformance() async throws {
    let ocrService = OCRService()
    let testImage = createLargeReceiptImage()
    
    let startTime = CFAbsoluteTimeGetCurrent()
    _ = try await ocrService.recognizeText(from: testImage)
    let timeElapsed = CFAbsoluteTimeGetCurrent() - startTime
    
    #expect(timeElapsed < 5.0) // OCR should complete within 5 seconds
}
```

#### Database Performance
- Large dataset queries
- Batch operations
- Memory usage monitoring

### Accessibility Testing

#### WCAG 2.2 Compliance
- VoiceOver navigation
- Dynamic Type support
- Color contrast validation
- Keyboard navigation

## Test Organization

### Directory Structure
```
Alles TeurerTests/
├── Unit/
│   ├── Services/
│   │   ├── OCRServiceTests.swift
│   │   └── DataManagerTests.swift
│   ├── ViewModels/
│   │   └── ReceiptScannerViewModelTests.swift
│   └── Models/
│       └── ReceiptModelTests.swift
├── Integration/
│   ├── ReceiptFlowTests.swift
│   └── DataPersistenceTests.swift
└── Performance/
    └── OCRPerformanceTests.swift

Alles TeurerUITests/
├── ReceiptScannerUITests.swift
├── ProductListUITests.swift
└── AccessibilityUITests.swift
```

### Test Data Management

#### Mock Data Creation
```swift
extension Receipt {
    static func mockGermanReceipt() -> Receipt {
        Receipt(
            storeName: "EDEKA",
            scanDate: Date(),
            items: [
                ReceiptItem(name: "Vollmilch 3,5%", unitPrice: 1.19, totalPrice: 1.19),
                ReceiptItem(name: "Schwarzbrot", unitPrice: 2.49, totalPrice: 2.49)
            ],
            totalAmount: 3.68
        )
    }
}
```

#### Test Image Assets
- Sample German receipts for OCR testing
- Various image qualities and formats
- Edge cases (blurry, rotated, damaged receipts)

## Continuous Integration

### Xcode Cloud Integration
- Automated test runs on commits
- Performance regression detection
- Accessibility validation
- Device matrix testing (iPhone, iPad)

### Test Metrics
- Code coverage targets (>80%)
- Test execution time monitoring
- Flaky test detection and remediation

## Privacy Testing

### On-Device Processing Validation
- Network activity monitoring during OCR
- Data encryption verification
- Local storage security testing

## Platform-Specific Considerations

### iOS Version Compatibility
- iOS 17.0+ for Swift Testing
- Backward compatibility testing
- Feature availability validation

### Device Testing
- iPhone form factors (various sizes)
- iPad compatibility
- Camera hardware variations
- Performance across device generations

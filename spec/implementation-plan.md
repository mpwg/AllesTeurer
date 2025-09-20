# AllesTeurer - iOS-First Implementation Plan

## Executive Summary

This document provides a detailed implementation plan for AllesTeurer, beginning with a privacy-focused iOS application that enables users to track product prices through receipt scanning and local price monitoring. The app starts as an iOS-only solution using local Core Data storage and Apple's Vision Framework, with a clear evolution path to multi-platform support and cloud services.

## 1. Project Architecture Overview

### 1.1 iOS-First Technology Stack

**iOS Application (Phase 1):**

- **Language:** Swift 6.0+
- **UI Framework:** SwiftUI
- **Architecture:** MVVM with async/await
- **Local Storage:** Core Data with CloudKit sync
- **AI/ML:** Vision Framework + Apple Intelligence
- **Camera:** AVFoundation for receipt scanning
- **Charts:** Swift Charts for data visualization
- **Deployment:** Xcode Cloud + App Store Connect

**Future Expansion (Phase 2+):**

- **Backend Services:** Node.js + GraphQL (when needed)
- **Database:** PostgreSQL for cloud sync
- **Android App:** Kotlin + Jetpack Compose
- **Web App:** Next.js + React

### 1.2 iOS Project Structure

```
AllesTeurer.xcodeproj
├── AllesTeurer/
│   ├── App/
│   │   ├── AllesTeurerApp.swift
│   │   └── ContentView.swift
│   ├── Models/
│   │   ├── CoreData/
│   │   │   ├── AllesTeurer.xcdatamodeld
│   │   │   ├── Receipt+CoreDataClass.swift
│   │   │   ├── Product+CoreDataClass.swift
│   │   │   ├── PriceRecord+CoreDataClass.swift
│   │   │   └── Store+CoreDataClass.swift
│   │   └── ViewModels/
│   │       ├── ReceiptScannerViewModel.swift
│   │       ├── PriceHistoryViewModel.swift
│   │       ├── ProductDetailViewModel.swift
│   │       └── AnalyticsViewModel.swift
│   ├── Views/
│   │   ├── Scanner/
│   │   │   ├── CameraView.swift
│   │   │   ├── ReceiptScannerView.swift
│   │   │   └── ScanResultView.swift
│   │   ├── Products/
│   │   │   ├── ProductListView.swift
│   │   │   ├── ProductDetailView.swift
│   │   │   └── PriceHistoryView.swift
│   │   ├── Analytics/
│   │   │   ├── DashboardView.swift
│   │   │   ├── SpendingTrendsView.swift
│   │   │   └── InflationTrackerView.swift
│   │   └── Settings/
│   │       ├── SettingsView.swift
│   │       └── DataExportView.swift
│   ├── Services/
│   │   ├── DataManager.swift         // Core Data operations
│   │   ├── OCRService.swift          // Vision Framework
│   │   ├── ReceiptParser.swift       // Text processing
│   │   ├── ProductMatcher.swift      // Local matching
│   │   ├── PriceAnalyzer.swift       // Local analytics
│   │   └── CloudKitService.swift     // Device sync
│   └── Utils/
│       ├── Extensions/
│       │   ├── String+Extensions.swift
│       │   └── Date+Extensions.swift
│       └── Helpers/
│           ├── CameraPermissions.swift
│           └── NumberFormatter.swift
├── AllesTeurerTests/
└── AllesTeurerUITests/
```

## 2. Implementation Phases

### Phase 1: iOS Foundation & Core Data Setup (Weeks 1-2)

#### 2.1 iOS Project Setup

**Timeline:** Week 1  
**Effort:** 3 days  
**Dependencies:** None

**Tasks:**

1. **Initialize iOS Xcode project**

   ```swift
   // AllesTeurer/AllesTeurerApp.swift
   import SwiftUI
   import CoreData

   @main
   struct AllesTeurerApp: App {
       let persistenceController = PersistenceController.shared

       var body: some Scene {
           WindowGroup {
               ContentView()
                   .environment(\.managedObjectContext, persistenceController.container.viewContext)
           }
       }
   }
   ```

2. **Configure project structure**

   - Set iOS 17.0+ minimum deployment target
   - Create folder structure for Models, Views, Services
   - Configure SwiftUI + Core Data integration
   - Set up basic navigation structure

3. **Development environment setup**
   - Configure SwiftLint for code quality
   - Set up unit testing target
   - Configure Xcode Cloud for CI/CD
   - Set up App Store Connect integration

#### 2.2 Core Data Model Setup

**Timeline:** Week 2  
**Effort:** 4 days  
**Dependencies:** iOS project setup

**Tasks:**

1. **Design Core Data model**

   ```swift
   // Receipt Entity
   @objc(Receipt)
   public class Receipt: NSManagedObject {
       @NSManaged public var id: UUID
       @NSManaged public var storeName: String?
       @NSManaged public var totalAmount: Decimal
       @NSManaged public var purchaseDate: Date
       @NSManaged public var imageData: Data?
       @NSManaged public var ocrText: String?
       @NSManaged public var isProcessed: Bool
       @NSManaged public var items: NSSet?
   }

   // Product Entity
   @objc(Product)
   public class Product: NSManagedObject {
       @NSManaged public var id: UUID
       @NSManaged public var name: String
       @NSManaged public var category: String?
       @NSManaged public var brand: String?
       @NSManaged public var priceRecords: NSSet?
   }

   // PriceRecord Entity
   @objc(PriceRecord)
   public class PriceRecord: NSManagedObject {
       @NSManaged public var id: UUID
       @NSManaged public var price: Decimal
       @NSManaged public var quantity: Decimal
       @NSManaged public var recordedDate: Date
       @NSManaged public var storeName: String
       @NSManaged public var product: Product?
       @NSManaged public var receipt: Receipt?
   }
   ```

2. **Core Data service layer**
   ```swift
   // Services/DataManager.swift
   class DataManager: ObservableObject {
       static let shared = DataManager()

       lazy var persistentContainer: NSPersistentContainer = {
           let container = NSPersistentContainer(name: "AllesTeurer")
           container.loadPersistentStores { _, error in
               if let error = error {
                   fatalError("Core Data error: \(error)")
               }
           }
           return container
       }()

       func save() {
           let context = persistentContainer.viewContext
           if context.hasChanges {
               try? context.save()
           }
       }

       func saveReceipt(_ receipt: Receipt) {
           save()
       }

       func fetchProducts() -> [Product] {
           let request: NSFetchRequest<Product> = Product.fetchRequest()
           return (try? persistentContainer.viewContext.fetch(request)) ?? []
       }
   }
   ```

### Phase 2: Receipt Scanning & OCR (Weeks 3-5)

#### 2.3 Camera Integration

**Timeline:** Week 3  
**Effort:** 4 days  
**Dependencies:** Core Data setup

**Tasks:**

1. **Camera permissions and setup**

   ```swift
   // Services/CameraService.swift
   import AVFoundation
   import SwiftUI

   class CameraService: NSObject, ObservableObject {
       @Published var isCameraAuthorized = false

       func requestCameraPermission() {
           AVCaptureDevice.requestAccess(for: .video) { granted in
               DispatchQueue.main.async {
                   self.isCameraAuthorized = granted
               }
           }
       }
   }
   ```

2. **Receipt capture view**
   ```swift
   // Views/Scanner/CameraView.swift
   struct CameraView: UIViewControllerRepresentable {
       @Binding var capturedImage: UIImage?
       @Environment(\.dismiss) var dismiss

       func makeUIViewController(context: Context) -> UIImagePickerController {
           let picker = UIImagePickerController()
           picker.delegate = context.coordinator
           picker.sourceType = .camera
           return picker
       }

       func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}

       func makeCoordinator() -> Coordinator {
           Coordinator(self)
       }

       class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
           let parent: CameraView

           init(_ parent: CameraView) {
               self.parent = parent
           }

           func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
               if let image = info[.originalImage] as? UIImage {
                   parent.capturedImage = image
               }
               parent.dismiss()
           }
       }
   }
   ```

#### 2.4 Vision Framework OCR

**Timeline:** Week 4-5  
**Effort:** 6 days  
**Dependencies:** Camera integration

**Tasks:**

1. **OCR service implementation**

   ```swift
   // Services/OCRService.swift
   import Vision
   import UIKit

   class OCRService: ObservableObject {
       @Published var recognizedText = ""
       @Published var isProcessing = false

       func processReceiptImage(_ image: UIImage) async -> ReceiptData {
           isProcessing = true
           defer { isProcessing = false }

           guard let cgImage = image.cgImage else {
               return ReceiptData(text: "", items: [])
           }

           let requestHandler = VNImageRequestHandler(cgImage: cgImage)
           let request = VNRecognizeTextRequest { [weak self] request, error in
               guard let observations = request.results as? [VNRecognizedTextObservation] else { return }

               let recognizedStrings = observations.compactMap { observation in
                   observation.topCandidates(1).first?.string
               }

               DispatchQueue.main.async {
                   self?.recognizedText = recognizedStrings.joined(separator: "\n")
               }
           }

           request.recognitionLevel = .accurate
           request.usesLanguageCorrection = true

           do {
               try requestHandler.perform([request])
               return parseReceiptText(recognizedText)
           } catch {
               print("OCR Error: \(error)")
               return ReceiptData(text: recognizedText, items: [])
           }
       }

       private func parseReceiptText(_ text: String) -> ReceiptData {
           // Parse receipt text to extract items, prices, store info
           let lines = text.components(separatedBy: .newlines)
           var items: [ReceiptItem] = []
           var storeName: String?
           var totalAmount: Decimal?

           for line in lines {
               if let item = parseReceiptLine(line) {
                   items.append(item)
               }
               if storeName == nil {
                   storeName = detectStoreName(line)
               }
               if totalAmount == nil {
                   totalAmount = detectTotalAmount(line)
               }
           }

           return ReceiptData(
               text: text,
               items: items,
               storeName: storeName,
               totalAmount: totalAmount
           )
       }

       private func parseReceiptLine(_ line: String) -> ReceiptItem? {
           // Implement receipt line parsing logic
           // Look for patterns like "Product Name 2.99" or "2x Product 5.98"
           return nil // Placeholder
       }
   }

   struct ReceiptData {
       let text: String
       let items: [ReceiptItem]
       let storeName: String?
       let totalAmount: Decimal?
   }

   struct ReceiptItem {
       let name: String
       let price: Decimal
       let quantity: Decimal
   }
   ```

2. **Receipt parsing algorithms**
   - Implement German receipt format detection
   - Extract product names, prices, quantities
   - Handle different store formats (REWE, Edeka, etc.)
   - Validate extracted data

### Phase 3: Local Product Management (Weeks 6-7)

#### 2.5 Product Matching & Storage

**Timeline:** Week 6-7  
**Effort:** 6 days  
**Dependencies:** OCR implementation

**Tasks:**

1. **Product matching service**

   ```swift
   // Services/ProductMatcher.swift
   import Foundation

   class ProductMatcher: ObservableObject {
       private let dataManager = DataManager.shared

       func findOrCreateProduct(name: String, category: String? = nil) async -> Product {
           // First, try to find existing product with fuzzy matching
           if let existingProduct = await findSimilarProduct(name: name) {
               return existingProduct
           }

           // Create new product if no match found
           return createNewProduct(name: name, category: category)
       }

       private func findSimilarProduct(name: String) async -> Product? {
           let products = dataManager.fetchProducts()

           // Use string similarity algorithms
           for product in products {
               if similarity(name, product.name) > 0.8 {
                   return product
               }
           }

           return nil
       }

       private func similarity(_ s1: String, _ s2: String) -> Double {
           // Implement Levenshtein distance or similar algorithm
           return 0.0 // Placeholder
       }

       private func createNewProduct(name: String, category: String?) -> Product {
           let context = dataManager.persistentContainer.viewContext
           let product = Product(context: context)
           product.id = UUID()
           product.name = name
           product.category = category
           dataManager.save()
           return product
       }
   }
   ```

2. **Product categorization**
   - Implement automatic categorization based on keywords
   - Use Apple's Natural Language framework
   - Create predefined categories (Food, Electronics, etc.)
   - Allow manual category assignment

### Phase 4: Analytics & Visualization (Weeks 8-9)

#### 2.6 Price History & Analytics

**Timeline:** Week 8-9  
**Effort:** 6 days  
**Dependencies:** Product management

**Tasks:**

1. **Analytics service**

   ```swift
   // Services/PriceAnalyzer.swift
   import Foundation
   import CoreData

   class PriceAnalyzer: ObservableObject {
       private let dataManager = DataManager.shared

       func calculateInflationRate(for category: String, period: DateInterval) -> Double {
           let priceRecords = fetchPriceRecords(category: category, period: period)

           guard priceRecords.count > 1 else { return 0.0 }

           let sortedRecords = priceRecords.sorted { $0.recordedDate < $1.recordedDate }
           let startPrice = sortedRecords.first!.price
           let endPrice = sortedRecords.last!.price

           return Double(truncating: ((endPrice - startPrice) / startPrice * 100))
       }

       func getSpendingTrends() -> [SpendingTrend] {
           let receipts = fetchRecentReceipts()
           var trends: [String: Decimal] = [:]

           for receipt in receipts {
               let monthKey = DateFormatter.monthYear.string(from: receipt.purchaseDate)
               trends[monthKey, default: 0] += receipt.totalAmount
           }

           return trends.map { SpendingTrend(period: $0.key, amount: $0.value) }
       }

       func getPriceHistory(for product: Product) -> [PriceRecord] {
           guard let priceRecords = product.priceRecords as? Set<PriceRecord> else { return [] }
           return Array(priceRecords).sorted { $0.recordedDate < $1.recordedDate }
       }
   }

   struct SpendingTrend {
       let period: String
       let amount: Decimal
   }
   ```

2. **Chart views with Swift Charts**

   ```swift
   // Views/Analytics/PriceHistoryView.swift
   import SwiftUI
   import Charts

   struct PriceHistoryView: View {
       let product: Product
       @StateObject private var analyzer = PriceAnalyzer()

       var body: some View {
           VStack {
               Text(product.name)
                   .font(.headline)

               Chart {
                   ForEach(analyzer.getPriceHistory(for: product), id: \.id) { record in
                       LineMark(
                           x: .value("Date", record.recordedDate),
                           y: .value("Price", Double(truncating: record.price))
                       )
                   }
               }
               .frame(height: 200)
           }
           .navigationTitle("Price History")
       }
   }
   ```

### Phase 5: User Interface & Experience (Weeks 10-11)

#### 2.7 Main App Interface

**Timeline:** Week 10-11  
**Effort:** 6 days  
**Dependencies:** Analytics implementation

**Tasks:**

1. **Main navigation structure**

   ```swift
   // ContentView.swift
   struct ContentView: View {
       var body: some View {
           TabView {
               DashboardView()
                   .tabItem {
                       Image(systemName: "house.fill")
                       Text("Dashboard")
                   }

               ReceiptScannerView()
                   .tabItem {
                       Image(systemName: "camera.fill")
                       Text("Scan")
                   }

               ProductListView()
                   .tabItem {
                       Image(systemName: "list.bullet")
                       Text("Products")
                   }

               AnalyticsView()
                   .tabItem {
                       Image(systemName: "chart.bar.fill")
                       Text("Analytics")
                   }

               SettingsView()
                   .tabItem {
                       Image(systemName: "gear")
                       Text("Settings")
                   }
           }
       }
   }
   ```

2. **Dashboard implementation**
   ```swift
   // Views/Analytics/DashboardView.swift
   struct DashboardView: View {
       @StateObject private var analyzer = PriceAnalyzer()

       var body: some View {
           NavigationView {
               ScrollView {
                   LazyVStack(spacing: 16) {
                       SpendingSummaryCard()
                       RecentReceiptsCard()
                       InflationTrackerCard()
                       TopCategoriesCard()
                   }
                   .padding()
               }
               .navigationTitle("AllesTeurer")
               .refreshable {
                   // Refresh data
               }
           }
       }
   }
   ```

### Phase 6: CloudKit Sync & Polish (Weeks 12)

#### 2.8 CloudKit Integration

**Timeline:** Week 12  
**Effort:** 4 days  
**Dependencies:** Main UI complete

**Tasks:**

1. **CloudKit sync setup**

   ```swift
   // Services/CloudKitService.swift
   import CloudKit
   import CoreData

   class CloudKitService: ObservableObject {
       private let container = CKContainer(identifier: "iCloud.com.allesteuer.app")

       func setupCloudKit() {
           // Configure CloudKit container
           // Set up sync with Core Data
         }

       func syncData() async {
           // Implement bidirectional sync
           // Handle conflicts
       }
   }
   ```

2. **Data export functionality**
   - Export receipts as CSV
   - Export analytics as PDF reports
   - Backup/restore functionality

## 3. Core Data Model Specifications

### 3.1 iOS Core Data Schema

```swift
// AllesTeurer.xcdatamodeld

// Receipt Entity
entity Receipt {
    id: UUID
    storeName: String?
    totalAmount: Decimal
    purchaseDate: Date
    imageData: Data?
    ocrText: String?
    isProcessed: Bool
    createdAt: Date
    updatedAt: Date

    // Relationships
    items: [ReceiptItem] (One-to-Many)
}

// Product Entity
entity Product {
    id: UUID
    name: String
    category: String?
    brand: String?
    normalizedName: String  // For matching
    createdAt: Date
    updatedAt: Date

    // Relationships
    priceRecords: [PriceRecord] (One-to-Many)
}

// PriceRecord Entity
entity PriceRecord {
    id: UUID
    price: Decimal
    quantity: Decimal
    unitPrice: Decimal
    recordedDate: Date
    storeName: String
    createdAt: Date

    // Relationships
    product: Product (Many-to-One)
    receipt: Receipt (Many-to-One)
}

// ReceiptItem Entity
entity ReceiptItem {
    id: UUID
    itemName: String
    extractedPrice: Decimal
    extractedQuantity: Decimal?
    lineText: String  // Original OCR text
    isMatched: Bool
    createdAt: Date

    // Relationships
    receipt: Receipt (Many-to-One)
    product: Product? (Many-to-One, optional)
}

// Store Entity
entity Store {
    id: UUID
    name: String
    location: String?
    logoImage: Data?
    createdAt: Date

    // Relationships
    receipts: [Receipt] (One-to-Many)
    priceRecords: [PriceRecord] (One-to-Many)
}

// Category Entity
entity Category {
    id: UUID
    name: String
    iconName: String?
    colorHex: String?
    sortOrder: Int32

    // Relationships
    products: [Product] (One-to-Many)
}
```

### 3.2 CloudKit Integration Schema

```swift
// CloudKit Record Types (for device sync)

// CKRecord: Receipt
recordType: "Receipt"
fields: [
    "storeName": CKRecord.Reference,
    "totalAmount": NSNumber,
    "purchaseDate": Date,
    "imageAsset": CKAsset,
    "ocrText": String,
    "isProcessed": Bool
]

// CKRecord: Product
recordType: "Product"
fields: [
    "name": String,
    "category": CKRecord.Reference,
    "brand": String,
    "normalizedName": String
]

// CKRecord: PriceRecord
recordType: "PriceRecord"
fields: [
    "product": CKRecord.Reference,
    "receipt": CKRecord.Reference,
    "price": NSNumber,
    "quantity": NSNumber,
    "recordedDate": Date,
    "storeName": String
]
```

## 4. Advanced Features Implementation

### 4.1 Apple Intelligence Integration

```swift
// Services/IntelligenceService.swift
import NaturalLanguage

class IntelligenceService: ObservableObject {
    private let categorizer = NLModel(mlModel: try! CategoryClassifier(configuration: .init()).model)

    func categorizeProduct(_ productName: String) async -> String {
        let prediction = try? categorizer.prediction(from: productName)
        return prediction?.label ?? "Other"
    }

    func extractProductInfo(_ ocrText: String) async -> [ProductInfo] {
        let tagger = NLTagger(tagSchemes: [.nameType, .lexicalClass])
        tagger.string = ocrText

        var products: [ProductInfo] = []

        tagger.enumerateTagsInRange(ocrText.startIndex..<ocrText.endIndex,
                                   unit: .word,
                                   scheme: .nameType) { tag, range in
            // Extract product names and prices using NLP
            return true
        }

        return products
    }
}

struct ProductInfo {
    let name: String
    let price: Decimal?
    let quantity: String?
    let confidence: Double
}
```

### 4.2 Local Search & Matching

```swift
// Services/LocalSearchService.swift
import Foundation

class LocalSearchService: ObservableObject {
    private let dataManager = DataManager.shared

    func searchProducts(query: String) async -> [Product] {
        let products = dataManager.fetchProducts()
        return products.filter { product in
            product.name.localizedCaseInsensitiveContains(query) ||
            product.brand?.localizedCaseInsensitiveContains(query) == true ||
            product.category?.localizedCaseInsensitiveContains(query) == true
        }.sorted { lhs, rhs in
            // Sort by relevance score
            relevanceScore(query, lhs.name) > relevanceScore(query, rhs.name)
        }
    }

    private func relevanceScore(_ query: String, _ text: String) -> Double {
        // Implement string similarity algorithm
        let distance = levenshteinDistance(query.lowercased(), text.lowercased())
        return 1.0 - Double(distance) / Double(max(query.count, text.count))
    }

    private func levenshteinDistance(_ s1: String, _ s2: String) -> Int {
        // Implement Levenshtein distance algorithm
        let m = s1.count, n = s2.count
        var dp = Array(repeating: Array(repeating: 0, count: n + 1), count: m + 1)

        for i in 0...m { dp[i][0] = i }
        for j in 0...n { dp[0][j] = j }

        for i in 1...m {
            for j in 1...n {
                if Array(s1)[i-1] == Array(s2)[j-1] {
                    dp[i][j] = dp[i-1][j-1]
                } else {
                    dp[i][j] = 1 + min(dp[i-1][j], dp[i][j-1], dp[i-1][j-1])
                }
            }
        }

        return dp[m][n]
    }
}
```

## 5. Future Evolution Strategy

### Phase 7: Backend Integration Preparation (Future)

When ready to add backend services, the iOS app architecture supports easy integration:

#### 5.1 Network Layer Addition

```swift
// Services/NetworkService.swift (Future)
class NetworkService: ObservableObject {
    private let baseURL = URL(string: "https://api.allesteuer.com")!

    func syncWithBackend() async {
        // Sync local Core Data with backend
        await uploadPendingReceipts()
        await downloadLatestPriceData()
        await syncUserPreferences()
    }

    func uploadPendingReceipts() async {
        // Upload receipts that haven't been synced
    }

    func downloadLatestPriceData() async {
        // Download price comparison data from backend
    }
}
```

#### 5.2 Multi-Platform Expansion

- **Android App**: Kotlin + Room Database + Retrofit
- **Web App**: Next.js + React + IndexedDB
- **Backend**: Node.js + GraphQL + PostgreSQL

#### 5.3 Real-Time Price Monitoring

```swift
// Future: Real-time price tracking integration
class PriceMonitoringService {
    func startMonitoring(products: [Product]) async {
        // Connect to backend price monitoring service
        // Receive push notifications for price changes
    }

    func getExternalPrices(product: Product) async -> [ExternalPrice] {
        // Query external APIs (Amazon, eBay, etc.)
        return []
    }
}

struct ExternalPrice {
    let retailer: String
    let price: Decimal
    let url: String
    let availability: String
}
```

## 6. Testing Strategy

### 6.1 Unit Testing

```swift
// AllesTeurerTests/OCRServiceTests.swift
import XCTest
@testable import AllesTeurer

class OCRServiceTests: XCTestCase {
    var ocrService: OCRService!

    override func setUp() {
        super.setUp()
        ocrService = OCRService()
    }

    func testReceiptTextParsing() async {
        let sampleReceiptText = """
        REWE Markt
        Milch 1,5% 1,29
        Brot Vollkorn 2,49
        Äpfel 1kg 3,99
        SUMME 7,77
        """

        let result = await ocrService.parseReceiptText(sampleReceiptText)

        XCTAssertEqual(result.items.count, 3)
        XCTAssertEqual(result.totalAmount, Decimal(7.77))
        XCTAssertEqual(result.storeName, "REWE")
    }

    func testProductMatching() async {
        // Create test products in Core Data
        let context = DataManager.shared.persistentContainer.viewContext
        let milk = Product(context: context)
        milk.name = "Milch 1,5%"
        milk.category = "Dairy"

        try! context.save()

        // Test matching
        let matcher = ProductMatcher()
        let matched = await matcher.findSimilarProduct(name: "Milch 1,5%")

        XCTAssertNotNil(matched)
        XCTAssertEqual(matched?.name, "Milch 1,5%")
    }
}
```

### 6.2 UI Testing

```swift
// AllesTeurerUITests/ReceiptScanningTests.swift
import XCTest

class ReceiptScanningUITests: XCTestCase {
    var app: XCUIApplication!

    override func setUp() {
        super.setUp()
        app = XCUIApplication()
        app.launch()
    }

    func testReceiptScanningFlow() {
        // Navigate to scanner
        app.tabBars.buttons["Scan"].tap()

        // Test camera permission request
        if app.alerts.element.exists {
            app.alerts.buttons["OK"].tap()
        }

        // Test scan button exists
        XCTAssertTrue(app.buttons["Scan Receipt"].exists)

        // Test results view navigation
        app.buttons["Scan Receipt"].tap()
        // Add more UI tests...
    }
}
```

## 7. Performance & Optimization

### 7.1 Core Data Optimization

```swift
// Optimized Core Data fetching
extension DataManager {
    func fetchRecentReceipts(limit: Int = 20) -> [Receipt] {
        let request: NSFetchRequest<Receipt> = Receipt.fetchRequest()
        request.sortDescriptors = [NSSortDescriptor(key: "purchaseDate", ascending: false)]
        request.fetchLimit = limit
        request.relationshipKeyPathsForPrefetching = ["items", "items.product"]

        return (try? persistentContainer.viewContext.fetch(request)) ?? []
    }

    func fetchProductsWithPriceHistory() -> [Product] {
        let request: NSFetchRequest<Product> = Product.fetchRequest()
        request.relationshipKeyPathsForPrefetching = ["priceRecords"]
        request.predicate = NSPredicate(format: "priceRecords.@count > 0")

        return (try? persistentContainer.viewContext.fetch(request)) ?? []
    }
}
```

### 7.2 Image Processing Optimization

```swift
// Optimized image handling
extension OCRService {
    private func preprocessImage(_ image: UIImage) -> UIImage? {
        // Resize image for optimal OCR processing
        let targetSize = CGSize(width: 1024, height: 1024)

        return image.resized(to: targetSize)?
                   .enhanced(contrast: 1.2, brightness: 0.1)
    }
}

extension UIImage {
    func resized(to size: CGSize) -> UIImage? {
        UIGraphicsBeginImageContextWithOptions(size, false, 0.0)
        defer { UIGraphicsEndImageContext() }
        draw(in: CGRect(origin: .zero, size: size))
        return UIGraphicsGetImageFromCurrentImageContext()
    }
}
```

## 8. Privacy & Security

### 8.1 Data Privacy Implementation

```swift
// Privacy-focused data handling
class PrivacyManager {
    static let shared = PrivacyManager()

    // All data stays on device by default
    func configurePrivacySettings() {
        // Configure Core Data for local-only storage
        // Set up CloudKit with user consent
        // Implement data export/deletion
    }

    func exportUserData() -> Data {
        // Export all user data as JSON
        let context = DataManager.shared.persistentContainer.viewContext

        let receipts = try! context.fetch(Receipt.fetchRequest())
        let products = try! context.fetch(Product.fetchRequest())

        let exportData = ExportData(receipts: receipts, products: products)
        return try! JSONEncoder().encode(exportData)
    }

    func deleteAllUserData() async {
        // Complete data deletion for privacy compliance
        let context = DataManager.shared.persistentContainer.viewContext

        // Delete all entities
        await context.perform {
            // Implementation
        }
    }
}
```

## 9. Deployment & App Store

### 9.1 App Store Optimization

**App Store Listing:**

- **Title**: "AllesTeurer - Receipt Price Tracker"
- **Subtitle**: "Track inflation with receipt scanning"
- **Keywords**: "receipt, price, tracker, inflation, shopping, OCR"
- **Category**: Finance or Shopping

**Screenshots Strategy:**

1. Receipt scanning in action
2. Price history charts
3. Analytics dashboard
4. Product comparison view
5. Privacy-focused features

### 9.2 Release Strategy

**Beta Testing:**

- Internal testing with TestFlight (2 weeks)
- External beta with 100 users (2 weeks)
- Feedback integration and bug fixes

**Phased Rollout:**

- Release to Germany first (primary market)
- Expand to Austria and Switzerland
- European Union rollout
- Global availability

## Conclusion

This iOS-first implementation plan provides a clear 12-week roadmap to deliver a privacy-focused, locally-intelligent receipt scanning and price tracking app. The architecture is designed to:

- **Start Simple**: iOS-only with local Core Data storage
- **Deliver Value**: Core features work without backend dependencies
- **Scale Smart**: Easy evolution path to multi-platform with cloud services
- **Maintain Privacy**: All processing happens on-device initially
- **Ensure Quality**: Comprehensive testing and accessibility compliance

The app can launch in the App Store within 3 months, validate the market, and then expand with backend services and additional platforms based on user feedback and traction.

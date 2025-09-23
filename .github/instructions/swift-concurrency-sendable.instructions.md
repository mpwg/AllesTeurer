# Swift Concurrency: Sending Data Safely Across Actor Boundaries - Key Learnings

## Core Problem

When working with actors and async/await, Swift prevents non-Sendable types from crossing actor boundaries to avoid data races. The error message is:
"Non-sendable type 'YourType' returned by implicitly asynchronous call to nonisolated function cannot cross actor boundary."

## Making Classes Sendable

### Requirements for Class Sendability:

1. **Must be `final`** - Cannot be subclassed (prevents unsafe subclassing)
2. **Must conform to `Sendable` protocol**
3. **All properties must be Sendable** (constants, value types, or other Sendable types)

### Example:

```swift
final class User: Sendable {
    let name: String        // Constant value types are Sendable
    let password: String

    init(name: String, password: String) {
        self.name = name
        self.password = password
    }
}
```

## Automatic Sendable Conformance

### Types that are automatically Sendable:

- **Actors** - Inherently Sendable due to built-in synchronization
- **Structs** - If all properties are Sendable
- **Enums** - If all associated values are Sendable
- **Value types** - Strings, Integers, etc.

## Functions and Sendability

### @Sendable Functions:

Functions that cross actor boundaries must be marked `@escaping @Sendable`:

```swift
func runLater(_ function: @escaping @Sendable () -> Void) {
    DispatchQueue.global().asyncAfter(deadline: .now() + 3, execute: function)
}
```

### Tasks are Sendable if their work is Sendable:

```swift
repeatAction {
    Task {
        await counter.increment()  // Safe if counter is Sendable
    }
}
```

## SwiftData Models and Sendability

### Problem with our current code:

SwiftData models like `@Model class Rechnung` are **NOT** automatically Sendable because:

1. They're classes (not structs)
2. They're not marked as `final`
3. They contain mutable properties
4. SwiftData models have complex internal state

### Solutions for SwiftData Models:

#### Option 1: Value Type Transfer Objects

```swift
struct ReceiptData: Sendable {
    let id: UUID
    let storeName: String
    let scanDate: Date
    let totalAmount: Decimal
}

// In DataManager (@ModelActor)
func getReceiptData() throws -> [ReceiptData] {
    return receipts.map { receipt in
        ReceiptData(
            id: receipt.id,
            storeName: receipt.geschaeftsname,
            scanDate: receipt.scanDatum,
            totalAmount: receipt.gesamtbetrag
        )
    }
}
```

#### Option 2: Use nonisolated methods

```swift
@ModelActor
actor DataManager {
    nonisolated func getReceiptCount() -> Int {
        // Can only access immutable data
    }
}
```

#### Option 3: Keep ViewModels non-isolated

```swift
// Don't use @MainActor if working with non-Sendable data
@Observable
class ReceiptListViewModel {  // No @MainActor
    // Work with SwiftData models directly
}
```

## Best Practices for MVVM with SwiftData

### 1. Repository Pattern with Value Types:

```swift
@ModelActor
actor DataRepository {
    func fetchReceiptSummaries() throws -> [ReceiptSummary] {
        // Convert @Model objects to Sendable value types
    }
}

@MainActor
@Observable
class ReceiptListViewModel {
    private let repository: DataRepository
    var receiptSummaries: [ReceiptSummary] = []

    func loadReceipts() async {
        do {
            receiptSummaries = try await repository.fetchReceiptSummaries()
        } catch {
            // Handle error
        }
    }
}
```

### 2. Task-based Operations:

```swift
@MainActor
@Observable
class ReceiptListViewModel {
    func deleteReceipt(_ receiptID: UUID) async {
        Task {
            try await repository.deleteReceipt(id: receiptID)
            await loadReceipts() // Refresh
        }
    }
}
```

## Key Takeaways for AllesTeurer Project:

1. **SwiftData models cannot be made Sendable easily** - they're complex classes with mutable state
2. **Use value types for data transfer** between actors
3. **Keep ViewModels @MainActor** for UI updates, but transfer data via Sendable types
4. **DataManager (@ModelActor)** should convert model objects to value types before returning
5. **Actors are inherently Sendable** - can be passed between contexts safely

## Applied Solution Strategy:

Instead of trying to make `Rechnung` Sendable, create transfer objects:

```swift
struct ReceiptListItem: Sendable {
    let id: UUID
    let storeName: String
    let scanDate: Date
    let totalAmount: Double  // Convert Decimal to Double
    let itemCount: Int
}

@ModelActor
actor DataManager {
    func getReceiptListItems() throws -> [ReceiptListItem] {
        return receipts.map { receipt in
            ReceiptListItem(
                id: receipt.id,
                storeName: receipt.geschaeftsname,
                scanDate: receipt.scanDatum,
                totalAmount: Double(truncating: receipt.gesamtbetrag as NSDecimalNumber),
                itemCount: receipt.artikel.count
            )
        }
    }
}
```

This approach maintains proper concurrency safety while enabling MVVM architecture with SwiftData.

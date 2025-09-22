# GitHub Copilot Instructions

These instructions define the mandatory architecture and patterns for the AllesTeurer iOS project.

## 🏗️ MANDATORY MVVM Architecture with Async/Await

**CRITICAL ARCHITECTURE RULES - ALWAYS ENFORCE:**

- **UI**: SwiftUI with Observable ViewModels and data binding
- **ViewModels**: MUST be @Observable classes (Swift 5.9+) with async/await operations only
- **Repositories**: Data access abstraction with SwiftData integration using ModelActor for thread-safety
- **Use Cases**: Domain logic encapsulation with async/await for complex operations
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

## 🧩 Patterns to Follow

- Use enums with associated values to manage

### 🚫 Patterns to Avoid

- Don't use force unwraps (`!`) unless you're certain the value is non-nil.
- Avoid putting business logic directly inside views.
- Don't mix UIKit and SwiftUI unnecessarily.
- Avoid deeply nested views or view models—break them into modules.
- Don't expose internal state directly—use access control (`private`, `internal`, `public`).
- Avoid hardcoding strings or magic numbers—use constants or localization.
- **DO NOT use Core Data** - this project uses Swift Data exclusively.view state.
- Prefer Swift's `Codable` for JSON encoding/decoding.

## 💾 Data Persistence

**IMPORTANT**: This app uses **Swift Data**, NOT Core Data.

### Swift Data Guidelines

- Use `@Model` macro to define data models instead of Core Data's `NSManagedObject`.
- Use `ModelContainer` for data storage configuration.
- Use `@Query` property wrapper in SwiftUI views to fetch data reactively.
- Use `ModelContext` for data operations (insert, delete, save).
- Leverage Swift Data's automatic relationship management.
- Use `@Attribute` for custom property configurations (unique constraints, etc.).

### SwiftData Predicate Best Practices

**CRITICAL RULES - ALWAYS FOLLOW:**

- Use `#Predicate<ModelType>` with explicit type annotation for @Query
- For FetchDescriptor, `#Predicate { model in ... }` type inference usually works
- String comparisons: Use `contains()`, `starts(with:)` - NOT `hasPrefix()` or `hasSuffix()`
- Case-insensitive search: Use `localizedStandardContains()` - NOT `uppercased()` or `lowercased()`
- Boolean logic: Use `&&` and `||` for single expressions - NOT nested if statements
- Negation: Use `!condition` - NOT `condition == false` (crashes at runtime)
- Relationship queries: Use `collection.contains { }`, `collection.filter { }`, `collection.isEmpty`
- External values: Create local copies (`let now = Date.now`) - can't reference external variables directly
- Date comparisons: Always create local date variables in predicate scope

**PREDICATE PATTERNS THAT WORK:**

```swift
// String filtering
#Predicate<Movie> { movie in movie.name.starts(with: "Back") }
#Predicate<Movie> { movie in movie.name.localizedStandardContains("JAWS") }

// Relationship queries
#Predicate<Movie> { movie in movie.cast.count > 10 }
#Predicate<Movie> { movie in !movie.cast.isEmpty }
#Predicate<Movie> { movie in movie.cast.contains { $0.name == "Tom Cruise" } }

// Date filtering with local variables
let now = Date.now
#Predicate<Movie> { movie in movie.releaseDate > now }

// Boolean combinations
#Predicate<Movie> { movie in
    movie.director.name.contains("Steven") && movie.cost > 100_000_000
}
```

**PREDICATE PATTERNS THAT CRASH:**

```swift
// AVOID - These will crash at runtime
movie.cast.isEmpty == false  // Use !movie.cast.isEmpty instead
movie.name.uppercased()      // Use localizedStandardContains() instead
movie.releaseDate > Date.now // Create local variable first
```

### Example Swift Data Usage

````swift
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
```ine how GitHub Copilot should assist with this project. The goal is to ensure consistent, high-quality code generation aligned with our conventions, stack, and best practices.

## 🧠 Context

- **Project Type**: iOS App / macOS App / CLI Tool / Swift Package
- **Language**: Swift
- **Framework / Libraries**: SwiftUI / UIKit / Combine / Foundation / Vapor / Alamofire
- **Data Persistence**: Swift Data (NOT Core Data)
- **Architecture**: MVVM / Clean Architecture / VIPER / Modular

## 🔧 General Guidelines

- Use idiomatic Swift conventions (camelCase, struct over class when possible).
- Prefer `let` over `var` for immutability.
- Use Swift’s optionals and `guard`/`if let` for safe unwrapping.
- Avoid force unwraps (`!`) unless absolutely safe and justified.
- Format using `swift-format` or SwiftLint.
- Document public methods and types using `///` doc comments.
- Leverage value types (structs, enums) and protocol-oriented design.

## 📁 File Structure

Use this structure as a guide when creating or updating files:

```text
Sources/
  App/
    Models/
    Views/
    ViewModels/
    Services/
    Utilities/
    Extensions/
Tests/
  Unit/
  Integration/
Resources/
````

## 🧶 Patterns

### ✅ Patterns to Follow

- Use MVVM for SwiftUI-based UIs.
- Use property wrappers like `@State`, `@ObservedObject`, and `@EnvironmentObject` properly.
- Leverage Combine or async/await for reactive and asynchronous logic.
- Use dependency injection through initializers or protocols.
- Break down views into reusable components.
- Validate user input using model or view model logic.
- Use enums with associated values to manage screen/view state.
- Prefer Swift’s `Codable` for JSON encoding/decoding.

### 🚫 Patterns to Avoid

- Don’t use force unwraps (`!`) unless you’re certain the value is non-nil.
- Avoid putting business logic directly inside views.
- Don’t mix UIKit and SwiftUI unnecessarily.
- Avoid deeply nested views or view models—break them into modules.
- Don’t expose internal state directly—use access control (`private`, `internal`, `public`).
- Avoid hardcoding strings or magic numbers—use constants or localization.

## 🧪 Testing Guidelines

- Use `XCTest` for unit and UI tests.
- Use `@testable import` to access internal modules when needed.
- Write snapshot/UI tests for reusable views or complex components.
- Use mock services conforming to protocols for ViewModel tests.
- Test async behavior using `XCTestExpectation` or `async/await`.

## 🧩 Example Prompts

- `Copilot, create a SwiftUI view that shows a list of users with their names and avatars.`
- `Copilot, write a model struct for a Product with name, price, and optional discount.`
- `Copilot, implement a Combine publisher that fetches data from a URL and decodes JSON.`
- `Copilot, write unit tests for the LoginViewModel using a mock AuthService.`
- `Copilot, define an enum for authentication state with associated values for success and error.`
- `Copilot, create a Swift Data model for a User with @Model macro and relationships.`
- `Copilot, implement a SwiftUI view that uses @Query to display Swift Data objects.`

## 🔁 Iteration & Review

- Review Copilot output for proper Swift idioms and memory safety.
- Refactor large or nested SwiftUI views into reusable components.
- Use Xcode warnings and SwiftLint to catch violations.
- Guide Copilot with comments for complex UI layout or async logic.

## 📚 References

- [The Swift Programming Language Book](https://swift.org/documentation/#the-swift-programming-language)
- [Apple SwiftUI Documentation](https://developer.apple.com/documentation/swiftui)
- [Apple Swift Data Documentation](https://developer.apple.com/documentation/swiftdata)
- [Swift.org API Guidelines](https://swift.org/documentation/api-design-guidelines/)
- [Combine Framework Guide](https://developer.apple.com/documentation/combine)
- [SwiftLint (Linter)](https://realm.github.io/SwiftLint/)
- [Vapor Web Framework](https://docs.vapor.codes/)
- [Alamofire Networking Library](https://github.com/Alamofire/Alamofire)
- [Apple XCTest Framework](https://developer.apple.com/documentation/xctest)
- [Swift Package Manager Docs](https://developer.apple.com/documentation/swift_packages/)

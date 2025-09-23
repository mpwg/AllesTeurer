---
description: "Instructions for using SwiftUI native types instead of UIKit/AppKit"
applyTo: "**/*.swift"
---

# SwiftUI Native Types Instructions

## CRITICAL RULE: Prefer SwiftUI Native Types

**ALWAYS use SwiftUI native types instead of UIKit/AppKit when possible.**

### Image Handling

**AVOID:**

```swift
import UIKit
func processImage(_ image: UIImage) -> Data? {
    return image.jpegData(compressionQuality: 0.8)
}
```

**PREFERRED:**

```swift
// Use Data directly for image processing
func processImageData(_ imageData: Data) async throws -> ProcessedResult {
    // Process raw image data
}

// Or use SwiftUI's Image with ImageRenderer for conversion
import SwiftUI
func renderImageToData(_ image: Image) -> Data? {
    let renderer = ImageRenderer(content: image)
    return renderer.cgImage?.dataProvider?.data as Data?
}
```

### Platform-Agnostic Approach

**AVOID:**

```swift
#if canImport(UIKit)
    import UIKit
    typealias PlatformImage = UIImage
#else
    import AppKit
    typealias PlatformImage = NSImage
#endif
```

**PREFERRED:**

```swift
// Use Data for cross-platform image handling
func processImageData(_ data: Data) -> ProcessedResult {
    // Platform-agnostic processing
}

// Use SwiftUI types that work across platforms
import SwiftUI
struct ImageProcessor {
    func process(_ image: Image) -> ProcessedImage {
        // SwiftUI Image works on all platforms
    }
}
```

### OCR Integration

**PREFERRED PATTERN:**

```swift
import SwiftUI
import Vision

@MainActor
@Observable
final class OCRService {
    func processImageData(_ imageData: Data) async throws -> OCRResult {
        guard let cgImage = CGImage.fromData(imageData) else {
            throw OCRError.invalidImageData
        }

        return try await performOCR(cgImage: cgImage)
    }
}
```

### Rationale

1. **Platform Independence**: SwiftUI types work across iOS, macOS, tvOS, watchOS
2. **Future-Proof**: SwiftUI is Apple's future direction
3. **Simplified Code**: Less conditional compilation
4. **Better Performance**: Native SwiftUI rendering pipeline
5. **Consistent APIs**: Same interface across all Apple platforms

### Migration Guidelines

When migrating from UIKit/AppKit:

1. Replace UIImage/NSImage with Data for storage
2. Use SwiftUI Image for display
3. Use ImageRenderer for SwiftUI → CGImage conversion
4. Remove platform-specific imports when possible
5. Use Vision framework directly with CGImage from Data

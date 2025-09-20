# AllesTeurer Multiplatform Development Guide

## Quick Start

```bash
# Clone and setup
git clone https://github.com/mpwg/AllesTeurer.git
cd AllesTeurer

# Install dependencies
./gradlew build

# Start development
./gradlew :apps:composeApp:installDebug  # Android
./gradlew :apps:composeApp:iosSimulatorArm64Test  # iOS Simulator
```

## Project Commands

### Gradle Commands

```bash
./gradlew build                    # Build all targets
./gradlew test                     # Run all tests
./gradlew ktlintCheck             # Lint Kotlin code
./gradlew ktlintFormat            # Format Kotlin code
./gradlew clean                   # Clean all build outputs
```

### Platform-Specific Commands

```bash
# Android
./gradlew :apps:composeApp:installDebug
./gradlew :apps:composeApp:testDebugUnitTest

# iOS
./gradlew :apps:composeApp:iosSimulatorArm64Test
open apps/composeApp/iosApp/iosApp.xcodeproj

# Desktop
./gradlew :apps:composeApp:runDistributable
```

### Infrastructure Commands

```bash
docker-compose up -d        # Start local services (database, etc.)
docker-compose down         # Stop local services
docker-compose logs -f      # View service logs
```

## Platform Status

- ✅ **Android**: Primary platform (KMP + Compose Multiplatform)
- ✅ **iOS**: Primary platform (KMP + Compose Multiplatform)
- 🔄 **Desktop**: Supported (Compose Desktop)
- 🔄 **Web**: Experimental (Compose for Web)
- 🔄 **Backend**: Optional (Ktor/GraphQL)

## Shared Business Logic

The `shared` module contains platform-agnostic business logic written in Kotlin Multiplatform:

- **Data Models**: Kotlin data classes with @Serializable annotations
- **Analytics**: Price analysis and inflation calculation algorithms
- **Matching**: Product fuzzy matching and categorization logic
- **OCR Processing**: Expect/actual declarations for platform-specific OCR
- **Database**: SQLDelight for type-safe, multiplatform SQL queries
- **Utilities**: Date processing, text processing, and validation functions

This shared code ensures 100% business logic consistency across all platforms while allowing platform-specific OCR implementations and native UI experiences.

## Development Workflow

1. **Multiplatform-First**: Develop shared business logic in KMP
2. **Platform-Specific OCR**: Implement native OCR (Vision Framework for iOS, ML Kit for Android)
3. **Compose Multiplatform UI**: Build cross-platform UI with platform adaptations
4. **Native Integrations**: Add platform-specific features (widgets, shortcuts, etc.)
5. **Optional Backend**: Add server-side features using Ktor if needed

## Privacy-First Architecture

- ✅ Local-first data processing
- ✅ On-device OCR and analytics
- ✅ Optional CloudKit sync only
- ✅ No third-party tracking
- ✅ User-controlled data export

## Learn More

- [KMP Instructions](/.github/instructions/kotlin.instructions.md) - Kotlin Multiplatform patterns
- [Compose Multiplatform Guide](/.github/instructions/compose-multiplatform.instructions.md) - UI development
- [Architecture Docs](/spec/architecture.md) - Technical architecture
- [Requirements](/spec/Anforderungen.md) - Functional requirements (German)
- [Accessibility Guidelines](/.github/instructions/a11y.instructions.md) - WCAG compliance

---

_Building the future of privacy-first price tracking_ 🚀

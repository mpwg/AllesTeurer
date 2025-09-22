# Native iOS Infrastructure Setup for AllesTeurer

## Overview

Configure a complete native iOS development setup with Xcode project configuration, SwiftData persistence, Vision Framework integration, and App Store deployment pipeline for AllesTeurer price tracking application.

## Directory Structure

```text
AllesTeurer/
├── .github/
│   ├── workflows/
│   │   ├── ci.yml                  # iOS CI/CD pipeline
│   │   └── deploy-ios.yml          # App Store deployment
├── Alles Teurer/                   # Main iOS App
│   ├── Alles_TeurerApp.swift       # App Entry Point with SwiftData ModelContainer
│   ├── ContentView.swift           # Main SwiftUI View with TabView
│   ├── Models/                     # SwiftData Models
│   │   ├── Receipt.swift           # Receipt entity with @Model
│   │   ├── Product.swift           # Product entity with @Model
│   │   └── PriceHistory.swift      # Price tracking entity
│   ├── ViewModels/                 # Observable ViewModels
│   │   ├── ScannerViewModel.swift  # Receipt scanning logic
│   │   ├── ProductsViewModel.swift # Product management
│   │   └── AnalyticsViewModel.swift # Price analytics
│   ├── Views/                      # SwiftUI Views
│   │   ├── Scanner/                # Receipt scanning interface
│   │   ├── Products/               # Product list and details
│   │   ├── Analytics/              # Price charts and insights
│   │   └── Settings/               # App configuration
│   ├── Services/                   # Business Logic
│   │   ├── OCRService.swift        # Vision Framework integration
│   │   ├── DataManager.swift       # SwiftData operations
│   │   └── CloudKitService.swift   # Optional CloudKit sync
│   ├── Extensions/                 # Swift Extensions
│   └── Resources/                  # Assets and Configuration
│       ├── Assets.xcassets/        # App icons and images
│       ├── Info.plist              # App configuration
│       └── Alles_Teurer.entitlements # App capabilities
├── Alles Teurer.xcodeproj/        # Xcode Project Configuration
├── Alles TeurerTests/             # Unit Tests using Swift Testing
├── Alles TeurerUITests/           # UI Tests using XCUITest
├── fastlane/                      # App Store deployment automation
│   ├── Appfile                    # App Store Connect configuration
│   ├── Fastfile                   # Deployment lanes
│   └── Matchfile                  # Code signing certificates
├── docs/                          # Documentation
└── spec/                          # Requirements and architecture docs
```

## Configuration Files

### Xcode Project Configuration

```xml
<!-- Info.plist -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleDisplayName</key>
    <string>Alles Teurer</string>
    <key>CFBundleIdentifier</key>
    <string>eu.mpwg.allesteurer</string>
    <key>CFBundleVersion</key>
    <string>1</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0.0</string>
    <key>LSRequiresIPhoneOS</key>
    <true/>
    <key>UIRequiredDeviceCapabilities</key>
    <array>
        <string>armv7</string>
        <string>camera-flash</string>
    </array>
    <key>NSCameraUsageDescription</key>
    <string>Used to scan and extract text from receipts for price tracking</string>
    <key>UILaunchStoryboardName</key>
    <string>Launch Screen</string>
    <key>UISupportedInterfaceOrientations</key>
    <array>
        <string>UIInterfaceOrientationPortrait</string>
        <string>UIInterfaceOrientationLandscapeLeft</string>
        <string>UIInterfaceOrientationLandscapeRight</string>
    </array>
    <key>UISupportedInterfaceOrientations~ipad</key>
    <array>
        <string>UIInterfaceOrientationPortrait</string>
        <string>UIInterfaceOrientationPortraitUpsideDown</string>
        <string>UIInterfaceOrientationLandscapeLeft</string>
        <string>UIInterfaceOrientationLandscapeRight</string>
    </array>
</dict>
</plist>
```

### App Entitlements

```xml
<!-- Alles_Teurer.entitlements -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>com.apple.developer.icloud-container-identifiers</key>
    <array>
        <string>iCloud.eu.mpwg.allesteurer</string>
    </array>
    <key>com.apple.developer.ubiquity-kvstore-identifier</key>
    <string>$(TeamIdentifierPrefix)eu.mpwg.allesteurer</string>
    <key>com.apple.developer.icloud-services</key>
    <array>
        <string>CloudKit</string>
    </array>
</dict>
</plist>
```

} mavenContent {

````includeGroupAndSubgroups("androidx")

                includeGroupAndSubgroups("com.android")

### gradle/libs.versions.toml                includeGroupAndSubgroups("com.google")

            }

```toml        }

[versions]        mavenCentral()

kotlin = "2.2.20"        gradlePluginPortal()

compose = "1.7.1"    }

android-compileSdk = "35"}

android-minSdk = "24"

android-targetSdk = "35"dependencyResolutionManagement {

agp = "8.7.2"    repositories {

sqldelight = "2.0.2"        google {

koin = "4.0.0"            mavenContent {

ktor = "3.0.1"                includeGroupAndSubgroups("androidx")

coroutines = "1.9.0"                includeGroupAndSubgroups("com.android")

serialization = "1.7.3"                includeGroupAndSubgroups("com.google")

kmp-nativecoroutines = "1.0.0-ALPHA-36"            }

        }

[libraries]        mavenCentral()

kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }    }

kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }}

kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }```



compose-runtime = { module = "org.jetbrains.compose.runtime:runtime", version.ref = "compose" }### gradle/libs.versions.toml

compose-foundation = { module = "org.jetbrains.compose.foundation:foundation", version.ref = "compose" }

compose-material3 = { module = "org.jetbrains.compose.material3:material3", version.ref = "compose" }```toml

compose-ui-tooling = { module = "org.jetbrains.compose.ui:ui-tooling", version.ref = "compose" }[versions]

compose-ui-tooling-preview = { module = "org.jetbrains.compose.ui:ui-tooling-preview", version.ref = "compose" }kotlin = "2.2.20"

compose = "1.7.1"

sqldelight-driver-sqlite = { module = "app.cash.sqldelight:sqlite-driver", version.ref = "sqldelight" }android-compileSdk = "35"

sqldelight-driver-android = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }android-minSdk = "24"

sqldelight-driver-native = { module = "app.cash.sqldelight:native-driver", version.ref = "sqldelight" }android-targetSdk = "35"

agp = "8.7.2"

koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }sqldelight = "2.0.2"

koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }koin = "4.0.0"

ktor = "3.0.1"

ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }coroutines = "1.9.0"

ktor-client-cio = { module = "io.ktor:ktor-client-cio", version.ref = "ktor" }serialization = "1.7.3"

ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }kmp-nativecoroutines = "1.0.0-ALPHA-36"



[plugins][libraries]

kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }

kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }

kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }

android-application = { id = "com.android.application", version.ref = "agp" }

android-library = { id = "com.android.library", version.ref = "agp" }compose-runtime = { module = "org.jetbrains.compose.runtime:runtime", version.ref = "compose" }

compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "compose" }compose-foundation = { module = "org.jetbrains.compose.foundation:foundation", version.ref = "compose" }

sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }compose-material3 = { module = "org.jetbrains.compose.material3:material3", version.ref = "compose" }

```compose-ui-tooling = { module = "org.jetbrains.compose.ui:ui-tooling", version.ref = "compose" }

compose-ui-tooling-preview = { module = "org.jetbrains.compose.ui:ui-tooling-preview", version.ref = "compose" }

## Local Development Setup

sqldelight-driver-sqlite = { module = "app.cash.sqldelight:sqlite-driver", version.ref = "sqldelight" }

### Development Environment Requirementssqldelight-driver-android = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }

sqldelight-driver-native = { module = "app.cash.sqldelight:native-driver", version.ref = "sqldelight" }

- **JDK**: 17 or higher

- **Android Studio**: Latest stable (for Android development)koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }

- **Xcode**: 15.0+ (for iOS development, macOS only)koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }

- **Kotlin**: 2.2.20+

- **Gradle**: 8.0+ (wrapper included)ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }

ktor-client-cio = { module = "io.ktor:ktor-client-cio", version.ref = "ktor" }

### Optional Backend Services (Ktor)ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }



```yaml[plugins]

# docker-compose.dev.ymlkotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }

version: '3.8'kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }

kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }

services:android-application = { id = "com.android.application", version.ref = "agp" }

  postgres:android-library = { id = "com.android.library", version.ref = "agp" }

    image: postgres:15-alpinecompose-multiplatform = { id = "org.jetbrains.compose", version.ref = "compose" }

    environment:sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }

      POSTGRES_USER: alles_teuer```

      POSTGRES_PASSWORD: local_dev_password

      POSTGRES_DB: alles_teuer_dev## Local Development Setup

    ports:

      - "5432:5432"### Development Environment Requirements

    volumes:

      - postgres_data:/var/lib/postgresql/data- **JDK**: 17 or higher

- **Android Studio**: Latest stable (for Android development)

  redis:- **Xcode**: 15.0+ (for iOS development, macOS only)

    image: redis:7-alpine- **Kotlin**: 2.2.20+

    ports:- **Gradle**: 8.0+ (wrapper included)

      - "6379:6379"

    command: redis-server --appendonly yes### Optional Backend Services (Ktor)

    volumes:

      - redis_data:/data```yaml

# docker-compose.dev.yml

volumes:version: '3.8'

  postgres_data:

  redis_data:services:

```  postgres:

    image: postgres:15-alpine

### Development Setup Scripts    environment:

      POSTGRES_USER: alles_teuer

#### setup.sh      POSTGRES_PASSWORD: local_dev_password

      POSTGRES_DB: alles_teuer_dev

```bash    ports:

#!/bin/bash      - "5432:5432"

    volumes:

echo "🚀 Setting up AllesTeurer KMP development environment..."      - postgres_data:/var/lib/postgresql/data



# Check prerequisites  redis:

command -v java >/dev/null 2>&1 || { echo "Java JDK 17+ is required but not installed. Aborting." >&2; exit 1; }    image: redis:7-alpine

command -v docker >/dev/null 2>&1 || { echo "Docker is required but not installed. Aborting." >&2; exit 1; }    ports:

      - "6379:6379"

# Setup environment files for optional backend    command: redis-server --appendonly yes

echo "🔧 Setting up environment files..."    volumes:

if [ -f .env.example ]; then      - redis_data:/data

    cp .env.example .env.local

fivolumes:

  postgres_data:

# Start optional infrastructure services  redis_data:

echo "🐳 Starting optional Docker services..."```

if [ -f docker-compose.dev.yml ]; then

    docker-compose -f docker-compose.dev.yml up -d### Development Setup Scripts

    echo "⏳ Waiting for services to be ready..."

    sleep 10#### setup.sh

fi

```bash

# Build KMP project#!/bin/bash

echo "🏗️ Building Kotlin Multiplatform project..."

./gradlew buildecho "🚀 Setting up AllesTeurer KMP development environment..."



# Run tests# Check prerequisites

echo "🧪 Running tests..."command -v node >/dev/null 2>&1 || { echo "Node.js is required but not installed. Aborting." >&2; exit 1; }

./gradlew testcommand -v pnpm >/dev/null 2>&1 || { echo "Installing pnpm..."; npm install -g pnpm; }

command -v docker >/dev/null 2>&1 || { echo "Docker is required but not installed. Aborting." >&2; exit 1; }

echo "✅ Setup complete! You can now:"

echo "  - Open the project in Android Studio or IntelliJ IDEA"# Install dependencies

echo "  - Run Android app: ./gradlew :apps:composeApp:installDebug"echo "📦 Installing dependencies..."

echo "  - Run iOS app: open apps/composeApp/iosApp/iosApp.xcodeproj"pnpm install

echo "  - Run desktop app: ./gradlew :apps:composeApp:runDistributable"

echo "  - Run tests: ./gradlew test"# Setup environment files

echo "  - Generate SQLDelight code: ./gradlew generateSqlDelightInterface"echo "🔧 Setting up environment files..."

```cp .env.example .env.local

cp apps/backend/.env.example apps/backend/.env.local

## CI/CD Pipeline (GitHub Actions)cp apps/web/.env.example apps/web/.env.local



### .github/workflows/ci.yml# Start infrastructure

echo "🐳 Starting Docker services..."

```yamldocker-compose up -d

name: CI Pipeline

# Wait for services

on:echo "⏳ Waiting for services to be ready..."

  push:sleep 10

    branches: [ main, develop ]

  pull_request:# Run database migrations

    branches: [ main ]echo "🗄️ Running database migrations..."

pnpm run db:migrate

jobs:

  test-multiplatform:# Seed database

    runs-on: ubuntu-latestecho "🌱 Seeding database..."

    steps:pnpm run db:seed

      - uses: actions/checkout@v4

      echo "✅ Setup complete! Run 'pnpm dev' to start development servers."

      - name: Set up JDK 17```

        uses: actions/setup-java@v4

        with:GitHub Actions CI/CD

          java-version: '17'

          distribution: 'temurin'CI Workflow (.github/workflows/ci.yml)



      - name: Cache Gradle packages``` YAML

        uses: actions/cache@v3name: CI

        with:

          path: |on:

            ~/.gradle/caches  push:

            ~/.gradle/wrapper    branches: [main, develop]

          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}  pull_request:

          restore-keys: |    branches: [main]

            ${{ runner.os }}-gradle-

jobs:

      - name: Grant execute permission for gradlew  lint:

        run: chmod +x gradlew    runs-on: ubuntu-latest

    steps:

      - name: Run shared tests      - uses: actions/checkout@v4

        run: ./gradlew :apps:composeApp:testDebugUnitTest      - uses: pnpm/action-setup@v2

        with:

      - name: Build KMP project          version: 8

        run: ./gradlew build      - uses: actions/setup-node@v4

        with:

  build-android:          node-version: '20'

    runs-on: ubuntu-latest          cache: 'pnpm'

    needs: test-multiplatform      - run: pnpm install --frozen-lockfile

    steps:      - run: pnpm run lint

      - uses: actions/checkout@v4

        test:

      - name: Set up JDK 17    runs-on: ubuntu-latest

        uses: actions/setup-java@v4    services:

        with:      postgres:

          java-version: '17'        image: postgres:15

          distribution: 'temurin'        env:

                    POSTGRES_PASSWORD: postgres

      - name: Cache Gradle packages        options: >-

        uses: actions/cache@v3          --health-cmd pg_isready

        with:          --health-interval 10s

          path: |          --health-timeout 5s

            ~/.gradle/caches          --health-retries 5

            ~/.gradle/wrapper    steps:

          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}      - uses: actions/checkout@v4

          restore-keys: |      - uses: pnpm/action-setup@v2

            ${{ runner.os }}-gradle-        with:

          version: 8

      - name: Grant execute permission for gradlew      - uses: actions/setup-node@v4

        run: chmod +x gradlew        with:

          node-version: '20'

      - name: Build Android Release          cache: 'pnpm'

        run: ./gradlew :apps:composeApp:assembleRelease      - run: pnpm install --frozen-lockfile

      - run: pnpm run test

      - name: Upload Android APK        env:

        uses: actions/upload-artifact@v3          DATABASE_URL: postgresql://postgres:postgres@localhost:5432/test

        with:

          name: android-release  build:

          path: apps/composeApp/build/outputs/apk/release/*.apk    runs-on: ubuntu-latest

    strategy:

  build-ios:      matrix:

    runs-on: macos-latest        app: [backend, web]

    needs: test-multiplatform    steps:

    steps:      - uses: actions/checkout@v4

      - uses: actions/checkout@v4      - uses: pnpm/action-setup@v2

              with:

      - name: Set up JDK 17          version: 8

        uses: actions/setup-java@v4      - uses: actions/setup-node@v4

        with:        with:

          java-version: '17'          node-version: '20'

          distribution: 'temurin'          cache: 'pnpm'

      - run: pnpm install --frozen-lockfile

      - name: Cache Gradle packages      - run: pnpm run build --filter=${{ matrix.app }}

        uses: actions/cache@v3

        with:  build-ios:

          path: |    runs-on: macos-latest

            ~/.gradle/caches    steps:

            ~/.gradle/wrapper      - uses: actions/checkout@v4

          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}      - uses: actions/setup-node@v4

          restore-keys: |        with:

            ${{ runner.os }}-gradle-          node-version: '20'

      - run: cd apps/ios && xcodebuild -scheme AllesTeurer -sdk iphonesimulator

      - name: Grant execute permission for gradlew

        run: chmod +x gradlew  build-android:

    runs-on: ubuntu-latest

      - name: Build iOS Framework    steps:

        run: ./gradlew :apps:composeApp:embedAndSignAppleFrameworkForXcode      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4

      - name: Build iOS App        with:

        run: |          distribution: 'temurin'

          cd apps/composeApp/iosApp          java-version: '17'

          xcodebuild -workspace iosApp.xcworkspace -scheme iosApp -configuration Release -destination 'generic/platform=iOS' -allowProvisioningUpdates      - run: cd apps/android && ./gradlew build

````

Implementation Tasks

## Gradle Commands Reference

- Initialize monorepo with Turborepo

### Development Commands - Setup pnpm workspaces

- Configure TypeScript paths and aliases

```bash - Setup ESLint and Prettier

# Build all targets - Configure Husky and lint-staged

./gradlew build - Create Docker development environment

 - Setup CI/CD pipelines

# Run tests across all platforms - Configure environment variables management

./gradlew test - Setup database migrations system

 - Create development setup scripts

# Clean build - Document development workflow

./gradlew clean

# Generate SQLDelight code
./gradlew generateSqlDelightInterface

# Format Kotlin code (if ktlint is configured)
./gradlew ktlintFormat

# Run on specific platforms
./gradlew :apps:composeApp:installDebug             # Android
./gradlew :apps:composeApp:runDistributable         # Desktop

# Platform-specific tests
./gradlew :apps:composeApp:testDebugUnitTest         # Android
./gradlew :apps:composeApp:iosSimulatorArm64Test    # iOS
```

### iOS Development Commands

```bash
# Open iOS project in Xcode
open apps/composeApp/iosApp/iosApp.xcodeproj

# Build iOS framework (required before opening Xcode)
./gradlew :apps:composeApp:embedAndSignAppleFrameworkForXcode

# Build for iOS simulator
./gradlew :apps:composeApp:iosSimulatorArm64Test
```

## Development Workflow

### Initial Setup Checklist

- [ ] Clone the repository
- [ ] Run `./setup.sh` to configure environment
- [ ] Open project in Android Studio or IntelliJ IDEA
- [ ] For iOS development: Open the Xcode project after building the framework
- [ ] Configure platform-specific SDKs (Android SDK, iOS SDK)
- [ ] Run `./gradlew build` to ensure everything compiles
- [ ] Run `./gradlew test` to verify all tests pass

### Daily Development Workflow

1. **Pull latest changes**: `git pull origin main`
2. **Build project**: `./gradlew build`
3. **Run tests**: `./gradlew test`
4. **Make changes** to shared code in `commonMain`
5. **Platform-specific changes** in `androidMain`/`iosMain`
6. **Test on platforms**:
   - Android: `./gradlew :apps:composeApp:installDebug`
   - iOS: Build framework then open Xcode
7. **Format code**: `./gradlew ktlintFormat`
8. **Commit changes**: Use conventional commits

### Key Files to Monitor

- `gradle/libs.versions.toml`: Dependency versions
- `build.gradle.kts`: Build configuration
- `commonMain/sqldelight/`: Database schema changes
- Platform-specific expect/actual implementations

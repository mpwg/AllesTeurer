# AllesTeurer 📱💰

> _"Alles wird teurer"_ - Eine plattformübergreifende App, die dabei hilft, Preisänderungen durch Scannen von Kassenbons zu verfolgen

**AllesTeurer** ist eine datenschutzfokussierte Kotlin Multiplatform Mobile (KMP) Anwendung, die lokale Kassenbon-Verarbeitung mit leistungsstarken Preisanalysen kombiniert. Die App verwendet plattformspezifische OCR-Implementierungen (Vision Framework für iOS, ML Kit für Android) und SQLDelight für sichere lokale Datenspeicherung, um eine vollständige Preishistorie ohne Cloud-Abhängigkeiten zu erstellen.

## 🎯 Überblick

Da steigende Kosten jeden betreffen, ermöglicht diese App den Nutzern:

- **🔒 Privacy-First**: Alle Daten bleiben auf Ihrem Gerät - keine Cloud-Services erforderlich
- **📊 Lokale Preisanalyse**: Verfolgen Sie Inflation und Ausgabenmuster mit On-Device-Berechnungen
- **🔍 Intelligente Produkterkennung**: Automatische Zuordnung ähnlicher Produkte über verschiedene Kassenbons
- **📈 Preistrends**: Visualisierung von Preisänderungen über Zeit und Händler hinweg
- **🌍 Plattformübergreifend**: Native Leistung auf iOS und Android mit geteilter Geschäftslogik

## ✨ Funktionen

### Kernfunktionalität

- 📸 **Kassenbon-Scanning**: Plattformspezifische Kamera-Integration (iOS/Android)
- � **OCR-Verarbeitung**: Vision Framework (iOS) / ML Kit (Android) für Texterkennung
- 💾 **Lokale Datenspeicherung**: SQLDelight für typsichere, plattformübergreifende Datenbankoperationen
- � **Produktabgleich**: Intelligente Algorithmen zur Erkennung gleicher Produkte

### Analysen & Einblicke

- � **Preishistorie**: Verfolgen Sie Preisänderungen für jedes Produkt über Zeit
- � **Interaktive Diagramme**: Compose Multiplatform Charts für Trendvisualisierung
- 🏪 **Händlervergleiche**: Preisunterschiede zwischen verschiedenen Geschäften analysieren
- 💡 **Inflationsindikatoren**: Lokale Berechnungen von Preistrendstatistiken

### Plattformspezifische Features

#### iOS Features

- 📱 **iOS Share Extension**: Kassenbon-Sharing von anderen Apps
- 🔍 **Spotlight Integration**: Produktsuche über iOS-Systemsuche
- 📋 **Widget Support**: Home Screen Widgets für Ausgabenübersicht
- 🗣️ **Shortcuts Integration**: Siri Shortcuts für häufige Aktionen

#### Android Features

- 📤 **Share Intent**: Receipt-Sharing zwischen Apps
- 🚀 **App Shortcuts**: Dynamische Shortcuts für schnellen Zugriff
- 🎨 **Material Design**: Material You mit dynamischen Farben
- 📱 **Adaptive Icons**: Native Android Icon-Unterstützung

## 🏗️ Projektstruktur

```text
alles-teurer/
├── apps/composeApp/         # Hauptanwendung (KMP)
│   ├── src/
│   │   ├── commonMain/      # Geteilte Geschäftslogik
│   │   │   ├── kotlin/
│   │   │   │   ├── data/        # Repositories und Datenquellen
│   │   │   │   ├── domain/      # Geschäftslogik und Use Cases
│   │   │   │   ├── presentation/# ViewModels und UI-State
│   │   │   │   └── ui/          # Compose Multiplatform UI
│   │   │   └── sqldelight/     # Datenbankschema und Queries
│   │   ├── androidMain/     # Android-spezifische Implementierung
│   │   │   └── kotlin/
│   │   │       ├── ocr/         # ML Kit OCR
│   │   │       └── platform/   # Android-spezifische Utilities
│   │   └── iosMain/         # iOS-spezifische Implementierung
│   │       └── kotlin/
│   │           ├── ocr/         # Vision Framework OCR
│   │           └── platform/   # iOS-spezifische Utilities
├── spec/                   # Anforderungen und Architektur-Dokumentation
└── tools/                  # Entwicklungstools und Scripts
```

## 🚀 Erste Schritte

### Voraussetzungen

**Kotlin Multiplatform Development:**

- **Kotlin 2.2.20+** mit K2 Compiler
- **Gradle 9.0+** mit Kotlin DSL
- **JDK 17+** für Android-Entwicklung

**iOS-Entwicklung:**

- **Xcode 15.0+** für iOS-spezifische Entwicklung
- **iOS 15.0+** als Mindest-Deployment-Version
- **Apple Developer Account** für Gerätetests

**Android-Entwicklung:**

- **Android Studio Hedgehog+** mit Kotlin-Plugin
- **Android API 24+** (Android 7.0+) als Mindest-Version
- **Android SDK** mit neuesten Build-Tools

### Installation

1. **Repository klonen**

   ```bash
   git clone https://github.com/mpwg/AllesTeurer.git
   cd AllesTeurer
   ```

2. **Abhängigkeiten installieren**

   ```bash
   # Gradle Wrapper für alle Plattformen verwenden
   ./gradlew build
   ```

3. **Plattformspezifische Setups**

   **Für iOS-Entwicklung:**

   ```bash
   # iOS-Projekt in Xcode öffnen
   open apps/composeApp/iosApp/iosApp.xcodeproj
   ```

   **Für Android-Entwicklung:**

   ```bash
   # Android Studio mit dem Projekt öffnen oder CLI verwenden
   ./gradlew :apps:composeApp:installDebug
   ```

### Erforderliche Berechtigungen

Die App benötigt folgende plattformspezifische Berechtigungen:

**iOS:**

- **Kamera-Zugriff**: Für Kassenbon-Scanning (NSCameraUsageDescription)
- **Fotobibliothek**: Für Import bestehender Bilder (NSPhotoLibraryUsageDescription)

**Android:**

- **Kamera-Berechtigung**: `android.permission.CAMERA`
- **Speicher-Zugriff**: `android.permission.READ_EXTERNAL_STORAGE`

## �️ Entwicklungsworkflow

### Gradle-Befehle

```bash
# Projekt für alle Plattformen kompilieren
./gradlew build

# Tests auf allen Plattformen ausführen
./gradlew test

# Plattformspezifische Builds
./gradlew :apps:composeApp:assembleDebug        # Android
./gradlew :apps:composeApp:iosSimulatorArm64Test  # iOS Simulator

# Code-Formatierung
./gradlew ktlintFormat
```

### Multiplatform-Entwicklung

```bash
# Shared Code testen
./gradlew :apps:composeApp:testDebugUnitTest

# Platform-spezifische Tests
./gradlew :apps:composeApp:connectedAndroidTest  # Android
./gradlew :apps:composeApp:iosTest              # iOS
```

## 🏛️ Architektur

### Kerntechnologien

- **Kotlin Multiplatform**: Geteilte Geschäftslogik zwischen iOS und Android
- **Compose Multiplatform**: Geteiltes UI-Framework mit nativen Adaptionen
- **SQLDelight**: Typsichere SQL-Datenbank für alle Plattformen
- **Kotlin Coroutines + Flow**: Asynchrone Programmierung und reaktive Datenströme
- **Expect/Actual**: Platform-spezifische Implementierungen (OCR, Kamera)

### Design-Pattern

- **MVVM**: Model-View-ViewModel-Architektur mit Compose
- **Repository Pattern**: Datenbank-Zugriffsabstraktion
- **Use Cases**: Domain-Layer für Geschäftslogik-Kapselung
- **Dependency Injection**: Testbarkeit und modularer Code

### Platform-spezifische Integrationen

**iOS:**

- Vision Framework für OCR-Texterkennung
- AVFoundation für Kamera-Integration
- CloudKit für optionale Gerätesynchronisation

**Android:**

- ML Kit für OCR-Texterkennung
- CameraX für Kamera-Integration
- Google Drive für optionale Cloud-Synchronisation

## 📖 Nutzung

> [!NOTE]
> Die App befindet sich derzeit in der Entwicklungsphase. Die folgenden Features sind für die finale Version geplant.

### Ersten Kassenbon scannen

1. **App starten** und zum Scanner-Tab navigieren
2. **Kassenbon positionieren** und Aufnahme-Button drücken
3. **OCR-Ergebnisse überprüfen** - die App erkennt automatisch:
   - Händlernamen und -adresse
   - Kaufdatum und -uhrzeit
   - Einzelne Artikel mit Preisen und Mengen
4. **Daten bestätigen** und lokal speichern

### Preishistorie analysieren

1. **Zum Produkte-Tab wechseln**
2. **Produkt auswählen** für detaillierte Preishistorie
3. **Interaktive Charts erkunden** mit Compose Multiplatform
4. **Preisvergleiche** zwischen verschiedenen Händlern einsehen

### Analytics-Dashboard

1. **Analytics-Tab öffnen** für umfassende Einblicke
2. **Ausgabenmuster** nach Kategorien und Zeiträumen anzeigen
3. **Inflationstrends** für persönliche Einkäufe verfolgen
4. **Preisalarme** für signifikante Änderungen einrichten

## 📄 Datenschutz & Daten

> [!IMPORTANT]
> AllesTeurer folgt einem "Privacy-by-Design"-Ansatz mit vollständiger Datenkontrolle.

- **🔒 Lokale Speicherung**: Alle Kassenbons und Preisdaten bleiben auf Ihrem Gerät
- **🚫 Keine Cloud-Abhängigkeiten**: Funktioniert vollständig offline ohne externe Services
- **🔄 Optionale Synchronisation**: Plattformspezifische Cloud-Sync nur auf Wunsch (CloudKit/Google Drive)
- **📤 Vollständiger Export**: Alle Daten jederzeit als CSV/PDF exportierbar
- **🗑️ Einfache Datenlöschung**: DSGVO-konforme Datenlöschung ohne Rückstände

## 🤝 Mitwirken

Wir begrüßen Beiträge zur Entwicklung von AllesTeurer! Hier ist, wie Sie helfen können:

### Entwicklungssetup

1. **Repository forken** und lokalen Branch erstellen
2. **Entwicklungsumgebung einrichten** (siehe [Installation](#installation))
3. **Unseren Coding-Standards folgen** (siehe `.github/instructions/`)
4. **Tests schreiben** für neue Features
5. **Pull Request einreichen** mit ausführlicher Beschreibung

### Gradle-Befehle für Entwicklung

```bash
# Tests vor Pull Request ausführen
./gradlew check

# Shared Code kompilieren
./gradlew :apps:composeApp:compileKotlinMetadata

# Platform-spezifische Builds überprüfen
./gradlew :apps:composeApp:compileDebugKotlinAndroid
./gradlew :apps:composeApp:compileKotlinIosSimulatorArm64
```

## � Systemanforderungen

### Minimum Requirements

**iOS:**

- iOS 15.0 oder neuer
- iPhone 8 oder neuer (64-bit Prozessor)
- 100MB freier Speicherplatz

**Android:**

- Android 7.0 (API Level 24) oder neuer
- 2GB RAM (empfohlen: 4GB+)
- 100MB freier Speicherplatz
- Kamera für Kassenbon-Scanning

### Empfohlene Spezifikationen

- **Neuere Geräte**: Bessere OCR-Leistung und schnellere Verarbeitung
- **Gute Kamera**: Für optimale Kassenbon-Erkennung
- **Ausreichend Speicher**: Für lokale Speicherung der Preishistorie

## 📞 Support & Community

- **🐛 Bug Reports**: [GitHub Issues](https://github.com/mpwg/AllesTeurer/issues)
- **💬 Diskussionen**: [GitHub Discussions](https://github.com/mpwg/AllesTeurer/discussions)
- **📖 Dokumentation**: [Projekt Wiki](https://github.com/mpwg/AllesTeurer/wiki)
- **� Roadmap**: [GitHub Projects](https://github.com/mpwg/AllesTeurer/projects)

## 🙏 Danksagungen

- **JetBrains**: Für Kotlin Multiplatform und Compose Multiplatform
- **Google**: Für ML Kit und Android-Entwicklungstools
- **Apple**: Für Vision Framework und iOS-Entwicklungsplattform
- **Community**: Für Open-Source-Bibliotheken und Feedback

---

Mit ❤️ erstellt, um dabei zu helfen, die steigenden Kosten von allem zu verfolgen

_Geben Sie diesem Repo einen Stern ⭐, wenn Sie es hilfreich finden!_

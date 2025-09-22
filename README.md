# AllesTeurer 📱💰

> _"Alles wird teurer"_ - Eine native iOS-App, die dabei hilft, Preisänderungen durch Scannen von Kassenbons zu verfolgen

**AllesTeurer** ist eine datenschutzfokussierte native iOS-Anwendung, die lokale Kassenbon-Verarbeitung mit leistungsstarken Preisanalysen kombiniert. Die App verwendet SwiftUI für eine moderne Benutzeroberfläche, SwiftData für lokale Datenspeicherung, und Apples Vision Framework für präzise OCR-Texterkennung, um eine vollständige Preishistorie ohne Cloud-Abhängigkeiten zu erstellen.

## 🎯 Überblick

Da steigende Kosten jeden betreffen, ermöglicht diese native iOS-App den Nutzern:

- **🔒 Privacy-First**: Alle Daten bleiben auf Ihrem iPhone - keine Cloud-Services erforderlich
- **📊 Lokale Preisanalyse**: Verfolgen Sie Inflation und Ausgabenmuster mit On-Device-Berechnungen
- **🔍 Intelligente Produkterkennung**: Automatische Zuordnung ähnlicher Produkte über verschiedene Kassenbons
- **📈 Preistrends**: Visualisierung von Preisänderungen über Zeit und Händler hinweg mit Swift Charts
- **� Native iOS Experience**: Optimiert für iOS mit SwiftUI, SwiftData und Apple Intelligence Integration

## ✨ Funktionen

### Kernfunktionalität

- 📸 **Kassenbon-Scanning**: Native iOS Kamera-Integration mit AVFoundation
- 👁️ **OCR-Verarbeitung**: Apple Vision Framework für präzise deutsche Texterkennung
- 💾 **Lokale Datenspeicherung**: SwiftData für typsichere, native iOS-Datenpersistierung
- 🔗 **Produktabgleich**: Intelligente Algorithmen zur Erkennung gleicher Produkte

### Analysen & Einblicke

- 📊 **Preishistorie**: Verfolgen Sie Preisänderungen für jedes Produkt über Zeit
- 📈 **Interaktive Diagramme**: Swift Charts für native iOS-Trendvisualisierung
- 🏪 **Händlervergleiche**: Preisunterschiede zwischen verschiedenen Geschäften analysieren
- 💡 **Inflationsindikatoren**: Lokale Berechnungen von Preistrendstatistiken

### Native iOS Features

- 📱 **iOS Share Extension**: Kassenbon-Sharing von anderen Apps
- 🔍 **Spotlight Integration**: Produktsuche über iOS-Systemsuche
- 📋 **Widget Support**: Home Screen und Lock Screen Widgets für Ausgabenübersicht
- 🗣️ **Shortcuts Integration**: Siri Shortcuts für häufige Aktionen
- 🤖 **Apple Intelligence**: Intelligente Produktkategorisierung mit dem Natural Language Framework
- 🌙 **Dynamic Appearance**: Automatische Unterstützung für Light und Dark Mode
- ♿ **Accessibility**: VoiceOver und Dynamic Type Unterstützung

## 🏗️ Projektstruktur

```text
AllesTeurer/
├── Alles Teurer/            # Native iOS App
│   ├── Alles_TeurerApp.swift       # App Entry Point
│   ├── ContentView.swift           # Main SwiftUI View
│   ├── Item.swift                  # SwiftData Models
│   ├── Info.plist                  # App Configuration
│   ├── Alles_Teurer.entitlements  # App Capabilities
│   └── Assets.xcassets/            # App Assets and Icons
├── Alles Teurer.xcodeproj/  # Xcode Project
├── Alles TeurerTests/       # Unit Tests
├── Alles TeurerUITests/     # UI Tests
├── spec/                    # Anforderungen und Architektur-Dokumentation
├── docs/                    # Zusätzliche Dokumentation
└── README.md               # Projekt-Übersicht
```

## 🚀 Erste Schritte

### Voraussetzungen

**iOS-Entwicklung:**

- **Xcode 15.0+** mit Swift 5.9+
- **iOS 17.0+** als Mindest-Deployment-Version (für SwiftData)
- **macOS 13.0+** für Xcode-Entwicklung
- **Apple Developer Account** für Gerätetests und App Store Distribution

### Installation

1. **Repository klonen**

   ```bash
   git clone https://github.com/mpwg/AllesTeurer.git
   cd AllesTeurer
   ```

2. **iOS-Projekt öffnen**

   ```bash
   # Xcode-Projekt öffnen
   open "Alles Teurer.xcodeproj"
   ```

3. **App auf Gerät oder Simulator ausführen**

   - Zielgerät auswählen (iPhone/iPad oder Simulator)
   - ⌘+R drücken oder auf "Build and Run" klicken

### Erforderliche Berechtigungen

Die App benötigt folgende iOS-Berechtigungen:

- **Kamera-Zugriff**: Für Kassenbon-Scanning (NSCameraUsageDescription)
- **Fotobibliothek**: Für Import bestehender Bilder (NSPhotoLibraryUsageDescription)

## 🛠️ Entwicklungsworkflow

### Xcode-Entwicklung

```bash
# Projekt öffnen
open "Alles Teurer.xcodeproj"

# Tests ausführen
xcodebuild test -scheme "Alles Teurer" -destination "platform=iOS Simulator,name=iPhone 15"

# Build für Distribution
xcodebuild archive -scheme "Alles Teurer" -archivePath "AllesTeurer.xcarchive"
```

### Swift Package Manager

Das Projekt nutzt SwiftUI und SwiftData aus den iOS-System-Frameworks. Zusätzliche Dependencies können über Swift Package Manager hinzugefügt werden:

```swift
// In Package.swift
dependencies: [
    .package(url: "https://github.com/apple/swift-charts.git", from: "1.0.0")
]
```

## 🏛️ Architektur

### Kerntechnologien

- **SwiftUI**: Deklarative UI-Entwicklung mit nativer iOS-Performance
- **SwiftData**: Typsichere Datenpersistierung mit Core Data-Backend
- **Vision Framework**: Hochpräzise OCR-Texterkennung speziell für deutsche Texte
- **Swift Charts**: Native Datenvisualisierung für Preisanalysen
- **Async/Await**: Moderne asynchrone Programmierung
- **Observation Framework**: Reaktive Datenflüsse und UI-Updates

### Design-Pattern

- **MVVM**: Model-View-ViewModel-Architektur mit SwiftUI
- **Repository Pattern**: Datenbank-Zugriffsabstraktion über SwiftData
- **Use Cases**: Domain-Layer für Geschäftslogik-Kapselung
- **Dependency Injection**: Testbarkeit und modularer Code

### iOS-spezifische Integrationen

- **Vision Framework**: OCR-Texterkennung aus Kassenbon-Bildern
- **AVFoundation**: Kamera-Integration für Receipt-Scanning
- **CloudKit**: Optionale Private-Cloud-Synchronisation
- **Natural Language**: Intelligente Produktkategorisierung
- **Core Spotlight**: Systemweite Produktsuche

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
3. **Interaktive Charts erkunden** mit Swift Charts
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

### Release-System

AllesTeurer verwendet ein **tag-basiertes Release-System** für manuelle Kontrolle über Deployments:

- **🏷️ Beta Releases**: `git tag v1.0.0-beta.1` → TestFlight
- **🚀 Production Releases**: `git tag v1.0.0` → App Store
- **📋 Builds & Tests**: Branch pushes → nur Build und Test

Siehe [Tag-Based Releases Guide](docs/TAG_BASED_RELEASES.md) für detaillierte Anweisungen.
2. **Entwicklungsumgebung einrichten** (siehe [Installation](#installation))
3. **Unseren Coding-Standards folgen** (siehe `.github/instructions/`)
4. **Tests schreiben** für neue Features
5. **Pull Request einreichen** mit ausführlicher Beschreibung

### Xcode-Befehle für Entwicklung

```bash
# Tests vor Pull Request ausführen
xcodebuild test -scheme "Alles Teurer" -destination "platform=iOS Simulator,name=iPhone 15"

# SwiftLint für Code-Qualität (falls installiert)
swiftlint

# Build überprüfen
xcodebuild build -scheme "Alles Teurer" -destination "platform=iOS Simulator,name=iPhone 15"
```

## 📱 Systemanforderungen

### Minimum Requirements

**iOS:**

- iOS 17.0 oder neuer (für SwiftData-Unterstützung)
- iPhone 12 oder neuer / iPad (9. Generation) oder neuer
- 64-bit A-Series Prozessor (A12 Bionic oder neuer)
- 100MB freier Speicherplatz

### Empfohlene Spezifikationen

- **iPhone 14 oder neuer**: Optimierte Performance für Vision Framework
- **Gute Kamera**: Für optimale Kassenbon-Erkennung mit automatischem Fokus
- **Ausreichend Speicher**: 1GB+ für lokale Speicherung der Preishistorie
- **Neural Engine**: A12 Bionic oder neuer für bessere OCR-Performance

## 📞 Support & Community

- **🐛 Bug Reports**: [GitHub Issues](https://github.com/mpwg/AllesTeurer/issues)
- **💬 Diskussionen**: [GitHub Discussions](https://github.com/mpwg/AllesTeurer/discussions)
- **📖 Dokumentation**: [Projekt Wiki](https://github.com/mpwg/AllesTeurer/wiki)
- **� Roadmap**: [GitHub Projects](https://github.com/mpwg/AllesTeurer/projects)

## 🙏 Danksagungen

- **Apple**: Für SwiftUI, SwiftData, Vision Framework und die hervorragenden iOS-Entwicklungstools
- **Swift Community**: Für Open-Source-Swift-Pakete und Bibliotheken
- **iOS Developer Community**: Für Best Practices und kontinuierliches Lernen

---

Mit ❤️ erstellt, um dabei zu helfen, die steigenden Kosten von allem zu verfolgen

_Geben Sie diesem Repo einen Stern ⭐, wenn Sie es hilfreich finden!_

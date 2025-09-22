# Anforderungen - Native iOS AllesTeurer

## Funktionale Anforderungen

### Core Features (iOS Native)

- [ ] **Receipt Scanning Interface** - AVFoundation Kamera-Integration für Kassenbons
- [ ] **Local OCR Processing** - Vision Framework für deutsche Texterkennung
- [ ] **Product Matching** - Automatische Produktzuordnung mit lokalen Algorithmen
- [ ] **Price History Tracking** - Preisentwicklung mit SwiftData
- [ ] **Local Analytics** - Inflation/Ausgaben-Trends mit Swift Charts
- [ ] **Data Export** - CSV/JSON Export für eigene Analysen

### iOS-Specific Features

- [ ] **CloudKit Sync** - Geräte-übergreifende private Synchronisation
- [ ] **iOS Share Extension** - Receipt-Sharing von anderen Apps
- [ ] **Spotlight Integration** - Produkt-Suche über iOS Spotlight
- [ ] **Widget Support** - Home Screen und Lock Screen Widgets
- [ ] **Shortcuts Integration** - Siri Shortcuts für häufige Aktionen
- [ ] **Apple Intelligence** - Intelligente Produktkategorisierung
- [ ] **Dynamic Type Support** - Vollständige Accessibility-Unterstützung
- [ ] **VoiceOver Integration** - Screen Reader Kompatibilität

## Technische Komponenten

```text
Alles Teurer/
├── Models/                 # SwiftData models
│   ├── Receipt.swift       # Receipt entity
│   ├── Product.swift       # Product entity
│   └── PriceHistory.swift  # Price tracking
├── ViewModels/            # Observable ViewModels
│   ├── ScannerViewModel.swift
│   ├── ProductsViewModel.swift
│   └── AnalyticsViewModel.swift
├── Views/                 # SwiftUI views
│   ├── Scanner/           # Receipt scanning interface
│   ├── Products/          # Product list and details
│   ├── Analytics/         # Price charts and insights
│   └── Settings/          # App configuration
├── Services/              # Business logic
│   ├── OCRService.swift   # Vision Framework integration
│   ├── DataManager.swift  # SwiftData operations
│   └── PriceAnalyzer.swift # Analytics calculations
│       └── theme/           # Design system
├── androidMain/kotlin/
│   ├── ocr/                 # ML Kit OCR implementation
│   ├── camera/              # Android camera integration
└── Utils/                 # Extensions and helpers
```

## User Stories (iOS Native)

### 1. Receipt Scanning

**Als iOS-Nutzer** möchte ich einen Kassenbon fotografieren und automatisch alle Produkte und Preise extrahiert bekommen, damit ich meine Ausgaben einfach tracken kann.

**Akzeptanzkriterien:**

- [ ] Kamera öffnet sich direkt beim Tippen auf "Scan" mit AVFoundation
- [ ] Vision Framework erkennt deutschen Text mit hoher Präzision
- [ ] Automatische Extraktion von Produktnamen, Preisen, und Datum
- [ ] Manuelle Korrektur-Möglichkeit bei OCR-Fehlern mit SwiftUI Interface
- [ ] Receipt wird lokal in SwiftData-Datenbank gespeichert

### 2. Local Product Management

**Als iOS-Nutzer** möchte ich, dass die App automatisch ähnliche Produkte erkennt und deren Preisverlauf lokal tracked, ohne dass Daten an externe Server gesendet werden.

**Akzeptanzkriterien:**

- [ ] Fuzzy-Matching für ähnliche Produktnamen mit lokalen Swift Algorithmen
- [ ] Automatische Kategorisierung mit Natural Language Framework
- [ ] Preisverlauf-Charts mit Swift Charts
- [ ] Alle Daten bleiben auf dem iPhone (SwiftData lokal)

### 3. Privacy-First Analytics

**Als iOS-Nutzer** möchte ich Einblicke in meine Ausgaben und Inflation trends erhalten, ohne dass meine persönlichen Daten das Gerät verlassen.

**Akzeptanzkriterien:**

- [ ] Lokale Berechnung von Ausgaben-Trends mit Swift
- [ ] Inflation-Rate pro Kategorie mit interaktiven Charts
- [ ] Monatliche/wöchentliche Übersichten in nativer iOS UI
- [ ] Export-Funktion für eigene Analysen (CSV/JSON)

## Technische Anforderungen

### Performance Requirements

- [ ] App-Start unter 1.5 Sekunden auf iPhone 12 oder neuer
- [ ] OCR-Verarbeitung unter 3 Sekunden pro Receipt mit Vision Framework
- [ ] Smooth 60fps Animationen (Compose Multiplatform)
- [ ] SQLDelight Abfragen unter 100ms

### Privacy & Security Requirements

- [ ] Alle Daten bleiben lokal auf dem Gerät
- [ ] Platform-specific sync nur optional (CloudKit iOS / Google Drive Android)
- [ ] Keine Third-Party Analytics
- [ ] Vollständige Daten-Exportierbarkeit
- [ ] DSGVO-konforme Datenlöschung

### Platform Integration Requirements

#### iOS Requirements

- [ ] iOS 15.0+ Mindest-Version (für Compose Multiplatform)
- [ ] VoiceOver Accessibility Support
- [ ] Dynamic Type Support
- [ ] Dark Mode Support
- [ ] Focus/Control Center Integration

#### Android Requirements

- [ ] Android API 24+ (Android 7.0+) Mindest-Version
- [ ] TalkBack Accessibility Support
- [ ] Material You Dynamic Colors (Android 12+)
- [ ] Dark Mode Support
- [ ] App Shortcuts Integration

## Offline-First Funktionalität

### Was funktioniert offline

- [ ] Receipt Scanning und OCR (platform-specific)
- [ ] Alle Analytics und Charts (shared business logic)
- [ ] Produkt-Suche und -Verwaltung (SQLDelight lokal)
- [ ] Data Export
- [ ] Vollständige App-Funktionalität ohne Internet

### Platform-Specific Sync (Optional)

- [ ] iOS: CloudKit Sync zwischen iPhone/iPad/Mac
- [ ] Android: Google Drive Backup und Restore
- [ ] Konflikterkennung und -lösung
- [ ] Benutzer kann Sync deaktivieren
- [ ] Lokale Backups zusätzlich zu Cloud-Sync

## Future Backend Integration (Phase 2)

### Vorbereitung für mögliche Backend-Erweiterung

- [ ] Netzwerk-Layer Interface in shared module definiert
- [ ] Offline-First Architektur beibehalten
- [ ] Optional: Preisvergleich mit externen Quellen
- [ ] Optional: Community Features
- [ ] Migration von lokal zu hybrid möglich

## Dependencies & Constraints

### KMP Dependencies

- Kotlin Multiplatform 2.2.20+
- Gradle 9+
- Compose Multiplatform
- SQLDelight (multiplatform database)
- Kotlinx Serialization
- Ktor (optional backend client)

### Platform-Specific Dependencies

#### iOS

- Vision Framework (OCR)
- CloudKit (optional sync)
- AVFoundation (Kamera)

#### Android

- ML Kit (OCR)
- CameraX (Kamera)
- Google Drive API (optional sync)

### Entwicklungs-Constraints

- Kotlin 2.2.20+ (K2 compiler)
- Gradle 9+ with Kotlin DSL
- IntelliJ IDEA / Android Studio
- iOS 15+ / Android 7+ Deployment Targets
- Compose Multiplatform UI Architecture

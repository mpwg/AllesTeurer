# Anforderungen - iOS-First AllesTeurer

## Funktionale Anforderungen

### Core Features (iOS-First)

- [ ] **Receipt Scanning Interface** - Kamera-Integration für Kassenbons
- [ ] **Local OCR Processing** - Vision Framework Texterkennung
- [ ] **Product Matching** - Automatische Produktzuordnung (lokal)
- [ ] **Price History Tracking** - Preisentwicklung per Core Data
- [ ] **Local Analytics** - Inflation/Ausgaben-Trends ohne Backend
- [ ] **Data Export** - CSV/PDF Export für eigene Analysen

### iOS-Spezifische Features

- [ ] **CloudKit Sync** - Geräte-übergreifende Synchronisation
- [ ] **iOS Share Extension** - Receipt-Sharing von anderen Apps
- [ ] **Spotlight Integration** - Produkt-Suche über iOS Spotlight
- [ ] **Widget Support** - Home Screen Widgets für Ausgaben-Übersicht
- [ ] **Shortcuts Integration** - Siri Shortcuts für häufige Aktionen

## Technische Komponenten

```
AllesTeurer.xcodeproj/
├── Models/
│   ├── CoreData/
│   │   └── AllesTeurer.xcdatamodeld
│   └── ViewModels/
│       ├── ReceiptScannerViewModel.swift
│       ├── ProductListViewModel.swift
│       └── AnalyticsViewModel.swift
├── Services/
│   ├── OCRService.swift           // Vision Framework
│   ├── DataManager.swift          // Core Data Manager
│   ├── ProductMatcher.swift       // Local Matching Logic
│   ├── PriceAnalyzer.swift        // Local Analytics
│   └── CloudKitService.swift      // Device Sync
└── Views/
    ├── Scanner/
    ├── Products/
    ├── Analytics/
    └── Settings/
```

## User Stories (iOS-Fokussiert)

### 1. Receipt Scanning

**Als iOS-Nutzer** möchte ich einen Kassenbon fotografieren und automatisch alle Produkte und Preise extrahiert bekommen, damit ich meine Ausgaben einfach tracken kann.

**Akzeptanzkriterien:**

- [ ] Kamera öffnet sich direkt beim Tippen auf "Scan"
- [ ] Vision Framework erkennt deutschen Text auf Kassenbons
- [ ] Automatische Extraktion von Produktnamen, Preisen, und Datum
- [ ] Manuelle Korrektur-Möglichkeit bei OCR-Fehlern
- [ ] Receipt wird lokal in Core Data gespeichert

### 2. Local Product Management

**Als Nutzer** möchte ich, dass die App automatisch ähnliche Produkte erkennt und deren Preisverlauf lokal tracked, ohne dass Daten an externe Server gesendet werden.

**Akzeptanzkriterien:**

- [ ] Fuzzy-Matching für ähnliche Produktnamen
- [ ] Automatische Kategorisierung (Lebensmittel, etc.)
- [ ] Preisverlauf-Charts mit Swift Charts
- [ ] Alle Daten bleiben auf dem Gerät

### 3. Privacy-First Analytics

**Als Nutzer** möchte ich Einblicke in meine Ausgaben und Inflation trends erhalten, ohne dass meine persönlichen Daten das Gerät verlassen.

**Akzeptanzkriterien:**

- [ ] Lokale Berechnung von Ausgaben-Trends
- [ ] Inflation-Rate pro Kategorie
- [ ] Monatliche/wöchentliche Übersichten
- [ ] Export-Funktion für eigene Analysen

## Technische Anforderungen

### Performance Requirements

- [ ] App-Start unter 2 Sekunden
- [ ] OCR-Verarbeitung unter 5 Sekunden pro Receipt
- [ ] Smooth 60fps Animationen
- [ ] Core Data Abfragen unter 100ms

### Privacy & Security Requirements

- [ ] Alle Daten bleiben lokal auf dem Gerät
- [ ] CloudKit nur für Geräte-Sync (optional)
- [ ] Keine Third-Party Analytics
- [ ] Vollständige Daten-Exportierbarkeit
- [ ] DSGVO-konforme Datenlöschung

### iOS Integration Requirements

- [ ] iOS 17.0+ Mindest-Version
- [ ] VoiceOver Accessibility Support
- [ ] Dynamic Type Support
- [ ] Dark Mode Support
- [ ] Focus/Control Center Integration

## Offline-First Funktionalität

### Was funktioniert offline:

- [ ] Receipt Scanning und OCR
- [ ] Alle Analytics und Charts
- [ ] Produkt-Suche und -Verwaltung
- [ ] Data Export
- [ ] Vollständige App-Funktionalität

### CloudKit Sync (Optional):

- [ ] Sync zwischen iPhone/iPad/Mac
- [ ] Konflikterkennung und -lösung
- [ ] Benutzer kann Sync deaktivieren
- [ ] Lokale Backups zusätzlich zu CloudKit

## Future Backend Integration (Phase 2)

### Vorbereitung für mögliche Backend-Erweiterung:

- [ ] Netzwerk-Layer Interface definiert
- [ ] Offline-First Architektur beibehalten
- [ ] Optional: Preisvergleich mit externen Quellen
- [ ] Optional: Community Features
- [ ] Migration von lokal zu hybrid möglich

## Dependencies & Constraints

### iOS Dependencies:

- Vision Framework (OCR)
- Core Data (lokale Speicherung)
- CloudKit (optionale Sync)
- Swift Charts (Visualisierung)
- AVFoundation (Kamera)

### Entwicklungs-Constraints:

- Swift 6.0+ (Strict Concurrency)
- Xcode 15+
- iOS 17+ Deployment Target
- SwiftUI + Combine Architecture

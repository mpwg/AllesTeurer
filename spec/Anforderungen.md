# Anforderungen - Kotlin Multiplatform AllesTeurer

## Funktionale Anforderungen

### Core Features (Multiplatform)

- [ ] **Receipt Scanning Interface** - Kamera-Integration für Kassenbons (iOS/Android)
- [ ] **Local OCR Processing** - Vision Framework (iOS) / ML Kit (Android)
- [ ] **Product Matching** - Automatische Produktzuordnung (shared logic)
- [ ] **Price History Tracking** - Preisentwicklung per SQLDelight
- [ ] **Local Analytics** - Inflation/Ausgaben-Trends ohne Backend
- [ ] **Data Export** - CSV/PDF Export für eigene Analysen

### Platform-Specific Features

#### iOS Features

- [ ] **CloudKit Sync** - Geräte-übergreifende Synchronisation (iOS-only)
- [ ] **iOS Share Extension** - Receipt-Sharing von anderen Apps
- [ ] **Spotlight Integration** - Produkt-Suche über iOS Spotlight
- [ ] **Widget Support** - Home Screen Widgets für Ausgaben-Übersicht
- [ ] **Shortcuts Integration** - Siri Shortcuts für häufige Aktionen

#### Android Features

- [ ] **Google Drive Backup** - Cloud-Synchronisation (Android-only)
- [ ] **Share Intent** - Receipt-Sharing von anderen Apps
- [ ] **App Shortcuts** - Dynamische Shortcuts für häufige Aktionen
- [ ] **Widget Support** - Home Screen Widgets für Ausgaben-Übersicht
- [ ] **Adaptive Icon** - Android 8+ adaptive Icons

## Technische Komponenten

```
apps/composeApp/src/
├── commonMain/kotlin/
│   ├── data/
│   │   ├── local/           # SQLDelight database
│   │   ├── repository/      # Repository implementations
│   │   └── models/          # Data models (@Serializable)
│   ├── domain/
│   │   ├── models/          # Domain entities
│   │   ├── repository/      # Repository interfaces
│   │   └── usecase/         # Business logic use cases
│   ├── presentation/
│   │   ├── viewmodel/       # Shared ViewModels
│   │   └── state/           # UI state classes
│   └── ui/
│       ├── screens/         # Compose screens
│       ├── components/      # Reusable UI components
│       └── theme/           # Design system
├── androidMain/kotlin/
│   ├── ocr/                 # ML Kit OCR implementation
│   ├── camera/              # Android camera integration
│   └── platform/           # Android-specific utilities
├── iosMain/kotlin/
│   ├── ocr/                 # Vision Framework OCR
│   ├── camera/              # iOS camera integration
│   └── platform/           # iOS-specific utilities
└── commonMain/sqldelight/
    └── database/           # SQL schema and queries
```

## User Stories (Multiplatform)

### 1. Receipt Scanning

**Als Nutzer** möchte ich einen Kassenbon fotografieren und automatisch alle Produkte und Preise extrahiert bekommen, damit ich meine Ausgaben einfach tracken kann.

**Akzeptanzkriterien:**

- [ ] Kamera öffnet sich direkt beim Tippen auf "Scan" (iOS/Android)
- [ ] Vision Framework (iOS) / ML Kit (Android) erkennt deutschen Text
- [ ] Automatische Extraktion von Produktnamen, Preisen, und Datum
- [ ] Manuelle Korrektur-Möglichkeit bei OCR-Fehlern
- [ ] Receipt wird lokal in SQLDelight-Datenbank gespeichert

### 2. Local Product Management

**Als Nutzer** möchte ich, dass die App automatisch ähnliche Produkte erkennt und deren Preisverlauf lokal tracked, ohne dass Daten an externe Server gesendet werden.

**Akzeptanzkriterien:**

- [ ] Fuzzy-Matching für ähnliche Produktnamen (shared Kotlin logic)
- [ ] Automatische Kategorisierung (Lebensmittel, etc.)
- [ ] Preisverlauf-Charts mit Compose Charts
- [ ] Alle Daten bleiben auf dem Gerät (SQLDelight lokal)

### 3. Privacy-First Analytics

**Als Nutzer** möchte ich Einblicke in meine Ausgaben und Inflation trends erhalten, ohne dass meine persönlichen Daten das Gerät verlassen.

**Akzeptanzkriterien:**

- [ ] Lokale Berechnung von Ausgaben-Trends (shared business logic)
- [ ] Inflation-Rate pro Kategorie
- [ ] Monatliche/wöchentliche Übersichten
- [ ] Export-Funktion für eigene Analysen

## Technische Anforderungen

### Performance Requirements

- [ ] App-Start unter 2 Sekunden (Android/iOS)
- [ ] OCR-Verarbeitung unter 5 Sekunden pro Receipt
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

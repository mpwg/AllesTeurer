# Alles Teurer 📱💰

> *"Alles wird teurer"* - Eine iOS-App, die Ihnen hilft, Preisänderungen durch das Scannen von Kassenbons zu verfolgen

**Alles Teurer** ist eine intelligente iOS-Anwendung, die Apple Intelligence und das Vision Framework verwendet, um automatisch Produktinformationen aus Kassenbon-Fotos zu extrahieren und eine umfassende Preishistorie-Datenbank zu erstellen, um Inflation und Ausgabenmuster zu verfolgen.

## 🎯 Überblick

Da steigende Kosten alle betreffen, ermöglicht diese App den Nutzern:
- **Preisänderungen verfolgen**: Überwachen Sie, wie sich Produktpreise über die Zeit entwickeln
- **Intelligente Kassenbon-Verarbeitung**: Automatische Extraktion von Händlernamen, Daten und aufgelisteten Käufen
- **Visuelle Analysen**: Sehen Sie Preistrends durch interaktive Diagramme und Grafiken
- **Ausgaben-Einblicke**: Erhalten Sie detaillierte Statistiken über Ihre Kaufmuster

## ✨ Funktionen

### Kernfunktionalität
- 📸 **Kassenbon-Scanning**: Kamera-Integration zum Aufnehmen von Kassenbon-Fotos
- 🧠 **Apple Intelligence**: Automatische Datenextraktion mit Vision und Natural Language Frameworks
- 💾 **Intelligente Speicherung**: Core Data-Integration für zuverlässige lokale Datenpersistierung
- 📊 **Preishistorie**: Verfolgen Sie Preisänderungen für Produkte bei verschiedenen Händlern und Daten

### Analysen & Einblicke
- 📈 **Interaktive Diagramme**: Visualisieren Sie Preistrends mit Swift Charts
- 🔍 **Produktabgleich**: Intelligente Algorithmen zur Verknüpfung ähnlicher Produkte über verschiedene Kassenbons
- 📋 **Statistische Analyse**: Durchschnittspreise, Volatilitätsmessungen und Inflationsindikatoren
- 🏪 **Händlervergleich**: Preise zwischen verschiedenen Einzelhändlern vergleichen

### Benutzererfahrung
- 🎨 **Moderne SwiftUI-Oberfläche**: Sauberes, intuitives Design nach iOS-Design-Richtlinien
- 🌙 **Dark Mode-Unterstützung**: Vollständige Unterstützung für helle und dunkle Darstellungen
- ♿ **Barrierefreiheit**: VoiceOver-Unterstützung und Dynamic Type-Kompatibilität
- 🔔 **Preiswarnungen**: Benachrichtigungen bei erheblichen Preisänderungen

## 🏗️ Projektstruktur

```
AllesTeurer/
├── Models/              # Core Data-Modelle und Entities
├── Views/               # SwiftUI-Views und UI-Komponenten
├── Services/            # Geschäftslogik und Datenverarbeitung
├── Utils/               # Hilfsfunktionen und Erweiterungen
└── Resources/           # Assets, Lokalisierung und Konfiguration
```

## 🚀 Erste Schritte

### Voraussetzungen
- **Xcode 26.0+**
- **iOS 26.0+** (Ziel-Deployment)
- **Apple Developer Account** (für Gerätetests)

### Installation

1. **Repository klonen**
   ```bash
   git clone https://github.com/mpwg/AllesTeurer.git
   cd AllesTeurer
   ```

2. **In Xcode öffnen**
   ```bash
   open AllesTeurer.xcodeproj
   ```

3. **Bundle Identifier konfigurieren**
   - Bundle Identifier auf Ihren eigenen ändern: `com.yourdomain.allesteurer`
   - Ihr Entwicklungsteam in den Projekteinstellungen auswählen

4. **Erstellen und Ausführen**
   - Ihr Zielgerät oder Simulator auswählen
   - `Cmd+R` drücken zum Erstellen und Ausführen

### Erforderliche Berechtigungen
Die App benötigt folgende Berechtigungen:
- **Kamera-Zugriff**: Zum Scannen von Kassenbon-Fotos
- **Fotobibliothek**: Zum Importieren vorhandener Kassenbon-Bilder (optional)

## 🛠️ Entwicklungsroadmap

Unsere Entwicklung folgt einem strukturierten Meilenstein-Ansatz:

### 📋 [Meilenstein 1: Grundinfrastruktur](https://github.com/mpwg/AllesTeurer/milestone/1)
- iOS-Projekt-Setup mit SwiftUI
- Core Data-Modelldefinitionen
- Konfiguration der Abhängigkeitsverwaltung

### 📱 [Meilenstein 2: Kassenbon-Verarbeitung](https://github.com/mpwg/AllesTeurer/milestone/2)
- Kamera-Integration und Fotoaufnahme
- Vision Framework für Texterkennung
- Apple Intelligence-Datenextraktion
- Foto-Import-Funktionalität

### 💾 [Meilenstein 3: Datenverwaltung](https://github.com/mpwg/AllesTeurer/milestone/3)
- Implementierung von Core Data-Operationen
- Produktabgleichs-Algorithmen
- Datenvalidierung und -bereinigung

### 📊 [Meilenstein 4: Analytics-Engine](https://github.com/mpwg/AllesTeurer/milestone/4)
- Preishistorie-Verfolgung
- Diagramm-Visualisierung mit Swift Charts
- Statistische Analyse-Berechnungen
- Analytics-Dashboard

### 🎨 [Meilenstein 5: Benutzeroberfläche](https://github.com/mpwg/AllesTeurer/milestone/5)
- Hauptnavigation-Implementierung
- Kassenbon-Scanning-Oberfläche
- Verlaufs- und Produktverwaltungsansichten
- Einstellungen und Präferenzen

### 🧪 [Meilenstein 6: Testen & Verfeinerung](https://github.com/mpwg/AllesTeurer/milestone/6)
- Umfassende Test-Suite
- Fehlerbehandlung und Benutzerfeedback
- Leistungsoptimierung
- App Store-Vorbereitung

## 🏛️ Architektur

### Kerntechnologien
- **SwiftUI**: Modernes deklaratives UI-Framework
- **Core Data**: Lokale Datenpersistierung und -verwaltung
- **Vision Framework**: Optische Zeichenerkennung (OCR)
- **Natural Language**: Textverarbeitung und Entity-Extraktion
- **Swift Charts**: Datenvisualisierung und interaktive Grafiken
- **AVFoundation**: Kamera-Integration und Medienaufnahme

### Design-Pattern
- **MVVM**: Model-View-ViewModel-Architektur
- **Repository Pattern**: Datenbank-Zugriffsabstraktion
- **Service Layer**: Trennung der Geschäftslogik
- **Dependency Injection**: Testbarer und modularer Code

## 📖 Nutzung

### Ihren ersten Kassenbon scannen
1. **App öffnen** und auf den "Scannen"-Tab tippen
2. **Kamera auf einen Kassenbon richten** und den Aufnahmeknopf drücken
3. **Extrahierte Daten überprüfen** - die App erkennt automatisch:
   - Händlernamen
   - Kaufdatum
   - Einzelne Artikel mit Preisen
4. **Kassenbon speichern** - Daten werden lokal auf Ihrem Gerät gespeichert

### Preishistorie anzeigen
1. **Zum "Produkte"-Tab navigieren**
2. **Beliebiges Produkt auswählen**, um dessen Preishistorie zu sehen
3. **Interaktive Diagramme erkunden**, die Preistrends über die Zeit zeigen
4. **Preise vergleichen** zwischen verschiedenen Händlern

### Analytics-Dashboard
1. **"Analytics"-Tab besuchen** für Einblicke
2. **Ausgabenmuster anzeigen** nach Kategorie und Händler
3. **Inflationsindikatoren verfolgen** für Ihre Käufe
4. **Preiswarnungen einrichten** für erhebliche Änderungen

## 🤝 Mitwirken

Wir begrüßen Beiträge! So können Sie helfen:

1. **Prüfen Sie unsere [Issues](https://github.com/mpwg/AllesTeurer/issues)** für Aufgaben, die Aufmerksamkeit brauchen
2. **Repository forken** und einen Feature-Branch erstellen
3. **Unserem User-Stories-Format folgen** für neue Funktionen
4. **Tests schreiben** für Ihre Beiträge
5. **Pull Request einreichen** mit einer klaren Beschreibung

### Entwicklungssetup
```bash
# Abhängigkeiten installieren (falls Swift Package Manager verwendet wird)
# Abhängigkeiten werden automatisch von Xcode aufgelöst

# Tests ausführen
xcodebuild test -scheme AllesTeurer -destination 'platform=iOS Simulator,name=iPhone 15'

# Code-Formatierung (falls SwiftFormat verwendet wird)
swiftformat .
```

## 📄 Datenschutz & Daten

- **Nur lokale Speicherung**: Alle Daten verbleiben auf Ihrem Gerät
- **Keine Cloud-Synchronisation**: Wir speichern Ihre Kassenbons oder Kaufdaten nicht
- **Minimale Berechtigungen**: Nur Kamera-Zugriff zum Scannen
- **Transparente Verarbeitung**: Open-Source-Implementierung

## 📱 Systemanforderungen

- **iOS 26.0** oder neuer
- **iPhone** (optimiert für alle Größen)
- **Kamera** erforderlich für Kassenbon-Scanning
- **50MB** Speicherplatz

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/mpwg/AllesTeurer/issues)
- **Diskussionen**: [GitHub Discussions](https://github.com/mpwg/AllesTeurer/discussions)
- **Dokumentation**: Schauen Sie in unser [Wiki](https://github.com/mpwg/AllesTeurer/wiki)

## 📜 Lizenz

TBD

## 🙏 Danksagungen

- **Apple** für die Vision- und Natural Language-Frameworks
- **Swift Community** für exzellente Open-Source-Bibliotheken
- **Mitwirkende**, die helfen, dieses Projekt besser zu machen

---

**Mit ❤️ erstellt, um dabei zu helfen, die steigenden Kosten von allem zu verfolgen**

*Geben Sie diesem Repo einen Stern ⭐, wenn Sie es hilfreich finden!*

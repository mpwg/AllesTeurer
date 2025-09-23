//
//  Models.swift
//  Alles-Teurer
//
//  Created by AI Agent on 22.09.25.
//

import CoreLocation
import Foundation
import SwiftData

// MARK: - Enums

/// ProduktKategorie - Kategorien für Produkte
enum ProduktKategorie: String, CaseIterable, Codable {
    case lebensmittel = "Lebensmittel"
    case getraenke = "Getränke"
    case haushaltsprodukte = "Haushaltsprodukte"
    case koerperpflege = "Körperpflege"
    case tiernahrung = "Tiernahrung"
    case baby = "Baby & Kind"
    case tiefkuehlung = "Tiefkühlung"
    case suessigkeiten = "Süßigkeiten"
    case backwaren = "Backwaren"
    case molkereiprodukte = "Molkereiprodukte"
    case fleisch = "Fleisch & Wurst"
    case obst = "Obst & Gemüse"
    case sonstiges = "Sonstiges"

    var symbolName: String {
        switch self {
        case .lebensmittel: return "basket"
        case .getraenke: return "cup.and.saucer"
        case .haushaltsprodukte: return "house"
        case .koerperpflege: return "heart"
        case .tiernahrung: return "pawprint"
        case .baby: return "figure.child"
        case .tiefkuehlung: return "snowflake"
        case .suessigkeiten: return "gift"
        case .backwaren: return "birthday.cake"
        case .molkereiprodukte: return "drop"
        case .fleisch: return "fork.knife"
        case .obst: return "leaf"
        case .sonstiges: return "questionmark.circle"
        }
    }
}

/// GeschaeftTyp - Verschiedene Geschäftstypen
enum GeschaeftTyp: String, CaseIterable, Codable {
    case supermarkt = "Supermarkt"
    case discounter = "Discounter"
    case drogerie = "Drogerie"
    case bioladen = "Bio-Laden"
    case metzgerei = "Metzgerei"
    case baeckerei = "Bäckerei"
    case tankstelle = "Tankstelle"
    case kiosk = "Kiosk"
    case markt = "Wochenmarkt"
    case online = "Online-Shop"
    case sonstiges = "Sonstiges"

    var symbolName: String {
        switch self {
        case .supermarkt: return "cart"
        case .discounter: return "tag"
        case .drogerie: return "heart.circle"
        case .bioladen: return "leaf.circle"
        case .metzgerei: return "fork.knife.circle"
        case .baeckerei: return "birthday.cake"
        case .tankstelle: return "fuelpump"
        case .kiosk: return "storefront"
        case .markt: return "tent"
        case .online: return "network"
        case .sonstiges: return "building.2"
        }
    }
}

// MARK: - SwiftData Models

/// Rechnung - Repräsentiert eine gescannte Rechnung
@Model
final class Rechnung {
    @Attribute(.unique) var id: UUID
    var geschaeftsname: String
    var geschaeftsadresse: String?
    var scanDatum: Date
    var gesamtbetrag: Decimal

    @Relationship(deleteRule: .cascade)
    var artikel: [RechnungsArtikel]

    @Relationship(inverse: \Geschaeft.rechnungen)
    var geschaeft: Geschaeft?

    /// OCR-Vertrauensgrad (0.0 - 1.0)
    var ocrVertrauen: Double
    /// Roher OCR-Text für Debugging
    var roherOCRText: String?
    /// Originalbild des Kassenbons (optional)
    var bildDaten: Data?
    /// Kassenbon-Nummer falls verfügbar
    var kassenbonNummer: String?
    /// Steuerinformationen
    var mehrwertsteuer: Decimal?

    init(geschaeftsname: String, scanDatum: Date = .now, gesamtbetrag: Decimal) {
        self.id = UUID()
        self.geschaeftsname = geschaeftsname
        self.scanDatum = scanDatum
        self.gesamtbetrag = gesamtbetrag
        self.artikel = []
        self.ocrVertrauen = 0.0
        self.mehrwertsteuer = 0.0
    }
}

/// KassenbonArtikel - Einzelner Artikel auf einem Kassenbon
@Model
final class RechnungsArtikel {
    @Attribute(.unique) var id: UUID
    var name: String
    var menge: Int
    var einzelpreis: Decimal
    var gesamtpreis: Decimal

    @Relationship(inverse: \Rechnung.artikel)
    var rechnung: Rechnung?

    @Relationship(inverse: \Produkt.rechnungsArtikel)
    var produkt: Produkt?

    /// Einheit (z.B. "kg", "Stück", "Liter")
    var einheit: String?
    /// Pfand falls zutreffend
    var pfand: Decimal?
    /// Rabatt falls zutreffend
    var rabatt: Decimal?

    init(name: String, menge: Int = 1, einzelpreis: Decimal, gesamtpreis: Decimal) {
        self.id = UUID()
        self.name = name
        self.menge = menge
        self.einzelpreis = einzelpreis
        self.gesamtpreis = gesamtpreis
        self.einheit = "Stück"
        self.pfand = 0.0
        self.rabatt = 0.0
    }
}

/// Produkt - Repräsentiert ein Produkt mit Preisverlauf
@Model
final class Produkt {
    @Attribute(.unique) var id: UUID
    var name: String
    var kategorie: ProduktKategorie
    var marke: String?
    var barcode: String?

    @Relationship(deleteRule: .cascade)
    var preisverlauf: [PreisEintrag]

    @Relationship(deleteRule: .nullify)
    var rechnungsArtikel: [RechnungsArtikel]

    var letzteAktualisierung: Date
    /// Durchschnittspreis basierend auf letzten 10 Einkäufen
    var durchschnittspreis: Decimal?
    /// Niedrigster jemals erfasster Preis
    var niedrigsterPreis: Decimal?
    /// Höchster jemals erfasster Preis
    var hoechsterPreis: Decimal?

    init(name: String, kategorie: ProduktKategorie) {
        self.id = UUID()
        self.name = name
        self.kategorie = kategorie
        self.preisverlauf = []
        self.rechnungsArtikel = []
        self.letzteAktualisierung = .now
    }

    /// Berechnet die Inflation basierend auf dem ersten und letzten Preis
    func inflationsrate() -> Decimal {
        guard preisverlauf.count >= 2,
            let ersterPreis = preisverlauf.first?.preis,
            let letzterPreis = preisverlauf.last?.preis,
            ersterPreis > 0
        else { return 0 }

        // Use pure Decimal arithmetic to maintain precision
        let differenz = letzterPreis - ersterPreis
        let inflation = (differenz / ersterPreis) * 100
        return inflation
    }

    /// Aktualisiert die Preisstatistiken
    func aktualisierePreisstatistiken() {
        guard !preisverlauf.isEmpty else { return }

        let preise = preisverlauf.map { $0.preis }
        self.niedrigsterPreis = preise.min()
        self.hoechsterPreis = preise.max()

        // Durchschnitt der letzten 10 Preise
        let letztePreise = Array(preise.suffix(10))
        let summe = letztePreise.reduce(0, +)
        self.durchschnittspreis = summe / Decimal(letztePreise.count)

        self.letzteAktualisierung = .now
    }
}

/// PreisEintrag - Einzelner Preiseintrag für ein Produkt
@Model
final class PreisEintrag {
    @Attribute(.unique) var id: UUID
    var preis: Decimal
    var datum: Date
    var geschaeftsname: String

    @Relationship(inverse: \Produkt.preisverlauf)
    var produkt: Produkt?

    /// Menge auf die sich der Preis bezieht
    var menge: Decimal
    /// Einheit der Menge
    var einheit: String

    init(
        preis: Decimal, datum: Date = .now, geschaeftsname: String, menge: Decimal = 1,
        einheit: String = "Stück"
    ) {
        self.id = UUID()
        self.preis = preis
        self.datum = datum
        self.geschaeftsname = geschaeftsname
        self.menge = menge
        self.einheit = einheit
    }
}

/// Geschaeft - Repräsentiert ein Geschäft/einen Laden
@Model
final class Geschaeft {
    @Attribute(.unique) var id: UUID
    var name: String
    var typ: GeschaeftTyp
    var adresse: String?
    var plz: String?
    var stadt: String?

    @Relationship(deleteRule: .cascade)
    var rechnungen: [Rechnung]

    /// Koordinaten für Kartenansicht
    var breitengrad: Double?
    var laengengrad: Double?

    /// Durchschnittliches Preisniveau (berechnet aus allen Rechnungen)
    var durchschnittlicherWarenkorb: Decimal?
    /// Anzahl der gescannten Rechnungen
    var anzahlRechnungen: Int
    /// Letzter Besuch
    var letzterBesuch: Date?
    /// Notizen zum Geschäft
    var notizen: String?

    init(name: String, typ: GeschaeftTyp = .supermarkt) {
        self.id = UUID()
        self.name = name
        self.typ = typ
        self.rechnungen = []
        self.anzahlRechnungen = 0
    }

    /// Berechnet das durchschnittliche Preisniveau basierend auf allen Rechnungen
    func berechneDurchschnittlicherWarenkorb() {
        guard !rechnungen.isEmpty else {
            self.durchschnittlicherWarenkorb = 0
            return
        }

        let gesamtbetrag = rechnungen.reduce(Decimal(0)) { $0 + $1.gesamtbetrag }
        self.durchschnittlicherWarenkorb = gesamtbetrag / Decimal(rechnungen.count)
        self.anzahlRechnungen = rechnungen.count
        self.letzterBesuch = rechnungen.map { $0.scanDatum }.max()
    }

    /// Entfernung zu gegebenen Koordinaten in Kilometern
    func entfernungZu(breitengrad: Double, laengengrad: Double) -> Double? {
        guard let eigenBreitengrad = self.breitengrad,
            let eigenLaengengrad = self.laengengrad
        else { return nil }

        let eigenStandort = CLLocation(latitude: eigenBreitengrad, longitude: eigenLaengengrad)
        let zielStandort = CLLocation(latitude: breitengrad, longitude: laengengrad)

        return eigenStandort.distance(from: zielStandort) / 1000.0  // in Kilometern
    }
}

// MARK: - OCR Types (Shared between OCRService and ViewModels)

/// OCR processing result data
struct OCRResult: Sendable {
    let recognizedText: String
    let confidence: Double
    let detectedItems: [ArtikelData]
    let storeName: String?
    let totalAmount: Decimal?
}

/// Article data from OCR processing
struct ArtikelData: Sendable {
    let name: String
    let preis: Decimal
    let menge: Int

    init(name: String, preis: Decimal, menge: Int = 1) {
        self.name = name
        self.preis = preis
        self.menge = menge
    }
}

// MARK: - Sendable Transfer Objects for MVVM

/// Sendable representation of a Receipt for ViewModels
struct ReceiptListItem: Sendable {
    let id: UUID
    let storeName: String
    let storeAddress: String?
    let scanDate: Date
    let totalAmount: Double  // Converted from Decimal
    let itemCount: Int
    let confidence: Double
    let receiptNumber: String?
}

/// Sendable representation of a Receipt Item for ViewModels
struct ReceiptItemData: Sendable {
    let id: UUID
    let name: String
    let quantity: Int
    let unitPrice: Double  // Converted from Decimal
    let totalPrice: Double  // Converted from Decimal
    let category: ProduktKategorie
}

/// Sendable representation of a Store for ViewModels
struct StoreData: Sendable {
    let id: UUID
    let name: String
    let address: String?
    let type: GeschaeftTyp
    let receiptCount: Int
    let totalSpent: Double  // Converted from Decimal
}

/// Sendable representation of spending analytics
struct SpendingAnalytics: Sendable {
    let totalSpent: Double
    let averageReceiptValue: Double
    let receiptCount: Int
    let spendingByStore: [StoreSpending]
    let spendingByCategory: [CategorySpending]
    let monthlyTrends: [MonthlySpending]
}

struct StoreSpending: Sendable {
    let storeName: String
    let amount: Double
    let receiptCount: Int
}

struct CategorySpending: Sendable {
    let category: ProduktKategorie
    let amount: Double
    let itemCount: Int
}

struct MonthlySpending: Sendable {
    let month: Date
    let amount: Double
    let receiptCount: Int
}

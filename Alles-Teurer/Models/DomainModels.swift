//
//  DomainModels.swift
//  Alles-Teurer
//
//  Created by Copilot on 25.09.25.
//

import Foundation
import SwiftData

// MARK: - Core Domain Models (SwiftData 2.0)

@Model
final class Rechnung {
    @Attribute(.unique) var id: UUID
    var geschaeftsname: String
    var ort: String?
    var scanDatum: Date
    var gesamtbetrag: Decimal

    @Relationship(deleteRule: .cascade)
    var artikel: [RechnungsArtikel]

    @Relationship(inverse: \Geschaeft.rechnungen)
    var geschaeft: Geschaeft?

    var ocrConfidence: Double
    var rawOCRText: String?
    var bildDaten: Data?

    init(geschaeftsname: String, scanDatum: Date = .now, gesamtbetrag: Decimal) {
        self.id = UUID()
        self.geschaeftsname = geschaeftsname
        self.scanDatum = scanDatum
        self.gesamtbetrag = gesamtbetrag
        self.artikel = []
        self.ocrConfidence = 0.0
    }
}

@Model
final class RechnungsArtikel {
    @Attribute(.unique) var id: UUID
    var name: String
    var beschreibung: String?
    var menge: Int
    var einzelpreis: Decimal
    var gesamtpreis: Decimal

    @Relationship(inverse: \Rechnung.artikel)
    var rechnung: Rechnung?

    @Relationship(inverse: \Produkt.kaufHistorie)
    var produkt: Produkt?

    init(name: String, menge: Int, einzelpreis: Decimal, gesamtpreis: Decimal) {
        self.id = UUID()
        self.name = name
        self.menge = menge
        self.einzelpreis = einzelpreis
        self.gesamtpreis = gesamtpreis
    }
}

@Model
final class Produkt {
    @Attribute(.unique) var id: UUID
    var name: String
    var kategorie: ProduktKategorie
    var marke: String?
    var strichcode: String?

    @Relationship(deleteRule: .cascade)
    var preisHistorie: [PreisEintrag]

    @Relationship
    var kaufHistorie: [RechnungsArtikel]

    var zuletztAktualisiert: Date

    init(name: String, kategorie: ProduktKategorie, zuletztAktualisiert: Date = .now) {
        self.id = UUID()
        self.name = name
        self.kategorie = kategorie
        self.preisHistorie = []
        self.kaufHistorie = []
        self.zuletztAktualisiert = zuletztAktualisiert
    }
}

@Model
final class PreisEintrag {
    @Attribute(.unique) var id: UUID
    var datum: Date
    var preis: Decimal

    @Relationship(inverse: \Produkt.preisHistorie)
    var produkt: Produkt?

    @Relationship(inverse: \Geschaeft.preise)
    var geschaeft: Geschaeft?

    init(datum: Date, preis: Decimal) {
        self.id = UUID()
        self.datum = datum
        self.preis = preis
    }
}

@Model
final class Geschaeft {
    @Attribute(.unique) var id: UUID
    var name: String
    var stadt: String?  // e.g., Wien

    @Relationship
    var rechnungen: [Rechnung]

    @Relationship
    var preise: [PreisEintrag]

    init(name: String, stadt: String? = "Wien") {
        self.id = UUID()
        self.name = name
        self.stadt = stadt
        self.rechnungen = []
        self.preise = []
    }
}

// MARK: - Unterstützende Typen

enum ProduktKategorie: String, Codable, CaseIterable, Sendable {
    case lebensmittel
    case getraenke
    case haushalt
    case drogerie
    case sonstiges
}

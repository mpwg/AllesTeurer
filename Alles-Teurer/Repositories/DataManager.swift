//
//  DataManager.swift
//  Alles-Teurer
//
//  Created by Copilot on 25.09.25.
//

import Foundation
import SwiftData

// Models are defined in the same module under Models/

// Ensure model types like `Produkt` are visible in this file's scope
// (same module, but explicit import not needed; adding comment for clarity)

@ModelActor
actor DataManager {
    // MARK: - Receipt Operations
    func speichereRechnung(_ rechnung: Rechnung) async throws {
        modelContext.insert(rechnung)
        try modelContext.save()
    }

    /// Create a receipt inside the actor to avoid crossing actor boundaries with @Model types
    func erzeugeRechnung(
        geschaeftsname: String, scanDatum: Date, gesamtbetrag: Decimal, rawText: String?
    ) async throws -> PersistentIdentifier {
        let r = Rechnung(
            geschaeftsname: geschaeftsname, scanDatum: scanDatum, gesamtbetrag: gesamtbetrag)
        r.rawOCRText = rawText
        modelContext.insert(r)
        try modelContext.save()
        return r.persistentModelID
    }

    /// Append an item to a receipt identified by its PersistentIdentifier
    func fuegeArtikelHinzu(
        zu receiptID: PersistentIdentifier, name: String, menge: Int, einzelpreis: Decimal,
        gesamtpreis: Decimal
    ) async throws {
        guard let r = modelContext.model(for: receiptID) as? Rechnung else { return }
        let item = RechnungsArtikel(
            name: name, menge: menge, einzelpreis: einzelpreis, gesamtpreis: gesamtpreis)
        r.artikel.append(item)
        try modelContext.save()
    }

    func ladeRechnungen() async throws -> [Rechnung] {
        let descriptor = FetchDescriptor<Rechnung>()
        let result: [Rechnung] = try modelContext.fetch(descriptor)
        return result.sorted { $0.scanDatum > $1.scanDatum }
    }

    func loescheRechnung(_ rechnung: Rechnung) async throws {
        modelContext.delete(rechnung)
        try modelContext.save()
    }

    func loescheRechnung(mitID id: PersistentIdentifier) async throws {
        if let objekt = modelContext.model(for: id) as? Rechnung {
            modelContext.delete(objekt)
            try modelContext.save()
        }
    }

    // MARK: - Analytics
    func inflationsRate(fuer produkt: Produkt) async -> Double {
        // Very basic calculation: compare first and last price
        let preise = produkt.preisHistorie.sorted { $0.datum < $1.datum }
        guard let first = preise.first?.preis, let last = preise.last?.preis, first > 0 else {
            return 0
        }
        let firstNum = NSDecimalNumber(decimal: first)
        let lastNum = NSDecimalNumber(decimal: last)
        let diff = lastNum.subtracting(firstNum)
        let ratio = diff.dividing(by: firstNum)
        return ratio.doubleValue * 100.0
    }

    // MARK: - Safe value-returning helpers
    func anzahlRechnungen() async throws -> Int {
        let descriptor = FetchDescriptor<Rechnung>()
        let result: [Rechnung] = try modelContext.fetch(descriptor)
        return result.count
    }
}

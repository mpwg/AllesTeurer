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

    // MARK: - Search Operations

    /// Search receipts by store name or OCR text content
    func sucheRechnungen(query: String? = nil, limit: Int = 100) async throws -> [Rechnung] {
        var descriptor = FetchDescriptor<Rechnung>(
            sortBy: [SortDescriptor(\.scanDatum, order: .reverse)]
        )

        if let query = query, !query.isEmpty {
            let searchQuery = query.lowercased()
            descriptor.predicate = #Predicate<Rechnung> { receipt in
                receipt.geschaeftsname.localizedStandardContains(searchQuery)
                    || (receipt.rawOCRText?.localizedStandardContains(searchQuery) ?? false)
            }
        }

        descriptor.fetchLimit = limit
        return try modelContext.fetch(descriptor)
    }

    /// Search receipts by date range
    func sucheRechnungenNachDatum(
        von startDate: Date,
        bis endDate: Date,
        limit: Int = 100
    ) async throws -> [Rechnung] {
        var descriptor = FetchDescriptor<Rechnung>(
            predicate: #Predicate<Rechnung> { receipt in
                receipt.scanDatum >= startDate && receipt.scanDatum <= endDate
            },
            sortBy: [SortDescriptor(\.scanDatum, order: .reverse)]
        )
        descriptor.fetchLimit = limit
        return try modelContext.fetch(descriptor)
    }

    /// Search receipts by amount range
    func sucheRechnungenNachBetrag(
        mindestens minAmount: Decimal,
        hoechstens maxAmount: Decimal,
        limit: Int = 100
    ) async throws -> [Rechnung] {
        var descriptor = FetchDescriptor<Rechnung>(
            predicate: #Predicate<Rechnung> { receipt in
                receipt.gesamtbetrag >= minAmount && receipt.gesamtbetrag <= maxAmount
            },
            sortBy: [SortDescriptor(\.gesamtbetrag, order: .reverse)]
        )
        descriptor.fetchLimit = limit
        return try modelContext.fetch(descriptor)
    }

    /// Search products by name
    func sucheProdukte(query: String? = nil, limit: Int = 100) async throws -> [Produkt] {
        var descriptor = FetchDescriptor<Produkt>(
            sortBy: [SortDescriptor(\.name)]
        )

        if let query = query, !query.isEmpty {
            let searchQuery = query.lowercased()
            descriptor.predicate = #Predicate<Produkt> { product in
                product.name.localizedStandardContains(searchQuery)
            }
        }

        descriptor.fetchLimit = limit
        return try modelContext.fetch(descriptor)
    }

    /// Search products by category
    func sucheProduktNachKategorie(
        _ category: ProduktKategorie,
        limit: Int = 100
    ) async throws -> [Produkt] {
        var descriptor = FetchDescriptor<Produkt>(
            predicate: #Predicate<Produkt> { product in
                product.kategorie == category
            },
            sortBy: [SortDescriptor(\.name)]
        )
        descriptor.fetchLimit = limit
        return try modelContext.fetch(descriptor)
    }

    /// Get spending summary by store for date range
    func ausgabenZusammenfassungNachGeschaeft(
        von startDate: Date,
        bis endDate: Date
    ) async throws -> [(geschaeft: String, gesamtbetrag: Decimal, anzahlRechnungen: Int)] {
        let receipts = try await sucheRechnungenNachDatum(
            von: startDate,
            bis: endDate,
            limit: 1000
        )

        // Group by store and calculate totals
        let grouped = Dictionary(grouping: receipts, by: { $0.geschaeftsname })

        return grouped.map { (store, receipts) in
            let totalAmount = receipts.reduce(Decimal.zero) { $0 + $1.gesamtbetrag }
            return (geschaeft: store, gesamtbetrag: totalAmount, anzahlRechnungen: receipts.count)
        }.sorted { $0.gesamtbetrag > $1.gesamtbetrag }
    }

    // MARK: - Safe value-returning helpers
    func anzahlRechnungen() async throws -> Int {
        let descriptor = FetchDescriptor<Rechnung>()
        let result: [Rechnung] = try modelContext.fetch(descriptor)
        return result.count
    }
}

//
//  DataManagerSearchTests.swift
//  Alles-TeurerTests
//
//  Created by Copilot on 25.09.25.
//

import Foundation
import SwiftData
import Testing

@testable import Alles_Teurer

@Suite("DataManager Search Functionality")
struct DataManagerSearchTests {

    @Test("Search receipts by store name")
    func searchReceiptsByStore() async throws {
        let container = try createInMemoryContainer()
        let dataManager = DataManager(modelContainer: container)

        // Create test receipts
        let billaReceipt = Rechnung(geschaeftsname: "BILLA", scanDatum: Date(), gesamtbetrag: 10.50)
        let sparReceipt = Rechnung(geschaeftsname: "SPAR", scanDatum: Date(), gesamtbetrag: 15.25)
        let dmReceipt = Rechnung(geschaeftsname: "dm", scanDatum: Date(), gesamtbetrag: 8.90)

        try await dataManager.speichereRechnung(billaReceipt)
        try await dataManager.speichereRechnung(sparReceipt)
        try await dataManager.speichereRechnung(dmReceipt)

        // Test search for "BILLA"
        let billaResults = try await dataManager.sucheRechnungen(geschaeftsname: "BILLA")
        #expect(billaResults.count == 1)
        #expect(billaResults.first?.geschaeftsname == "BILLA")

        // Test partial search
        let sResults = try await dataManager.sucheRechnungen(geschaeftsname: "S")
        #expect(sResults.count == 1)
        #expect(sResults.first?.geschaeftsname == "SPAR")

        // Test case insensitive search
        let dmResults = try await dataManager.sucheRechnungen(geschaeftsname: "DM")
        #expect(dmResults.count == 1)
        #expect(dmResults.first?.geschaeftsname == "dm")
    }

    @Test("Search receipts by date range")
    func searchReceiptsByDateRange() async throws {
        let container = try createInMemoryContainer()
        let dataManager = DataManager(modelContainer: container)

        let today = Date()
        let yesterday = Calendar.current.date(byAdding: .day, value: -1, to: today)!
        let lastWeek = Calendar.current.date(byAdding: .day, value: -7, to: today)!

        // Create receipts on different dates
        let todayReceipt = Rechnung(geschaeftsname: "Store1", scanDatum: today, gesamtbetrag: 10.0)
        let yesterdayReceipt = Rechnung(
            geschaeftsname: "Store2", scanDatum: yesterday, gesamtbetrag: 20.0)
        let lastWeekReceipt = Rechnung(
            geschaeftsname: "Store3", scanDatum: lastWeek, gesamtbetrag: 30.0)

        try await dataManager.speichereRechnung(todayReceipt)
        try await dataManager.speichereRechnung(yesterdayReceipt)
        try await dataManager.speichereRechnung(lastWeekReceipt)

        // Test date range search
        let recentReceipts = try await dataManager.sucheRechnungenNachDatum(
            von: yesterday, bis: today)
        #expect(recentReceipts.count == 2)

        let allReceipts = try await dataManager.sucheRechnungenNachDatum(von: lastWeek, bis: today)
        #expect(allReceipts.count == 3)
    }

    @Test("Search receipts by amount range")
    func searchReceiptsByAmountRange() async throws {
        let container = try createInMemoryContainer()
        let dataManager = DataManager(modelContainer: container)

        // Create receipts with different amounts
        let cheapReceipt = Rechnung(geschaeftsname: "Store1", scanDatum: Date(), gesamtbetrag: 5.99)
        let midReceipt = Rechnung(geschaeftsname: "Store2", scanDatum: Date(), gesamtbetrag: 25.50)
        let expensiveReceipt = Rechnung(
            geschaeftsname: "Store3", scanDatum: Date(), gesamtbetrag: 150.00)

        try await dataManager.speichereRechnung(cheapReceipt)
        try await dataManager.speichereRechnung(midReceipt)
        try await dataManager.speichereRechnung(expensiveReceipt)

        // Test amount range search
        let budgetReceipts = try await dataManager.sucheRechnungenNachBetrag(
            minBetrag: 0, maxBetrag: 30)
        #expect(budgetReceipts.count == 2)

        let expensiveReceipts = try await dataManager.sucheRechnungenNachBetrag(
            minBetrag: 100, maxBetrag: 200)
        #expect(expensiveReceipts.count == 1)
        #expect(expensiveReceipts.first?.gesamtbetrag == 150.00)
    }

    @Test("Search products by name")
    func searchProductsByName() async throws {
        let container = try createInMemoryContainer()
        let dataManager = DataManager(modelContainer: container)

        // Create products
        let milch = Produkt(name: "Milch 3,5%", kategorie: "Dairy")
        let vollmilch = Produkt(name: "Vollmilch Bio", kategorie: "Dairy")
        let brot = Produkt(name: "Vollkornbrot", kategorie: "Bakery")

        // Save products through receipts with articles
        let receipt = Rechnung(geschaeftsname: "Test Store", scanDatum: Date(), gesamtbetrag: 10.0)
        let article1 = RechnungsArtikel(
            name: "Milch 3,5%", menge: 1, einzelpreis: 1.49, gesamtpreis: 1.49)
        let article2 = RechnungsArtikel(
            name: "Vollmilch Bio", menge: 1, einzelpreis: 1.99, gesamtpreis: 1.99)
        let article3 = RechnungsArtikel(
            name: "Vollkornbrot", menge: 1, einzelpreis: 2.99, gesamtpreis: 2.99)

        article1.produkt = milch
        article2.produkt = vollmilch
        article3.produkt = brot

        receipt.artikel = [article1, article2, article3]

        try await dataManager.speichereRechnung(receipt)

        // Test product search
        let milchResults = try await dataManager.sucheProdukte(suchbegriff: "Milch")
        #expect(milchResults.count == 2)

        let breadResults = try await dataManager.sucheProdukte(suchbegriff: "Brot")
        #expect(breadResults.count == 1)
        #expect(breadResults.first?.name == "Vollkornbrot")
    }

    @Test("Spending summary by store")
    func spendingSummaryByStore() async throws {
        let container = try createInMemoryContainer()
        let dataManager = DataManager(modelContainer: container)

        // Create multiple receipts for same stores
        let billa1 = Rechnung(geschaeftsname: "BILLA", scanDatum: Date(), gesamtbetrag: 25.50)
        let billa2 = Rechnung(geschaeftsname: "BILLA", scanDatum: Date(), gesamtbetrag: 18.75)
        let spar1 = Rechnung(geschaeftsname: "SPAR", scanDatum: Date(), gesamtbetrag: 12.30)

        try await dataManager.speichereRechnung(billa1)
        try await dataManager.speichereRechnung(billa2)
        try await dataManager.speichereRechnung(spar1)

        // Test spending summary
        let summary = try await dataManager.ausgabenZusammenfassungNachGeschaeft()
        #expect(summary.count == 2)

        // Find BILLA summary
        let billaSummary = summary.first { $0.geschaeftsname == "BILLA" }
        #expect(billaSummary != nil)
        #expect(billaSummary?.gesamtausgaben == 44.25)
        #expect(billaSummary?.anzahlRechnungen == 2)

        // Find SPAR summary
        let sparSummary = summary.first { $0.geschaeftsname == "SPAR" }
        #expect(sparSummary != nil)
        #expect(sparSummary?.gesamtausgaben == 12.30)
        #expect(sparSummary?.anzahlRechnungen == 1)
    }

    private func createInMemoryContainer() throws -> ModelContainer {
        let schema = Schema([
            Rechnung.self,
            RechnungsArtikel.self,
            Produkt.self,
            PreisEintrag.self,
            Geschaeft.self,
        ])
        let config = ModelConfiguration(schema: schema, isStoredInMemoryOnly: true)
        return try ModelContainer(for: schema, configurations: [config])
    }
}

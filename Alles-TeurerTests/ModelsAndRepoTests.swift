//
//  ModelsAndRepoTests.swift
//  Alles-TeurerTests
//
//  Created by Copilot on 25.09.25.
//

import Foundation
import SwiftData
import Testing

@testable import Alles_Teurer

@Suite("Models & Repository")
struct ModelsAndRepoTests {

    @Test("Create and fetch a Rechnung")
    func createAndFetch() async throws {
        let schema = Schema([
            Rechnung.self,
            RechnungsArtikel.self,
            Produkt.self,
            PreisEintrag.self,
            Geschaeft.self,
        ])
        let config = ModelConfiguration(schema: schema, isStoredInMemoryOnly: true)
        let container = try ModelContainer(for: schema, configurations: [config])

        let repo = DataManager(modelContainer: container)

        let r = Rechnung(geschaeftsname: "BILLA", scanDatum: Date(), gesamtbetrag: 3.99)
        try await repo.speichereRechnung(r)

        let count = try await repo.anzahlRechnungen()
        #expect(count == 1)
    }
}

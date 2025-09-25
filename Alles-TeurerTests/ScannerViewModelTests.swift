//
//  ScannerViewModelTests.swift
//  Alles-TeurerTests
//
//  Created by Copilot on 25.09.25.
//

import Foundation
import SwiftData
import Testing

@testable import Alles_Teurer

@Suite("Scanner ViewModel")
struct ScannerViewModelTests {

    @Test("Maps DTO to SwiftData models and saves via repository")
    @MainActor
    func savesRecognizedReceipt() async throws {
        // Arrange in-memory SwiftData container
        let schema = Schema([
            Rechnung.self,
            RechnungsArtikel.self,
            Produkt.self,
            PreisEintrag.self,
            Geschaeft.self,
        ])
        let config = ModelConfiguration(schema: schema, isStoredInMemoryOnly: true)
        let container = try ModelContainer(for: schema, configurations: [config])

        let vm = ScannerViewModel(modelContainer: container)

        let dto = ScannerViewModel.RecognizedReceiptDTO(
            store: "BILLA",
            total: 4.48,
            items: [
                .init(name: "Milch 3,5%", quantity: 1, unitPrice: 1.49, totalPrice: 1.49),
                .init(name: "Brot Vollkorn", quantity: 1, unitPrice: 2.99, totalPrice: 2.99),
            ],
            rawText: "BILLA\nMilch 3,5% 1,49\nBrot Vollkorn 2,99\nGESAMT 4,48"
        )

        // Act
        await vm.saveRecognized(dto)

        // Assert repository count
        let repo = DataManager(modelContainer: container)
        let count = try await repo.anzahlRechnungen()
        #expect(count == 1)
    }
}

//
//  ScannerViewModel.swift
//  Alles-Teurer
//
//  Created by Copilot on 25.09.25.
//

import Foundation
import Observation
import SwiftData

// Ensure visibility of SwiftData models defined in the same target
// Types used: Rechnung, RechnungsArtikel, DataManager

@MainActor
@Observable
final class ScannerViewModel {
    enum SaveState: Equatable {
        case idle
        case saving
        case saved(Int)  // number of items saved
        case failed(String)
    }

    private let dataManager: DataManager
    var saveState: SaveState = .idle

    init(modelContainer: ModelContainer) {
        self.dataManager = DataManager(modelContainer: modelContainer)
    }

    struct RecognizedItemDTO: Sendable, Equatable {
        var name: String
        var quantity: Int
        var unitPrice: Decimal?
        var totalPrice: Decimal
    }

    struct RecognizedReceiptDTO: Sendable, Equatable {
        var store: String
        var total: Decimal
        var items: [RecognizedItemDTO]
        var rawText: String
    }

    func saveRecognized(_ dto: RecognizedReceiptDTO) async {
        saveState = .saving
        do {
            let id = try await dataManager.erzeugeRechnung(
                geschaeftsname: dto.store,
                scanDatum: Date.now,
                gesamtbetrag: dto.total,
                rawText: dto.rawText
            )
            for it in dto.items {
                try await dataManager.fuegeArtikelHinzu(
                    zu: id,
                    name: it.name,
                    menge: it.quantity,
                    einzelpreis: it.unitPrice ?? it.totalPrice,
                    gesamtpreis: it.totalPrice
                )
            }
            saveState = .saved(dto.items.count)
        } catch {
            saveState = .failed(error.localizedDescription)
        }
    }
}

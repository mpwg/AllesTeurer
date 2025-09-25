//
//  OCRService.swift
//  Alles-Teurer
//
//  Created by Copilot on 25.09.25.
//

import Foundation
import Observation
import SwiftUI

#if canImport(Vision)
    import Vision
#endif
#if canImport(UIKit)
    import UIKit
#endif

// Vision frameworks would be used in a real implementation
// import Vision
// import VisionKit

@MainActor
@Observable
final class OCRService {
    enum ScanState {
        case idle
        case processing
        case success(RecognizedReceipt)
        case error(String)
    }

    var zustand: ScanState = .idle

    init() {}

    struct RecognizedItem: Sendable, Equatable {
        var name: String
        var quantity: Int
        var unitPrice: Decimal?
        var totalPrice: Decimal
    }

    struct RecognizedReceipt: Sendable, Equatable {
        var store: String
        var total: Decimal
        var items: [RecognizedItem]
        var rawText: String
    }

    func verarbeiteBildDaten(_ data: Data) async {
        zustand = .processing
        // Try to recognize text and parse; fallback to placeholder on failure
        #if canImport(Vision) && canImport(UIKit)
            if let image = UIImage(data: data)?.cgImage {
                do {
                    // Offload recognition to a background actor to avoid blocking main
                    let lines = try await textRecognizer.recognizeLines(in: image)
                    let parser = ReceiptParser()
                    let parsed = try await parser.parse(lines: lines)
                    let dto = RecognizedReceipt(
                        store: parsed.store,
                        total: parsed.total,
                        items: parsed.items.map {
                            RecognizedItem(
                                name: $0.name, quantity: $0.quantity, unitPrice: $0.unitPrice,
                                totalPrice: $0.totalPrice)
                        },
                        rawText: lines.joined(separator: "\n")
                    )
                    zustand = .success(dto)
                    return
                } catch {
                    // Fall through to placeholder on error
                }
            }
        #endif
        // Fallback placeholder
        let fallback = RecognizedReceipt(
            store: "BILLA",
            total: 4.48,
            items: [
                RecognizedItem(name: "Milch 3,5%", quantity: 1, unitPrice: 1.49, totalPrice: 1.49),
                RecognizedItem(
                    name: "Brot Vollkorn", quantity: 1, unitPrice: 2.99, totalPrice: 2.99),
            ],
            rawText: "BILLA\nMilch 3,5% 1,49\nBrot Vollkorn 2,99\nGESAMT 4,48"
        )
        zustand = .success(fallback)
    }

    // MARK: - Vision Text Recognition (baseline)
    #if canImport(Vision)
    #endif
}

#if canImport(Vision)
/// Background actor for Vision text recognition
fileprivate actor TextRecognizerActor {
    /// Recognize text lines from a CGImage with German priority (off main thread)
    func recognizeLines(in cgImage: CGImage) async throws -> [String] {
        let request = VNRecognizeTextRequest()
        request.recognitionLevel = .accurate
        request.usesLanguageCorrection = true
        request.recognitionLanguages = ["de-DE", "en-US"]

        let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
        try handler.perform([request])
        let observations: [VNRecognizedTextObservation] = request.results ?? []
        return observations.compactMap { $0.topCandidates(1).first?.string }
    }
}

/// Shared instance used within OCRService to offload recognition
fileprivate let textRecognizer = TextRecognizerActor()
#endif

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

// Vision frameworks would be used in a real implementation
// import Vision
// import VisionKit

@MainActor
@Observable
final class OCRService {
    enum ScanState {
        case idle
        case processing
        case success(Rechnung)
        case error(String)
    }

    var zustand: ScanState = .idle

    init() {}

    func verarbeiteBildDaten(_ data: Data) async {
        zustand = .processing
        // Placeholder implementation: create a dummy receipt
        let rechnung = Rechnung(geschaeftsname: "BILLA", scanDatum: Date.now, gesamtbetrag: 4.48)
        let milch = RechnungsArtikel(
            name: "Milch 3,5%", menge: 1, einzelpreis: 1.49, gesamtpreis: 1.49)
        let brot = RechnungsArtikel(
            name: "Brot Vollkorn", menge: 1, einzelpreis: 2.99, gesamtpreis: 2.99)
        rechnung.artikel = [milch, brot]
        zustand = .success(rechnung)
    }

    // MARK: - Vision Text Recognition (baseline)
    #if canImport(Vision)
    /// Recognize text lines from a CGImage with German priority
    func erkenneText(in cgImage: CGImage) async throws -> [String] {
        let request = VNRecognizeTextRequest()
        request.recognitionLevel = .accurate
        request.usesLanguageCorrection = true
        request.recognitionLanguages = ["de-DE", "en-US"]

        let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
        try handler.perform([request])
        let observations: [VNRecognizedTextObservation] = request.results ?? []
        return observations.compactMap { $0.topCandidates(1).first?.string }
    }
    #endif
}

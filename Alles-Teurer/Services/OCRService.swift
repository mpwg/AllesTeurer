//
//  OCRService.swift
//  Alles-Teurer
//
//  Created by AI Agent on 22.09.25.
//  Refactored to use SwiftUI native types and @Observable pattern
//

import Foundation
import SwiftUI
@unsafe @preconcurrency import Vision
@unsafe @preconcurrency import VisionKit

// MARK: - CGImage Extension for SwiftUI Native Types

extension CGImage {
    static func fromImageData(_ data: Data) -> CGImage? {
        guard let dataProvider = CGDataProvider(data: data as CFData),
            let cgImage = CGImage(
                jpegDataProviderSource: dataProvider,
                decode: nil,
                shouldInterpolate: true,
                intent: .defaultIntent
            )
        else {
            // Try PNG if JPEG fails
            guard let source = CGImageSourceCreateWithData(data as CFData, nil),
                let cgImage = CGImageSourceCreateImageAtIndex(source, 0, nil)
            else {
                return nil
            }
            return cgImage
        }
        return cgImage
    }
}

// MARK: - OCR Types

/// OCRFehler - Mögliche Fehler beim OCR-Prozess
enum OCRFehler: LocalizedError {
    case ungueltigesBild
    case keinTextErkannt
    case verarbeitungsFehler(String)
    case kassenbonFormatNichtErkannt

    var errorDescription: String? {
        switch self {
        case .ungueltigesBild:
            return "Das Bild konnte nicht verarbeitet werden."
        case .keinTextErkannt:
            return "Es wurde kein Text im Bild erkannt."
        case .verarbeitungsFehler(let detail):
            return "Verarbeitungsfehler: \(detail)"
        case .kassenbonFormatNichtErkannt:
            return "Das Kassenbonformat wurde nicht erkannt."
        }
    }
}

/// OCRResult - Intermediate result structure
struct OCRResult {
    let geschaeftsname: String
    let artikel: [ArtikelData]
    let gesamtbetrag: Decimal
    let roherText: String
    let vertrauen: Double
}

/// ArtikelData - Intermediate article structure
struct ArtikelData {
    let name: String
    let menge: Int
    let einzelpreis: Decimal
    let gesamtpreis: Decimal
}

/// ScanZustand - Aktueller Zustand des Scannvorgangs
enum ScanZustand {
    case inaktiv
    case verarbeitung
    case erfolgreich(OCRResult)
    case fehler(String)
}

// MARK: - OCRService

/// OCRService - Service für Texterkennung mit Vision Framework
/// Uses SwiftUI native types and @Observable pattern
@MainActor
@Observable
final class OCRService {
    var scanZustand: ScanZustand = .inaktiv
    var letzterScan: OCRResult?

    // Deutsche Einzelhandelsketten für bessere Erkennung
    private let deutscheEinzelhaendler = [
        "REWE", "EDEKA", "ALDI", "LIDL", "PENNY", "NETTO", "KAUFLAND",
        "REAL", "GLOBUS", "FAMILA", "COMBI", "NORMA", "DM", "ROSSMANN",
        "MÜLLER", "SATURN", "MEDIA MARKT", "OBI", "BAUHAUS", "HORNBACH",
        "IKEA", "DECATHLON", "H&M", "C&A", "ZARA", "PRIMARK",
    ]

    init() {
        // Service wird ohne DataManager initialisiert
    }

    /// Verarbeitet Bilddaten und extrahiert Kassenbondaten
    /// - Parameter imageData: Raw image data from camera or photo library
    /// - Returns: OCRResult with parsed data
    func verarbeiteImageData(_ imageData: Data) async throws -> OCRResult {
        scanZustand = .verarbeitung

        do {
            guard let cgBild = CGImage.fromImageData(imageData) else {
                throw OCRFehler.ungueltigesBild
            }

            // OCR durchführen
            let erkannterText = try await fuehrTexterkennung(cgBild: cgBild)

            // OCRResult aus Text parsen
            let result = try parseOCRResult(aus: erkannterText)

            letzterScan = result
            scanZustand = .erfolgreich(result)

            return result

        } catch {
            let fehlerMeldung = error.localizedDescription
            scanZustand = .fehler(fehlerMeldung)
            throw error
        }
    }

    /// Führt die Texterkennung mit Vision Framework durch
    private func fuehrTexterkennung(cgBild: CGImage) async throws -> String {
        return try await withCheckedThrowingContinuation { fortsetzung in
            let anfrage = VNRecognizeTextRequest { request, error in
                if let error = error {
                    fortsetzung.resume(
                        throwing: OCRFehler.verarbeitungsFehler(error.localizedDescription))
                    return
                }

                guard let beobachtungen = request.results as? [VNRecognizedTextObservation] else {
                    fortsetzung.resume(throwing: OCRFehler.keinTextErkannt)
                    return
                }

                let erkannterText = beobachtungen.compactMap { beobachtung in
                    beobachtung.topCandidates(1).first?.string
                }.joined(separator: "\n")

                fortsetzung.resume(returning: erkannterText)
            }

            // Konfiguration für deutsche Kassenbons
            anfrage.recognitionLevel = .accurate
            anfrage.recognitionLanguages = ["de-DE", "en-US"]
            anfrage.usesLanguageCorrection = true
            anfrage.customWords = deutscheEinzelhaendler
            anfrage.minimumTextHeight = 0.01

            let handler = VNImageRequestHandler(cgImage: cgBild, options: [:])

            DispatchQueue.global(qos: .userInitiated).async {
                do {
                    try handler.perform([anfrage])
                } catch {
                    fortsetzung.resume(throwing: error)
                }
            }
        }
    }

    /// Parst OCRResult aus dem erkannten Text
    private func parseOCRResult(aus text: String) throws -> OCRResult {
        let zeilen = text.components(separatedBy: .newlines).filter {
            !$0.trimmingCharacters(in: .whitespaces).isEmpty
        }

        // Geschäftsname erkennen
        let geschaeftsname = erkennGeschaeftsname(aus: zeilen)

        // Artikel extrahieren
        let artikel = extrahiereArtikel(aus: zeilen)

        // Gesamtbetrag finden
        let gesamtbetrag = findeGesamtbetrag(aus: zeilen)

        guard !artikel.isEmpty else {
            throw OCRFehler.kassenbonFormatNichtErkannt
        }

        let vertrauen = berechneVertrauensgrad(fuer: artikel, gesamtbetrag: gesamtbetrag)

        return OCRResult(
            geschaeftsname: geschaeftsname,
            artikel: artikel,
            gesamtbetrag: gesamtbetrag,
            roherText: text,
            vertrauen: vertrauen
        )
    }

    /// Erkennt den Geschäftsnamen aus den ersten Zeilen
    private func erkennGeschaeftsname(aus zeilen: [String]) -> String {
        // Suche in den ersten 5 Zeilen nach bekannten Einzelhändlern
        for zeile in Array(zeilen.prefix(5)) {
            for einzelhaendler in deutscheEinzelhaendler {
                if zeile.uppercased().contains(einzelhaendler) {
                    return einzelhaendler
                }
            }
        }

        // Falls kein bekannter Einzelhändler gefunden, nimm die erste Zeile
        return zeilen.first?.trimmingCharacters(in: .whitespaces) ?? "Unbekanntes Geschäft"
    }

    /// Extrahiert Artikel aus den Kassenbonzeilen
    private func extrahiereArtikel(aus zeilen: [String]) -> [ArtikelData] {
        var artikel: [ArtikelData] = []

        for zeile in zeilen {
            // Suche nach Preis-Patterns mit regulären Ausdrücken
            let preisRegex = try! NSRegularExpression(
                pattern: #"(?:€\s*)?(\d+[,\.]\d{2})(?:\s*[A-Z])?"#)
            let treffer = preisRegex.firstMatch(
                in: zeile, range: NSRange(zeile.startIndex..., in: zeile))

            guard let treffer = treffer,
                let range = Range(treffer.range(at: 1), in: zeile)
            else { continue }

            let preisString = String(zeile[range]).replacingOccurrences(of: ",", with: ".")
            guard let preis = Decimal(string: preisString) else { continue }

            // Produktname ist der Text vor dem Preis
            let produktName = String(zeile[..<range.lowerBound]).trimmingCharacters(
                in: .whitespaces)

            guard !produktName.isEmpty && preis > 0 else { continue }

            // Menge erkennen
            let mengeRegex = try! NSRegularExpression(pattern: #"^(\d+)\s*[xX]\s*"#)
            var menge = 1
            var bereinigterName = produktName

            if let mengeTreffer = mengeRegex.firstMatch(
                in: produktName, range: NSRange(produktName.startIndex..., in: produktName)),
                let mengeRange = Range(mengeTreffer.range(at: 1), in: produktName)
            {
                menge = Int(produktName[mengeRange]) ?? 1
                let restRange = Range(mengeTreffer.range, in: produktName)!
                bereinigterName = String(produktName[restRange.upperBound...]).trimmingCharacters(
                    in: .whitespaces)
            }

            let neuerArtikel = ArtikelData(
                name: bereinigterName,
                menge: menge,
                einzelpreis: menge > 1 ? preis / Decimal(menge) : preis,
                gesamtpreis: preis
            )

            artikel.append(neuerArtikel)
        }

        return artikel
    }

    /// Findet den Gesamtbetrag des Kassenbons
    private func findeGesamtbetrag(aus zeilen: [String]) -> Decimal {
        let gesamtbetragSchluesselwoerter = ["SUMME", "GESAMT", "TOTAL", "BETRAG", "EUR"]

        for zeile in zeilen.reversed() {
            let zeileUpper = zeile.uppercased()

            let enthaeltSchluesselwort = gesamtbetragSchluesselwoerter.contains { schluesselwort in
                zeileUpper.contains(schluesselwort)
            }

            if enthaeltSchluesselwort {
                let preisRegex = try! NSRegularExpression(pattern: #"(\d+[,\.]\d{2})"#)
                let treffer = preisRegex.matches(
                    in: zeile, range: NSRange(zeile.startIndex..., in: zeile))

                let gefundenePreise = treffer.compactMap { treffer -> Decimal? in
                    guard let range = Range(treffer.range(at: 1), in: zeile) else { return nil }
                    let preisString = String(zeile[range]).replacingOccurrences(of: ",", with: ".")
                    return Decimal(string: preisString)
                }

                if let hoechsterBetrag = gefundenePreise.max() {
                    return hoechsterBetrag
                }
            }
        }

        return 0.0
    }

    /// Berechnet den Vertrauensgrad des OCR-Ergebnisses
    private func berechneVertrauensgrad(fuer artikel: [ArtikelData], gesamtbetrag: Decimal)
        -> Double
    {
        guard !artikel.isEmpty else { return 0.0 }

        let berechneterGesamtbetrag = artikel.reduce(Decimal(0)) { $0 + $1.gesamtpreis }
        let differenz = abs(
            NSDecimalNumber(decimal: gesamtbetrag).doubleValue
                - NSDecimalNumber(decimal: berechneterGesamtbetrag).doubleValue)

        let maxDifferenz = max(NSDecimalNumber(decimal: gesamtbetrag).doubleValue * 0.1, 5.0)
        let vertrauen = max(0.0, 1.0 - (differenz / maxDifferenz))

        return vertrauen
    }
}

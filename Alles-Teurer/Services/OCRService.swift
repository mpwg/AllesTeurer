//
//  OCRService.swift
//  Alles-Teurer
//
//  Created by AI Agent on 22.09.25.
//

import Foundation
@unsafe @preconcurrency import Vision
@unsafe @preconcurrency import VisionKit
import SwiftUI
import Combine

#if canImport(UIKit)
import UIKit
#endif

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

/// ScanZustand - Aktueller Zustand des Scannvorgangs
enum ScanZustand {
    case inaktiv
    case verarbeitung
    case erfolgreich(Kassenbon)
    case fehler(String)
}

/// OCRService - Service für Texterkennung mit Vision Framework
@MainActor
final class OCRService: ObservableObject {
    @Published var scanZustand: ScanZustand = .inaktiv
    @Published var erkannterKassenbon: Kassenbon?
    
    // Deutsche Einzelhandelsketten für bessere Erkennung
    private let deutscheEinzelhaendler = [
        "REWE", "EDEKA", "ALDI", "LIDL", "PENNY", "NETTO", "KAUFLAND", 
        "REAL", "GLOBUS", "FAMILA", "COMBI", "NORMA", "DM", "ROSSMANN",
        "MÜLLER", "SATURN", "MEDIA MARKT", "OBI", "BAUHAUS", "HORNBACH",
        "IKEA", "DECATHLON", "H&M", "C&A", "ZARA", "PRIMARK"
    ]
    
    init() {
        // Service wird ohne DataManager initialisiert
    }
    
    /// Verarbeitet ein Bild und extrahiert Kassenbondaten
    func verarbeiteBild(_ bild: UIImage) async throws -> Kassenbon {
        scanZustand = .verarbeitung
        
        do {
            guard let cgBild = bild.cgImage else {
                throw OCRFehler.ungueltigesBild
            }
            
            // OCR durchführen
            let erkannterText = try await fuehrTexterkennung(cgBild: cgBild)
            
            // Kassenbon aus Text parsen
            let kassenbon = try parseKassenbon(aus: erkannterText, originalBild: bild)
            
            erkannterKassenbon = kassenbon
            scanZustand = .erfolgreich(kassenbon)
            
            return kassenbon
            
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
                    fortsetzung.resume(throwing: OCRFehler.verarbeitungsFehler(error.localizedDescription))
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
    
    /// Parst einen Kassenbon aus dem erkannten Text
    private func parseKassenbon(aus text: String, originalBild: UIImage) throws -> Kassenbon {
        let zeilen = text.components(separatedBy: .newlines).filter { !$0.trimmingCharacters(in: .whitespaces).isEmpty }
        
        // Geschäftsname erkennen
        let geschaeftsname = erkennGeschaeftsname(aus: zeilen)
        
        // Artikel extrahieren
        let artikel = extrahiereArtikel(aus: zeilen)
        
        // Gesamtbetrag finden
        let gesamtbetrag = findeGesamtbetrag(aus: zeilen)
        
        guard !artikel.isEmpty else {
            throw OCRFehler.kassenbonFormatNichtErkannt
        }
        
        // Kassenbon erstellen
        let kassenbon = Kassenbon(
            geschaeftsname: geschaeftsname,
            scanDatum: Date.now,
            gesamtbetrag: gesamtbetrag
        )
        
        // OCR-Daten hinzufügen
        kassenbon.roherOCRText = text
        kassenbon.bildDaten = originalBild.jpegData(compressionQuality: 0.8)
        kassenbon.ocrVertrauen = berechneVertrauensgrad(fuer: artikel, gesamtbetrag: gesamtbetrag)
        
        // Artikel hinzufügen
        kassenbon.artikel = artikel
        
        return kassenbon
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
    private func extrahiereArtikel(aus zeilen: [String]) -> [KassenbonArtikel] {
        var artikel: [KassenbonArtikel] = []
        
        for zeile in zeilen {
            // Suche nach Preis-Patterns mit regulären Ausdrücken
            let preisRegex = try! NSRegularExpression(pattern: #"(?:€\s*)?(\d+[,\.]\d{2})(?:\s*[A-Z])?"#)
            let treffer = preisRegex.firstMatch(in: zeile, range: NSRange(zeile.startIndex..., in: zeile))
            
            guard let treffer = treffer,
                  let range = Range(treffer.range(at: 1), in: zeile) else { continue }
            
            let preisString = String(zeile[range]).replacingOccurrences(of: ",", with: ".")
            guard let preis = Decimal(string: preisString) else { continue }
            
            // Produktname ist der Text vor dem Preis
            let produktName = String(zeile[..<range.lowerBound]).trimmingCharacters(in: .whitespaces)
            
            guard !produktName.isEmpty && preis > 0 else { continue }
            
            // Menge erkennen
            let mengeRegex = try! NSRegularExpression(pattern: #"^(\d+)\s*[xX]\s*"#)
            var menge = 1
            var bereinigterName = produktName
            
            if let mengeTreffer = mengeRegex.firstMatch(in: produktName, range: NSRange(produktName.startIndex..., in: produktName)),
               let mengeRange = Range(mengeTreffer.range(at: 1), in: produktName) {
                menge = Int(produktName[mengeRange]) ?? 1
                let restRange = Range(mengeTreffer.range, in: produktName)!
                bereinigterName = String(produktName[restRange.upperBound...]).trimmingCharacters(in: .whitespaces)
            }
            
            let neuerArtikel = KassenbonArtikel(
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
                let treffer = preisRegex.matches(in: zeile, range: NSRange(zeile.startIndex..., in: zeile))
                
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
    private func berechneVertrauensgrad(fuer artikel: [KassenbonArtikel], gesamtbetrag: Decimal) -> Double {
        guard !artikel.isEmpty else { return 0.0 }
        
        let berechneterGesamtbetrag = artikel.reduce(Decimal(0)) { $0 + $1.gesamtpreis }
        let differenz = abs(NSDecimalNumber(decimal: gesamtbetrag).doubleValue - NSDecimalNumber(decimal: berechneterGesamtbetrag).doubleValue)
        
        let maxDifferenz = max(NSDecimalNumber(decimal: gesamtbetrag).doubleValue * 0.1, 5.0)
        let vertrauen = max(0.0, 1.0 - (differenz / maxDifferenz))
        
        return vertrauen
    }
}
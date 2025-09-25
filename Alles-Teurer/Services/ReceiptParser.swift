//
//  ReceiptParser.swift
//  Alles-Teurer
//
//  Created by Copilot on 25.09.25.
//

import Foundation

actor ReceiptParser {
    struct ParseError: Error, LocalizedError {
        var errorDescription: String? { "Konnte Rechnung nicht erkennen" }
    }

    struct ParsedItem: Sendable, Equatable {
        var name: String
        var quantity: Int
        var unitPrice: Decimal?
        var totalPrice: Decimal
    }

    struct ParsedReceipt: Sendable, Equatable {
        var store: String
        var total: Decimal
        var items: [ParsedItem]
        var rawLines: [String]
    }

    private let decimalFormatterAT: NumberFormatter = {
        let f = NumberFormatter()
        f.locale = Locale(identifier: "de_AT")
        f.numberStyle = .decimal
        f.decimalSeparator = ","
        f.groupingSeparator = "."
        return f
    }()

    private let decimalFormatterDE: NumberFormatter = {
        let f = NumberFormatter()
        f.locale = Locale(identifier: "de_DE")
        f.numberStyle = .decimal
        f.decimalSeparator = ","
        f.groupingSeparator = "."
        return f
    }()

    func parse(lines: [String]) async throws -> ParsedReceipt {
        let trimmed = lines.map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }.filter {
            !$0.isEmpty
        }
        let storeLine =
            trimmed.first(where: {
                $0.range(of: #"[A-Za-zÄÖÜäöüß]"#, options: .regularExpression) != nil
            }) ?? trimmed.first ?? "Unbekanntes Geschäft"

        // Total amount from bottom with keywords
        let totalKeywords = ["summe", "gesamt", "total", "gesamtsumme", "betrag"]
        var totalAmount: Decimal? = nil
        for line in trimmed.reversed() {
            let lower = line.lowercased()
            if totalKeywords.contains(where: { lower.contains($0) }),
                let amount = parseAmount(in: line)
            {
                totalAmount = amount
                break
            }
        }
        if totalAmount == nil {
            // Fallback: use the last price-like number as total
            for line in trimmed.reversed() {
                if let amount = parseAmount(in: line) {
                    totalAmount = amount
                    break
                }
            }
        }

        let items = parseLineItems(from: trimmed)

        return ParsedReceipt(
            store: storeLine, total: totalAmount ?? 0, items: items, rawLines: trimmed)
    }

    private func parseAmount(in line: String) -> Decimal? {
        // Matches patterns like "EUR 1.234,56", "1.234,56 €", "1,49" etc.
        let pattern = #"(?:€\s*)?([0-9]{1,3}(?:[\.\s][0-9]{3})*,[0-9]{2})(?:\s*€)?"#
        let regex = try? NSRegularExpression(pattern: pattern)
        if let match = regex?.firstMatch(
            in: line, range: NSRange(line.startIndex..<line.endIndex, in: line)),
            let range = Range(match.range(at: 1), in: line)
        {
            return parseAmountText(String(line[range]))
        }
        return nil
    }

    private func parseAmountText(_ text: String) -> Decimal? {
        let normalized =
            text
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: "€", with: "")
            .replacingOccurrences(of: " ", with: "")
        if let n = decimalFormatterAT.number(from: normalized) { return n.decimalValue }
        if let n = decimalFormatterDE.number(from: normalized) { return n.decimalValue }
        return Decimal(string: normalized.replacingOccurrences(of: ",", with: "."))
    }

    /// Extract line items by detecting trailing price tokens and using the preceding text as the item name.
    private func parseLineItems(from lines: [String]) -> [ParsedItem] {
        var items: [ParsedItem] = []
        // Detect a price token at end or within the line
        let pattern = #"([0-9]{1,3}(?:[\.\s][0-9]{3})*,[0-9]{2})(?:\s*€)?$"#
        let regex = try? NSRegularExpression(pattern: pattern)
        for line in lines {
            guard let regex else { continue }
            let nsRange = NSRange(line.startIndex..<line.endIndex, in: line)
            guard let match = regex.firstMatch(in: line, range: nsRange) else { continue }
            let priceRange = match.range(at: 1)
            guard let swiftRange = Range(priceRange, in: line) else { continue }
            let priceText = String(line[swiftRange])
            let name = line.replacingCharacters(in: swiftRange, with: "").replacingOccurrences(
                of: "€", with: ""
            ).trimmingCharacters(in: .whitespacesAndNewlines)
            let price = parseAmountText(priceText) ?? 0
            items.append(
                ParsedItem(
                    name: name.isEmpty ? "Artikel" : name, quantity: 1, unitPrice: nil,
                    totalPrice: price))
        }
        return items
    }
}

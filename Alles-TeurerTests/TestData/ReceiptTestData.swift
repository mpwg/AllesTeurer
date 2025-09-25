//
//  ReceiptTestData.swift
//  Alles-TeurerTests
//
//  Created by Copilot on 25.09.25.
//

import Foundation

struct ReceiptTestData {

    // MARK: - Real Austrian Receipt Samples

    static let billaReceiptLines = [
        "BILLA PLUS",
        "1234 Wien, Musterstraße 12",
        "Tel: 01/12345678",
        "",
        "Datum: 25.09.2025 14:30",
        "Kasse: 3 Bon: 1234567",
        "",
        "H-Milch 3,5% 1L           1,49 A",
        "Bio Vollkornbrot          2,99 A",
        "Bananen kg         0,856  2,45 A",
        "Joghurt Natur 500g        1,89 A",
        "Apfelsaft naturtrüb 1L    2,79 A",
        "",
        "SUMME EUR                11,61",
        "Gegeben                  20,00",
        "Rückgeld                  8,39",
        "",
        "Vielen Dank für Ihren Einkauf!",
        "MwSt 20%    Netto    Steuer   Brutto",
        "A           9,68      1,93     11,61",
    ]

    static let sparReceiptLines = [
        "SPAR Markt",
        "5020 Salzburg, Hauptplatz 5",
        "",
        "25.09.2025  15:45  Kasse 2",
        "Bedienung: Maria M.",
        "",
        "Schwarzbrot Vollkorn       3,20 B",
        "Bergkäse 100g              4,50 B",
        "Tomaten Cherry 250g        2,10 A",
        "Gurke Stk                  0,89 A",
        "Olivenöl extra nativ       8,90 A",
        "Mineralwasser 6x1,5L       4,20 A",
        "",
        "Zwischensumme:            23,79",
        "Rabatt Kundenkarte:       -1,20",
        "",
        "GESAMT EUR:               22,59",
        "Bar bezahlt:              25,00",
        "Rückgeld:                  2,41",
        "",
        "USt 10%: 0,28  USt 20%: 3,76",
    ]

    static let hoferReceiptLines = [
        "HOFER KG",
        "Filiale 1234",
        "6020 Innsbruck, Museumstraße 8",
        "",
        "25.09.2025        16:20",
        "Kassier: Johannes K.",
        "Bon-Nr: 987654321",
        "",
        "Simply Brot 500g           0,79 A",
        "Milfina H-Milch 1L         0,89 A",
        "Cucina Nudeln 500g         0,69 A",
        "Bio Eier 6 Stk             1,99 A",
        "Markenqualität Kaffee      3,49 A",
        "Obst Mix 1kg               2,99 A",
        "Putenschnitzel 400g        4,99 A",
        "",
        "Summe:                    15,83",
        "Gegeben (Karte):          15,83",
        "",
        "MwSt. 10%: Netto  1,44  Steuer 0,14",
        "MwSt. 20%: Netto 12,87  Steuer 2,58",
        "",
        "Vielen Dank!",
    ]

    // MARK: - Edge Cases for Testing

    static let unclearReceiptLines = [
        "MERKUR Markt",  // Store clear
        "Datum: 25.09.2025",
        "",
        "Apfel kg          1,234   ???",  // Unclear price
        "Brot Vollkorn             2,99 A",
        "Milch unclear text here   1,49",
        "",
        "Total might be:           ???",  // Unclear total
        "Cash payment:            10,00",
    ]

    static let multiLanguageReceiptLines = [
        "INTERSPAR Hypermarkt",
        "Retail Park Vienna",
        "",
        "Date: 25.09.2025 Time: 17:30",
        "Cashier: Anna S.",
        "",
        "Fresh Bread Vollkorn       2,80 A",
        "Organic Milk Bio-Milch     1,69 A",
        "Cheese Gouda Käse 200g     3,20 A",
        "Wine Rotwein Zweigelt      8,90 A",
        "Chocolate Schokolade       2,40 A",
        "",
        "Subtotal:                 18,99",
        "Discount Card Rabatt:     -0,95",
        "",
        "TOTAL GESAMT:             18,04",
        "Payment Zahlung:          20,00",
        "Change Wechselgeld:        1,96",
    ]

    // MARK: - Test Utilities

    static func createMockReceiptImage() -> Data? {
        // Create a simple test image with text (in a real implementation,
        // this would be actual receipt images)
        return "Mock Receipt Image Data".data(using: .utf8)
    }

    static func getAllTestReceipts() -> [(name: String, lines: [String])] {
        return [
            ("BILLA Receipt", billaReceiptLines),
            ("SPAR Receipt", sparReceiptLines),
            ("HOFER Receipt", hoferReceiptLines),
            ("Unclear Receipt", unclearReceiptLines),
            ("Multi-language Receipt", multiLanguageReceiptLines),
        ]
    }

    static func getExpectedResults() -> [String: (store: String, total: Decimal, itemCount: Int)] {
        return [
            "BILLA Receipt": (store: "BILLA PLUS", total: 11.61, itemCount: 5),
            "SPAR Receipt": (store: "SPAR", total: 22.59, itemCount: 6),
            "HOFER Receipt": (store: "HOFER", total: 15.83, itemCount: 7),
            "Multi-language Receipt": (store: "INTERSPAR", total: 18.04, itemCount: 5),
        ]
    }
}

// MARK: - Visual Intelligence Test Extensions

extension ReceiptTestData {

    /// Generate confidence test scenarios
    static func getConfidenceTestScenarios() -> [(
        lines: [String], expectedConfidence: ClosedRange<Double>
    )] {
        return [
            // High confidence - clear Austrian receipt
            (lines: billaReceiptLines, expectedConfidence: 0.8...1.0),

            // Medium confidence - mixed language
            (lines: multiLanguageReceiptLines, expectedConfidence: 0.5...0.8),

            // Low confidence - unclear text
            (lines: unclearReceiptLines, expectedConfidence: 0.0...0.5),
        ]
    }

    /// Performance benchmarking data
    static func getLargeReceiptSample() -> [String] {
        var largeReceipt = [
            "MERKUR Hypermarkt",
            "1010 Wien, Graben 19",
            "Tel: 01/515 15-0",
            "",
            "Datum: 25.09.2025 18:45",
            "Kasse: 7 Bon: 555123456",
            "Kassier: Thomas B.",
            "",
        ]

        // Add 50 items for performance testing
        for i in 1...50 {
            let price = String(format: "%.2f", Double.random(in: 0.50...25.00))
            largeReceipt.append("Artikel \(i) Test Item       \(price) A")
        }

        largeReceipt.append("")
        largeReceipt.append("SUMME EUR               567,89")
        largeReceipt.append("Kartenzahlung           567,89")
        largeReceipt.append("")
        largeReceipt.append("Vielen Dank für Ihren Einkauf!")

        return largeReceipt
    }
}

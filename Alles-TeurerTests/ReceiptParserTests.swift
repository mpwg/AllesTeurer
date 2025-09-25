//
//  ReceiptParserTests.swift
//  Alles-TeurerTests
//
//  Created by Copilot on 25.09.25.
//

import Foundation
import Testing

@testable import Alles_Teurer

@Suite("Receipt Parser")
struct ReceiptParserTests {

    @Test("Parses Austrian/German formatted lines")
    func parsesLines() async throws {
        let lines = [
            "BILLA Filiale 123",
            "Wien 1010, Rotenturmstraße",
            "Milch 3,5% 1,49",
            "Brot Vollkorn 2,99",
            "GESAMT 4,48",
        ]
        let parser = ReceiptParser()
        let parsed = try await parser.parse(lines: lines)
        #expect(parsed.store.contains("BILLA"))
        #expect(parsed.items.count >= 2)
        if let expected = Decimal(string: "4.48") {
            #expect(parsed.total == expected)
        } else {
            Issue.record("Failed to construct expected Decimal value")
        }
    }
}

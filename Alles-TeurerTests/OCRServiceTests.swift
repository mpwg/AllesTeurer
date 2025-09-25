//
//  OCRServiceTests.swift
//  Alles-TeurerTests
//
//  Created by Copilot on 25.09.25.
//

import Foundation
import Testing

@testable import Alles_Teurer

@Suite("OCR Service")
struct OCRServiceTests {

    @Test("Placeholder processing returns a recognized receipt DTO")
    @MainActor
    func placeholderProcessing() async {
        let service = OCRService()
        let fakeData = Data([0x00, 0x01, 0x02])
        await service.verarbeiteBildDaten(fakeData)

        switch service.zustand {
        case .success(let r):
            #expect(r.items.count == 2)
            #expect(r.total > 0)
        default:
            Issue.record("Expected success state with a receipt")
        }
    }
}

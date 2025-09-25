//
//  ScannerSmokeTests.swift
//  Alles-TeurerUITests
//

import XCTest

@MainActor
final class ScannerSmokeTests: XCTestCase {
    func testScanDemoShowsSuccessAlert() throws {
        let app = XCUIApplication()
        app.launch()

        // Tap the Scan Demo toolbar button
        let scanButton = app.buttons["Scan Demo"]
        XCTAssertTrue(scanButton.waitForExistence(timeout: 10), "Scan Demo button should exist")
        scanButton.tap()

        // Expect success alert or sheet (Catalyst sometimes exposes SwiftUI alerts as sheets)
        let alert = app.alerts["Scan gespeichert"]
        let sheet = app.sheets["Scan gespeichert"]

        let appeared =
            alert.waitForExistence(timeout: 10) || sheet.waitForExistence(timeout: 10)
            || app.staticTexts["Scan gespeichert"].waitForExistence(timeout: 10)
        XCTAssertTrue(appeared, "Success confirmation should appear")

        // Dismiss if possible
        if alert.exists {
            alert.buttons["OK"].tap()
        } else if sheet.exists {
            sheet.buttons["OK"].firstMatch.tap()
        }
    }
}

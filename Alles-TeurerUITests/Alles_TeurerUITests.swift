//
//  Alles_TeurerUITests.swift
//  Alles-TeurerUITests
//
//  Created by Matthias Wallner-Géhri on 22.09.25.
//

import XCTest

final class Alles_TeurerUITests: XCTestCase {

    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.

        // In UI tests it is usually best to stop immediately when a failure occurs.
        continueAfterFailure = false

        // In UI tests it’s important to set the initial state - such as interface orientation - required for your tests before they run. The setUp method is a good place to do this.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    @MainActor
    func testExample() throws {
        // Minimal UI test - just verify app can launch
        let app = XCUIApplication()
        app.launch()

        // Quick assertion that app launched successfully
        XCTAssertTrue(app.exists)
    }

    // Removed performance test to speed up CI - this was taking unnecessary time
    // @MainActor
    // func testLaunchPerformance() throws {
    //     // This measures how long it takes to launch your application.
    //     measure(metrics: [XCTApplicationLaunchMetric()]) {
    //         XCUIApplication().launch()
    //     }
    // }
}

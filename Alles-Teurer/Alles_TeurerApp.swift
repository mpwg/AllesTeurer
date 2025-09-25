//
//  Alles_TeurerApp.swift
//  Alles-Teurer
//
//  Created by Matthias Wallner-Géhri on 22.09.25.
//

import SwiftData
import SwiftUI

@main
struct Alles_TeurerApp: App {
    var sharedModelContainer: ModelContainer = {
        // Use an in-memory store when running under tests to improve reliability and speed
        let isTesting = ProcessInfo.processInfo.environment["XCTestConfigurationFilePath"] != nil
        let schema = Schema([
            Rechnung.self,
            RechnungsArtikel.self,
            Produkt.self,
            PreisEintrag.self,
            Geschaeft.self,
        ])

        // Use App Group container for production to enable widget data sharing
        let modelConfiguration: ModelConfiguration
        if isTesting {
            // Use in-memory for testing
            modelConfiguration = ModelConfiguration(schema: schema, isStoredInMemoryOnly: true)
        } else {
            // Use App Group container for production
            if let appGroupURL = FileManager.default.containerURL(
                forSecurityApplicationGroupIdentifier: "group.eu.mpwg.allesteurer.shared")
            {
                let storeURL = appGroupURL.appendingPathComponent("AllesTeurer.sqlite")
                modelConfiguration = ModelConfiguration(schema: schema, url: storeURL)
            } else {
                // Fallback to default location if App Group is not available
                modelConfiguration = ModelConfiguration(schema: schema)
            }
        }

        do {
            return try ModelContainer(for: schema, configurations: [modelConfiguration])
        } catch {
            fatalError("Could not create ModelContainer: \(error)")
        }
    }()

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .modelContainer(sharedModelContainer)
        .commands {
            // Mac Catalyst Menu-Befehle
            CommandGroup(replacing: .newItem) {
                Button("Rechnung scannen") {
                    // TODO: Scan-Aktion implementieren
                }
                .keyboardShortcut("r", modifiers: .command)
            }
        }
    }
}

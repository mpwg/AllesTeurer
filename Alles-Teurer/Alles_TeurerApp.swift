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

        // For development, use default container location
        // App Groups will be enabled later for production with proper provisioning
        let modelConfiguration = ModelConfiguration(
            schema: schema,
            isStoredInMemoryOnly: isTesting
        )

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

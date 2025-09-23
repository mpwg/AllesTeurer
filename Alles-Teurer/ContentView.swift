//
//  ContentView.swift
//  Alles-Teurer
//
//  Created by Matthias Wallner-Géhri on 22.09.25.
//

import SwiftData
import SwiftUI

// MARK: - Decimal Currency Formatting Extension

extension Decimal {
    /// Formats the decimal as EUR currency for consistent display across the app
    static let euroFormatter: Decimal.FormatStyle.Currency = .currency(code: "EUR")
}

// MARK: - ContentView

struct ContentView: View {
    @Environment(\.modelContext) private var modelContext
    @Query private var kassenbons: [Kassenbon]

    var body: some View {
        NavigationSplitView {
            List {
                ForEach(kassenbons) { kassenbon in
                    NavigationLink {
                        KassenbonDetailView(kassenbon: kassenbon)
                    } label: {
                        KassenbonRowView(kassenbon: kassenbon)
                    }
                }
                .onDelete(perform: deleteKassenbons)
            }
            #if os(macOS)
                .navigationSplitViewColumnWidth(min: 180, ideal: 200)
            #endif
            .navigationTitle("Kassenbons")
            .toolbar {
                #if os(iOS)
                    ToolbarItem(placement: .navigationBarTrailing) {
                        EditButton()
                    }
                #endif
                ToolbarItem {
                    Button(action: addTestKassenbon) {
                        Label("Test Kassenbon hinzufügen", systemImage: "plus")
                    }
                }
            }
        } detail: {
            if kassenbons.isEmpty {
                ContentUnavailableView(
                    "Keine Kassenbons",
                    systemImage: "receipt",
                    description: Text(
                        "Fügen Sie Ihren ersten Kassenbon hinzu, um mit der Preisverfolgung zu beginnen."
                    )
                )
            } else {
                Text("Wählen Sie einen Kassenbon aus")
            }
        }
    }

    private func addTestKassenbon() {
        // MVVM: Use repository pattern through DataManager
        Task { @MainActor in
            do {
                let dataManager = DataManager(modelContainer: modelContext.container)

                let testKassenbon = Kassenbon(
                    geschaeftsname: "REWE",
                    scanDatum: Date.now,
                    gesamtbetrag: 47.83
                )

                // Test-Artikel hinzufügen
                let artikel1 = KassenbonArtikel(
                    name: "Milch 3,5%",
                    menge: 1,
                    einzelpreis: 1.49,
                    gesamtpreis: 1.49
                )

                let artikel2 = KassenbonArtikel(
                    name: "Brot Vollkorn",
                    menge: 1,
                    einzelpreis: 2.99,
                    gesamtpreis: 2.99
                )

                testKassenbon.artikel = [artikel1, artikel2]

                // Use DataManager repository pattern instead of direct ModelContext access
                try await dataManager.speichereKassenbon(testKassenbon)

            } catch {
                print("Fehler beim Speichern: \(error)")
            }
        }
    }

    private func deleteKassenbons(offsets: IndexSet) {
        // MVVM: Use repository pattern through DataManager
        Task { @MainActor in
            do {
                let dataManager = DataManager(modelContainer: modelContext.container)
                let kassenbonsToDelete = offsets.map { kassenbons[$0] }

                for kassenbon in kassenbonsToDelete {
                    try await dataManager.loescheKassenbon(mitID: kassenbon.persistentModelID)
                }
            } catch {
                print("Fehler beim Löschen: \(error)")
            }
        }
    }
}

struct KassenbonRowView: View {
    let kassenbon: Kassenbon

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(kassenbon.geschaeftsname)
                    .font(.headline)
                Spacer()
                Text(kassenbon.gesamtbetrag, format: Decimal.euroFormatter)
                    .font(.headline)
                    .foregroundColor(.primary)
            }

            HStack {
                Text(kassenbon.scanDatum, style: .date)
                    .font(.caption)
                    .foregroundColor(.secondary)

                Spacer()

                Text("\(kassenbon.artikel.count) Artikel")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 2)
    }
}

struct KassenbonDetailView: View {
    let kassenbon: Kassenbon

    var body: some View {
        List {
            Section {
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Text("Geschäft")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Spacer()
                        Text(kassenbon.geschaeftsname)
                            .font(.headline)
                    }

                    HStack {
                        Text("Datum")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Spacer()
                        Text(kassenbon.scanDatum, style: .date)
                    }

                    HStack {
                        Text("Gesamtbetrag")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Spacer()
                        Text(kassenbon.gesamtbetrag, format: Decimal.euroFormatter)
                            .font(.headline)
                            .foregroundColor(.primary)
                    }
                }
                .padding(.vertical, 4)
            }

            Section("Artikel") {
                ForEach(kassenbon.artikel) { artikel in
                    HStack {
                        VStack(alignment: .leading) {
                            Text(artikel.name)
                                .font(.body)
                            if artikel.menge > 1 {
                                Text(
                                    "\(artikel.menge)x \(artikel.einzelpreis, format: Decimal.euroFormatter)"
                                )
                                .font(.caption)
                                .foregroundColor(.secondary)
                            }
                        }

                        Spacer()

                        Text(artikel.gesamtpreis, format: Decimal.euroFormatter)
                            .font(.body)
                    }
                    .padding(.vertical, 2)
                }
            }
        }
        .navigationTitle("Kassenbon Details")
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview {
    ContentView()
        .modelContainer(for: Kassenbon.self, inMemory: true)
}

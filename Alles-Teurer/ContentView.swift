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
    @Query private var rechnungen: [Rechnung]

    var body: some View {
        NavigationSplitView {
            List {
                ForEach(rechnungen) { rechnung in
                    NavigationLink {
                        RechnungDetailView(rechnung: rechnung)
                    } label: {
                        RechnungRowView(rechnung: rechnung)
                    }
                }
                .onDelete(perform: deleteRechnungen)
            }
            #if os(macOS)
                .navigationSplitViewColumnWidth(min: 180, ideal: 200)
            #endif
            .navigationTitle("Rechnungen")
            .toolbar {
                #if os(iOS)
                    ToolbarItem(placement: .navigationBarTrailing) {
                        EditButton()
                    }
                #endif
                ToolbarItem {
                    Button(action: addTestRechnung) {
                        Label("Test Rechnung hinzufügen", systemImage: "plus")
                    }
                }
            }
        } detail: {
            if rechnungen.isEmpty {
                ContentUnavailableView(
                    "Keine Rechnungen",
                    systemImage: "receipt",
                    description: Text(
                        "Fügen Sie Ihre erste Rechnung hinzu, um mit der Preisverfolgung zu beginnen."
                    )
                )
            } else {
                Text("Wählen Sie eine Rechnung aus")
            }
        }
    }

    private func addTestRechnung() {
        // MVVM: Use repository pattern through DataManager
        Task { @MainActor in
            do {
                let dataManager = DataManager(modelContainer: modelContext.container)

                let testRechnung = Rechnung(
                    geschaeftsname: "BILLA",
                    scanDatum: Date.now,
                    gesamtbetrag: 47.83
                )

                // Test-Artikel hinzufügen
                let artikel1 = RechnungsArtikel(
                    name: "Milch 3,5%",
                    menge: 1,
                    einzelpreis: 1.49,
                    gesamtpreis: 1.49
                )

                let artikel2 = RechnungsArtikel(
                    name: "Brot Vollkorn",
                    menge: 1,
                    einzelpreis: 2.99,
                    gesamtpreis: 2.99
                )

                testRechnung.artikel = [artikel1, artikel2]

                // Use DataManager repository pattern instead of direct ModelContext access
                try await dataManager.speichereRechnung(testRechnung)

            } catch {
                print("Fehler beim Speichern: \(error)")
            }
        }
    }

    private func deleteRechnungen(offsets: IndexSet) {
        // MVVM: Use repository pattern through DataManager
        Task { @MainActor in
            do {
                let dataManager = DataManager(modelContainer: modelContext.container)
                let rechnungenToDelete = offsets.map { rechnungen[$0] }

                for rechnung in rechnungenToDelete {
                    try await dataManager.loescheRechnung(mitID: rechnung.persistentModelID)
                }
            } catch {
                print("Fehler beim Löschen: \(error)")
            }
        }
    }
}

struct RechnungRowView: View {
    let rechnung: Rechnung

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(rechnung.geschaeftsname)
                    .font(.headline)
                Spacer()
                Text(rechnung.gesamtbetrag, format: Decimal.euroFormatter)
                    .font(.headline)
                    .foregroundColor(.primary)
            }

            HStack {
                Text(rechnung.scanDatum, style: .date)
                    .font(.caption)
                    .foregroundColor(.secondary)

                Spacer()

                Text("\(rechnung.artikel.count) Artikel")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 2)
    }
}

struct RechnungDetailView: View {
    let rechnung: Rechnung

    var body: some View {
        List {
            Section {
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Text("Geschäft")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Spacer()
                        Text(rechnung.geschaeftsname)
                            .font(.headline)
                    }

                    HStack {
                        Text("Datum")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Spacer()
                        Text(rechnung.scanDatum, style: .date)
                    }

                    HStack {
                        Text("Gesamtbetrag")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Spacer()
                        Text(rechnung.gesamtbetrag, format: Decimal.euroFormatter)
                            .font(.headline)
                            .foregroundColor(.primary)
                    }
                }
                .padding(.vertical, 4)
            }

            Section("Artikel") {
                ForEach(rechnung.artikel) { artikel in
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
        .navigationTitle("Rechnungs-Details")
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview {
    ContentView()
        .modelContainer(for: Rechnung.self, inMemory: true)
}

//
//  ContentView.swift
//  Alles-Teurer
//
//  Created by Matthias Wallner-Géhri on 22.09.25.
//

import SwiftData
import SwiftUI

// Services & ViewModels
// OCRService and ScannerViewModel are defined in the same target

// MARK: - Decimal Currency Formatting Extension

extension Decimal {
    /// Formats the decimal as EUR currency for consistent display across the app
    static let euroFormatter: Decimal.FormatStyle.Currency = .currency(code: "EUR")
}

// MARK: - ContentView

struct ContentView: View {
    @Environment(\.modelContext) private var modelContext
    @Query private var rechnungen: [Rechnung]
    @State private var ocrService = OCRService()
    @State private var scannerVM: ScannerViewModel?
    @State private var showingSaveAlert = false
    @State private var lastSaveCount = 0

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
                ToolbarItem {
                    Button(action: runScanDemo) {
                        Label("Scan Demo", systemImage: "camera.viewfinder")
                    }
                    .accessibilityLabel("Scan Demo")
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
        .task {
            // Initialize scanner view model with current ModelContainer
            scannerVM = ScannerViewModel(modelContainer: modelContext.container)
        }
        .alert(
            "Scan gespeichert", isPresented: $showingSaveAlert,
            actions: {
                Button("OK", role: .cancel) {}
            },
            message: {
                Text("\(lastSaveCount) Artikel hinzugefügt")
            })
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

extension ContentView {
    private func runScanDemo() {
        Task { @MainActor in
            // Ensure ViewModel is initialized even if .task hasn't run yet (avoids race in UI tests)
            if scannerVM == nil {
                scannerVM = ScannerViewModel(modelContainer: modelContext.container)
            }
            await ocrService.verarbeiteBildDaten(Data())
            guard case .success(let recognized) = ocrService.zustand, let vm = scannerVM else {
                return
            }
            // Map OCR DTO to ScannerViewModel DTO
            let items = recognized.items.map { item in
                ScannerViewModel.RecognizedItemDTO(
                    name: item.name,
                    quantity: item.quantity,
                    unitPrice: item.unitPrice,
                    totalPrice: item.totalPrice
                )
            }
            let dto = ScannerViewModel.RecognizedReceiptDTO(
                store: recognized.store,
                total: recognized.total,
                items: items,
                rawText: recognized.rawText
            )
            await vm.saveRecognized(dto)
            if case .saved(let count) = vm.saveState {
                lastSaveCount = count
                showingSaveAlert = true
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

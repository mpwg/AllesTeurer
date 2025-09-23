//
//  ReceiptScannerViewModel.swift
//  Alles-Teurer
//
//  Created by AI Agent on 23.09.25.
//  MVVM Architecture with @Observable pattern and async/await
//

import Foundation
import SwiftData
import SwiftUI

/// ReceiptScannerViewModel - Manages receipt scanning workflow
/// Complies with MVVM + Repository Pattern using async/await
@MainActor
@Observable
final class ReceiptScannerViewModel {

    // MARK: - Dependencies (Repository Pattern)

    private let ocrService: OCRService
    private let dataManager: DataManager

    // MARK: - Published State (Observable Pattern)

    var scanState: ScanState = .idle
    var currentOCRResult: OCRResult?
    var errorMessage: String?
    var isProcessing: Bool = false

    // MARK: - Computed Properties

    var canScanReceipt: Bool {
        !isProcessing && scanState != .processing
    }

    var shouldShowError: Bool {
        errorMessage != nil
    }

    // MARK: - Initialization

    init(modelContainer: ModelContainer) {
        self.ocrService = OCRService()
        self.dataManager = DataManager(modelContainer: modelContainer)
    }

    // MARK: - Public Interface (Async/Await Pattern)

    /// Processes receipt image data using OCR and saves to repository
    func processReceiptImage(_ imageData: Data) async {
        guard canScanReceipt else { return }

        await resetState()
        isProcessing = true
        scanState = .processing

        do {
            // Step 1: OCR Processing
            let ocrResult = try await ocrService.verarbeiteImageData(imageData)
            currentOCRResult = ocrResult

            // Step 2: Create and save via repository
            try await dataManager.createAndSaveReceipt(
                geschaeftsname: ocrResult.geschaeftsname,
                gesamtbetrag: ocrResult.gesamtbetrag,
                artikelData: ocrResult.artikel,
                roherText: ocrResult.roherText,
                vertrauen: ocrResult.vertrauen
            )

            // Step 3: Update UI state
            scanState = .success(ocrResult)

        } catch {
            await handleError(error)
        }

        isProcessing = false
    }

    /// Retries the last failed scan operation
    func retryScan() async {
        guard case .error = scanState else { return }

        // Reset error state and attempt to reprocess
        await resetState()
    }

    /// Manually corrects OCR result and saves
    func saveManuallyCorrectOCRResult(_ correctedResult: OCRResult) async {
        isProcessing = true

        do {
            try await dataManager.createAndSaveReceipt(
                geschaeftsname: correctedResult.geschaeftsname,
                gesamtbetrag: correctedResult.gesamtbetrag,
                artikelData: correctedResult.artikel,
                roherText: correctedResult.roherText,
                vertrauen: correctedResult.vertrauen
            )
            scanState = .success(correctedResult)
        } catch {
            await handleError(error)
        }

        isProcessing = false
    }

    /// Resets scanner state to idle
    func resetToIdle() async {
        await resetState()
        scanState = .idle
    }

    // MARK: - Private Implementation

    private func resetState() async {
        scanState = .idle
        currentOCRResult = nil
        errorMessage = nil
        isProcessing = false
    }

    private func handleError(_ error: any Error) async {
        scanState = .error(error.localizedDescription)
        errorMessage = error.localizedDescription
    }
}

// MARK: - ScanState Definition

/// Current state of the receipt scanning process
enum ScanState: Equatable {
    case idle
    case processing
    case success(OCRResult)
    case error(String)

    static func == (lhs: ScanState, rhs: ScanState) -> Bool {
        switch (lhs, rhs) {
        case (.idle, .idle), (.processing, .processing):
            return true
        case (.success(let lhsResult), .success(let rhsResult)):
            return lhsResult.geschaeftsname == rhsResult.geschaeftsname
        case (.error(let lhsError), .error(let rhsError)):
            return lhsError == rhsError
        default:
            return false
        }
    }
}

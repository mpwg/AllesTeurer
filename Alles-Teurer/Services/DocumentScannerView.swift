//
//  DocumentScannerView.swift
//  Alles-Teurer
//
//  Created by Copilot on 25.09.25.
//

import Foundation
import SwiftUI

#if os(iOS)
import VisionKit

@available(iOS 16.0, *)
struct DocumentScannerView: UIViewControllerRepresentable {
    final class Coordinator: NSObject, VNDocumentCameraViewControllerDelegate {
        func documentCameraViewControllerDidCancel(_ controller: VNDocumentCameraViewController) {
            controller.dismiss(animated: true)
        }
    }

    func makeUIViewController(context: Context) -> VNDocumentCameraViewController {
        let vc = VNDocumentCameraViewController()
        vc.delegate = context.coordinator
        return vc
    }

    func updateUIViewController(_ uiViewController: VNDocumentCameraViewController, context: Context) {}

    func makeCoordinator() -> Coordinator { Coordinator() }
}
#endif

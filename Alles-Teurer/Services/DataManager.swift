//
//  DataManager.swift
//  Alles-Teurer
//
//  Created by AI Agent on 22.09.25.
//  Properly structured DataManager with SwiftData predicate best practices
//

import Foundation
import SwiftData
import SwiftUI

/// DataManager - Actor für thread-sichere Datenoperationen
@ModelActor
actor DataManager {
    
    // MARK: - Kassenbon Operations
    
    /// Speichert einen neuen Kassenbon
    func speichereKassenbon(_ kassenbon: Kassenbon) throws {
        modelContext.insert(kassenbon)
        try modelContext.save()
        
        // Aktualisiere verknüpfte Produkte
        for artikel in kassenbon.artikel {
            try aktualisiereProduktPreis(fuerArtikel: artikel, kassenbon: kassenbon)
        }
    }
    
    /// Löscht einen Kassenbon
    func loescheKassenbon(_ kassenbon: Kassenbon) throws {
        modelContext.delete(kassenbon)
        try modelContext.save()
    }
    
    /// Lädt alle Kassenbons für ein bestimmtes Geschäft
    func ladeKassenbons(fuerGeschaeft geschaeft: Geschaeft) throws -> [Kassenbon] {
        let descriptor = FetchDescriptor<Kassenbon>(
            sortBy: [SortDescriptor(\.scanDatum, order: .reverse)]
        )
        let alleKassenbons = try modelContext.fetch(descriptor)
        return alleKassenbons.filter { $0.geschaeft?.name == geschaeft.name }
    }
    
    /// Lädt alle Kassenbons
    func ladeAlleKassenbons() throws -> [Kassenbon] {
        let descriptor = FetchDescriptor<Kassenbon>(
            sortBy: [SortDescriptor(\.scanDatum, order: .reverse)]
        )
        return try modelContext.fetch(descriptor)
    }
    
    // MARK: - Produkt Operations
    
    /// Erstellt oder aktualisiert ein Produkt basierend auf einem Kassenbon-Artikel
    func aktualisiereProduktPreis(fuerArtikel artikel: KassenbonArtikel, kassenbon: Kassenbon) throws {
        // Suche nach existierendem Produkt (verwende Swift filtering statt komplexer Prädikate)
        let descriptor = FetchDescriptor<Produkt>()
        let alleProdukte = try modelContext.fetch(descriptor)
        let existierendeProdukte = alleProdukte.filter { $0.name.localizedCaseInsensitiveContains(artikel.name) }
        
        let produkt: Produkt
        if let existierend = existierendeProdukte.first {
            produkt = existierend
        } else {
            // Erstelle neues Produkt mit intelligenter Kategorisierung
            let kategorie = kategorisiereProdukt(artikel.name)
            produkt = Produkt(name: artikel.name, kategorie: kategorie)
            modelContext.insert(produkt)
        }
        
        // Füge Preiseintrag hinzu
        let preisEintrag = PreisEintrag(
            preis: artikel.einzelpreis,
            datum: kassenbon.scanDatum,
            geschaeftsname: kassenbon.geschaeftsname,
            menge: Decimal(artikel.menge),
            einheit: artikel.einheit ?? "Stück"
        )
        
        produkt.preisverlauf.append(preisEintrag)
        artikel.produkt = produkt
        
        // Aktualisiere Statistiken
        produkt.aktualisierePreisstatistiken()
        
        try modelContext.save()
    }
    
    /// Berechnet Inflation für ein Produkt
    func berechneInflation(fuerProdukt produkt: Produkt) async -> Double {
        return produkt.inflationsrate()
    }
    
    /// Lädt Produkte nach Kategorie
    func ladeProdukte(kategorie: ProduktKategorie?) throws -> [Produkt] {
        if let kategorie = kategorie {
            // Use proper predicate with local variable for external values
            let suchKategorie = kategorie
            let descriptor = FetchDescriptor<Produkt>(
                predicate: #Predicate<Produkt> { produkt in
                    produkt.kategorie == suchKategorie
                },
                sortBy: [SortDescriptor(\.name)]
            )
            return try modelContext.fetch(descriptor)
        } else {
            let descriptor = FetchDescriptor<Produkt>(
                sortBy: [SortDescriptor(\.name)]
            )
            return try modelContext.fetch(descriptor)
        }
    }
    
    // MARK: - Geschäft Operations
    
    /// Erstellt oder aktualisiert ein Geschäft
    func erstelleOderAktualiereGeschaeft(name: String, typ: GeschaeftTyp = .supermarkt) throws -> Geschaeft {
        // Use proper predicate with localizedStandardContains for case-insensitive search
        let suchName = name
        let descriptor = FetchDescriptor<Geschaeft>(
            predicate: #Predicate<Geschaeft> { geschaeft in
                geschaeft.name.localizedStandardContains(suchName)
            }
        )
        
        let existierendeGeschaefte = try modelContext.fetch(descriptor)
        
        if let existierend = existierendeGeschaefte.first {
            return existierend
        } else {
            let geschaeft = Geschaeft(name: name, typ: typ)
            modelContext.insert(geschaeft)
            try modelContext.save()
            return geschaeft
        }
    }
    
    // MARK: - Analytics
    
    /// Berechnet Gesamtinflation über alle Produkte
    func berechneGesamtinflation() throws -> Double {
        let kategorie: ProduktKategorie? = nil
        let alleProdukte = try ladeProdukte(kategorie: kategorie)
        let inflationsraten = alleProdukte.compactMap { produkt in
            let inflation = produkt.inflationsrate()
            return inflation > 0 ? inflation : nil
        }
        
        guard !inflationsraten.isEmpty else { return 0.0 }
        
        let durchschnitt = inflationsraten.reduce(0, +) / Double(inflationsraten.count)
        return durchschnitt
    }
    
    /// Lädt die teuersten Produkte des Monats
    func ladeTeuersteProdukteDesMonats() throws -> [Produkt] {
        let kalender = Calendar.current
        let heute = Date()
        let monatsBeginn = kalender.dateInterval(of: .month, for: heute)?.start ?? heute
        
        // Create local variable for external date value
        let startDatum = monatsBeginn
        let descriptor = FetchDescriptor<Produkt>(
            predicate: #Predicate<Produkt> { produkt in
                produkt.letzteAktualisierung >= startDatum
            },
            sortBy: [SortDescriptor(\.hoechsterPreis, order: .reverse)]
        )
        
        return try modelContext.fetch(descriptor)
    }
    
    /// Lädt Kassenbons des letzten Monats für Statistiken
    func ladeKassenbonsDesLetztenMonats() throws -> [Kassenbon] {
        let kalender = Calendar.current
        let heute = Date()
        let monatsBeginn = kalender.dateInterval(of: .month, for: heute)?.start ?? heute
        
        // Create local variable for external date value
        let startDatum = monatsBeginn
        let descriptor = FetchDescriptor<Kassenbon>(
            predicate: #Predicate<Kassenbon> { kassenbon in
                kassenbon.scanDatum >= startDatum
            },
            sortBy: [SortDescriptor(\.scanDatum, order: .reverse)]
        )
        
        return try modelContext.fetch(descriptor)
    }
    
    // MARK: - Helper Functions
    
    /// Intelligente Kategorisierung von Produkten basierend auf dem Namen
    private func kategorisiereProdukt(_ name: String) -> ProduktKategorie {
        let nameLower = name.lowercased()
        
        // Getränke
        if nameLower.contains("wasser") || nameLower.contains("saft") || 
           nameLower.contains("bier") || nameLower.contains("wein") ||
           nameLower.contains("cola") || nameLower.contains("limo") {
            return .getraenke
        }
        
        // Molkereiprodukte
        if nameLower.contains("milch") || nameLower.contains("joghurt") || 
           nameLower.contains("käse") || nameLower.contains("butter") ||
           nameLower.contains("quark") || nameLower.contains("sahne") {
            return .molkereiprodukte
        }
        
        // Fleisch & Wurst
        if nameLower.contains("fleisch") || nameLower.contains("wurst") || 
           nameLower.contains("schinken") || nameLower.contains("hack") ||
           nameLower.contains("steak") || nameLower.contains("hähnchen") {
            return .fleisch
        }
        
        // Obst & Gemüse
        if nameLower.contains("apfel") || nameLower.contains("banane") || 
           nameLower.contains("tomate") || nameLower.contains("kartoffel") ||
           nameLower.contains("salat") || nameLower.contains("zwiebel") {
            return .obst
        }
        
        // Backwaren
        if nameLower.contains("brot") || nameLower.contains("brötchen") || 
           nameLower.contains("kuchen") || nameLower.contains("semmel") {
            return .backwaren
        }
        
        // Süßigkeiten
        if nameLower.contains("schokolade") || nameLower.contains("bonbon") || 
           nameLower.contains("gummi") || nameLower.contains("keks") {
            return .suessigkeiten
        }
        
        // Körperpflege
        if nameLower.contains("shampoo") || nameLower.contains("seife") || 
           nameLower.contains("creme") || nameLower.contains("zahnpasta") {
            return .koerperpflege
        }
        
        // Haushaltsprodukte
        if nameLower.contains("waschmittel") || nameLower.contains("spülmittel") || 
           nameLower.contains("reiniger") || nameLower.contains("papier") {
            return .haushaltsprodukte
        }
        
        // Tiefkühlung
        if nameLower.contains("tiefkühl") || nameLower.contains("frozen") || 
           nameLower.contains("eis") {
            return .tiefkuehlung
        }
        
        // Standard: Lebensmittel
        return .lebensmittel
    }
}
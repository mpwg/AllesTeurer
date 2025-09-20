## Anforderungen

### Funktionale Anforderungen
- [ ] Produkt-Eingabeinterface (Suche, Barcode-Scan, manuelle Eingabe)
- [ ] API-Integration für verschiedene Bezugsquellen
- [ ] Preisaggregation inkl. Lieferkosten
- [ ] Sortierung nach Gesamtkosten
- [ ] Verfügbarkeitscheck

### Technische Komponenten
```
/services
  ├── ProductSearchService.swift
  ├── PriceAggregator.swift
  └── /providers
      ├── AmazonProvider.swift
      ├── EbayProvider.swift
      └── LocalStoreProvider.swift
```

### User Stories
1. **Als Nutzer** möchte ich ein Produkt eingeben und alle verfügbaren Bezugsquellen mit Preisen sehen
2. **Als Nutzer** möchte ich die Gesamtkosten inkl. Lieferung auf einen Blick erkennen
3. **Als Nutzer** möchte ich Ergebnisse nach Preis, Lieferzeit oder Entfernung sortieren können

### Akzeptanzkriterien
- [ ] Mindestens 5 verschiedene Bezugsquellen integriert
- [ ] Reaktionszeit < 3 Sekunden für Suchanfragen
- [ ] Korrekte Berechnung der Gesamtkosten
- [ ] Offline-Caching für häufig gesuchte Produkte

### API-Integrationen benötigt
- Amazon Product API
- eBay Finding API
- Google Shopping API
- Lokale Supermarkt-APIs (Rewe, Edeka, etc.)
- Preisvergleichsportale (Idealo, Geizhals)

## Dependencies
- #2 (Datenbank-Struktur)
- #4 (Kassenbon-Scanner)

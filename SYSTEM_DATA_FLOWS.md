# System Data Flow and Interaction Diagrams

## Phase 1 Foundation - Data Architecture

### Database Schema Relationships

```mermaid
erDiagram
    Store ||--o{ Receipt : "has many"
    Receipt ||--o{ PriceRecord : "contains"
    Product ||--o{ PriceRecord : "matches to"

    Store {
        TEXT id PK
        TEXT name
        TEXT address
        TEXT city
        TEXT postal_code
        TEXT country
        INTEGER created_at
        INTEGER updated_at
    }

    Receipt {
        TEXT id PK
        TEXT store_id FK
        TEXT store_name
        REAL total_amount
        REAL tax_amount
        REAL discount_amount
        INTEGER receipt_date
        INTEGER scan_date
        TEXT receipt_number
        TEXT cashier_id
        TEXT payment_method
        TEXT currency
        TEXT raw_ocr_text
        REAL ocr_confidence
        TEXT image_path
        INTEGER is_validated
        TEXT notes
        INTEGER created_at
        INTEGER updated_at
    }

    Product {
        TEXT id PK
        TEXT name
        TEXT normalized_name
        TEXT category
        TEXT subcategory
        TEXT brand
        TEXT barcode
        TEXT unit_type
        INTEGER created_at
        INTEGER updated_at
    }

    PriceRecord {
        TEXT id PK
        TEXT receipt_id FK
        TEXT product_id FK
        TEXT product_name
        TEXT normalized_product_name
        REAL quantity
        REAL unit_price
        REAL total_price
        TEXT unit_type
        TEXT brand
        TEXT category
        REAL tax_rate
        REAL discount_amount
        TEXT raw_text
        INTEGER line_number
        INTEGER is_matched
        REAL match_confidence
        INTEGER created_at
        INTEGER updated_at
    }
```

### Data Flow Through Multiplatform Architecture

```mermaid
graph TD
    A[Camera/Image Input] --> B[Platform-Specific OCR]
    B --> C[OCRResult Model]
    C --> D[Receipt Processing UseCase]
    D --> E[Receipt Model]
    D --> F[PriceRecord Models]

    E --> G[Receipt Repository]
    F --> H[PriceRecord Repository]

    G --> I[SQLDelight Receipt Queries]
    H --> J[SQLDelight PriceRecord Queries]

    I --> K[Platform Database Driver]
    J --> K

    K --> L[Android SQLite]
    K --> M[iOS Native SQLite]
    K --> N[JVM JDBC SQLite]

    O[Product Matching] --> P[Product Repository]
    P --> Q[SQLDelight Product Queries]
    Q --> K

    R[Store Detection] --> S[Store Repository]
    S --> T[SQLDelight Store Queries]
    T --> K

    style A fill:#e1f5fe
    style B fill:#fff3e0
    style C fill:#f3e5f5
    style D fill:#e8f5e8
    style E fill:#f3e5f5
    style F fill:#f3e5f5
    style G fill:#fff9c4
    style H fill:#fff9c4
    style I fill:#ffecb3
    style J fill:#ffecb3
    style K fill:#ffcdd2
    style L fill:#ffcdd2
    style M fill:#ffcdd2
    style N fill:#ffcdd2
```

### Repository Pattern Implementation Flow

```mermaid
sequenceDiagram
    participant UI as Compose UI
    participant VM as ViewModel
    participant UC as UseCase
    participant REPO as Repository
    participant DB as SQLDelight Database
    participant DRIVER as Platform Driver

    UI->>VM: User Action (Scan Receipt)
    VM->>UC: processReceipt(ocrResult)
    UC->>REPO: saveReceipt(receipt)
    REPO->>DB: insertReceipt(...)
    DB->>DRIVER: Execute SQL INSERT
    DRIVER->>DB: Success/Failure
    DB->>REPO: Query Result
    REPO->>UC: Repository Result
    UC->>VM: UseCase Result
    VM->>UI: Update UI State

    Note over UC,REPO: Business Logic Layer
    Note over DB,DRIVER: Data Persistence Layer
```

### Multiplatform Code Organization

```mermaid
graph TB
    subgraph "commonMain (Shared Code)"
        A[Data Models]
        B[Repository Interfaces]
        C[Use Cases]
        D[SQLDelight Queries]
        E[Database Manager]
    end

    subgraph "androidMain (Android)"
        F[DatabaseDriverFactory.android]
        G[Android Context Integration]
    end

    subgraph "iosMain (iOS)"
        H[DatabaseDriverFactory.ios]
        I[iOS Native Integration]
    end

    subgraph "jvmMain (Desktop)"
        J[DatabaseDriverFactory.jvm]
        K[JDBC Integration]
    end

    A --> F
    A --> H
    A --> J

    B --> E
    C --> B
    D --> E

    E --> F
    E --> H
    E --> J

    F --> G
    H --> I
    J --> K

    style A fill:#e3f2fd
    style B fill:#e3f2fd
    style C fill:#e3f2fd
    style D fill:#e3f2fd
    style E fill:#e3f2fd
```

### Data Serialization Flow

```mermaid
graph LR
    A[Kotlin Data Models] --> B[kotlinx.serialization]
    B --> C[JSON Serialization]
    B --> D[Binary Serialization]

    C --> E[API Communication]
    C --> F[Local Storage]
    C --> G[Data Export]

    D --> H[Performance Critical Paths]

    I[Platform-Specific Data] --> J[expect/actual Pattern]
    J --> K[Shared Serialization Logic]
    K --> B

    style A fill:#e8f5e8
    style B fill:#fff3e0
    style C fill:#f3e5f5
    style D fill:#f3e5f5
    style E fill:#e1f5fe
    style F fill:#e1f5fe
    style G fill:#e1f5fe
    style H fill:#ffecb3
```

### Error Handling and Validation Chain

```mermaid
graph TD
    A[Data Input] --> B[Model Validation]
    B --> C{Validation Pass?}
    C -->|Yes| D[Repository Operation]
    C -->|No| E[Validation Error]

    D --> F[SQLDelight Query]
    F --> G{Database Success?}
    G -->|Yes| H[Success Result]
    G -->|No| I[Database Error]

    E --> J[Error Propagation]
    I --> J
    J --> K[ViewModel Error Handling]
    K --> L[UI Error Display]

    B --> M[Business Rule Validation]
    M --> N{Rules Pass?}
    N -->|Yes| D
    N -->|No| O[Business Logic Error]
    O --> J

    style A fill:#e3f2fd
    style B fill:#fff3e0
    style C fill:#ffecb3
    style D fill:#e8f5e8
    style E fill:#ffcdd2
    style F fill:#f3e5f5
    style G fill:#ffecb3
    style H fill:#c8e6c9
    style I fill:#ffcdd2
    style J fill:#ffcdd2
    style K fill:#fff9c4
    style L fill:#ffecb3
```

### Analytics Data Pipeline

```mermaid
graph LR
    A[PriceRecord Data] --> B[Price Analytics Engine]
    B --> C[Trend Calculation]
    B --> D[Inflation Analysis]
    B --> E[Category Aggregation]

    C --> F[Price History Charts]
    D --> G[Inflation Indicators]
    E --> H[Spending Categories]

    I[Store Data] --> J[Store Comparison]
    J --> K[Best Price Recommendations]

    A --> L[Product Matching Algorithm]
    L --> M[Automatic Categorization]
    M --> N[Improved Product Database]

    O[Receipt Data] --> P[Shopping Pattern Analysis]
    P --> Q[Budget Insights]

    style A fill:#e3f2fd
    style B fill:#fff3e0
    style C fill:#e8f5e8
    style D fill:#e8f5e8
    style E fill:#e8f5e8
    style F fill:#f3e5f5
    style G fill:#f3e5f5
    style H fill:#f3e5f5
    style I fill:#e1f5fe
    style J fill:#fff9c4
    style K fill:#c8e6c9
    style L fill:#ffecb3
    style M fill:#ffecb3
    style N fill:#c8e6c9
    style O fill:#e1f5fe
    style P fill:#fff9c4
    style Q fill:#c8e6c9
```

## Key Data Flow Patterns

### 1. Receipt Processing Pipeline

1. **Image Capture** → Platform-specific camera service
2. **OCR Processing** → Platform-specific OCR service (Vision/ML Kit)
3. **Data Extraction** → OCRResult model creation
4. **Receipt Parsing** → Receipt and PriceRecord model creation
5. **Store Matching** → Store identification and linking
6. **Product Matching** → Product identification and categorization
7. **Database Storage** → SQLDelight persistence
8. **UI Update** → ViewModel state management

### 2. Product Matching Workflow

1. **Raw Product Name** → From OCR text extraction
2. **Text Normalization** → Remove special chars, normalize case
3. **Fuzzy Matching** → Compare against existing products
4. **Confidence Scoring** → Calculate match probability
5. **Manual Review** → User validation for low confidence
6. **Product Creation** → Create new product if no match
7. **Price History** → Link to historical price data

### 3. Analytics Data Processing

1. **Raw Purchase Data** → PriceRecord entries
2. **Data Aggregation** → Group by product, category, time period
3. **Trend Analysis** → Calculate price changes over time
4. **Statistical Analysis** → Mean, median, variance calculations
5. **Visualization Data** → Format for chart components
6. **Insight Generation** → Identify significant trends
7. **Report Creation** → Generate user-friendly summaries

## Database Query Performance Patterns

### Optimized Query Strategies

1. **Indexed Lookups**: All foreign keys and search fields indexed
2. **Batch Operations**: Bulk inserts for receipt line items
3. **Selective Queries**: Only fetch required columns
4. **Aggregation Queries**: Database-level calculations for analytics
5. **Pagination Support**: Limit/offset for large result sets

### Caching Strategy

1. **Repository Level**: Cache frequently accessed data
2. **ViewModel Level**: Maintain UI state consistency
3. **Database Level**: SQLDelight query result caching
4. **Image Cache**: Store receipt images efficiently

This data flow architecture ensures:

- **Type Safety**: SQLDelight generates type-safe queries
- **Performance**: Optimized database schemas and queries
- **Consistency**: Same data model across all platforms
- **Scalability**: Support for large datasets and complex analytics
- **Maintainability**: Clear separation of concerns and data flow

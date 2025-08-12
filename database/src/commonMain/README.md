# Database Module Package Structure

de.gematik.ti.erp.app.database
│
├── realm
│ ├── v1 // Legacy Realm V1 implementation (Internal)
│ │ ├── migrations
│ │ ├── feature
│ │ ├────├── entity
│ │ └────└── datasource

│ │ └── datasource
│ └── v2 // Current Realm V2 schema (modern, active) (Internal)
│ │ ├── migrations
│ │ ├── feature
│ │ ├────├── entity
│ │ └────└── datasource
│
├── bridge // ✅ Publicly exposed implementation that delegates or compares
│ ├── feature.datasource
│ ├── logger // error logger that can be used to check if both the db versions match
│ └── verifier // (optional) shared logic for V1-V2 comparison
│
├── settings // SharedPrefs / Multiplatform Settings implementation
│ ├── SettingsProvider.kt
│ ├── SettingsKeys.kt
│ └── SharedPreferencesStore.kt
│
├── api // Interface contracts for localDataStore.
│
└── util // Common utilities (date parsing, ID gen, etc.)

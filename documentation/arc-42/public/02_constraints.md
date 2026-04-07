# 2 – Constraints

This section documents the boundary conditions and non-functional restrictions that shape our modularization of the existing **das e-Rezept** Android application.

## 2.1 Technical Constraints

- **Android SDK Version**: Must remain compatible with Android SDK 36 (Android 16) as per product roadmap.
- **Minimum API Level**: Support API level 26 (Android 8.0) and above to cover all target devices.
- **Language & Tooling**: Codebase is in Kotlin; Gradle must remain the build system, with no migration to other build tools in this phase.
- **Module Isolation**: Feature, shared, and lower-level modules must compile independently without circular dependencies.
- **CI/CD Pipeline**: Existing Jenkins pipelines must support module-level tasks; no overhaul of CI infrastructure.

## 2.2 Organizational Constraints

- **Team Structure**: Single development team; module boundaries must remain clear to support maintainability and future growth.
- **Release Cadence**: Monthly releases; modularization effort must not delay scheduled production deployments.
- **Demo Mode**: Demo-mode modules run independently by mocking repository interfaces with predefined mock data, without relying on a separate mock server.

## 2.3 Regulatory & Legal Constraints

- **GDPR Compliance**: All personally identifiable information (PII) must continue to be handled per GDPR; encryption-at-rest and in-transit must be preserved.
- **E-Health Standards**: Must adhere to the German e-Health Act (Digitale-Versorgung-Gesetz) and gematik interface specifications.
- **Data Residency**: No change to data storage location – all patient data stays on certified servers within EU jurisdictions.

## 2.4 Out-of-Scope Constraints

- **UI/UX Redesign**: The current user interface must remain unchanged; visual updates are not permitted during this refactoring phase.
- **Backend API Changes**: No modifications to existing ERP or prescription APIs; only the client-side architecture is refactored.
# 4 – Solution Strategy

This section summarizes the core solution ideas and explains how we meet our most important quality requirements while refactoring the monolithic **das e-Rezept** Android app.

## 4.1 Content & Motivation

We transform the existing monolith into a modular architecture to achieve:

- **Separation of Concerns**: Isolate features, shared services, and utilities into dedicated modules for clearer boundaries and easier maintenance.  
- **Modular Composition**: Build the app from independent feature, shared (Kotlin Multiplatform), lower-level, and data/mock modules orchestrated at runtime.  
- **Incremental Migration**: Refactor one module at a time, ensuring behavioral parity and passing CI tests after each step to minimize risk.  
- **Convention over Configuration**: Use a shared Gradle conventions plugin (e.g., `base-android-library`, `base-multiplatform`, `base-android-application`) to standardize dependency versions, compile targets, and publishing settings.  
- **Dependency Injection**: Leverage Kodein for decoupling implementations and enabling unit/UI tests through injected dependencies.  
- **Feature-Oriented Design**: Encapsulate UI and domain logic per feature (e.g., `digas`, `prescriptions`, `pharmacies`, `messages`, `profile`, `gid`, `others`, `demo.mode`).  
- **Shared KMP Libraries**: Provide multiplatform modules for core services (authentication, navigation, logging) to maximize code reuse.  
- **Lower-Level Libraries**: Offer foundational components (network client via Retrofit/Ktor, encrypted persistence with Room, FHIR parser) as standalone artifacts.  
- **Demo-Mode Modules**: Enable isolated UI and business logic development by mocking repositories with predefined data, without requiring a mock server.

## 4.2 Quality Requirements

We address our key quality attributes as follows:

- **Build Performance**: Module isolation enables incremental and parallel compilation, targeting a ≥30% reduction in full clean build time.  
- **Maintainability**: Clear module boundaries and a conventions plugin reduce inter-module coupling and simplify onboarding.  
- **Module Independence**: Each module compiles and tests in isolation, verified by CI before integration.  
- **Reusability & Distribution**: Modules can be published individually to internal Maven repos, encouraging reuse in other projects.  
- **Zero Regression**: CI smoke tests against production ERP endpoints ensure existing functionality remains intact throughout refactoring.

## 4.3 Modularization Approach

- **Feature Modules**: UI and domain logic for business flows (e.g., `digas`, `prescriptions`, `pharmacies`, `messages`, `profile`, `gid`, `others`, `demo.mode`).  
- **Shared KMP Modules**: Multiplatform utilities (`authentication`, `navigation`, `logging`).  
- **Lower-Level Modules**: Core libraries (`network`, `persistence`, `parser`).  
- **Data/Mock Modules**: Stub repositories and static fixtures for demo-mode and CI.  
- **Additional Modules**: Include components like `app-core`, `idp-communication`, and `fhir-parser` as defined in our detailed architecture.

## 4.4 Project Structure & Build Setup

- **settings.gradle.kts** registers all modules (`:feature:digas`, `:shared:core`, `:lower-level:network`, etc.).  
- **Conventions Plugin** enforces Kotlin targets (JVM, Android), Java compatibility, and consistent publishing.  
- **Version Catalog** centralizes dependencies to prevent version skew.  
- **Gradle Configuration** optimized for parallel execution and configuration caching.

## 4.5 Orchestration & Navigation

- **Feature-Orchestrator Module**: Coordinates navigation and lifecycle across feature modules.  
- **Route Registration**: Features register endpoints via a shared routing API, enabling loose coupling.  
- **Feature Flags**: Toggle demo-mode and optional features at runtime for targeted testing and rollout.

## 4.6 Testing & CI Strategy

- **Unit Tests**: JUnit + MockK tests per module targeting ≥80% coverage.  
- **Integration Smoke Tests**: CI job builds all modules and verifies critical flows against production ERP.  
- **Demo-Mode Validation**: Separate CI pipeline executes UI tests in demo-mode using mock data.  
- **Parallel Builds**: Gradle’s parallelism reduces overall pipeline runtime and speeds developer feedback.  
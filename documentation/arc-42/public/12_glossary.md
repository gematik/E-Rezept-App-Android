## 12 – Glossary <!-- {docsify-ignore} -->

This section defines the key terms and concepts used throughout the architecture documentation, ensuring a shared understanding among all stakeholders.

| Term                                | Definition |
|-------------------------------------|------------|
| **das e-Rezept**                    | Germany’s official electronic prescription system, enabling secure digital transmission of medical prescriptions. |
| **DiGA**                            | Digitale Gesundheitsanwendungen: approved health apps reimbursed by statutory health insurance, integrated via the das e-Rezept workflow. |
| **Monolith**                        | A single-module application where all code (UI, domain, data) lives in one Gradle project. |
| **Module**                          | A discrete Gradle subproject (feature, shared, lower-level, or data/mock) with its own responsibilities and published artifact. |
| **Feature Module**                  | Encapsulates UI screens and domain logic for a single user-visible flow (e.g., prescriptions, pharmacies). |
| **Shared Module**                   | Kotlin Multiplatform library providing cross-cutting services (authentication, navigation, logging) to multiple platforms. |
| **Lower-Level Module**              | Provides foundational libraries (network client, database, FHIR parser) consumed by higher-level modules. |
| **Data/Mock Module**                | Supplies in-memory repository implementations and static fixtures for demo mode, unit tests, and screenshot tests. |
| **APK / AAB**                       | Android packaging formats: APK is the installable package; AAB (Android App Bundle) is the publishable bundle to the Play Store. |
| **AAR**                             | Android library archive produced by `com.android.library` modules, consumed by other Android projects. |
| **KMP (Kotlin Multiplatform)**      | A technology for sharing common Kotlin code across JVM, Android, JS, and other platforms. |
| **MVVM (Model-View-ViewModel)**     | Architectural pattern where the ViewModel mediates between UI and domain logic, exposing state via observable properties. |
| **Use Case**                        | A single-responsibility class encapsulating a specific business action or workflow in the domain layer. |
| **Repository Pattern**              | Abstraction over data sources (local cache, network) providing a unified interface to use cases. |
| **Dependency Injection (DI)**       | Technique for supplying required dependencies to classes, decoupling implementations via frameworks like Kodein. |
| **BuildSrc**                        | A special Gradle directory holding build logic and custom tasks/plugins compiled and applied to the main build. |
| **Precompiled Script Plugin**       | Kotlin DSL plugin defined in `buildSrc` (e.g., `base-android-application`) that encapsulates common Gradle configuration. |
| **Version Catalog (`libs.versions.toml`)** | Central TOML file listing all dependency versions and plugin coordinates to enforce consistency. |
| **Settings Gradle (`settings.gradle.kts`)** | Top-level Gradle file that declares all included modules in the multi-project build. |
| **Compose (Jetpack Compose)**       | Android’s modern declarative UI toolkit, configured via `compose-convention.gradle.kts`. |
| **Room**                            | Android’s SQLite abstraction library for local persistence, used in lower-level modules. |
| **Retrofit / Ktor**                 | HTTP client libraries: Retrofit for synchronous/retrofit APIs; Ktor for multiplatform networking. |
| **FHIR**                            | Fast Healthcare Interoperability Resources: a standard for healthcare data, parsed via the `fhir-parser` module. |
| **Paparazzi**                       | Screenshot testing library that renders Compose or Views off-device and compares images. |
| **Docsify**                         | Static documentation generator that renders Markdown in the browser, used for Arc42 docs. |
| **PlantUML**                        | Text-based UML diagram generator used for sequence, component, and deployment diagrams. |
| **Jenkins Pipeline**                | Declarative Groovy script defining automated CI/CD stages (build, test, deploy). |
| **Artifact Repository (Nexus)**     | Internal Maven server hosting published AAR, JAR, and KMP library artifacts. |
| **Fastlane**                        | Automation tool for building, signing, and deploying mobile apps to various distribution targets. |
| **Quality-Detekt**                  | Precompiled script plugin applying static code analysis rules via the Detekt linter. |
| **Orthogonal Layout**               | PlantUML skinparam setting for right-angle connectors in component diagrams. |
| **HTTP Logger**                     | OkHttp interceptor capturing HTTP traffic; includes internal real-time logger and debug HTTP cache. |
| **Test Runner**                     | The Android instrumentation test harness (UI-test module) that executes Espresso or UI Automator tests. |

*This glossary reflects both domain-specific terminology (e-Rezept, DiGA, FHIR) and architecture/build concepts (KMP, precompiled script plugins, Artifacts, CI/CD tools).*
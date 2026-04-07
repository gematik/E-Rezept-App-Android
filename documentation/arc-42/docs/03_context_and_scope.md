# 3 – Context and Scope

This section defines the **system boundary** for our refactoring effort and clarifies the **scope** of decomposing the existing monolithic Android app into independently functioning modules.

## 3.1 Refactoring Context

Originally, **das e-Rezept** shipped as a single Android application package with tightly coupled components. The refactoring aims to transform this monolith into a set of discrete modules—each encapsulating a feature or shared service—that can be built, tested, and (optionally) published independently. This modularization improves:  
- **Build Performance**: by enabling incremental builds and parallel compilation of modules.  
- **Maintainability**: by clearly separating concerns and reducing inter-module coupling.  
- **Reusability & Distribution**: by allowing modules to be distributed to other projects or published to internal artifact repositories without the full app context.

## 3.2 System Boundary

We consider the **Android client** as the system boundary for this refactoring. Backend services (ERP APIs, authentication providers, etc.) and external actors (patients, pharmacists) remain unchanged and are out of scope. Our modules exist entirely within the client-side codebase and interact through well-defined interfaces.

```text
[ Existing Android Monolith ]   → refactor →   [ Feature Modules ] [ Shared Modules ] [ Lower-Level Modules ] [ Data/Mock Modules ]
```

## 3.3 Module Types & Purpose

| Module Type             | Responsibility                                                              | Deliverable                         |
|-------------------------|-------------------------------------------------------------------------------|-------------------------------------|
| **Feature Modules**     | Encapsulate business flows (e.g., prescription list, fulfillment process).     | AAR or source module with UI layer. |
| **Shared Modules**      | Provide cross-cutting services (e.g., authentication, navigation, logging, tracking, profile information).    | Multiplatform library with reusable utilities.        |
| **Lower-Level Modules** | Offer foundational libraries (e.g., networking, persistence, fhir-parsing).         | AAR or pure-JAR libraries.          |
| **Data/Mock Modules**   | Supply stub implementations and test data for demo-mode and CI pipelines.     | AAR with mock repositories and data models.         |

## 3.4 Scope of Work

### In Scope

- **Extraction** of existing functionality into discrete modules as defined in the high-, mid-, and detailed-level diagrams, including:  
  - **Feature Modules** (e.g., digas, prescriptions, pharmacies, messages, profile, gid, others, demo.mode)  
  - **Shared Modules** (core services, navigation, UI components, feature orchestrator, authentication, logging)  
  - **Lower-Level Modules** (database, repository, fhir-parser, network client)  
  - **Data/Mock Modules** (ERP model, mock repositories, static fixtures)  
  - **Additional modules** identified in the detailed architecture (e.g., card-communication, gid, ui-library)  
- **Define** clear Gradle project structure and inter-module dependencies.  
- **Update CI** to support module-level build, test, and artifact publishing.  
- **Verify** behavioral parity through end-to-end smoke tests against production backend.

### Out of Scope

- **Backend changes**: No modifications to ERP or external APIs.  
- **UI Redesign**: The user interface remains visually identical.  
- **Additional Platforms**: No iOS or web client refactoring in this phase.

## 3.5 Success Criteria

- **Build Time Reduction**: Full clean build time reduced by ≥50%.  
- **Module Independence**: Each module can compile in isolation with all its tests passing.  
- **Publishable Artifacts**: At least two modules successfully published to our internal Maven repository.  
- **Zero Regression**: No functional regressions in core flows as verified by CI smoke tests.
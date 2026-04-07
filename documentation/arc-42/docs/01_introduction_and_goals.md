<img src="https://www.das-e-rezept-fuer-deutschland.de/_assets/675db9488470277a8576f6df36f1bee8/Images/Logo-gematik-d-flagge-rechts.svg"
     alt="Gematik Logo"
     style="float: left; width: 200px; margin-right: 1em; margin-bottom: 0.5em;" />
<br clear="left"/>

<!-- use a flex container so the H1 is left and the icon is right -->
<div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1em;">
  <h1 style="margin: 0;">1 – Introduction and Goals</h1>
  <img
    src="https://www.das-e-rezept-fuer-deutschland.de/_assets/675db9488470277a8576f6df36f1bee8/Images/Logo-E-Rezept.svg"
    alt="das e-Rezept App Icon"
    style="width: 80px; height: auto;"
  />
</div>

## 1.1 Elevator Pitch

This project refactors the existing **das e-Rezept** Android monolithic application into a clean, modular architecture. By decomposing the monolith into discrete **feature**, **shared**, **lower-level**, and **data/mock** modules, we enhance maintainability, accelerate build and test cycles, and empower parallel development of independent features while preserving existing functionality.

## 1.2 Key Stakeholders

- **Patients**: Access and manage digital prescriptions seamlessly.
- **Pharmacists**: Verify and fulfill prescriptions through an efficient UI.
- **Healthcare Providers**: Issue, revoke, and update e-prescriptions.
- **ERP System**: Backend system providing prescription data and billing.
- **Development Team**: Engineers responsible for module boundaries, integration, and delivery.
- **QA & Test Teams**: Validate correctness in both demo and production environments.

## 1.3 Functional Goals

1. **Modularization**: Extract all existing monolithic features into discrete, reusable modules.  
2. **Feature Independence**: Enable building, testing, and deploying individual modules without recompiling the entire app.  
3. **Behavioral Parity**: Preserve all current capabilities of das e-Rezept with no regressions.  
4. **Demo Environment**: Maintain a mock backend (demo mode) for testing new modules in isolation.  

## 1.4 Quality Goals (Non-Functional Requirements)

- **Maintainability**: Reduce coupling between features; target <15% inter-module coupling.  
- **Build Performance**: Decrease full-build time by at least 50% compared to the monolith.  
- **Testability**: Achieve ≥80% unit-test coverage in each module and end-to-end CI validation for core scenarios.  
- **Performance**: Ensure screen-to-screen navigation remains under 200 ms on mid-range devices.  

## 1.5 Scope & Non-Goals

- **In Scope**: Refactoring all existing monolithic features into modules; updating CI pipeline to support module-level builds; verifying feature parity.  
- **Out of Scope**: Adding new business features or UI redesigns; replacing backend APIs or altering ERP contracts.
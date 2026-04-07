# 11 – Risks & Technical Debt

## Content & Motivation

You want to know the technical risks of your system, so you can address potential future problems.  
In addition, you want to support your management stakeholders (project management, product owner) by identifying areas of technical debt that may slow down future delivery or increase maintenance costs.

---

## 11.1 Risks

| Risk                                                                                         | Impact                                                         | Mitigation / Status                                            |
|----------------------------------------------------------------------------------------------|----------------------------------------------------------------|---------------------------------------------------------------|
| **Hidden module dependencies**                                                               | Accidental coupling between modules could break isolation and slow down builds | Enforce strict dependency rules via CI; periodically audit dependency graph |
| **Kotlin Multiplatform publishing instability**                                              | KMP artifacts may fail to publish or resolve in consuming modules | Automate KMP publish in CI; add health‐check job for KMP repo |
| **DI configuration errors at runtime**                                                       | Missing or incorrect bindings can cause runtime crashes         | Add CI smoke tests that start each feature module in isolation |
| **Out‐of‐sync mock fixtures**                                                               | Mock data views drift from real API schema, leading to false test success | Integrate fixture schema validation against real Swagger/OpenAPI definitions |
| **Performance regressions due to modularization**                                            | Excessive inter‐module calls could slow down cold‐start or navigation | Profile startup and add caching; set performance budget and CI‐enforced benchmarks |
| **Gradle script plugin bugs propagate**                                                     | A defect in `scripts/` module can break builds across all modules | Version and test `scripts/` separately; add automated validation of each plugin |
| **BuildSrc complexity**                                                                      | Over‐complex custom tasks in `buildSrc` become hard to maintain | Regularly review and refactor buildSrc; document each task clearly |
| **CI pipeline flakiness**                                                                    | Parallel tests or external service calls (Snyk, Fastlane) may intermittently fail | Add retries for external steps; isolate flaky tests; monitor uptime of external services |
| **Docsify authentication bypass**                                                            | Basic‐Auth on Docsify is easily circumvented and not enterprise‐grade | Plan migration to an SSO‐backed docs hosting solution in Q3 |

---

## 11.2 Technical Debt

| Area                                  | Description                                                                                             | Plan / Remediation                        |
|---------------------------------------|---------------------------------------------------------------------------------------------------------|-------------------------------------------|
| **Gradle Kotlin DSL Script Plugins**  | Many conventions concentrated in `scripts/` are poorly documented and hard to debug                     | Invest 1 sprint to split and document each plugin module |
| **Mock‐Data Maintenance**             | Static JSON fixtures live separately from production API schemas, requiring manual sync                 | Automate fixture generation from API contracts |
| **Deprecated Android APIs**           | Some lower‐level modules use older Android APIs (e.g., older Room versions)                            | Schedule dependency upgrades in the next quarter |
| **Sparse KMP Test Coverage**         | KMP modules lack comprehensive unit tests on non‐Android targets (JS, JVM)                              | Add JS/JVM unit test jobs in CI            |
| **Ad‐hoc Jenkins Groovy Logic**       | Complex parameter handling and scripting in Jenkinsfile increases cognitive load                        | Refactor pipeline into shared library; simplify parameter validation |
| **Insufficient Observability**        | No standardized metrics collection in production APK                                                     | Integrate a lightweight metrics SDK and dashboard by next release |

---

By tracking these risks and paying down technical debt, the das e-Rezept architecture remains resilient, maintainable, and primed for future growth.
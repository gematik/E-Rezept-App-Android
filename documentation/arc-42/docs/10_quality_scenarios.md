## 10.1 – Quality Requirements

This section refines the top‐level quality goals introduced in Section 1.2 into concrete, operational scenarios. Each scenario describes an attribute, the specific requirement, and its metric or pass/fail criterion.

| Attribute       | Quality Scenario                                                                                                  |
|-----------------|-------------------------------------------------------------------------------------------------------------------|
| **Correctness** | All feature flows (e.g., prescription retrieval, submission, pharmacy lookup) execute without logical errors.     |
| **Completeness**| Every public API endpoint and UI screen is covered by at least one automated positive and one negative test.     |
| **Safety**      | All sensitive user data (PHI) is encrypted at rest (Room + SQLCipher) and in transit (TLS 1.2+), with violations logged. |
| **Performance** | Cold‐start time ≤ 2 s on a mid-range Android device; full clean build ≤ 5 min on CI agents.                        |
| **Scalability** | Adding a new feature or shared module increases incremental build time by ≤ 5 % and does not affect existing modules. |
| **Flexibility** | New modules (feature, library, KMP) can be created and integrated by applying the correct script plugin, with no manual Gradle boilerplate. |
| **Resilience**  | Offline mode returns cached data (last successful fetch) within 500 ms when network is unavailable.               |
| **Testability** | Module‐level unit test coverage ≥ 80 %; screenshot tests detect any UI regressions within 3 s of interaction.     |
| **Maintainability** | A new developer can clone the repo, run in ≤ 30 min.    |

## 10.2 Quality Tree (excerpt)

```text
Quality
├── Correctness
├── Safety
├── Performance
├── Scalability
├── Flexibility
├── Resilience
├── Testability
└── Maintainability
```

These scenarios ensure that the refactored, modular **das e-Rezept** client remains robust, performant, and easy to extend and maintain.
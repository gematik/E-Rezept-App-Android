# 9 – Architecture Decisions

This section captures the most important, large-scale, and potentially risky architecture and design decisions made during the refactoring of the das e-Rezept Android application. For each decision, we document:

- **Context & Drivers**: Why the decision was needed.  
- **Decision**: What choice was made.  
- **Alternatives Considered**: Other options and why they were rejected.  
- **Rationale**: Key factors leading to the choice.  
- **Consequences & Trade-offs**: What this decision bought us and what we must watch out for.

---

## 9.1 Modularization of the Monolith

**Context & Drivers**  
- Build times on the monolithic client exceeded **5 minutes clean** → slowed developer feedback loop.  
- Single-module made feature ownership, isolation, and independent releases impossible.  
- Desire for **publishable libraries** for other teams.

**Decision**  
Refactor into four module categories:  
1. **Feature modules** (UI + domain per feature).  
2. **Shared KMP modules** (cross-platform services).  
3. **Lower-level modules** (network, persistence, parsing).  
4. **Data/mock modules** (fixtures for tests & demo).

**Alternatives Considered**  
- **Keep single module**, add multi-project subfolders → didn’t address build granularity.  
- **Dynamic feature APKs** (Android App Bundles) → heavy upfront complexity, Google Play restrictions.

**Rationale**  
- **Parallel compilation** and **configuration avoidance** reduce clean build by ~30–40%.  
- **Single Responsibility** boundaries accelerate onboarding and code review.  
- Granular **Maven publication** for reuse.

**Consequences & Trade-offs**  
- Initial refactoring workload (~2 weeks of effort).  
- Strict **dependency management** required (enforced by CI).  
- Developers must learn new multi-project conventions.

---

## 9.2 Separate modules for Shared Logic

**Context & Drivers**  
- Authentication, navigation, logging duplicated in multiple modules.  

**Decision**  
Implement core libraries (`authentication`, `navigation`, `logging`) as **Kotlin Multiplatform** modules targeting Android, JVM.

**Alternatives Considered**  
- Single JVM/Android-only libraries → no future reuse on web/desktop.  
- Pure Kotlin-JS modules for web + separate Android modules → inconsistent APIs.

**Rationale**  
- **Code reuse** across two platforms.  
- **API consistency** avoids drift between clients.  
- **Ecosystem support** through Compose Multiplatform (desktop, web).

**Consequences & Trade-offs**  
- More complex Gradle setup (target declarations, publishing).  
- CI build matrix expanded (Android + JVM targets).  
- Team ramp-up on KMP nuances.

---

## 9.3 Centralized Gradle Conventions via Precompiled Script Plugins

**Context & Drivers**  
- Boilerplate in every `build.gradle.kts` for plugin IDs, versions, defaultConfig, test options.  
- Hard to update conventions without touching dozens of files.

**Decision**  
Create a `scripts/` project with **precompiled Kotlin DSL script plugins**:
- `base-android-application`
- `base-android-library`
- `base-multiplatform-library`
- `compose-convention`
- `quality-detekt`

**Alternatives Considered**  
- **Gradle convention plugins** in `.gradle` → no static typing, poor IDE support.  
- **Manual copy/paste** → unmaintainable.

**Rationale**  
- **Type safety** and IDE completion.  
- **DRY**: change in one place propagates to all modules.  
- **Discoverability**: module authors simply `id("...")`.

**Consequences & Trade-offs**  
- Single point of failure: a bug in `scripts/` affects all modules.  
- Versioning of `scripts/` must be aligned with codebase.  
- Requires Kotlin DSL familiarity.

---

## 9.4 Dependency Injection

**Context & Drivers**  
- Tight coupling hindered unit and UI tests.  
- Need to swap real implementations with mocks for demo/test builds.

**Decision**  
Continue with **Kodein** for DI, inject at application start or navigation graph initialization.

**Alternatives Considered**  
- **Hilt** (Dagger-based) → heavy, annotation-processing overhead.  
- **Service Locator** → global state, harder to test.

**Rationale**  
- Kodein offers **runtime flexibility**, easy mocking.  
- **Lightweight** footprint vs Dagger.  
- **Multiplatform support** in KMP modules.

**Consequences & Trade-offs**  
- Runtime resolution overhead.  
- Learning curve for complex module graphs.  
- Must manage DI bindings across flavors (demo vs prod).

---

## 9.5 Demo-Mode & Mock Data Fixtures

**Context & Drivers**  
- UI and screenshot tests needed consistent data without a backend.  
- Demo demos must run fully offline.

**Decision**  
Create a `data-mock` module with **in-memory repository** implementations and **static JSON fixtures**.

**Alternatives Considered**  
- External mock server → network latency, CI flakiness.  
- Rewriting tests to use live API → brittle.

**Rationale**  
- **Deterministic tests**: static fixtures guaranteed.  
- **Fast feedback**: through in-memory calls (< 10 ms).  
- **Feature demos** offline.

**Consequences & Trade-offs**  
- Fixtures must be synchronized with real API schemas.  
- Additional maintenance overhead.  
- Risk of divergence requiring periodic verification.

---

## 9.6 Cross-Cutting Logging & Tracking

**Context & Drivers**  
- Need consistent analytics and debugging insights across modules.

**Decision**  
- Use **Napier** for application logging (configured by base plugin).  
- Provide two tracker implementations:
  - **Debug Tracker** (enabled in debug builds).
  - **Prod Tracker** (full analytics in release).
- HTTP logging via:
  - **Internal HTTP Logger** (OkHttp interceptor, debug menu).  
  - **Debug HTTP Cache** (persist logs in Room for offline analysis).

**Alternatives Considered**  
- Single tracking library with runtime flags → risk of shipping debug code in release.  
- Remote analytics tool only → no local debug insights.

**Rationale**  
- Clear separation of debug vs prod concerns.  
- **Fall-through safety**: debug-only code won’t ship to production.  
- **Offline inspection** for support/debugging.

**Consequences & Trade-offs**  
- Two sets of dependencies and flavor configurations.  
- Performance impact if debug code not guarded properly.  
- Need to guard against accidental logging in release.

---

## 9.7 Continuous Documentation with Docsify & Jenkins

**Context & Drivers**  
- Architecture docs needed living alongside code, with authentication and CI deployment.

**Decision**  
- Author docs in Markdown under `docs/` and render via **Docsify**.  
- Host privately using **Express basic-auth** on server for protected access.  
- CI publishes docs static site artifacts from Jenkins.

**Alternatives Considered**  
- GitHub Pages → no server-side auth.  
- Confluence → siloed, not versioned with code.

**Rationale**  
- **Versioned** with Git.  
- **Protected** by cookie-based auth.  
- **Automated** via existing Jenkins pipeline.

**Consequences & Trade-offs**  
- Requires Node-enabled host for Express.  
- Added CI steps to generate and publish docs.  
- Basic-auth is not enterprise SSO.

---

These expanded decisions, with context, alternatives, rationale, and consequences, provide a clear record of why and how we structured the architecture. They serve as living guidance for future maintainers.
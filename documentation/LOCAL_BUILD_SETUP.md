# Local Build Setup — Credential Configuration

This guide explains how to configure credentials on your local machine to build the erp-app-android
project. **None of these files must ever be committed to git** — they are all covered by
`.gitignore`.

---

## Overview

The project separates secrets into two categories:

| Category                              | Who uses it                                     | How it is supplied                                                    |
|---------------------------------------|-------------------------------------------------|-----------------------------------------------------------------------|
| **CI-only** (Nexus, GitLab, Teams)    | Gradle tasks only — never reaches `BuildConfig` | `ci/local/ci-overrides.properties` locally, Jenkins Credentials on CI |
| **App-runtime** (API keys, endpoints) | Goes into `BuildConfig` and the app binary      | `ci/local/secrets.properties` locally, Jenkins Credentials on CI      |

---

## Step 1 — Create CI Local Properties Files

Create a directory `ci/local/` in the **root of the repository** and populate it with two files:

```
erp-app-android/
├── ci/
│   └── local/
│       ├── ci-overrides.properties   ← create this (gitignored)
│       ├── secrets.properties        ← create this (gitignored)
│       └── apikeys.properties        ← create this (gitignored)
├── settings.gradle.kts
├── build.gradle.kts
└── ...
```

Populate `ci/local/secrets.properties` with app-runtime values:

```properties
# ---------------------------------------------------------------
# APP-RUNTIME — injected into config at build time
# These appear in the app binary. Only include keys actually
# required for the app to function at runtime.
# ---------------------------------------------------------------
FHIR_VZD_API_KEY_PU=<your_api_key>
FHIR_VZD_API_KEY_TU=<your_api_key>
FHIR_VZD_API_KEY_RU=<your_api_key>
PHARMACY_API_KEY=<your_api_key>
PHARMACY_API_KEY_TEST=<your_api_key>
BASE_SERVICE_URI_PU=<your_base_service_uri>
BASE_SERVICE_URI_TU=<your_base_service_uri>
BASE_SERVICE_URI_RU=<your_base_service_uri>
BASE_SERVICE_URI_TR=<your_base_service_uri>
BASE_SERVICE_URI_RU_DEV=<your_base_service_uri>
EREZEPT_BACKEND_URI_PU=<your_backend_uri>
EREZEPT_BACKEND_URI_RU=<your_backend_uri>
IDP_SERVICE_URI_RU=<your_idp_well_known_uri>
PHARMACY_SERVICE_URI=<your_pharmacy_service_uri>
PHARMACY_SERVICE_URI_TEST=<your_pharmacy_service_uri>
FHIRVZD_PHARMACY_SERVICE_URI_PU=<your_fhir_directory_uri>
FHIRVZD_PHARMACY_SERVICE_URI_RU=<your_fhir_directory_uri>
CLIENT_ID_PU=<your_client_id>
CLIENT_ID_TU=<your_client_id>
CLIENT_ID_RU=<your_client_id>
INTEGRITY_API_KEY=<your_integrity_api_key>
INTEGRITY_VERIFICATION_KEY=<your_integrity_verification_key>
CLOUD_PROJECT_NUMBER=<your_cloud_project_number>
```

Populate `ci/local/apikeys.properties` with ERP API keys:

```properties
# ---------------------------------------------------------------
# ERP API KEYS — injected into config at build time
# Separated from secrets.properties for organizational clarity
# ---------------------------------------------------------------
ERP_API_KEY_GOOGLE_PU=<ask team>
ERP_API_KEY_GOOGLE_TU=<ask team>
ERP_API_KEY_GOOGLE_RU=<ask team>
ERP_API_KEY_GOOGLE_TR=<ask team>
ERP_API_KEY_HUAWEI_PU=<ask team>
ERP_API_KEY_HUAWEI_TU=<ask team>
ERP_API_KEY_HUAWEI_TR=<ask team>
ERP_API_KEY_DESKTOP_PU=<ask team>
ERP_API_KEY_DESKTOP_TU=<ask team>
```

---

## Step 2 — Verify `.gitignore`

Confirm these lines are present in `.gitignore` at the repository root (they have already been
added):

```
/ci/local/
/local.properties
```

Run the following to double-check git is ignoring the directory:

```bash
git check-ignore -v ci/local/ci-overrides.properties
# Expected output: .gitignore:N:/ci/local/
```

---

## Step 3 — Build Locally

Once `ci/local/` files are in place, run a normal Gradle build. The build system automatically reads
the files as a local fallback:

```bash
# Debug build
./gradlew buildDebugApp

# Release TU build
./gradlew buildTuReleaseApp

# Release PU build
./gradlew buildPuReleaseApp
```

No extra `-P` flags are needed locally — `loadCiOnlyProperty()` and `resolveSecret()` both fall back
to the `ci/local/` files automatically.

---

## Step 4 — Credential Flow Reference

```
Local developer machine
  └── ci/local/ (gitignored)
      ├── ci-overrides.properties
      │   └── read by loadCiOnlyProperty()    → Gradle tasks only (Nexus, GitLab, Teams)
      ├── secrets.properties
      │   └── read by resolveSecret()         → buildConfigField (app-runtime keys)
      └── apikeys.properties
          └── read by DependenciesPlugin      → BuildKonfig (ERP API keys)

CI / Jenkins
  └── Jenkins Credentials Store
      └── injected as env vars via credentials() binding in Jenkinsfile
          └── passed to Gradle as -P params
              ├── loadCiOnlyProperty()  → env var → Gradle task
              ├── resolveSecret()       → env var → buildConfigField
              └── DependenciesPlugin    → env var → BuildKonfig
```

**Guard rails built into the code:**

- `resolveSecret()` throws a build error if you try to pass a CI-only key (`NEXUS_*`, `GITLAB_*`,
  `TEAMS_*`) into `buildConfigField`
- `loadCiOnlyProperty()` throws a build error if you try to use an app-runtime key through the
  CI-only path

---

## Credential Rotation Checklist

If any credential is rotated, update it in all locations below:

1. `ci/local/` files on developer machines
2. Jenkins Credentials Store entries
3. Any other CI/CD secret stores in use

---

## Security Notes

- **`ci/local/` files are local convenience only.** On CI, all values come exclusively from the
  Jenkins Credentials Store — these files are never present on the build agent.
- **App-runtime keys in `BuildConfig` are still readable from the APK.** Anyone who installs the app
  can extract them.
- **`MAPS_API_KEY` is written to `local.properties` and extracted from the manifest.** It does not
  appear in BuildConfig code constants, reducing exposure.

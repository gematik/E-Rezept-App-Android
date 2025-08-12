@file:Suppress("UnusedPrivateProperty", "VariableNaming", "PropertyName")

import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.LONG
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import de.gematik.ti.erp.app.plugins.dependencies.overrides
import de.gematik.ti.erp.app.tasks.generateSchemaMigrationsFile
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.base.kmp.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.dependency.overrides)
}
fun getGitHash(): String {
    val gitDir = File("${rootDir.path}/.git")
    if (!gitDir.exists()) {
        println("Git directory not found: ${gitDir.path}")
        return "n/a"
    }
    return try {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine = listOf("git", "rev-parse", "--short", "HEAD")
            standardOutput = stdout
        }
        stdout.toString().trim()
    } catch (e: Exception) {
        println("Failed to retrieve git hash: ${e.message}")
        "n/a"
    }
}

// versioning
val VERSION_NAME: String by overrides()
val VERSION_CODE: String by overrides()
val USER_AGENT: String by overrides()

// client id
val CLIENT_ID_RU: String by overrides()
val CLIENT_ID_TU: String by overrides()
val CLIENT_ID_PU: String by overrides()

// data protection
val DATA_PROTECTION_LAST_UPDATED: String by overrides()

// OCSP
val VAU_OCSP_RESPONSE_MAX_AGE: String by overrides()

// trust anchor
val APP_TRUST_ANCHOR_BASE64: String by overrides()
val APP_TRUST_ANCHOR_BASE64_TEST: String by overrides()

// pharmacy
val PHARMACY_SERVICE_URI: String by overrides()
val PHARMACY_SERVICE_URI_TEST: String by overrides()
val FHIRVZD_PHARMACY_SERVICE_URI_RU: String by overrides()
val FHIRVZD_PHARMACY_SERVICE_URI_PU: String by overrides()
val EREZEPT_BACKEND_URI_RU: String by overrides()
val EREZEPT_BACKEND_URI_PU: String by overrides()
val EREZEPT_BACKEND_URI_TU: String by overrides()
val FHIR_VZD_API_KEY_RU: String by overrides()
val FHIR_VZD_API_KEY_PU: String by overrides()
val FHIR_VZD_API_KEY_TU: String by overrides()
val PHARMACY_API_KEY: String by overrides()
val PHARMACY_API_KEY_TEST: String by overrides()

// organ donation
val ORGAN_DONATION_REGISTER_RU: String by overrides()
val ORGAN_DONATION_REGISTER_PU: String by overrides()
val ORGAN_DONATION_INFO: String by overrides()

// base service URIs
val BASE_SERVICE_URI_PU: String by overrides()
val BASE_SERVICE_URI_TU: String by overrides()
val BASE_SERVICE_URI_RU: String by overrides()
val BASE_SERVICE_URI_RU_DEV: String by overrides()
val BASE_SERVICE_URI_TR: String by overrides()

// IDP URIs
val IDP_SERVICE_URI_PU: String by overrides()
val IDP_SERVICE_URI_TU: String by overrides()
val IDP_SERVICE_URI_RU: String by overrides()
val IDP_SERVICE_URI_RU_DEV: String by overrides()
val IDP_SERVICE_URI_TR: String by overrides()

// ERP API keys google
val ERP_API_KEY_GOOGLE_PU: String by overrides()
val ERP_API_KEY_GOOGLE_TU: String by overrides()
val ERP_API_KEY_GOOGLE_RU: String by overrides()
val ERP_API_KEY_GOOGLE_TR: String by overrides()

// ERP API keys huawei
val ERP_API_KEY_HUAWEI_PU: String by overrides()
val ERP_API_KEY_HUAWEI_TU: String by overrides()
val ERP_API_KEY_HUAWEI_RU: String by overrides()
val ERP_API_KEY_HUAWEI_TR: String by overrides()

// ERP API keys desktop
val ERP_API_KEY_DESKTOP_PU: String by overrides()
val ERP_API_KEY_DESKTOP_TU: String by overrides()
val ERP_API_KEY_DESKTOP_RU: String by overrides()

// integrity
val INTEGRITY_API_KEY: String by overrides()
val INTEGRITY_VERIFICATION_KEY: String by overrides()

// cloud project number
val CLOUD_PROJECT_NUMBER: String by overrides()

// virtual health card
val DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE: String by overrides()
val DEFAULT_VIRTUAL_HEALTH_CARD_PRIVATE_KEY: String by overrides()

// debug
val DEBUG_TEST_IDS_ENABLED: String by overrides()
val DEBUG_VISUAL_TEST_TAGS: String? by project
val BUILD_TYPE_MINIFIED_DEBUG: String by overrides()

// app center
val APP_CENTER_SECRET: String by overrides()

// add db schema migration task
tasks.generateSchemaMigrationsFile()

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(project(":utils"))
                implementation(project(":fhir-parser"))
                implementation(project(":database"))
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.bundles.crypto)
                implementation(libs.bundles.di.viewmodel)
            }
        }
        val desktopMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.bundles.crypto)
                implementation(compose.preview)
            }
        }
    }
}
android {
    namespace = "de.gematik.ti.erp.lib"
}

enum class Platforms {
    Google, Huawei, Konnektathon, Desktop
}

enum class Environments {
    PU, TU, RU, DEVRU, TR, NONE
}

enum class Types {
    Internal, External
}

buildkonfig {
    packageName = "de.gematik.ti.erp.app"
    exposeObjectWithName = "BuildKonfig"
    // default config is required
    defaultConfigs {
        buildConfigField(STRING, "GIT_HASH", getGitHash())
        buildConfigField(STRING, "INTEGRITY_API_KEY", INTEGRITY_API_KEY)
        buildConfigField(STRING, "INTEGRITY_VERIFICATION_KEY", INTEGRITY_VERIFICATION_KEY)
        buildConfigField(STRING, "CLOUD_PROJECT_NUMBER", CLOUD_PROJECT_NUMBER)
        buildConfigField(
            STRING,
            "DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE",
            DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE
        )
        buildConfigField(
            STRING,
            "DEFAULT_VIRTUAL_HEALTH_CARD_PRIVATE_KEY",
            DEFAULT_VIRTUAL_HEALTH_CARD_PRIVATE_KEY
        )
        buildConfigField(STRING, "BUILD_FLAVOR", project.property("buildkonfig.flavor") as String)
        buildConfigField(STRING, "APP_CENTER_SECRET", APP_CENTER_SECRET)
        buildConfigField(STRING, "BUILD_TYPE_MINIFIED_DEBUG", BUILD_TYPE_MINIFIED_DEBUG)
    }
    fun defaultConfigs(
        flavor: String,
        isInternal: Boolean,
        baseServiceUri: String,
        idpServiceUri: String,
        erpApiKey: String,
        pharmacyServiceUri: String,
        pharmacyFhirVzdServiceUri: String,
        erezeptBackendUri: String,
        pharmacyFhirVzdApiKey: String,
        pharmacyServiceApiKey: String,
        trustAnchor: String,
        clientId: String,
        ocspResponseMaxAge: String
    ) {
        defaultConfigs(flavor) {
            buildConfigField(STRING, "VERSION_NAME", VERSION_NAME)
            buildConfigField(INT, "VERSION_CODE", VERSION_CODE)
            buildConfigField(BOOLEAN, "INTERNAL", isInternal.toString())
            // client ids
            buildConfigField(STRING, "CLIENT_ID_TU", CLIENT_ID_TU)
            buildConfigField(STRING, "CLIENT_ID_PU", CLIENT_ID_PU)
            buildConfigField(STRING, "CLIENT_ID_RU", CLIENT_ID_RU)
            if (isInternal) {
                // base service URIs
                buildConfigField(STRING, "BASE_SERVICE_URI_PU", BASE_SERVICE_URI_PU)
                buildConfigField(STRING, "BASE_SERVICE_URI_RU", BASE_SERVICE_URI_RU)
                buildConfigField(STRING, "BASE_SERVICE_URI_TU", BASE_SERVICE_URI_TU)
                buildConfigField(STRING, "BASE_SERVICE_URI_RU_DEV", BASE_SERVICE_URI_RU_DEV)
                buildConfigField(STRING, "BASE_SERVICE_URI_TR", BASE_SERVICE_URI_TR)
                // idp
                buildConfigField(STRING, "IDP_SERVICE_URI_PU", IDP_SERVICE_URI_PU)
                buildConfigField(STRING, "IDP_SERVICE_URI_TU", IDP_SERVICE_URI_TU)
                buildConfigField(STRING, "IDP_SERVICE_URI_RU", IDP_SERVICE_URI_RU)
                buildConfigField(STRING, "IDP_SERVICE_URI_RU_DEV", IDP_SERVICE_URI_RU_DEV)
                buildConfigField(STRING, "IDP_SERVICE_URI_TR", IDP_SERVICE_URI_TR)
                // apo-vzd
                buildConfigField(STRING, "PHARMACY_SERVICE_URI_PU", PHARMACY_SERVICE_URI)
                buildConfigField(STRING, "PHARMACY_SERVICE_URI_RU", PHARMACY_SERVICE_URI_TEST)
                // fhir-vzd
                buildConfigField(STRING, "FHIRVZD_PHARMACY_SERVICE_URI_RU", FHIRVZD_PHARMACY_SERVICE_URI_RU)
                buildConfigField(STRING, "FHIRVZD_PHARMACY_SERVICE_URI_PU", FHIRVZD_PHARMACY_SERVICE_URI_PU)
                buildConfigField(STRING, "EREZEPT_BACKEND_URI_RU", EREZEPT_BACKEND_URI_RU)
                buildConfigField(STRING, "EREZEPT_BACKEND_URI_PU", EREZEPT_BACKEND_URI_PU)
                buildConfigField(STRING, "EREZEPT_BACKEND_URI_TU", EREZEPT_BACKEND_URI_TU)
                buildConfigField(STRING, "FHIR_VZD_API_KEY_RU", FHIR_VZD_API_KEY_RU)
                buildConfigField(STRING, "FHIR_VZD_API_KEY_PU", FHIR_VZD_API_KEY_PU)
                buildConfigField(STRING, "FHIR_VZD_API_KEY_TU", FHIR_VZD_API_KEY_TU)
                // erp api keys
                buildConfigField(STRING, "ERP_API_KEY_GOOGLE_PU", ERP_API_KEY_GOOGLE_PU)
                buildConfigField(STRING, "ERP_API_KEY_GOOGLE_RU", ERP_API_KEY_GOOGLE_RU)
                buildConfigField(STRING, "ERP_API_KEY_GOOGLE_TU", ERP_API_KEY_GOOGLE_TU)
                buildConfigField(STRING, "ERP_API_KEY_GOOGLE_TR", ERP_API_KEY_GOOGLE_TR)
                // pharmacy api keys
                buildConfigField(STRING, "PHARMACY_API_KEY_PU", PHARMACY_API_KEY)
                buildConfigField(STRING, "PHARMACY_API_KEY_RU", PHARMACY_API_KEY_TEST)
                // trust anchor
                buildConfigField(STRING, "APP_TRUST_ANCHOR_BASE64_PU", APP_TRUST_ANCHOR_BASE64)
                buildConfigField(STRING, "APP_TRUST_ANCHOR_BASE64_TU", APP_TRUST_ANCHOR_BASE64_TEST)
                buildConfigField(STRING, "IDP_SCOPE_DEVRU", "e-rezept-dev openid")
            }

            // organ donation
            buildConfigField(STRING, "ORGAN_DONATION_REGISTER_RU", ORGAN_DONATION_REGISTER_RU)
            buildConfigField(STRING, "ORGAN_DONATION_REGISTER_PU", ORGAN_DONATION_REGISTER_PU)
            buildConfigField(STRING, "ORGAN_DONATION_INFO", ORGAN_DONATION_INFO)
            // apo-vzd
            buildConfigField(STRING, "PHARMACY_SERVICE_URI", pharmacyServiceUri)
            // fhir-vzd
            buildConfigField(STRING, "FHIRVZD_PHARMACY_SERVICE_URI", pharmacyFhirVzdServiceUri)
            buildConfigField(STRING, "FHIRVZD_SEARCH_ACCESS_TOKEN_URI", erezeptBackendUri)
            buildConfigField(STRING, "FHIRVZD_API_KEY", pharmacyFhirVzdApiKey)
            // base service
            buildConfigField(STRING, "BASE_SERVICE_URI", baseServiceUri)
            // client id
            buildConfigField(STRING, "CLIENT_ID", clientId)
            // erp api key
            buildConfigField(STRING, "ERP_API_KEY", erpApiKey)
            // pharmacy api key
            buildConfigField(STRING, "PHARMACY_API_KEY", pharmacyServiceApiKey)
            // trust anchor
            buildConfigField(STRING, "APP_TRUST_ANCHOR_BASE64", trustAnchor)
            // ocsp
            buildConfigField(LONG, "VAU_OCSP_RESPONSE_MAX_AGE", ocspResponseMaxAge)
            // test configs
            buildConfigField(BOOLEAN, "TEST_RUN_WITH_TRUSTSTORE_INTEGRATION", "false")
            buildConfigField(BOOLEAN, "TEST_RUN_WITH_IDP_INTEGRATION", "false")
            // idp
            buildConfigField(STRING, "IDP_SERVICE_URI", idpServiceUri)
            buildConfigField(
                STRING,
                "IDP_DEFAULT_SCOPE",
                if (flavor.contains(Environments.DEVRU.name, true)) {
                    "e-rezept-dev openid"
                } else {
                    "e-rezept openid"
                }
            )
        }
    }

    val platforms = Platforms.values()
    val environments = Environments.values()
    val types = Types.values()
    platforms.forEach { platform ->
        environments.forEach { environment ->
            types.forEach { type ->
                val plat = platform.name.lowercase()
                val env = environment.name.lowercase().capitalizeAsciiOnly()
                val typ = type.name.lowercase().capitalizeAsciiOnly()
                val flavor = plat + env + typ
                defaultConfigs(
                    flavor = flavor,
                    isInternal = type == Types.Internal,
                    baseServiceUri = when (environment) {
                        Environments.PU -> BASE_SERVICE_URI_PU
                        Environments.TU -> BASE_SERVICE_URI_TU
                        Environments.RU -> BASE_SERVICE_URI_RU
                        Environments.DEVRU -> BASE_SERVICE_URI_RU_DEV
                        Environments.TR -> BASE_SERVICE_URI_TR
                        Environments.NONE -> ""
                    },
                    idpServiceUri = when (environment) {
                        Environments.PU -> IDP_SERVICE_URI_PU
                        Environments.TU -> IDP_SERVICE_URI_TU
                        Environments.RU -> IDP_SERVICE_URI_RU
                        Environments.DEVRU -> IDP_SERVICE_URI_RU_DEV
                        Environments.TR -> IDP_SERVICE_URI_TR
                        Environments.NONE -> ""
                    },
                    erpApiKey = when (platform) {
                        Platforms.Google, Platforms.Konnektathon -> when (environment) {
                            Environments.PU -> ERP_API_KEY_GOOGLE_PU
                            Environments.TU -> ERP_API_KEY_GOOGLE_TU
                            Environments.DEVRU, Environments.RU -> ERP_API_KEY_GOOGLE_RU
                            Environments.TR -> ERP_API_KEY_GOOGLE_TR
                            Environments.NONE -> ""
                        }

                        Platforms.Desktop -> when (environment) {
                            Environments.PU -> ERP_API_KEY_DESKTOP_PU
                            Environments.TU -> ERP_API_KEY_DESKTOP_TU
                            Environments.TR -> ERP_API_KEY_GOOGLE_TR
                            Environments.DEVRU, Environments.RU -> ERP_API_KEY_DESKTOP_RU
                            Environments.NONE -> ""
                        }

                        Platforms.Huawei -> when (environment) {
                            Environments.PU -> ERP_API_KEY_HUAWEI_PU
                            Environments.TU -> ERP_API_KEY_HUAWEI_TU
                            Environments.DEVRU, Environments.RU -> ERP_API_KEY_HUAWEI_RU
                            Environments.TR -> ERP_API_KEY_HUAWEI_TR
                            Environments.NONE -> ""
                        }
                    },
                    pharmacyServiceUri = when (environment) {
                        Environments.PU -> PHARMACY_SERVICE_URI
                        Environments.TU,
                        Environments.RU,
                        Environments.DEVRU,
                        Environments.TR -> PHARMACY_SERVICE_URI_TEST

                        Environments.NONE -> ""
                    },
                    pharmacyServiceApiKey = when (environment) {
                        Environments.PU -> PHARMACY_API_KEY
                        Environments.TU,
                        Environments.RU,
                        Environments.DEVRU, Environments.TR -> PHARMACY_API_KEY_TEST

                        Environments.NONE -> ""
                    },
                    pharmacyFhirVzdServiceUri = when (environment) {
                        Environments.PU -> FHIRVZD_PHARMACY_SERVICE_URI_PU
                        Environments.TU,
                        Environments.RU,
                        Environments.DEVRU, Environments.TR -> FHIRVZD_PHARMACY_SERVICE_URI_RU

                        Environments.NONE -> ""
                    },
                    erezeptBackendUri = when (environment) {
                        Environments.PU -> EREZEPT_BACKEND_URI_PU
                        Environments.TU -> EREZEPT_BACKEND_URI_TU
                        Environments.RU, Environments.DEVRU, Environments.TR -> EREZEPT_BACKEND_URI_RU
                        Environments.NONE -> ""
                    },
                    pharmacyFhirVzdApiKey = when (environment) {
                        Environments.PU -> FHIR_VZD_API_KEY_PU
                        Environments.TU -> FHIR_VZD_API_KEY_TU
                        Environments.RU,
                        Environments.DEVRU, Environments.TR -> FHIR_VZD_API_KEY_RU

                        Environments.NONE -> ""
                    },
                    trustAnchor = when (environment) {
                        Environments.PU -> APP_TRUST_ANCHOR_BASE64
                        Environments.TU,
                        Environments.RU,
                        Environments.DEVRU, Environments.TR -> APP_TRUST_ANCHOR_BASE64_TEST

                        Environments.NONE -> ""
                    },
                    clientId = when (environment) {
                        Environments.PU -> CLIENT_ID_PU
                        Environments.TU, Environments.TR -> CLIENT_ID_TU
                        Environments.RU, Environments.DEVRU -> CLIENT_ID_RU
                        Environments.NONE -> ""
                    },
                    ocspResponseMaxAge = VAU_OCSP_RESPONSE_MAX_AGE
                )
            }
        }
    }
    targetConfigs {
        create("desktop") {
            buildConfigField(STRING, "USER_AGENT", USER_AGENT)
        }
        create("android") {
            buildConfigField(STRING, "USER_AGENT", USER_AGENT)
            buildConfigField(STRING, "DATA_PROTECTION_LAST_UPDATED", DATA_PROTECTION_LAST_UPDATED)
            // test tag config
            buildConfigField(BOOLEAN, "DEBUG_VISUAL_TEST_TAGS", DEBUG_VISUAL_TEST_TAGS ?: "false")
            // test configs
            buildConfigField(BOOLEAN, "DEBUG_TEST_IDS_ENABLED", DEBUG_TEST_IDS_ENABLED)
            // VAU feature toggles for development
            buildConfigField(BOOLEAN, "VAU_ENABLE_INTERCEPTOR", "true")
        }
    }
}

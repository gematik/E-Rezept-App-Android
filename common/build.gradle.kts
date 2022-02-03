import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.LONG
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import de.gematik.ti.erp.overriding
import org.jetbrains.compose.compose
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import java.io.ByteArrayOutputStream

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.codingfeline.buildkonfig")
    id("de.gematik.ti.erp.dependencies")
}

fun getGitHash() =
    if (File("${rootDir.path}/.git").exists()) {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine = listOf("git", "rev-parse", "--short", "HEAD")
            standardOutput = stdout
        }
        stdout.toString().trim()
    } else {
        "n/a"
    }

val USER_AGENT: String by overriding()
val DATA_PROTECTION_LAST_UPDATED: String by overriding()

val VERSION_CODE: String by overriding()
val VERSION_NAME: String by overriding()

val DEBUG_TEST_IDS_ENABLED: String by overriding()
val VAU_OCSP_RESPONSE_MAX_AGE: String by overriding()

val APP_TRUST_ANCHOR_BASE64: String by overriding()
val APP_TRUST_ANCHOR_BASE64_TEST: String by overriding()
val PHARMACY_SERVICE_URI: String by overriding()
val PHARMACY_SERVICE_URI_TEST: String by overriding()
val PHARMACY_API_KEY: String by overriding()
val PHARMACY_API_KEY_TEST: String by overriding()

val PIWIK_TRACKER_URI: String by overriding()

val BASE_SERVICE_URI_PU: String by overriding()
val BASE_SERVICE_URI_TU: String by overriding()
val BASE_SERVICE_URI_RU: String by overriding()
val BASE_SERVICE_URI_TR: String by overriding()
val IDP_SERVICE_URI_PU: String by overriding()
val IDP_SERVICE_URI_TU: String by overriding()
val IDP_SERVICE_URI_RU: String by overriding()
val IDP_SERVICE_URI_TR: String by overriding()

val ERP_API_KEY_GOOGLE_PU: String by overriding()
val ERP_API_KEY_GOOGLE_TU: String by overriding()
val ERP_API_KEY_GOOGLE_RU: String by overriding()
val ERP_API_KEY_GOOGLE_TR: String by overriding()
val ERP_API_KEY_HUAWEI_PU: String by overriding()
val ERP_API_KEY_HUAWEI_TU: String by overriding()
val ERP_API_KEY_HUAWEI_RU: String by overriding()
val ERP_API_KEY_HUAWEI_TR: String by overriding()

val PIWIK_TRACKER_ID_GOOGLE: String by overriding()
val PIWIK_TRACKER_ID_HUAWEI: String by overriding()

val SAFETYNET_API_KEY: String by overriding()

kotlin {
    android()
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "15"
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.materialIconsExtended)
                api(compose.ui)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
            }
        }
        val androidMain by getting {
            dependencies {
            }
        }
        val androidTest by getting {
            dependencies {
            }
        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)
            }
        }
        val desktopTest by getting
    }
}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 24
        targetSdk = 30
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    // namespace = "de.gematik.ti.erp.lib"
}

enum class Platforms {
    Google, Huawei, Konnektathon, Desktop
}

enum class Environments {
    PU, TU, RU, TR
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
        buildConfigField(STRING, "PIWIK_TRACKER_URI", PIWIK_TRACKER_URI)
        buildConfigField(STRING, "SAFETYNET_API_KEY", SAFETYNET_API_KEY)
        buildConfigField(STRING, "BUILD_FLAVOR", project.property("buildkonfig.flavor") as String)
    }

    fun defaultConfigs(
        flavor: String,
        isInternal: Boolean,
        baseServiceUri: String,
        idpServiceUri: String,
        erpApiKey: String,
        piwikTrackerId: String?,
        pharmacyServiceUri: String,
        pharmacyServiceApiKey: String,
        trustAnchor: String,
    ) {
        defaultConfigs(flavor) {
            buildConfigField(BOOLEAN, "INTERNAL", isInternal.toString())
            buildConfigField(STRING, "BASE_SERVICE_URI", baseServiceUri)
            buildConfigField(STRING, "IDP_SERVICE_URI", idpServiceUri)
            buildConfigField(STRING, "ERP_API_KEY", erpApiKey)
            piwikTrackerId?.let {
                buildConfigField(STRING, "PIWIK_TRACKER_ID", piwikTrackerId)
            }
            buildConfigField(STRING, "PHARMACY_SERVICE_URI", pharmacyServiceUri)
            buildConfigField(STRING, "PHARMACY_API_KEY", pharmacyServiceApiKey)
            buildConfigField(STRING, "APP_TRUST_ANCHOR_BASE64", trustAnchor)
        }
    }

    val platforms = Platforms.values()
    val environments = Environments.values()
    val types = Types.values()

    platforms.forEach { platform ->
        environments.forEach { environment ->
            types.forEach { type ->
                val plat = platform.name.toLowerCase()
                val env = environment.name.toLowerCase().capitalizeAsciiOnly()
                val typ = type.name.toLowerCase().capitalizeAsciiOnly()
                val flavor = plat + env + typ

                println("Flavor: $flavor")

                defaultConfigs(
                    flavor = flavor,
                    isInternal = type == Types.Internal,
                    baseServiceUri = when (environment) {
                        Environments.PU -> BASE_SERVICE_URI_PU
                        Environments.TU -> BASE_SERVICE_URI_TU
                        Environments.RU -> BASE_SERVICE_URI_RU
                        Environments.TR -> BASE_SERVICE_URI_TR
                    },
                    idpServiceUri = when (environment) {
                        Environments.PU -> IDP_SERVICE_URI_PU
                        Environments.TU -> IDP_SERVICE_URI_TU
                        Environments.RU -> IDP_SERVICE_URI_RU
                        Environments.TR -> IDP_SERVICE_URI_TR
                    },
                    erpApiKey = when (platform) {
                        Platforms.Desktop, Platforms.Google, Platforms.Konnektathon -> when (environment) {
                            Environments.PU -> ERP_API_KEY_GOOGLE_PU
                            Environments.TU -> ERP_API_KEY_GOOGLE_TU
                            Environments.RU -> ERP_API_KEY_GOOGLE_RU
                            Environments.TR -> ERP_API_KEY_GOOGLE_TR
                        }
                        Platforms.Huawei -> when (environment) {
                            Environments.PU -> ERP_API_KEY_HUAWEI_PU
                            Environments.TU -> ERP_API_KEY_HUAWEI_TU
                            Environments.RU -> ERP_API_KEY_HUAWEI_RU
                            Environments.TR -> ERP_API_KEY_HUAWEI_TR
                        }
                    },
                    piwikTrackerId = when (platform) {
                        Platforms.Google, Platforms.Konnektathon -> PIWIK_TRACKER_ID_GOOGLE
                        Platforms.Huawei -> PIWIK_TRACKER_ID_HUAWEI
                        Platforms.Desktop -> null
                    },
                    pharmacyServiceUri = when (environment) {
                        Environments.PU -> PHARMACY_SERVICE_URI
                        Environments.TU,
                        Environments.RU,
                        Environments.TR -> PHARMACY_SERVICE_URI_TEST
                    },
                    pharmacyServiceApiKey = when (environment) {
                        Environments.PU -> PHARMACY_API_KEY
                        Environments.TU,
                        Environments.RU,
                        Environments.TR -> PHARMACY_API_KEY_TEST
                    },
                    trustAnchor = when (environment) {
                        Environments.PU -> APP_TRUST_ANCHOR_BASE64
                        Environments.TU -> APP_TRUST_ANCHOR_BASE64_TEST
                        Environments.RU -> APP_TRUST_ANCHOR_BASE64_TEST
                        Environments.TR -> APP_TRUST_ANCHOR_BASE64_TEST
                    }
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

            // test configs
            buildConfigField(BOOLEAN, "TEST_RUN_WITH_TRUSTSTORE_INTEGRATION", "false")
            buildConfigField(BOOLEAN, "DEBUG_TEST_IDS_ENABLED", DEBUG_TEST_IDS_ENABLED)

            // VAU feature toggles for development
            buildConfigField(BOOLEAN, "VAU_ENABLE_INTERCEPTOR", "true")
            buildConfigField(LONG, "VAU_OCSP_RESPONSE_MAX_AGE", VAU_OCSP_RESPONSE_MAX_AGE)
        }
    }
}

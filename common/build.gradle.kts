import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.LONG
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import de.gematik.ti.erp.Dependencies
import de.gematik.ti.erp.inject
import de.gematik.ti.erp.overriding
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import java.io.ByteArrayOutputStream

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("io.realm.kotlin")
    id("org.jetbrains.compose")
    id("com.codingfeline.buildkonfig")
    id("de.gematik.ti.erp.dependencies")
    id("de.gematik.ti.erp.gradleplugins.TechnicalRequirementsPlugin")
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
val BASE_SERVICE_URI_PU: String by overriding()
val BASE_SERVICE_URI_TU: String by overriding()
val BASE_SERVICE_URI_RU: String by overriding()
val BASE_SERVICE_URI_RU_DEV: String by overriding()
val BASE_SERVICE_URI_TR: String by overriding()
val IDP_SERVICE_URI_PU: String by overriding()
val IDP_SERVICE_URI_TU: String by overriding()
val IDP_SERVICE_URI_RU: String by overriding()
val IDP_SERVICE_URI_RU_DEV: String by overriding()
val IDP_SERVICE_URI_TR: String by overriding()
val ERP_API_KEY_GOOGLE_PU: String by overriding()
val ERP_API_KEY_GOOGLE_TU: String by overriding()
val ERP_API_KEY_GOOGLE_RU: String by overriding()
val ERP_API_KEY_GOOGLE_TR: String by overriding()
val ERP_API_KEY_HUAWEI_PU: String by overriding()
val ERP_API_KEY_HUAWEI_TU: String by overriding()
val ERP_API_KEY_HUAWEI_RU: String by overriding()
val ERP_API_KEY_HUAWEI_TR: String by overriding()
val ERP_API_KEY_DESKTOP_PU: String by overriding()
val ERP_API_KEY_DESKTOP_TU: String by overriding()
val ERP_API_KEY_DESKTOP_RU: String by overriding()
val INTEGRITY_API_KEY: String by overriding()
val INTEGRITY_VERIFICATION_KEY: String by overriding()
val CLOUD_PROJECT_NUMBER: String by overriding()
val DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE: String by overriding()
val DEFAULT_VIRTUAL_HEALTH_CARD_PRIVATE_KEY: String by overriding()
val DEBUG_VISUAL_TEST_TAGS: String? by project
val APP_CENTER_SECRET: String by overriding()
val BUILD_TYPE_MINIFIED_DEBUG: String by overriding()

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = Dependencies.Versions.JavaVersion.KOTLIN_OPTIONS_JVM_TARGET
            }
        }
    }
    jvm("desktop")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
                inject {
                    androidX {
                        implementation(multiplatformPaging) {
                            // remove coroutine dependency; otherwise intellij will be confused with "duplicated class import"
                            exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
                        }
                    }
                    coroutines {
                        implementation(coroutinesCore)
                    }
                    dateTime {
                        implementation(datetime)
                    }
                    database {
                        implementation(realm)
                    }
                    crypto {
                        implementation(jose4j)
                        compileOnly(bouncycastleBcprov)
                        compileOnly(bouncycastleBcpkix)
                    }
                    serialization {
                        implementation(kotlinXJson)
                    }
                    logging {
                        implementation(napier)
                    }
                    network {
                        implementation(retrofit)
                        implementation(okhttp3)
                        implementation(retrofit2KotlinXSerialization)
                        implementation(okhttpLogging)
                    }
                    dependencyInjection {
                        implementation(kodeinCompose)
                    }
                }
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
            }
        }
        val commonTest by getting {
            dependencies {
                inject {
                    database {
                        implementation(realm)
                    }
                    coroutinesTest {
                        implementation(coroutinesTest)
                    }
                    serialization {
                        implementation(kotlinXJson)
                    }
                    test {
                        implementation(kotlinTest)
                        implementation(kotlinTestCommon)
                        implementation(kotlinReflect)
                        implementation(junit4)
                        implementation(mockkOld)
                        implementation(snakeyaml)
                    }
                    crypto {
                        implementation(jose4j)
                        implementation(bouncycastleBcprov)
                        implementation(bouncycastleBcpkix)
                    }
                    networkTest {
                        implementation(mockWebServer)
                    }
                    dateTime {
                        implementation(datetime)
                    }
                }
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                inject {
                    androidX {
                        implementation(coreKtx)
                    }
                    crypto {
                        implementation(bouncycastleBcprov)
                        implementation(bouncycastleBcpkix)
                    }
                    dependencyInjection {
                        implementation(kodeinViewModel)
                        implementation(kodeinSavedState)
                    }
                }
            }
        }
        val desktopMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(compose.preview)
            }
        }
        val desktopTest by getting {
            dependencies {
                inject {
                    crypto {
                        implementation(bouncycastleBcprov)
                        implementation(bouncycastleBcpkix)
                    }
                }
            }
        }
    }
}
android {
    buildToolsVersion = "33.0.1"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    compileSdk = Dependencies.Versions.SdkVersions.COMPILE_SDK_VERSION
    defaultConfig {
        minSdk = Dependencies.Versions.SdkVersions.MIN_SDK_VERSION
    }
    compileOptions {
        sourceCompatibility = Dependencies.Versions.JavaVersion.PROJECT_JAVA_VERSION
        targetCompatibility = Dependencies.Versions.JavaVersion.PROJECT_JAVA_VERSION
    }
    buildTypes {
        val debug by getting {
            isJniDebuggable = true
        }
        create("minifiedDebug") {
            initWith(debug)
        }
    }
    namespace = "de.gematik.ti.erp.lib"
}
enum class Platforms {
    Google, Huawei, Konnektathon, Desktop
}
enum class Environments {
    PU, TU, RU, DEVRU, TR
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
        buildConfigField(STRING, "DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE", DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE)
        buildConfigField(STRING, "DEFAULT_VIRTUAL_HEALTH_CARD_PRIVATE_KEY", DEFAULT_VIRTUAL_HEALTH_CARD_PRIVATE_KEY)
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
        pharmacyServiceApiKey: String,
        trustAnchor: String,
        ocspResponseMaxAge: String
    ) {
        defaultConfigs(flavor) {
            buildConfigField(STRING, "VERSION_NAME", VERSION_NAME)
            buildConfigField(INT, "VERSION_CODE", VERSION_CODE)
            buildConfigField(BOOLEAN, "INTERNAL", isInternal.toString())
            if (isInternal) {
                buildConfigField(STRING, "BASE_SERVICE_URI_PU", BASE_SERVICE_URI_PU)
                buildConfigField(STRING, "BASE_SERVICE_URI_RU", BASE_SERVICE_URI_RU)
                buildConfigField(STRING, "BASE_SERVICE_URI_TU", BASE_SERVICE_URI_TU)
                buildConfigField(STRING, "BASE_SERVICE_URI_RU_DEV", BASE_SERVICE_URI_RU_DEV)
                buildConfigField(STRING, "BASE_SERVICE_URI_TR", BASE_SERVICE_URI_TR)
                buildConfigField(STRING, "IDP_SERVICE_URI_PU", IDP_SERVICE_URI_PU)
                buildConfigField(STRING, "IDP_SERVICE_URI_TU", IDP_SERVICE_URI_TU)
                buildConfigField(STRING, "IDP_SERVICE_URI_RU", IDP_SERVICE_URI_RU)
                buildConfigField(STRING, "IDP_SERVICE_URI_RU_DEV", IDP_SERVICE_URI_RU_DEV)
                buildConfigField(STRING, "IDP_SERVICE_URI_TR", IDP_SERVICE_URI_TR)
                buildConfigField(STRING, "PHARMACY_SERVICE_URI_PU", PHARMACY_SERVICE_URI)
                buildConfigField(STRING, "PHARMACY_SERVICE_URI_RU", PHARMACY_SERVICE_URI_TEST)
                buildConfigField(STRING, "ERP_API_KEY_GOOGLE_PU", ERP_API_KEY_GOOGLE_PU)
                buildConfigField(STRING, "ERP_API_KEY_GOOGLE_RU", ERP_API_KEY_GOOGLE_RU)
                buildConfigField(STRING, "ERP_API_KEY_GOOGLE_TU", ERP_API_KEY_GOOGLE_TU)
                buildConfigField(STRING, "ERP_API_KEY_GOOGLE_TR", ERP_API_KEY_GOOGLE_TR)
                buildConfigField(STRING, "PHARMACY_API_KEY_PU", PHARMACY_API_KEY)
                buildConfigField(STRING, "PHARMACY_API_KEY_RU", PHARMACY_API_KEY_TEST)
                buildConfigField(STRING, "APP_TRUST_ANCHOR_BASE64_PU", APP_TRUST_ANCHOR_BASE64)
                buildConfigField(STRING, "APP_TRUST_ANCHOR_BASE64_TU", APP_TRUST_ANCHOR_BASE64_TEST)
                buildConfigField(STRING, "IDP_SCOPE_DEVRU", "e-rezept-dev openid")
            }
            buildConfigField(STRING, "BASE_SERVICE_URI", baseServiceUri)
            buildConfigField(STRING, "IDP_SERVICE_URI", idpServiceUri)
            buildConfigField(STRING, "ERP_API_KEY", erpApiKey)
            buildConfigField(STRING, "PHARMACY_SERVICE_URI", pharmacyServiceUri)
            buildConfigField(STRING, "PHARMACY_API_KEY", pharmacyServiceApiKey)
            buildConfigField(STRING, "APP_TRUST_ANCHOR_BASE64", trustAnchor)
            buildConfigField(LONG, "VAU_OCSP_RESPONSE_MAX_AGE", ocspResponseMaxAge)
            buildConfigField(BOOLEAN, "TEST_RUN_WITH_TRUSTSTORE_INTEGRATION", "false")
            buildConfigField(BOOLEAN, "TEST_RUN_WITH_IDP_INTEGRATION", "false")
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
                    },
                    idpServiceUri = when (environment) {
                        Environments.PU -> IDP_SERVICE_URI_PU
                        Environments.TU -> IDP_SERVICE_URI_TU
                        Environments.RU -> IDP_SERVICE_URI_RU
                        Environments.DEVRU -> IDP_SERVICE_URI_RU_DEV
                        Environments.TR -> IDP_SERVICE_URI_TR
                    },
                    erpApiKey = when (platform) {
                        Platforms.Google, Platforms.Konnektathon -> when (environment) {
                            Environments.PU -> ERP_API_KEY_GOOGLE_PU
                            Environments.TU -> ERP_API_KEY_GOOGLE_TU
                            Environments.DEVRU,
                            Environments.RU -> ERP_API_KEY_GOOGLE_RU
                            Environments.TR -> ERP_API_KEY_GOOGLE_TR
                        }
                        Platforms.Desktop -> when (environment) {
                            Environments.PU -> ERP_API_KEY_DESKTOP_PU
                            Environments.TU -> ERP_API_KEY_DESKTOP_TU
                            Environments.DEVRU,
                            Environments.RU -> ERP_API_KEY_DESKTOP_RU
                            Environments.TR -> ERP_API_KEY_GOOGLE_TR
                        }
                        Platforms.Huawei -> when (environment) {
                            Environments.PU -> ERP_API_KEY_HUAWEI_PU
                            Environments.TU -> ERP_API_KEY_HUAWEI_TU
                            Environments.DEVRU,
                            Environments.RU -> ERP_API_KEY_HUAWEI_RU
                            Environments.TR -> ERP_API_KEY_HUAWEI_TR
                        }
                    },
                    pharmacyServiceUri = when (environment) {
                        Environments.PU -> PHARMACY_SERVICE_URI
                        Environments.TU,
                        Environments.RU,
                        Environments.DEVRU,
                        Environments.TR -> PHARMACY_SERVICE_URI_TEST
                    },
                    pharmacyServiceApiKey = when (environment) {
                        Environments.PU -> PHARMACY_API_KEY
                        Environments.TU,
                        Environments.RU,
                        Environments.DEVRU,
                        Environments.TR -> PHARMACY_API_KEY_TEST
                    },
                    trustAnchor = when (environment) {
                        Environments.PU -> APP_TRUST_ANCHOR_BASE64
                        Environments.TU,
                        Environments.RU,
                        Environments.DEVRU,
                        Environments.TR -> APP_TRUST_ANCHOR_BASE64_TEST
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

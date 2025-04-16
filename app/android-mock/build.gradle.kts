@file:Suppress("VariableNaming", "PropertyName", "UnusedPrivateProperty")

import de.gematik.ti.erp.app.plugins.dependencies.overrides
import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    id("base-android-application")
    id("de.gematik.ti.erp.names")
    id("de.gematik.ti.erp.dependency-overrides")
    alias(libs.plugins.compose.compiler)
}

val VERSION_CODE: String by overrides()
val VERSION_NAME: String by overrides()
val gematik = AppDependencyNamesPlugin()

android {
    namespace = gematik.moduleName("mock")
    defaultConfig {
        applicationId = gematik.idName("mock")
        versionCode = VERSION_CODE.toInt()
        versionName = VERSION_NAME

        testApplicationId = gematik.moduleName("mock.test")
        // Check if MAPS_API_KEY is defined, otherwise provide a default value
        val mapsApiKey = project.findProperty("MAPS_API_KEY") ?: "DEFAULT_PLACEHOLDER_KEY"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }
    androidResources {
        generateLocaleConfig = true
    }
    buildTypes {
        val release by getting {
            resValue("string", "app_label", "E-Rezept Mock")
        }
        val debug by getting {
            applicationIdSuffix = ".debug"
            resValue("string", "app_label", "E-Rezept Mock")
            versionNameSuffix = "-debug"
        }
        create(gematik.minifiedDebug) {
            initWith(debug)
        }
    }
}

dependencies {
    implementation(project(":utils"))
    implementation(project(gematik.feature))
    implementation(project(gematik.demoMode))
    implementation(project(gematik.uiComponents))
    implementation(project(gematik.multiplatform))
    implementation(project(gematik.fhirParser))
    implementation(libs.tracing)
    implementation(libs.bundles.crypto)
    implementation(libs.bundles.accompanist)
    implementation(libs.bundles.database)
    testImplementation(project(gematik.multiplatform))
    androidTestImplementation(project(gematik.testActions))
    androidTestImplementation(project(gematik.testTags))
    debugImplementation(libs.tracing)
}

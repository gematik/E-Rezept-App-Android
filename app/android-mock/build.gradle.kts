@file:Suppress("VariableNaming", "PropertyName", "UnusedPrivateProperty", "unused")

import de.gematik.ti.erp.app.plugins.dependencies.overrides
import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    alias(libs.plugins.base.android.app)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.module.names)
    alias(libs.plugins.dependency.overrides)
}

val VERSION_CODE: String by overrides()
val VERSION_NAME: String by overrides()
val namesPlugin = AppDependencyNamesPlugin()

android {
    namespace = namesPlugin.moduleName("mock")
    defaultConfig {
        applicationId = namesPlugin.idName("mock")
        versionCode = VERSION_CODE.toInt()
        versionName = VERSION_NAME

        testApplicationId = namesPlugin.moduleName("mock.test")
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
    }
}

dependencies {
    implementation(project(namesPlugin.utils))
    implementation(project(namesPlugin.fhirParser))
    implementation(project(namesPlugin.feature))
    implementation(project(namesPlugin.core))
    implementation(project(namesPlugin.demoMode))
    implementation(project(namesPlugin.uiComponents))
    implementation(project(namesPlugin.multiplatform))
    implementation(project(namesPlugin.fhirParser))
    implementation(project(namesPlugin.database))
    implementation(project(namesPlugin.eurezept))
    implementation(libs.bundles.crypto)
    implementation(libs.bundles.accompanist)
    implementation(libs.bundles.database)
    androidTestImplementation(project(namesPlugin.testActions))
    androidTestImplementation(project(namesPlugin.testTags))
}

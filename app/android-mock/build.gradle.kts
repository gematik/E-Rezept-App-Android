@file:Suppress("VariableNaming", "PropertyName", "UnusedPrivateProperty", "unused")

import de.gematik.ti.erp.app.plugins.dependencies.overrides
import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin
import java.util.Properties

plugins {
    alias(libs.plugins.base.android.app)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.module.names)
    alias(libs.plugins.dependency.overrides)
}

val VERSION_CODE: String by overrides()
val VERSION_NAME: String by overrides()
val namesPlugin = AppDependencyNamesPlugin()

val rootProject = project.rootProject

android {
    namespace = namesPlugin.moduleName("mock")
    defaultConfig {
        applicationId = namesPlugin.idName("mock")
        versionCode = VERSION_CODE.toInt()
        versionName = VERSION_NAME

        testApplicationId = namesPlugin.moduleName("mock.test")

        // Load MAPS_API_KEY from local.properties
        val localPropertiesFile = rootProject.file("local.properties")
        val mapsApiKey = if (localPropertiesFile.exists()) {
            val localProps = Properties()
            localProps.load(localPropertiesFile.inputStream())
            localProps.getProperty("MAPS_API_KEY", "DEFAULT_PLACEHOLDER_KEY")
        } else {
            "DEFAULT_PLACEHOLDER_KEY"
        }
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }
    androidResources {
        generateLocaleConfig = true
    }

    buildTypes {
        val debug by getting {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue("string", "app_label", "E-Rezept Mock Debug")
            if (rootProject.file("keystore/debug.keystore").exists()) {
                signingConfigs {
                    getByName("debug") {
                        storeFile = rootProject.file("keystore/debug.keystore")
                        keyAlias = "androiddebugkey"
                        storePassword = "android"
                        keyPassword = "android"
                    }
                }
            }
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
    implementation(project(namesPlugin.pushNotifications))
    implementation(libs.bundles.crypto)
    implementation(libs.bundles.accompanist)
    implementation(libs.bundles.database)
    androidTestImplementation(project(namesPlugin.testActions))
    androidTestImplementation(project(namesPlugin.testTags))
}

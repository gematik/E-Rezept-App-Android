import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    alias(libs.plugins.base.android.library)
    alias(libs.plugins.module.names)
    alias(libs.plugins.compose.compiler)
}

val namesPlugin = AppDependencyNamesPlugin()

android {
    namespace = namesPlugin.moduleName("consent")
    defaultConfig {
        testApplicationId = namesPlugin.moduleName("consent.test")
    }
}

dependencies {
    implementation(project(namesPlugin.utils))
    implementation(project(namesPlugin.fhirParser))
    implementation(project(namesPlugin.core))
    implementation(project(namesPlugin.multiplatform))
    implementation(project(namesPlugin.uiComponents))
    implementation(libs.bundles.animation)
}

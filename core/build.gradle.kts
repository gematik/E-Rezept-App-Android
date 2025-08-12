import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    alias(libs.plugins.base.android.library)
    alias(libs.plugins.module.names)
    alias(libs.plugins.compose.compiler)
}

val namesPlugin = AppDependencyNamesPlugin()

android {
    namespace = namesPlugin.moduleName("core")
    defaultConfig {
        testApplicationId = namesPlugin.moduleName("core.test")
    }
}

dependencies {
    implementation(project(namesPlugin.utils))
    implementation(project(namesPlugin.fhirParser))
    implementation(project(namesPlugin.multiplatform))
    implementation(project(namesPlugin.navigation))
    implementation(libs.text.translation)
    implementation(project(namesPlugin.uiComponents))
    implementation(libs.text.recognition)
}

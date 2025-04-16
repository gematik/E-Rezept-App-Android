import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    id("base-android-library")
    id("de.gematik.ti.erp.names")
    alias(libs.plugins.compose.compiler)
}

val namesPlugin = AppDependencyNamesPlugin()

android {
    namespace = namesPlugin.moduleName("digas")
    defaultConfig {
        testApplicationId = namesPlugin.moduleName("digas.test")
    }
}

dependencies {
    implementation(project(namesPlugin.navigation))
    implementation(project(namesPlugin.testTags))
    implementation(project(namesPlugin.multiplatform))
    implementation(project(namesPlugin.uiComponents))
    testImplementation(libs.test.turbine) // to test flows
    testImplementation(project(namesPlugin.multiplatform))
}

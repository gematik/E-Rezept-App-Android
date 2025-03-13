import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    id("base-android-library")
    id("de.gematik.ti.erp.names")
    id("jacoco")
    alias(libs.plugins.paparazzi)
    alias(libs.plugins.compose.compiler)
}

val namesPlugin = AppDependencyNamesPlugin()

android {
    namespace = namesPlugin.moduleName("features")
    defaultConfig {
        testApplicationId = namesPlugin.moduleName("test")
    }
}

dependencies {
    implementation(project(namesPlugin.demoMode))
    implementation(project(namesPlugin.testTags))
    implementation(project(namesPlugin.multiplatform))
    implementation(project(namesPlugin.uiComponents))
    implementation(libs.androidx.work)
    implementation(libs.certificatetransparency.android)
    testImplementation(libs.test.turbine) // to test flows
    testImplementation(project(namesPlugin.multiplatform))
}

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
    implementation(project(namesPlugin.core))
    implementation(project(namesPlugin.utils))
    implementation(project(namesPlugin.fhirParser))
    implementation(project(namesPlugin.demoMode))
    implementation(project(namesPlugin.digas))
    implementation(project(namesPlugin.tracker))
    implementation(project(namesPlugin.navigation))
    implementation(project(namesPlugin.testTags))
    implementation(project(namesPlugin.multiplatform))
    implementation(project(namesPlugin.uiComponents))
    implementation(libs.androidx.work)
    implementation(libs.certificatetransparency.android)
    debugImplementation(libs.chucker)
    debugImplementation(libs.leak.canary)
    testImplementation(libs.test.turbine)
    testImplementation(project(namesPlugin.multiplatform))
}

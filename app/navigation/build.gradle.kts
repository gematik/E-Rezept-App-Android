import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    id("base-android-library")
    id("de.gematik.ti.erp.names")
    alias(libs.plugins.compose.compiler)
}

val namesPlugin = AppDependencyNamesPlugin()

android {
    namespace = namesPlugin.moduleName("navigation")
    defaultConfig {
        testApplicationId = namesPlugin.moduleName("navigation.test")
    }
}

dependencies {
    implementation(project(namesPlugin.tracker))
    implementation(project(namesPlugin.testTags))
    implementation(project(namesPlugin.uiComponents))
    implementation(project(namesPlugin.multiplatform))
    implementation(project(namesPlugin.utils))
}

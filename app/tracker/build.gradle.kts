import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    id("base-android-library")
    id("de.gematik.ti.erp.names")
    alias(libs.plugins.compose.compiler)
}

val namesPlugin = AppDependencyNamesPlugin()

android {
    namespace = namesPlugin.moduleName("tracker")
    defaultConfig {
        testApplicationId = namesPlugin.moduleName("tracker.test")
    }
}

dependencies {
    implementation(project(namesPlugin.multiplatform))
    implementation(project(namesPlugin.utils))
}

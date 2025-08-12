import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    id("base-android-library")
    id("de.gematik.ti.erp.names")
    alias(libs.plugins.compose.compiler)
}

val namesPlugin = AppDependencyNamesPlugin()

android {
    namespace = namesPlugin.moduleName("ui_components")
    defaultConfig {
        testApplicationId = namesPlugin.moduleName("ui_components.test")
    }
}

dependencies {
    implementation(libs.compose.ui)
    implementation(libs.androidx.work)
    implementation(project(namesPlugin.testTags))
    implementation(project(namesPlugin.utils))
}

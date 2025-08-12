import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    alias(libs.plugins.base.android.library)
    alias(libs.plugins.module.names)
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

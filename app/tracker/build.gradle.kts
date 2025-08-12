import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    alias(libs.plugins.base.android.library)
    alias(libs.plugins.module.names)
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
    implementation(libs.bundles.tracking)
}

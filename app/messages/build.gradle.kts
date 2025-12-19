import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    alias(libs.plugins.base.android.library)
    alias(libs.plugins.module.names)
    alias(libs.plugins.compose.compiler)
}

val namesPlugin = AppDependencyNamesPlugin()

android {
    namespace = namesPlugin.moduleName("messages")
    defaultConfig {
        testApplicationId = namesPlugin.moduleName("messages.test")
    }
}

dependencies {
    implementation(project(namesPlugin.utils))
    implementation(project(namesPlugin.database))
    implementation(project(namesPlugin.core))
    implementation(project(namesPlugin.navigation))
    implementation(project(namesPlugin.testTags))
    implementation(project(namesPlugin.tracker))
    implementation(project(namesPlugin.multiplatform))
    implementation(project(namesPlugin.uiComponents))
    implementation(libs.bundles.animation)
    testImplementation(libs.bundles.corutinestest)
    testImplementation(libs.bundles.testing)
    testImplementation(project(namesPlugin.mocks))
    testImplementation(libs.test.turbine)
}

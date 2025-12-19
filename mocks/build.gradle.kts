import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    alias(libs.plugins.base.android.library)
    alias(libs.plugins.module.names)
    alias(libs.plugins.compose.compiler)
}

val namesPlugin = AppDependencyNamesPlugin()

android {
    namespace = namesPlugin.moduleName("mocks")
    defaultConfig {
        testApplicationId = namesPlugin.moduleName("mocks")
    }
}

// Module to hold test for all higher level modules
dependencies {
    implementation(project(namesPlugin.core))
    implementation(project(namesPlugin.utils))
    implementation(project(namesPlugin.fhirParser))
    implementation(project(namesPlugin.database))
    implementation(project(namesPlugin.multiplatform))
    implementation(libs.test.mockk) // using it directly since this is only a test based mock module from gradle wiring
}

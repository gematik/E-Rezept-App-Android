import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    alias(libs.plugins.base.android.library)
    alias(libs.plugins.module.names)
    alias(libs.plugins.compose.compiler)
}

val namesPlugin = AppDependencyNamesPlugin()

android {
    namespace = namesPlugin.moduleName("demomode")
    defaultConfig {
        testApplicationId = namesPlugin.moduleName("demomode.test")
    }
}

dependencies {
    implementation(project(namesPlugin.utils))
    implementation(project(namesPlugin.fhirParser))
    implementation(project(namesPlugin.core))
    implementation(project(namesPlugin.database))
    implementation(project(namesPlugin.digas))
    implementation(project(namesPlugin.eurezept))
    implementation(project(namesPlugin.multiplatform))
    implementation(project(namesPlugin.uiComponents))
}

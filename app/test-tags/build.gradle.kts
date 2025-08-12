import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    alias(libs.plugins.base.android.library)
    alias(libs.plugins.module.names)
    alias(libs.plugins.compose.compiler)
}

val gematik = AppDependencyNamesPlugin()

android {
    namespace = gematik.moduleName("test_tags")
    defaultConfig {
        testApplicationId = gematik.moduleName("test_tags.test")
    }
}

dependencies {
    implementation(project(":utils"))
    implementation(libs.compose.ui)
}

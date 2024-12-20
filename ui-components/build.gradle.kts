import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    id("base-android-library")
    id("de.gematik.ti.erp.names")
}

val gematik = AppDependencyNamesPlugin()

android {
    namespace = gematik.moduleName("ui_components")
    defaultConfig {
        testApplicationId = gematik.moduleName("ui_components.test")
    }
}

dependencies {
    implementation(libs.compose.ui)
}

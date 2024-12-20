import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    id("base-android-library")
    id("de.gematik.ti.erp.names")
}

val gematik = AppDependencyNamesPlugin()

android {
    namespace = gematik.moduleName("test_tags")
    defaultConfig {
        testApplicationId = gematik.moduleName("test_tags.test")
    }
}

dependencies {
    implementation(libs.compose.ui)
}
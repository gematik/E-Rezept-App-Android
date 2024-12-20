import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    id("base-android-library")
    id("de.gematik.ti.erp.names")
}

val gematik = AppDependencyNamesPlugin()

android {
    namespace = gematik.moduleName("demomode")
    defaultConfig {
        testApplicationId = gematik.moduleName("demomode.test")
    }
}

dependencies {
    implementation(project(gematik.multiplatform))
    implementation(project(gematik.uiComponents))
}

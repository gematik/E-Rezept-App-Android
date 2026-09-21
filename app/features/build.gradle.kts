import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    alias(libs.plugins.base.android.library)
    alias(libs.plugins.module.names)
    id("jacoco")
    alias(libs.plugins.paparazzi)
    alias(libs.plugins.compose.compiler)
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.kotlin.serialization)
}

val namesPlugin = AppDependencyNamesPlugin()

android {
    namespace = namesPlugin.moduleName("features")
    defaultConfig {
        testApplicationId = namesPlugin.moduleName("test")
    }
}

dependencies {
    implementation(project(namesPlugin.core))
    implementation(project(namesPlugin.utils))
    implementation(project(namesPlugin.fhirParser))
    implementation(project(namesPlugin.demoMode))
    implementation(project(namesPlugin.digas))
    implementation(project(namesPlugin.eurezept))
    implementation(project(namesPlugin.pushNotifications))
    implementation(project(namesPlugin.messages))
    implementation(project(namesPlugin.tracker))
    implementation(project(namesPlugin.navigation))
    implementation(project(namesPlugin.testTags))
    implementation(project(namesPlugin.database))
    implementation(project(namesPlugin.multiplatform))
    implementation(project(namesPlugin.uiComponents))
    implementation(project(namesPlugin.consent))
    implementation(libs.androidx.work)
    implementation(libs.kotlin.reflect)
    debugImplementation(libs.chucker)
    debugImplementation(libs.leak.canary)

    testImplementation(libs.test.turbine)
    testImplementation(project(namesPlugin.mocks))
    testImplementation(project(namesPlugin.multiplatform))
    testImplementation(libs.robolectric)
    testImplementation(libs.room.testing)

    implementation(libs.text.recognition)
    implementation(libs.bundles.serialization)
}


// Room schema export configuration
kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }
}

android {
    sourceSets.getByName("test") {
        assets.srcDirs(files("$projectDir/schemas"))
    }
}

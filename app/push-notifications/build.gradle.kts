import de.gematik.ti.erp.app.plugins.dependencies.overrides
import de.gematik.ti.erp.app.plugins.names.AppDependencyNamesPlugin

plugins {
    alias(libs.plugins.base.android.library)
    alias(libs.plugins.module.names)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dependency.overrides)
}

val namesPlugin = AppDependencyNamesPlugin()
val PUSH_NOTIFICATION_DEBUG_INITIAL_SHARED_SECRET: String by overrides()
val PUSH_NOTIFICATION_DEBUG_TIME_ISS_CREATED: String by overrides()
val PUSH_NOTIFICATION_DEBUG_KEY_IDENTIFIER: String by overrides()

android {
    namespace = namesPlugin.moduleName("pushnotifications")
    defaultConfig {
        testApplicationId = namesPlugin.moduleName("pushnotifications.test")
        buildConfigField(
            "String",
            "PUSH_NOTIFICATION_DEBUG_INITIAL_SHARED_SECRET",
            "\"$PUSH_NOTIFICATION_DEBUG_INITIAL_SHARED_SECRET\""
        )
        buildConfigField(
            "String",
            "PUSH_NOTIFICATION_DEBUG_TIME_ISS_CREATED",
            "\"$PUSH_NOTIFICATION_DEBUG_TIME_ISS_CREATED\""
        )
        buildConfigField(
            "String",
            "PUSH_NOTIFICATION_DEBUG_KEY_IDENTIFIER",
            "\"$PUSH_NOTIFICATION_DEBUG_KEY_IDENTIFIER\""
        )
    }
}

dependencies {
    implementation(project(namesPlugin.utils))
    implementation(project(namesPlugin.database))
    implementation(project(namesPlugin.core))
    implementation(project(namesPlugin.navigation))
    implementation(project(namesPlugin.testTags))
    implementation(project(namesPlugin.multiplatform))
    implementation(project(namesPlugin.uiComponents))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.security.crypto)
    implementation(libs.bundles.serialization)
    testImplementation(project(namesPlugin.mocks))
    testImplementation(libs.test.turbine)
}

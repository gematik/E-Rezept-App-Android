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

val PUSH_GATEWAY_URL_RU: String by overrides()
val PUSH_GATEWAY_URL_DEV: String by overrides()
val PUSH_GATEWAY_URL_PU: String by overrides()
val APP_DISPLAY_NAME: String by overrides()
val ENCRYPTION_METHOD: String by overrides()
val PLATFORM_IDENTIFIER: String by overrides()
val RECEIVING_APP_ID_KONNEKTATHON: String by overrides()
val RECEIVING_APP_ID_TU: String by overrides()
val RECEIVING_APP_ID_PU: String by overrides()

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
        buildConfigField(
            "String",
            "PUSH_GATEWAY_URL_RU",
            "\"$PUSH_GATEWAY_URL_RU\""
        )
        buildConfigField(
            "String",
            "PUSH_GATEWAY_URL_DEV",
            "\"$PUSH_GATEWAY_URL_DEV\""
        )
        buildConfigField(
            "String",
            "PUSH_GATEWAY_URL_PU",
            "\"$PUSH_GATEWAY_URL_PU\""
        )
        buildConfigField(
            "String",
            "APP_DISPLAY_NAME",
            "\"$APP_DISPLAY_NAME\""
        )
        buildConfigField(
            "String",
            "ENCRYPTION_METHOD",
            "\"$ENCRYPTION_METHOD\""
        )
        buildConfigField(
            "String",
            "PLATFORM_IDENTIFIER",
            "\"$PLATFORM_IDENTIFIER\""
        )
        buildConfigField(
            "String",
            "RECEIVING_APP_ID_KONNEKTATHON",
            "\"$RECEIVING_APP_ID_KONNEKTATHON\""
        )
        buildConfigField(
            "String",
            "RECEIVING_APP_ID_TU",
            "\"$RECEIVING_APP_ID_TU\""
        )
        buildConfigField(
            "String",
            "RECEIVING_APP_ID_PU",
            "\"$RECEIVING_APP_ID_PU\""
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

@file:Suppress("UnusedPrivateProperty")

import de.gematik.ti.erp.app.tasks.generateRoomSchemaMigrationsFile

plugins {
    alias(libs.plugins.base.kmp.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.ksp)
}

buildkonfig {
    packageName = "de.gematik.ti.erp.app.database"
    exposeObjectWithName = "BuildKonfig"
    defaultConfigs {
        // Custom flags to check if the app should start with V1 or V2 version of DB
    }
}

android {
    namespace = "de.gematik.ti.erp.app.database"
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":erp-model"))
                implementation(project(":utils"))
                implementation(libs.kotlin.stdlib)
                implementation(libs.androidx.datastore.preferences)
                implementation(compose.runtime)
                implementation(libs.room.runtime)
                implementation(libs.androidx.sqlite.bundled)
                // Add KMP dependencies here
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.security.crypto)
                implementation(libs.sqlcipher)
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
            }
        }

        androidUnitTest {
            dependencies {
                implementation(libs.robolectric)
                implementation(libs.androidx.test.junit)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.test.mockk.android)
                implementation(libs.androidx.test.core)
                implementation(libs.androidx.test.runner)
                implementation(libs.androidx.test.rules)
                implementation(libs.kotlin.test)
                implementation(libs.robolectric)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspDesktop", libs.room.compiler)
}

// add Room db schema migration task
tasks.generateRoomSchemaMigrationsFile()

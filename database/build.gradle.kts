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
                implementation(project(":utils"))
                implementation(libs.kotlin.stdlib)
                implementation(libs.multiplatform.settings)
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
        }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspDesktop", libs.room.compiler)
}

plugins {
    alias(libs.plugins.base.kmp.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.buildkonfig)
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
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
            }
        }

        androidUnitTest {

        }
    }
}

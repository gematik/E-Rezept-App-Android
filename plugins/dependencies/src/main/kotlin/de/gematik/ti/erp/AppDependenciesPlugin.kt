/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

@file:Suppress("Unused")

package de.gematik.ti.erp

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import java.util.Properties

@Suppress("UnstableApiUsage")
class AppDependenciesPlugin : Plugin<Project> {
    val overrideProperties = Properties()

    override fun apply(project: Project) {
        // set android & compose options for all android plugins: `android { ... }`
        project.plugins.all {
            if (this is AppPlugin) {
                project.extensions.getByType(BaseAppModuleExtension::class).apply {
                    composeOptions.kotlinCompilerExtensionVersion = Dependencies.composeVersion
                    buildFeatures {
                        compose = true
                    }
                    compileSdk = Dependencies.CompileSdkVersion
                    defaultConfig {
                        minSdk = Dependencies.MinimumSdkVersion
                        targetSdk = Dependencies.TargetSdkVersion
                    }
                    compileOptions {
                        isCoreLibraryDesugaringEnabled = true
                        sourceCompatibility = JavaVersion.VERSION_1_8
                        targetCompatibility = JavaVersion.VERSION_1_8
                    }
                }
            }
        }

        // Values in local.properties will override values with same names
        val propertiesFile = project.rootProject.file("ci-overrides.properties")
        if (propertiesFile.exists()) {
            overrideProperties.load(propertiesFile.inputStream())
        }
    }

    object Dependencies {
        const val MinimumSdkVersion = 24
        const val CompileSdkVersion = 31
        const val TargetSdkVersion = 31

        object DependencyInjection {
            fun hilt(module: String) = "com.google.dagger:hilt-$module:2.40.5"
            fun kodein(module: String) = "org.kodein.di:kodein-$module:7.10.0"
        }

        object Tracker {
            const val piwik = "pro.piwik.sdk:piwik-sdk:1.0.1"
        }

        object DataMatrix {
            const val mlkitBarcodeScanner = "com.google.mlkit:barcode-scanning:17.0.0"

            // Zxing - used for generating 2d data matrix codes
            const val zxing = "com.google.zxing:core:3.4.1"
        }

        object KotlinX {
            fun coroutines(target: String) = "org.jetbrains.kotlinx:kotlinx-coroutines-$target:1.6.0"
            object Test {
                val coroutinesTest = coroutines("test")
            }
        }

        object PlayServices {
            const val location = "com.google.android.gms:play-services-location:19.0.1"
            const val safetynet = "com.google.android.gms:play-services-safetynet:18.0.1"
        }

        object Android {
            const val desugaring = "com.android.tools:desugar_jdk_libs:1.1.5"
            const val appcompat = "androidx.appcompat:appcompat:1.4.0"
            const val legacySupport = "androidx.legacy:legacy-support-v4:1.0.0"
            const val coreKtx = "androidx.core:core-ktx:1.6.0"
            const val datastorePreferences = "androidx.datastore:datastore-preferences:1.0.0"
            const val biometric = "androidx.biometric:biometric:1.1.0"

            const val security = "androidx.security:security-crypto:1.1.0-alpha03"

            fun lifecycle(module: String) = "androidx.lifecycle:lifecycle-$module:2.4.0"

            const val composeNavigation = "androidx.navigation:navigation-compose:2.4.0-rc01"
            const val composeHiltNavigation = "androidx.hilt:hilt-navigation-compose:1.0.0-rc01"
            const val composeActivity = "androidx.activity:activity-compose:1.4.0"
            const val composePaging = "androidx.paging:paging-compose:1.0.0-alpha14"
            const val constraintLayout = "androidx.constraintlayout:constraintlayout-compose:1.0.0-rc02"

            const val cameraViewVersion = "1.0.0-alpha32"
            const val cameraVersion = "1.1.0-alpha12"
            fun camera(module: String, version: String = cameraVersion) = "androidx.camera:camera-$module:$version"

            const val processPhoenix = "com.jakewharton:process-phoenix:2.1.2"

            object Test {
                const val runner = "androidx.test:runner:1.4.1-alpha03"
                const val orchestrator = "androidx.test:orchestrator:1.4.1-beta01"
                const val archCore = "androidx.arch.core:core-testing:2.1.0"
                const val core = "androidx.test:core:1.4.1-alpha03"
                const val espresso = "androidx.test.espresso:espresso-core:3.4.0"
                const val junitExt = "androidx.test.ext:junit:1.1.3"
                const val navigation = "androidx.navigation:navigation-testing:2.3.5"
            }
        }

        object AndroidX {
            fun paging(suffix: String) = "androidx.paging:paging-$suffix:3.1.0"
        }

        object Logging {
            const val timber = "com.jakewharton.timber:timber:5.0.1"
            const val napier = "io.github.aakira:napier:2.3.0"
            const val slf4jNoOp = "org.slf4j:slf4j-nop:2.0.0-alpha5"
        }

        object Serialization {
            fun moshi(target: String) = "com.squareup.moshi:$target:1.13.0"
            const val kotlinXJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0"

            const val fhir = "ca.uhn.hapi.fhir:hapi-fhir-structures-r4:5.5.1"
        }

        object Crypto {
            const val jose4j = "org.bitbucket.b_c:jose4j:0.7.9"

            fun bouncyCastle(provider: String, targetPlatform: String = "jdk15to18") =
                "org.bouncycastle:$provider-$targetPlatform:1.70"
        }

        object Network {
            fun retrofit2(module: String) = "com.squareup.retrofit2:$module:2.9.0"
            const val retrofit2KotlinXSerialization = "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0"
            fun okhttp3(module: String) = "com.squareup.okhttp3:$module:4.9.2"
            object Test {
                val mockWebServer = okhttp3("mockwebserver")
            }
        }

        object Database {
            const val sqlCipher = "net.zetetic:android-database-sqlcipher:4.5.0"
            fun room(target: String) = "androidx.room:room-$target:2.4.1"
            object Test {
                val roomTesting = room("testing")
            }
        }

        internal const val composeVersion = "1.1.0-rc01"

        object Compose {
            const val compiler = "androidx.compose.compiler:compiler:$composeVersion"
            const val animation = "androidx.compose.animation:animation:$composeVersion"
            const val foundation = "androidx.compose.foundation:foundation:$composeVersion"
            const val material = "androidx.compose.material:material:$composeVersion"
            const val runtime = "androidx.compose.runtime:runtime:$composeVersion"
            const val ui = "androidx.compose.ui:ui:$composeVersion"
            const val uiTooling = "androidx.compose.ui:ui-tooling:$composeVersion"
            const val preview = "androidx.compose.ui:ui-tooling-preview:$composeVersion"
            const val materialIcons =
                "androidx.compose.material:material-icons-core:$composeVersion"
            const val materialIconsExtended =
                "androidx.compose.material:material-icons-extended:$composeVersion"

            fun accompanist(module: String) = "com.google.accompanist:accompanist-$module:0.22.1-rc"

            object Test {
                const val ui = "androidx.compose.ui:ui-test:$composeVersion"
                const val junit4 = "androidx.compose.ui:ui-test-junit4:$composeVersion"
            }
        }

        object PasswordStrength {
            const val zxcvbn = "com.nulab-inc:zxcvbn:1.5.2"
        }

        object Test {
            fun mockk(module: String) = "io.mockk:$module:1.12.2"
            const val junit4 = "junit:junit:4.13.2"
            const val snakeyaml = "org.yaml:snakeyaml:1.30"
            const val json = "org.json:json:20211205"
        }
    }
}

object App {
    fun dependencyInjection(init: AppDependenciesPlugin.Dependencies.DependencyInjection.() -> Unit) =
        AppDependenciesPlugin.Dependencies.DependencyInjection.init()

    fun tracker(init: AppDependenciesPlugin.Dependencies.Tracker.() -> Unit) =
        AppDependenciesPlugin.Dependencies.Tracker.init()

    fun dataMatrix(init: AppDependenciesPlugin.Dependencies.DataMatrix.() -> Unit) =
        AppDependenciesPlugin.Dependencies.DataMatrix.init()

    fun kotlinX(init: AppDependenciesPlugin.Dependencies.KotlinX.() -> Unit) =
        AppDependenciesPlugin.Dependencies.KotlinX.init()

    fun kotlinXTest(init: AppDependenciesPlugin.Dependencies.KotlinX.Test.() -> Unit) =
        AppDependenciesPlugin.Dependencies.KotlinX.Test.init()

    fun playServices(init: AppDependenciesPlugin.Dependencies.PlayServices.() -> Unit) =
        AppDependenciesPlugin.Dependencies.PlayServices.init()

    fun android(init: AppDependenciesPlugin.Dependencies.Android.() -> Unit) =
        AppDependenciesPlugin.Dependencies.Android.init()

    fun androidX(init: AppDependenciesPlugin.Dependencies.AndroidX.() -> Unit) =
        AppDependenciesPlugin.Dependencies.AndroidX.init()

    fun androidTest(init: AppDependenciesPlugin.Dependencies.Android.Test.() -> Unit) =
        AppDependenciesPlugin.Dependencies.Android.Test.init()

    fun logging(init: AppDependenciesPlugin.Dependencies.Logging.() -> Unit) =
        AppDependenciesPlugin.Dependencies.Logging.init()

    fun serialization(init: AppDependenciesPlugin.Dependencies.Serialization.() -> Unit) =
        AppDependenciesPlugin.Dependencies.Serialization.init()

    fun crypto(init: AppDependenciesPlugin.Dependencies.Crypto.() -> Unit) =
        AppDependenciesPlugin.Dependencies.Crypto.init()

    fun network(init: AppDependenciesPlugin.Dependencies.Network.() -> Unit) =
        AppDependenciesPlugin.Dependencies.Network.init()

    fun networkTest(init: AppDependenciesPlugin.Dependencies.Network.Test.() -> Unit) =
        AppDependenciesPlugin.Dependencies.Network.Test.init()

    fun database(init: AppDependenciesPlugin.Dependencies.Database.() -> Unit) =
        AppDependenciesPlugin.Dependencies.Database.init()

    fun databaseTest(init: AppDependenciesPlugin.Dependencies.Database.Test.() -> Unit) =
        AppDependenciesPlugin.Dependencies.Database.Test.init()

    fun compose(init: AppDependenciesPlugin.Dependencies.Compose.() -> Unit) =
        AppDependenciesPlugin.Dependencies.Compose.init()

    fun composeTest(init: AppDependenciesPlugin.Dependencies.Compose.Test.() -> Unit) =
        AppDependenciesPlugin.Dependencies.Compose.Test.init()

    fun passwordStrength(init: AppDependenciesPlugin.Dependencies.PasswordStrength.() -> Unit) =
        AppDependenciesPlugin.Dependencies.PasswordStrength.init()

    fun test(init: AppDependenciesPlugin.Dependencies.Test.() -> Unit) =
        AppDependenciesPlugin.Dependencies.Test.init()
}

fun app(init: App.() -> Unit) = App.init()
val app = AppDependenciesPlugin.Dependencies

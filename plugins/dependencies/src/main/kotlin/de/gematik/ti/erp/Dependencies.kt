/*
 * Copyright (c) 2024 gematik GmbH
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

@file:Suppress("MemberNameEqualsClassName")

package de.gematik.ti.erp

object Dependencies {
    object Versions {
        object SdkVersions {
            const val MIN_SDK_VERSION = 26
            const val COMPILE_SDK_VERSION = 34
            const val TARGET_SDK_VERSION = 34
        }

        object JavaVersion {
            const val KOTLIN_OPTIONS_JVM_TARGET = "17"
            const val KOTLIN_OPTIONS_JVM_TARGET_INT = 17
            val PROJECT_JAVA_VERSION = org.gradle.api.JavaVersion.VERSION_17
        }
    }

    object Accompanist {
        private const val accompanist_version = "0.32.0"

        private fun accompanist(module: String) = "com.google.accompanist:accompanist-$module:$accompanist_version"

        val swipeRefresh = accompanist("swiperefresh")
        val flowLayout = accompanist("flowlayout")
        val pager = accompanist("pager")
        val pageIndicator = accompanist("pager-indicators")
        val systemUiController = accompanist("systemuicontroller")
    }

    object Android {
        private const val desugar_version = "2.0.3"
        const val desugaring = "com.android.tools:desugar_jdk_libs:$desugar_version"
        const val processPhoenix = "com.jakewharton:process-phoenix:2.1.2" // TODO: Not used
        const val imageCropper = "com.github.CanHub:Android-Image-Cropper:4.3.2" // TODO: remove lib for cropping
    }

    object AndroidX {
        private const val appcompat_version = "1.6.1"
        private const val legacy_version = "1.0.0"
        private const val core_ktx_version = "1.11.0-beta02" // 1.12.0 needs compile version 34
        private const val data_store_version = "1.1.0-alpha04" // 1.1.0-alpha05 needs compile version 34
        private const val biometric_version = "1.2.0-alpha05"
        private const val webkit_version = "1.7.0" // 1.8.0 needs compile version 34
        private const val security_crypto_version = "1.1.0-alpha06"

        private const val lifecycle_version = "2.6.2" // needs compile version 34 to go to a higher version
        private const val compose_navigation_version = "2.6.0" // needs compile version 34 for 2.7.3
        private const val compose_activity_version = "1.7.2"
        private const val compose_paging_version = "3.2.1"
        private const val camerax_version = "1.3.0-beta01" // needs compile version 34 for 1.3.0-rc02
        private const val multiplatform_paging_version = "3.2.1" // 3.3.0-alpha02 needs compile version 34

        const val appcompat = "androidx.appcompat:appcompat:$appcompat_version"

        const val legacySupport = "androidx.legacy:legacy-support-v4:$legacy_version"

        const val coreKtx = "androidx.core:core-ktx:$core_ktx_version"

        const val datastorePreferences = "androidx.datastore:datastore-preferences:$data_store_version"

        const val biometric = "androidx.biometric:biometric:$biometric_version"

        const val webkit = "androidx.webkit:webkit:$webkit_version"

        const val security = "androidx.security:security-crypto:$security_crypto_version"

        const val lifecycleViewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"
        const val lifecycleProcess = "androidx.lifecycle:lifecycle-process:$lifecycle_version"
        const val lifecycleComposeRuntime = "androidx.lifecycle:lifecycle-runtime-compose:$lifecycle_version"

        const val composeNavigation = "androidx.navigation:navigation-compose:$compose_navigation_version"

        const val composeActivity = "androidx.activity:activity-compose:$compose_activity_version"

        const val composePaging = "androidx.paging:paging-compose:$compose_paging_version"
        const val multiplatformPaging = "androidx.paging:paging-common-ktx:$multiplatform_paging_version"

        const val camerax2 = "androidx.camera:camera-camera2:$camerax_version"
        const val cameraxLifecycle = "androidx.camera:camera-lifecycle:$camerax_version"
        const val cameraxView = "androidx.camera:camera-view:$camerax_version"

        object Test {
            private const val test_runner_version = "1.6.0-alpha04"
            private const val test_orchestrator_version = "1.5.0-alpha01"
            private const val test_arch_core_version = "2.2.0"
            private const val test_core_version = "1.6.0-alpha02"
            private const val test_rules_version = "1.6.0-alpha01"
            private const val test_espresso_version = "3.6.0-alpha01"
            private const val test_junit_extension_version = "1.2.0-alpha01"
            private const val test_navigation_version = "2.7.2"

            const val runner = "androidx.test:runner:$test_runner_version"
            const val orchestrator = "androidx.test:orchestrator:$test_orchestrator_version"
            const val services = "androidx.test.services:test-services:$test_orchestrator_version"
            const val core = "androidx.test:core:$test_core_version"
            const val rules = "androidx.test:rules:$test_rules_version"
            const val espresso = "androidx.test.espresso:espresso-core:$test_espresso_version"
            const val espressoIntents = "androidx.test.espresso:espresso-intents:$test_espresso_version"
            const val junitExt = "androidx.test.ext:junit:$test_junit_extension_version"

            const val archCore = "androidx.arch.core:core-testing:$test_arch_core_version"

            const val navigation = "androidx.navigation:navigation-testing:$test_navigation_version"
        }

        // Added due to AGP error -> https://github.com/android/android-test/issues/1755
        object Tracing {
            const val tracing = "androidx.tracing:tracing:1.2.0-rc01"
        }
    }

    object Compose {
        private const val compose_version = "1.5.0-beta02"

        const val compiler = "androidx.compose.compiler:compiler:$compose_version"
        const val animation = "androidx.compose.animation:animation:$compose_version"
        const val foundation = "androidx.compose.foundation:foundation:$compose_version"
        const val material = "androidx.compose.material:material:$compose_version"
        const val runtime = "androidx.compose.runtime:runtime:$compose_version"
        const val ui = "androidx.compose.ui:ui:$compose_version"
        const val uiTooling = "androidx.compose.ui:ui-tooling:$compose_version"
        const val preview = "androidx.compose.ui:ui-tooling-preview:$compose_version"
        const val materialIcons = "androidx.compose.material:material-icons-core:$compose_version"
        const val materialIconsExtended = "androidx.compose.material:material-icons-extended:$compose_version"

        object Test {
            const val ui = "androidx.compose.ui:ui-test:$compose_version"
            const val uiManifest = "androidx.compose.ui:ui-test-manifest:$compose_version"
            const val junit4 = "androidx.compose.ui:ui-test-junit4:$compose_version"
        }
    }

    object Coroutines {
        private const val coroutines_version = "1.7.3"
        private fun coroutines(target: String) = "org.jetbrains.kotlinx:kotlinx-coroutines-$target:$coroutines_version"

        val coroutinesCore = coroutines("core")
        val coroutinesAndroid = coroutines("android")
        val coroutinesPlayServices = coroutines("play-services")
        val coroutinesSwing = coroutines("swing")

        object Test {
            val coroutinesTest = coroutines("test")
        }
    }

    object Crypto {
        private const val json_web_token_version = "0.9.3"
        private const val bouncy_castle_version = "1.76"

        const val jose4j = "org.bitbucket.b_c:jose4j:$json_web_token_version"

        const val bouncycastleBcprov = "org.bouncycastle:bcprov-jdk18on:$bouncy_castle_version"
        const val bouncycastleBcpkix = "org.bouncycastle:bcpkix-jdk18on:$bouncy_castle_version"
    }

    object Database {
        // throws error if we go to a higher version (project-issue)
        private const val realm_version = "1.8.0"

        const val realm = "io.realm.kotlin:library-base:$realm_version"
    }

    object DataMatrix {
        private const val ml_kit_version = "17.2.0"
        private const val zxing_version = "3.5.2"

        // TODO: Get rid of this lib and use a non-google safer lib like zing (its just below)
        const val mlkitBarcodeScanner = "com.google.mlkit:barcode-scanning:$ml_kit_version"

        // Zxing - used for generating 2d data matrix codes
        const val zxing = "com.google.zxing:core:$zxing_version"
    }

    object Datetime {
        private const val datetime_version = "0.4.1"

        const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:$datetime_version"
    }

    object DependencyInjection {
        private const val kodein_version = "7.20.2"

        val kodeinCompose = kodein("di-framework-compose")
        val kodeinViewModel = kodein("di-framework-android-x-viewmodel")
        val kodeinSavedState = kodein("di-framework-android-x-viewmodel-savedstate")
        private fun kodein(module: String) = "org.kodein.di:kodein-$module:$kodein_version"
    }

    // Should move to open-street-maps
    object GoogleMaps {
        private const val maps_version = "18.1.0"
        private const val maps_ktx_version = "4.0.0"
        private const val location_version = "21.0.1"
        private const val maps_compose_version = "2.15.0" // 3.1.1 needs core_ktx_version=1.12.0

        val maps = gms("maps", maps_version)
        val location = gms("location", location_version)

        val mapsCompose = gmaps("maps-compose", maps_compose_version)
        val mapsKtx = gmaps("maps-ktx", maps_ktx_version)
        val mapsAndroidUtils = gmaps("maps-utils-ktx", maps_ktx_version)
        private fun gms(module: String, version: String) =
            "com.google.android.gms:play-services-$module:$version"

        private fun gmaps(module: String, version: String) =
            "com.google.maps.android:$module:$version"
    }

    object Logging {
        private const val napier_version = "2.6.1"
        private const val slf4j_version = "2.0.9"

        const val napier = "io.github.aakira:napier:$napier_version"

        // for desktop
        const val slf4jNoOp = "org.slf4j:slf4j-nop:$slf4j_version"
    }

    object Lottie {
        private const val lottie_version = "6.1.0"

        const val lottie = "com.airbnb.android:lottie-compose:$lottie_version"
    }

    object Network {
        private const val retrofit_version = "2.9.0"
        private const val retrofit_serialization_version = "1.0.0"
        private const val okio_version = "3.6.0"
        private const val okhttp_version = "5.0.0-alpha.11"

        const val retrofit = "com.squareup.retrofit2:retrofit:$retrofit_version"
        const val retrofit2KotlinXSerialization =
            "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:$retrofit_serialization_version"

        const val okhttp3 = "com.squareup.okhttp3:okhttp:$okhttp_version"
        const val okhttpLogging = "com.squareup.okhttp3:logging-interceptor:$okhttp_version"

        // To work around a vulnerable Okio version 3.1.0 (CVE-2023-3635) we include a newer, non-vulnerable version
        // to be selected by Gradle instead instead of the old one. Can be removed as soon as Retrofit releases a
        // new version >2.9.0.
        const val okio = "com.squareup.okio:okio:$okio_version"

        object Test {
            const val mockWebServer = "com.squareup.okhttp3:mockwebserver:$okhttp_version"
        }
    }

    object PlayServices {
        private const val integrity_version = "1.2.0"
        private const val app_review_version = "2.0.1"

        const val integrity = "com.google.android.play:integrity:$integrity_version"
        const val appReview = "com.google.android.play:review-ktx:$app_review_version"
        const val appUpdate = "com.google.android.play:app-update-ktx:$app_review_version"
    }

    object Serialization {
        private const val kotlinx_serialization_version = "1.6.0"
        private const val fhir_serialization_version = "6.8.3"

        const val kotlinXJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinx_serialization_version"
        const val kotlinXCore = "org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.6.0"

        const val fhir = "ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$fhir_serialization_version"
    }

    object PasswordStrength {
        private const val zxcvbn_version = "1.8.2"

        const val zxcvbn = "com.nulab-inc:zxcvbn:$zxcvbn_version"
    }

    object Tracking {
        private const val content_square_version = "4.21.0"

        const val contentSquare = "com.contentsquare.android:library:$content_square_version"
    }

    object Test {
        private const val mockk_old_version = "1.13.7"
        private const val mockk_version = "1.13.8"
        private const val junit_version = "4.13.2"
        private const val snake_yaml__version = "2.2"
        private const val json_version = "20231013"
        private const val kotlin_test_version = "1.9.20-RC"

        const val junit4 = "junit:junit:$junit_version"

        // need a separate method to maintain a different version for common, android
        const val mockkOld = "io.mockk:mockk:$mockk_old_version"
        const val mockk = "io.mockk:mockk:$mockk_version"
        const val mockkAndroid = "io.mockk:mockk-android:$mockk_version"
        const val snakeyaml = "org.yaml:snakeyaml:$snake_yaml__version"

        const val json = "org.json:json:$json_version"

        const val kotlinTest = "org.jetbrains.kotlin:kotlin-test:$kotlin_test_version"
        const val kotlinTestCommon = "org.jetbrains.kotlin:kotlin-test-common:$kotlin_test_version"

        const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:$kotlin_test_version"
    }
}

val app = Dependencies

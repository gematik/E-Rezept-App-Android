@file:Suppress("UnusedPrivateProperty")

import extensions.BuildNames.targetDesktop
import extensions.BuildNames.versionCatalogLibrary
import extensions.RealmPaparazziFix
import extensions.Versions.BUILD_TOOLS_VERSION
import extensions.Versions.JavaVersion.KOTLIN_OPTIONS_JVM_TARGET
import extensions.Versions.JavaVersion.PROJECT_JAVA_VERSION
import extensions.Versions.SdkVersions.COMPILE_SDK_VERSION
import extensions.Versions.SdkVersions.MIN_SDK_VERSION
import extensions.coroutinesCoreLibrary
import extensions.coroutinesTestBundle
import extensions.cryptoBundle
import extensions.databaseBundle
import extensions.datetimeBundle
import extensions.diKotlinBundle
import extensions.junitBundle
import extensions.kotlinTestBundle
import extensions.multiplatformPagingLibrary
import extensions.napierLibrary
import extensions.networkBundle
import extensions.networkOkhttpMockWebServerLibrary
import extensions.networkRetrofitLibrary
import extensions.serializationBundle

plugins {
    id("com.android.library")
    id("kotlin-multiplatform")
    id("com.codingfeline.buildkonfig")
    kotlin("plugin.serialization")
    id("quality-detekt")
    id("io.realm.kotlin")
    id("org.jetbrains.compose")
}

val versionCatalog: VersionCatalog = extensions.getByType<VersionCatalogsExtension>()
    .named(versionCatalogLibrary)

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = KOTLIN_OPTIONS_JVM_TARGET
            }
        }
    }
    jvm(targetDesktop)
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
                implementation(versionCatalog.multiplatformPagingLibrary)
                implementation(versionCatalog.coroutinesCoreLibrary)
                implementation(versionCatalog.datetimeBundle)
                implementation(versionCatalog.databaseBundle)
                implementation(versionCatalog.cryptoBundle)
                implementation(versionCatalog.serializationBundle)
                implementation(versionCatalog.napierLibrary)
                implementation(versionCatalog.networkBundle)
                implementation(versionCatalog.diKotlinBundle)
                implementation(RealmPaparazziFix.realmKotlinV3)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(versionCatalog.databaseBundle)
                implementation(versionCatalog.coroutinesTestBundle)
                implementation(versionCatalog.serializationBundle)
                implementation(versionCatalog.kotlinTestBundle)
                implementation(versionCatalog.junitBundle)
                implementation(versionCatalog.cryptoBundle)
                implementation(versionCatalog.datetimeBundle)
                implementation(versionCatalog.networkRetrofitLibrary)
                implementation(versionCatalog.networkOkhttpMockWebServerLibrary)
            }
        }
        val desktopTest by getting {
            dependencies {
                dependsOn(commonTest)
                implementation(versionCatalog.cryptoBundle)
                implementation(versionCatalog.datetimeBundle)
            }
        }
    }
}

android {
    buildToolsVersion = BUILD_TOOLS_VERSION
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    compileSdk = COMPILE_SDK_VERSION
    defaultConfig {
        minSdk = MIN_SDK_VERSION
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = PROJECT_JAVA_VERSION
        targetCompatibility = PROJECT_JAVA_VERSION
    }
    buildTypes {
        val debug by getting {
            isJniDebuggable = true
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

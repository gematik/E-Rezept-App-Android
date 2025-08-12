@file:Suppress("UnusedPrivateProperty", "unused")
@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import extensions.BuildNames.targetDesktop
import extensions.BuildNames.versionCatalogLibrary
import extensions.Versions.BUILD_TOOLS_VERSION
import extensions.Versions.JavaVersion.KOTLIN_OPTIONS_JVM_TARGET
import extensions.Versions.JavaVersion.PROJECT_JAVA_VERSION
import extensions.Versions.SdkVersions.COMPILE_SDK_VERSION
import extensions.Versions.SdkVersions.MIN_SDK_VERSION
import extensions.commonMainDependencies
import extensions.commonTestDependencies
import generated.cryptoBundle
import generated.datetimeBundle
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("com.android.library")
    id("kotlin-multiplatform")
    kotlin("plugin.serialization")
    id("quality-detekt")
    id("io.realm.kotlin")
    id("org.jetbrains.compose")
}

val versionCatalog: VersionCatalog = extensions.getByType<VersionCatalogsExtension>()
    .named(versionCatalogLibrary)

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(listOf("-Xopt-in=kotlin.RequiresOptIn"))
    }
    androidTarget {
        java {
            toolchain.languageVersion.set(JavaLanguageVersion.of(KOTLIN_OPTIONS_JVM_TARGET))
        }
    }
    jvm(targetDesktop)
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":erp-model"))
                commonMainDependencies(versionCatalog)
            }
        }
        val commonTest by getting {
            dependencies { commonTestDependencies(versionCatalog) }
        }
        val desktopTest by getting {
            dependsOn(commonTest)
            dependencies {
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

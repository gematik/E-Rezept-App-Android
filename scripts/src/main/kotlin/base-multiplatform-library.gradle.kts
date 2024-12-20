@file:Suppress("UnusedPrivateProperty")

import extensions.BuildNames.minifiedDebug
import extensions.BuildNames.targetDesktop
import extensions.BuildNames.versionCatalogLibrary
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
import extensions.excludeList
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
    id("jacoco")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// JaCoCo configuration for Desktop
tasks.register<JacocoReport>("jacocoTestReportKmm") {
    dependsOn("desktopTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val jvmClasses = fileTree("build/classes/kotlin/desktop/main").apply {
        excludeList()
    }
    val jvmSourceFiles = files("src/desktopMain/kotlin")
    classDirectories.setFrom(jvmClasses)
    sourceDirectories.setFrom(jvmSourceFiles)
    executionData.setFrom(fileTree("build").include("**/desktopTest.exec"))
}

val libs: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named(versionCatalogLibrary)

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
                 implementation(libs.multiplatformPagingLibrary)
                 implementation(libs.coroutinesCoreLibrary)
                 implementation(libs.datetimeBundle)
                 implementation(libs.databaseBundle)
                 implementation(libs.cryptoBundle)
                 implementation(libs.serializationBundle)
                 implementation(libs.napierLibrary)
                 implementation(libs.networkBundle)
                 implementation(libs.diKotlinBundle)
             }
         }
         val commonTest by getting {
             dependencies {
                 implementation(libs.databaseBundle)
                 implementation(libs.coroutinesTestBundle)
                 implementation(libs.serializationBundle)
                 implementation(libs.kotlinTestBundle)
                 implementation(libs.junitBundle)
                 implementation(libs.cryptoBundle)
                 implementation(libs.datetimeBundle)
                 implementation(libs.networkRetrofitLibrary)
                 implementation(libs.networkOkhttpMockWebServerLibrary)
             }
         }
         val desktopTest by getting {
             dependencies {
                 dependsOn(commonTest)
                 implementation(libs.cryptoBundle)
                 implementation(libs.datetimeBundle)
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
        create(minifiedDebug) {
            initWith(debug)
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

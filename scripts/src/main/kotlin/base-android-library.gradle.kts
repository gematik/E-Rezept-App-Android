@file:Suppress("UnstableApiUsage")

import extensions.BuildNames
import extensions.BuildNames.versionCatalogLibrary
import extensions.Versions.BUILD_TOOLS_VERSION
import extensions.Versions.JavaVersion.KOTLIN_OPTIONS_JVM_TARGET
import extensions.Versions.JavaVersion.PROJECT_JAVA_VERSION
import extensions.Versions.SdkVersions.COMPILE_SDK_VERSION
import extensions.Versions.SdkVersions.MIN_SDK_VERSION
import extensions.accompanistBundle
import extensions.androidTestExtension
import extensions.androidxBundle
import extensions.animationBundle
import extensions.cameraBundle
import extensions.checks
import extensions.coroutinesBundle
import extensions.cryptoBundle
import extensions.databaseBundle
import extensions.datamatrixBundle
import extensions.datetimeBundle
import extensions.diBundle
import extensions.excludeList
import extensions.imageBundle
import extensions.junitExtension
import extensions.lifecycleBundle
import extensions.mapsBundle
import extensions.materialLibrary
import extensions.napierLibrary
import extensions.networkBundle
import extensions.othersBundle
import extensions.pdfboxBundle
import extensions.playBundle
import extensions.processPhoenixBundle
import extensions.serializationBundle
import extensions.testExtension
import extensions.trackingBundle

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
    id("io.realm.kotlin")
    id("quality-detekt")
    id("org.jetbrains.compose")
    id("org.owasp.dependencycheck")
    id("com.jaredsburrows.license")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("jacoco")
    id("compose-convention")
}

val versionCatalog: VersionCatalog =
    extensions.getByType<VersionCatalogsExtension>().named(versionCatalogLibrary)

licenseReport {
    generateCsvReport = false
    generateHtmlReport = false
    generateJsonReport = true
    copyJsonReportToAssets = true
}

android {
    buildToolsVersion = BUILD_TOOLS_VERSION
    compileSdk = COMPILE_SDK_VERSION
    defaultConfig {
        minSdk = MIN_SDK_VERSION
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    dependencyCheck {
        checks(project.configurations)
    }
    kotlinOptions {
        jvmTarget = KOTLIN_OPTIONS_JVM_TARGET
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
    compileOptions {
        sourceCompatibility = PROJECT_JAVA_VERSION
        targetCompatibility = PROJECT_JAVA_VERSION
    }
    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests.isIncludeAndroidResources = true
    }
    // for JNA and JNA-platform
    // for byte-buddy
    packaging {
        resources {
            excludes += "META-INF/**"
            // for JNA and JNA-platform
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
            // for byte-buddy
            excludes += "META-INF/licenses/ASM"
            pickFirsts += "win32-x86-64/attach_hotspot_windows.dll"
            pickFirsts += "win32-x86/attach_hotspot_windows.dll"
        }
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
    androidResources {
        noCompress.addAll(listOf("srt", "csv", "json"))
    }
}

jacoco {
    toolVersion = BuildNames.jacocoToolsVersion
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val mainSrc = files("src/main/java", "src/main/kotlin")
    val debugTree = fileTree("${layout.buildDirectory}/intermediates/javac/debug/classes")
    val kotlinDebugTree = fileTree("${layout.buildDirectory}/tmp/kotlin-classes/debug")
    val execFiles = fileTree(layout.buildDirectory).include("**/*.exec")

    sourceDirectories.setFrom(mainSrc)
    classDirectories.setFrom(files(debugTree, kotlinDebugTree).asFileTree.matching {
        excludeList()
    })
    executionData.setFrom(execFiles)
}

dependencies {
    implementation(versionCatalog.pdfboxBundle) {
        exclude(group = "org.bouncycastle")
    }
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(versionCatalog.materialLibrary)
    implementation(versionCatalog.datamatrixBundle)
    implementation(versionCatalog.coroutinesBundle)
    implementation(versionCatalog.datetimeBundle)
    implementation(versionCatalog.accompanistBundle)
    implementation(versionCatalog.othersBundle)
    debugImplementation(versionCatalog.processPhoenixBundle)
    implementation(versionCatalog.androidxBundle)
    implementation(versionCatalog.lifecycleBundle)
    implementation(versionCatalog.cameraBundle)
    compileOnly(versionCatalog.databaseBundle)
    implementation(versionCatalog.diBundle)
    implementation(versionCatalog.imageBundle)
    implementation(versionCatalog.cryptoBundle)
    implementation(versionCatalog.mapsBundle)
    implementation(versionCatalog.networkBundle)
    implementation(versionCatalog.animationBundle)
    implementation(versionCatalog.napierLibrary)
    implementation(versionCatalog.playBundle)
    implementation(versionCatalog.serializationBundle)
    implementation(versionCatalog.trackingBundle)

    androidTestExtension(versionCatalog)
    testExtension(versionCatalog)
    junitExtension(versionCatalog)
}

secrets {
    defaultPropertiesFileName = when {
        project.rootProject.file("ci-overrides.properties").exists() -> "ci-overrides.properties"
        else -> "gradle.properties"
    }
}


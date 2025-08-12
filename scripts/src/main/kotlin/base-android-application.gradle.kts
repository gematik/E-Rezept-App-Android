@file:Suppress("UnusedPrivateProperty", "UnstableApiUsage")

import com.android.build.api.dsl.ManagedVirtualDevice
import extensions.BuildNames.pixel5Api30
import extensions.BuildNames.pixel8Api34
import extensions.BuildNames.versionCatalogLibrary
import extensions.Versions.BUILD_TOOLS_VERSION
import extensions.Versions.JavaVersion.KOTLIN_OPTIONS_JVM_TARGET
import extensions.Versions.JavaVersion.PROJECT_JAVA_VERSION
import extensions.Versions.SdkVersions.COMPILE_SDK_VERSION
import extensions.Versions.SdkVersions.MIN_SDK_VERSION
import extensions.Versions.SdkVersions.TARGET_SDK_VERSION
import extensions.androidTestExtension
import extensions.checks
import extensions.junitExtension
import extensions.testExtension
import generated.androidxAppBundle
import generated.databaseBundle
import generated.datetimeBundle
import generated.desugarJdkLibsLibrary
import generated.diComposeLibrary
import generated.imageBundle
import generated.kotlinStdlibLibrary
import generated.lifecycleBundle
import generated.loggingNapierLibrary
import generated.networkBundle
import generated.pdfboxBundle
import generated.processphoenixBundle
import generated.serializationBundle

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
    id("io.realm.kotlin")
    id("quality-detekt")
    id("org.jetbrains.compose")
    id("org.owasp.dependencycheck")
    id("com.jaredsburrows.license")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("compose-convention")
}

afterEvaluate {
    val taskRegEx = """assemble(Google|Huawei)(PuExternalDebug|PuExternalRelease)""".toRegex()
    tasks.forEach { task ->
        taskRegEx.matchEntire(task.name)?.let {
            val (_, version, flavor) = it.groupValues
            task.dependsOn(tasks.getByName("license${version}${flavor}Report"))
        }
    }
}
val versionCatalog: VersionCatalog = extensions.getByType<VersionCatalogsExtension>()
    .named(versionCatalogLibrary)

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
        targetSdk = TARGET_SDK_VERSION
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // todo: check later if we need this
        // testInstrumentationRunnerArguments += "clearPackageData" to "true"
        // testInstrumentationRunnerArguments += "useTestStorageService" to "true"
    }
    dependencyCheck {
        checks(project.configurations)
    }
    kotlinOptions {
        jvmTarget = KOTLIN_OPTIONS_JVM_TARGET
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
    androidResources {
        generateLocaleConfig = true
        noCompress.addAll(listOf("srt", "csv", "json"))
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = PROJECT_JAVA_VERSION
        targetCompatibility = PROJECT_JAVA_VERSION
    }
    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
        emulatorSnapshots.compressSnapshots = true
        managedDevices.allDevices {
            maybeCreate<ManagedVirtualDevice>(pixel5Api30.name).apply {
                device = pixel5Api30.device
                apiLevel = pixel5Api30.apiLevel
                systemImageSource = pixel5Api30.systemImageSource
            }
            maybeCreate<ManagedVirtualDevice>(pixel8Api34.name).apply {
                device = pixel8Api34.device
                apiLevel = pixel8Api34.apiLevel
                systemImageSource = pixel8Api34.systemImageSource
            }
        }
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

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":erp-model"))
    implementation(versionCatalog.pdfboxBundle) {
        exclude(group = "org.bouncycastle")
    }
    implementation(versionCatalog.kotlinStdlibLibrary)
    implementation(kotlin("reflect"))
    implementation(versionCatalog.datetimeBundle)
    debugImplementation(versionCatalog.processphoenixBundle)
    implementation(versionCatalog.androidxAppBundle)
    implementation(versionCatalog.lifecycleBundle)
    compileOnly(versionCatalog.databaseBundle)
    implementation(versionCatalog.networkBundle)
    implementation(versionCatalog.serializationBundle)
    compileOnly(versionCatalog.diComposeLibrary)
    implementation(versionCatalog.loggingNapierLibrary)
    coreLibraryDesugaring(versionCatalog.desugarJdkLibsLibrary)
    implementation(versionCatalog.imageBundle)

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

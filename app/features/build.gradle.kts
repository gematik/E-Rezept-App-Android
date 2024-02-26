@file:Suppress("UnstableApiUsage")

import de.gematik.ti.erp.Dependencies
import de.gematik.ti.erp.inject
import org.owasp.dependencycheck.reporting.ReportGenerator.Format

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("io.realm.kotlin")
    id("kotlin-parcelize")
    id("org.owasp.dependencycheck")
    id("com.jaredsburrows.license")
    id("de.gematik.ti.erp.dependencies")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("de.gematik.ti.erp.technical-requirements")
}

licenseReport {
    generateCsvReport = false
    generateHtmlReport = false
    generateJsonReport = true
    copyJsonReportToAssets = true
}

android {
    namespace = "${de.gematik.ti.erp.AppDependenciesPlugin.APP_NAME_SPACE}.features"
    defaultConfig {
        testApplicationId = "de.gematik.ti.erp.app.test"
    }
    kotlinOptions {
        jvmTarget = Dependencies.Versions.JavaVersion.KOTLIN_OPTIONS_JVM_TARGET
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
    dependencyCheck {
        analyzers.assemblyEnabled = false
        suppressionFile = "${project.rootDir}" + "/config/dependency-check/suppressions.xml"
        formats = listOf(Format.HTML, Format.XML)
        scanConfigurations = configurations.filter {
            it.name.startsWith("api") ||
                it.name.startsWith("implementation") ||
                it.name.startsWith("kapt")
        }.map { it.name }
    }
    buildTypes {
        val debug by getting {
            isJniDebuggable = true
        }
        create("minifiedDebug") {
            initWith(debug)
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":app:demo-mode"))
    implementation("com.google.android.material:material:1.10.0")
    testImplementation(project(":common"))
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    implementation("com.tom-roush:pdfbox-android:2.0.27.0") {
        exclude(group = "org.bouncycastle")
    }
    // TODO: Make a common inject for all libs that we don't need to copy again and again
    inject {
        dataMatrix {
            implementation(mlkitBarcodeScanner)
            implementation(zxing)
        }
        coroutines {
            implementation(coroutinesCore)
            implementation(coroutinesAndroid)
            implementation(coroutinesPlayServices)
        }
        dateTime {
            implementation(datetime)
            testCompileOnly(datetime)
        }
        accompanist {
            implementation(navigationMaterial)
            implementation(swipeRefresh)
            implementation(flowLayout)
            implementation(pager)
            implementation(pageIndicator)
            implementation(systemUiController)
        }
        android {
            implementation(imageCropper)
            debugImplementation(processPhoenix)
        }
        androidX {
            implementation(legacySupport)
            implementation(appcompat)
            implementation(coreKtx)
            implementation(datastorePreferences)
            implementation(security)
            implementation(biometric)
            implementation(webkit)

            implementation(lifecycleViewmodel)
            implementation(lifecycleComposeRuntime)
            implementation(lifecycleProcess)
            implementation(composeNavigation)
            implementation(composeActivity)
            implementation(composePaging)
            implementation(camerax2)
            implementation(cameraxLifecycle)
            implementation(cameraxView)
        }
        dependencyInjection {
            compileOnly(kodeinCompose)
            implementation(kodeinCompose)
            implementation(kodeinViewModel)
            implementation(kodeinAndroid)
            implementation(kodein)
            androidTestImplementation(kodeinCompose)
        }
        imageLoad {
            implementation(coil)
        }
        logging {
            implementation(napier)
        }
        lottie {
            implementation(lottie)
        }
        serialization {
            implementation(kotlinXJson)
        }
        crypto {
            implementation(jose4j)
            implementation(bouncycastleBcprov)
            implementation(bouncycastleBcpkix)
            testImplementation(bouncycastleBcprov)
            testImplementation(bouncycastleBcpkix)
        }
        network {
            implementation(retrofit)
            implementation(retrofit2KotlinXSerialization)
            implementation(okhttp3)
            implementation(okhttpLogging)
            // Work around vulnerable Okio version 3.1.0 (CVE-2023-3635).
            // Can be removed as soon as Retrofit releases a new version >2.9.0.
            implementation(okio)
            androidTestImplementation(okhttp3)
        }
        database {
            compileOnly(realm)
            testCompileOnly(realm)
        }
        compose {
            implementation(runtime)
            implementation(foundation)
            implementation(animation)
            implementation(uiTooling)
            implementation(preview)
        }
        material {
            implementation(material)
            implementation(material3)
            implementation(materialIcons)
            implementation(materialIconsExtended)
        }
        passwordStrength {
            implementation(zxcvbn)
        }
        stateManagement {
            implementation(reactiveState)
        }
        tracking {
            implementation(contentSquare)
            implementation(contentSquareCompose)
            implementation(contentSquareErrorAnalysis)
        }
        maps {
            implementation(location)
            implementation(maps)
            implementation(mapsAndroidUtils)
            implementation(mapsKtx)
            implementation(mapsCompose)
        }
        playServices {
            implementation(integrity)
            implementation(appReview)
            implementation(appUpdate)
        }
        shimmer {
            implementation(shimmer)
        }
        networkTest {
            testImplementation(mockWebServer)
        }
        test {
            testImplementation(junit4)
            testImplementation(snakeyaml)
            testImplementation(json)
            testImplementation(mockk)
            androidTestImplementation(mockkAndroid)
        }
    }
}

secrets {
    defaultPropertiesFileName = if (project.rootProject.file("ci-overrides.properties").exists()
    ) {
        "ci-overrides.properties"
    } else {
        "gradle.properties"
    }
}

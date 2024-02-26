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
    namespace = "${de.gematik.ti.erp.AppDependenciesPlugin.APP_NAME_SPACE}.demomode"
    defaultConfig {
        testApplicationId = "${de.gematik.ti.erp.AppDependenciesPlugin.APP_NAME_SPACE}.demomode.test"
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
        create("minifiedDebug")
    }
    // disable build config for demo-mode since we use only from features.
    // If needed we need a new-namespace
    buildFeatures {
        buildConfig = false
        resValues = false
    }
}

dependencies {
    implementation(project(":common"))
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    inject {
        coroutines {
            implementation(coroutinesCore)
            implementation(coroutinesAndroid)
        }
        dateTime {
            implementation(datetime)
        }
        androidX {
            implementation(legacySupport)
            implementation(appcompat)
            implementation(coreKtx)
            implementation(datastorePreferences)
            implementation(security)
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
        accompanist {
            implementation(systemUiController)
        }
        dependencyInjection {
            compileOnly(kodeinCompose)
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
            implementation(bouncycastleBcprov)
            implementation(bouncycastleBcpkix)
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
        tracking {
            implementation(contentSquare)
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

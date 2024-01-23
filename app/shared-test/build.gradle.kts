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
    id("de.gematik.ti.erp.gradleplugins.TechnicalRequirementsPlugin")
}

licenseReport {
    generateCsvReport = false
    generateHtmlReport = false
    generateJsonReport = true
    copyJsonReportToAssets = true
}

android {
    namespace = "de.gematik.ti.erp.app.sharedtest"
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
}

dependencies {
    implementation(project(":common"))
    implementation(project(mapOf("path" to ":app:features")))
    testImplementation(project(":common"))
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))

    inject {
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
        logging {
            implementation(napier)
        }
        androidXTest {
            testImplementation(archCore)
            implementation(core)
            implementation(rules)
            implementation(junitExt)
            implementation(runner)
            androidTestUtil(orchestrator)
            androidTestUtil(services)
            implementation(navigation)
            implementation(espresso)
            implementation(espressoIntents)
        }
        coroutinesTest {
            testImplementation(coroutinesTest)
        }
        composeTest {
            implementation(ui)
            debugImplementation(uiManifest)
            implementation(junit4)
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
    ) "ci-overrides.properties" else "gradle.properties"
}

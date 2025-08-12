@file:Suppress("UnstableApiUsage")

import java.util.Properties

gradle.beforeProject {
    // Pass down the TOML path as a property
    rootProject.extensions.extraProperties["libsToml"] = rootDir.resolve("gradle/libs.versions.toml")
}

pluginManagement {
    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://jitpack.io")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.codingfeline.buildkonfig") {
                useModule("com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:${requested.version}")
            }
        }
    }
    includeBuild("scripts")
    includeBuild("plugins/technical-requirements-plugin")
}

dependencyResolutionManagement {

    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    // decide if obtaining dependencies from nexus
    val properties = Properties()
    try {
        properties.load(File("ci-overrides.properties").inputStream())
    } catch (e: Exception) {
        println("Could not load ci-overrides.properties ${e.stackTraceToString()}")
    }

    val nexusUsername: String? = properties.getProperty("NEXUS_USERNAME")
    val nexusPassword: String? = properties.getProperty("NEXUS_PASSWORD")
    val nexusUrl: String? = properties.getProperty("NEXUS_URL")
    val obtainFromNexus = !nexusUrl.isNullOrEmpty() && !nexusUsername.isNullOrEmpty() && !nexusPassword.isNullOrEmpty()

    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        mavenCentral()
        maven("https://jitpack.io")

        if (obtainFromNexus && nexusUrl != null) {
            maven {
                name = "nexus"
                setUrl(nexusUrl)
                credentials {
                    username = nexusUsername
                    password = nexusPassword
                }
            }
        }
    }
}

includeBuild("rules") {
    dependencySubstitution {
        substitute(module("de.gematik.ti.erp.app:rules")).using(project(":"))
    }
}

include(":app:android")
include(":app:android-mock")
include(":app:features")
include(":app:tracker")
include(":app:demo-mode")
include(":app:digas")
include(":app:eu-rezept")
include(":app:navigation")
include(":app:test-tags")
include(":app:test-actions")
include(":common")
include(":core")
include(":database")
include(":erp-model")
include(":fhir-parser")
include(":ui-components")
include(":utils")
include(":ui-components")
include(":plugins:technical-requirements-plugin")

rootProject.name = "E-Rezept"

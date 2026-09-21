@file:Suppress("UnstableApiUsage")

import java.util.Properties

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

gradle.beforeProject {
    // Pass down the TOML path as a property
    rootProject.extensions.extraProperties["libsToml"] =
        rootDir.resolve("gradle/libs.versions.toml")
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

// Auto-downloads missing JDK for project, does not work on CI
/* plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
} */

dependencyResolutionManagement {

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    // Resolve Nexus repo credentials as CI-only inputs.
    val ciOverrides = Properties().apply {
        val ciOverridesFile = File("ci/local/ci-overrides.properties")
        if (ciOverridesFile.exists()) {
            ciOverridesFile.inputStream().use { load(it) }
        }
    }

    fun resolveCiOnlyProperty(key: String): String? {
        return System.getenv(key)
            ?: (gradle.startParameter.projectProperties[key])
            ?: ciOverrides.getProperty(key)
    }

    val nexusUsername: String? = resolveCiOnlyProperty("NEXUS_USERNAME")
    val nexusPassword: String? = resolveCiOnlyProperty("NEXUS_PASSWORD")
    val nexusUrl: String? = resolveCiOnlyProperty("NEXUS_URL")
    val obtainFromNexus =
        !nexusUrl.isNullOrEmpty() && !nexusUsername.isNullOrEmpty() && !nexusPassword.isNullOrEmpty()

    repositories {
        if (obtainFromNexus && nexusUrl != null) {
            maven {
                name = "nexus"
                setUrl(nexusUrl)

                credentials {
                    username = nexusUsername
                    password = nexusPassword
                }
            }
        } else {
            println("Skipping nexus repository")
        }
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        mavenCentral()
        maven("https://jitpack.io")
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
include(":app:messages")
include(":app:tracker")
include(":app:demo-mode")
include(":app:digas")
include(":app:eu-rezept")
include(":app:push-notifications")
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
include(":mocks")
include(":plugins:technical-requirements-plugin")
include(":app:consent")

rootProject.name = "E-Rezept"

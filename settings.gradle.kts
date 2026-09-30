@file:Suppress("UnstableApiUsage")

import java.util.Properties

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

gradle.beforeProject {
    // Pass down the TOML path as a property
    rootProject.extensions.extraProperties["libsToml"] =
        rootDir.resolve("gradle/libs.versions.toml")
}

// Resolve Nexus credentials once at the top level so both pluginManagement
// and dependencyResolutionManagement can share them without duplication.
val ciOverridesProps = Properties().apply {
    val f = File("ci/local/ci-overrides.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

fun resolveProperty(key: String): String? =
    System.getenv(key)
        ?: gradle.startParameter.projectProperties[key]
        ?: ciOverridesProps.getProperty(key)

val nexusUrl: String? = resolveProperty("NEXUS_URL")
val nexusUsername: String? = resolveProperty("NEXUS_USERNAME")
val nexusPassword: String? = resolveProperty("NEXUS_PASSWORD")
val hasNexus = !nexusUrl.isNullOrEmpty() && !nexusUsername.isNullOrEmpty() && !nexusPassword.isNullOrEmpty()

pluginManagement {
    repositories {
        // pluginManagement is evaluated in stage-1 settings compilation,
        // so compute Nexus credentials locally in this block.
        val pluginCiOverrides = java.util.Properties().apply {
            val f = java.io.File("ci/local/ci-overrides.properties")
            if (f.exists()) f.inputStream().use { this.load(it) }
        }
        fun pluginProperty(key: String): String? =
            System.getenv(key)
                ?: gradle.startParameter.projectProperties[key]
                ?: pluginCiOverrides.getProperty(key)

        val pluginNexusUrl = pluginProperty("NEXUS_URL")
        val pluginNexusUsername = pluginProperty("NEXUS_USERNAME")
        val pluginNexusPassword = pluginProperty("NEXUS_PASSWORD")
        val pluginHasNexus =
            !pluginNexusUrl.isNullOrEmpty() && !pluginNexusUsername.isNullOrEmpty() && !pluginNexusPassword.isNullOrEmpty()

        if (pluginHasNexus) {
            // In CI: all plugin resolution goes through Nexus (no direct external calls).
            // The Nexus allRepos group must proxy: gradlePluginPortal, mavenCentral, google,
            // maven.pkg.jetbrains.space, oss.sonatype.org/snapshots, and jitpack.io.
            maven {
                name = "nexus-plugins"
                setUrl(pluginNexusUrl!!)
                credentials {
                    username = pluginNexusUsername
                    password = pluginNexusPassword
                }
            }
        } else {
            // Local dev fallback — direct external repos when Nexus credentials are not set.
            maven("https://oss.sonatype.org/content/repositories/snapshots/")
            maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
            google()
            gradlePluginPortal()
            mavenCentral()
            maven("https://jitpack.io")
        }
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

    repositories {
        if (hasNexus) {
            maven {
                name = "nexus"
                setUrl(nexusUrl!!)
                credentials {
                    username = nexusUsername
                    password = nexusPassword
                }
            }
        } else {
            // Local dev fallback — direct external repos when Nexus credentials are not set.
            println("Skipping nexus repository")
            maven("https://oss.sonatype.org/content/repositories/snapshots/")
            maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
            google()
            mavenCentral()
            maven("https://jitpack.io")
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

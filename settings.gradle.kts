@file:Suppress("UnstableApiUsage")

import java.util.Properties


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
    // needed only for desktop app
    // includeBuild("plugins/dependencies")
    // includeBuild("plugins/resource-generation")
}

dependencyResolutionManagement {

    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    // decide if obtaining dependencies from nexus
    val properties = Properties()
    try {
        properties.load(File("ci-overrides.properties").inputStream())
    } catch (e: Exception) {
        println("Could not load ci-overrides.properties")
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

        if (obtainFromNexus) {
            maven {
                name = "nexus"
                setUrl(nexusUrl!!)
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

// needed only for desktop app
//includeBuild("smartcard-wrapper") {
//    dependencySubstitution {
//        substitute(module("de.gematik.ti.erp.app:smartcard-wrapper")).using(project(":"))
//    }
//}

// includeBuild("modules/fhir-parser") {
//    dependencySubstitution {
//        substitute(module("de.gematik.ti.erp.app:fhir-parser")).using(project(":"))
//    }
// }

include(":app:android")
include(":app:android-mock")
include(":app:features")
include(":app:tracker")
include(":app:demo-mode")
include(":app:digas")
include(":app:navigation")
include(":ui-components")
include(":app-core")
include(":utils")
include(":fhir-parser")
include(":common")
include(":ui-components")
include(":app:test-tags")
include(":app:test-actions")
include(":plugins:technical-requirements-plugin")
// needed only for desktop app
// include(":desktop")

rootProject.name = "E-Rezept"

pluginManagement {
    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.codingfeline.buildkonfig") {
                useModule("com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:${requested.version}")
            }
        }
    }

    includeBuild("plugins/dependencies")
    includeBuild("plugins/resource-generation")
    includeBuild("plugins/technical-requirements-plugin")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
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

includeBuild("smartcard-wrapper") {
    dependencySubstitution {
        substitute(module("de.gematik.ti.erp.app:smartcard-wrapper")).using(project(":"))
    }
}

// includeBuild("modules/fhir-parser") {
//    dependencySubstitution {
//        substitute(module("de.gematik.ti.erp.app:fhir-parser")).using(project(":"))
//    }
// }

include(":app:android")
include(":app:android-mock")
include(":app:features")
include(":app:shared-test")
include(":app:demo-mode")
include(":common")
include(":desktop")
include(":plugins:technical-requirements-plugin")

rootProject.name = "E-Rezept"

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
            if (requested.id.id == "dagger.hilt.android") {
                useModule("com.google.dagger:hilt-android-gradle-plugin:${requested.version}")
            }
            if (requested.id.id == "com.codingfeline.buildkonfig") {
                useModule("com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:${requested.version}")
            }
        }
    }
    includeBuild("plugins/dependencies")
    includeBuild("plugins/resource-generation")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        mavenCentral()
        maven ("https://jitpack.io")
        jcenter()
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

//includeBuild("modules/fhir-parser") {
//    dependencySubstitution {
//        substitute(module("de.gematik.ti.erp.app:fhir-parser")).using(project(":"))
//    }
//}

include(":android", ":desktop", ":common")

rootProject.name = "E-Rezept"

/*
 * ${GEMATIK_COPYRIGHT_STATEMENT}
 */

plugins {
    `kotlin-dsl`
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}

fun implementationClass(value: String) = "de.gematik.ti.erp.app.plugins.$value"
fun name(value: String) = "de.gematik.ti.erp.$value"

gradlePlugin {
    plugins.create("versionApp") {
        id = name("versioning")
        group = name("versioning")
        implementationClass = implementationClass("buildapp.VersionAppPlugin")
    }
    plugins.create("updateGradleProperties") {
        id = name("properties")
        group = name("properties")
        implementationClass = implementationClass("updateproperties.UpdatePropertiesPlugin")
    }
    plugins.create("buildAppFlavours") {
        id = name("flavours")
        group = name("flavours")
        implementationClass = implementationClass("buildapp.BuildAppFlavoursPlugin")
    }
    plugins.create("teamsCommunication") {
        id = name("teams")
        group = name("teams")
        implementationClass = implementationClass("teams.TeamsCommunicationPlugin")
    }
    plugins.create("dependencies") {
        id = name("dependency-overrides")
        group = name("dependency-overrides")
        implementationClass = implementationClass("dependencies.DependenciesPlugin")
    }
    plugins.create("names") {
        id = name("names")
        group = name("names")
        implementationClass = implementationClass("names.AppDependencyNamesPlugin")
    }
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(libs.kotlinx.datetime)
    implementation(libs.network.httpclient5)
    implementation(libs.network.httpclient5.fluent)
    implementation(libs.opencsv)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.mockk)
    implementation(libs.dependency.check.gradle)
    implementation(libs.kotlinx.serialization.json)
}

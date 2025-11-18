/*
 * Copyright 2025, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

plugins {
    `kotlin-dsl`
    // NOTE: The Kotlin plugin versions below are hardcoded because version catalogs are not available in buildSrc's plugins block.
    // If you update the Kotlin version in libs.versions.toml, you MUST also update it here manually!
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

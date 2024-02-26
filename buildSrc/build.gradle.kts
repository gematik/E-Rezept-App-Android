/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

plugins {
    `kotlin-dsl`
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
}

gradlePlugin {
    plugins.create("versionApp") {
        id = "de.gematik.ti.erp.versioning"
        implementationClass = "de.gematik.ti.erp.app.plugins.buildapp.VersionAppPlugin"
    }
    plugins.create("updateGradleProperties") {
        id = "de.gematik.ti.erp.properties"
        implementationClass = "de.gematik.ti.erp.app.plugins.buildapp.UpdatePropertiesPlugin"
    }
    plugins.create("buildAppFlavours") {
        id = "de.gematik.ti.erp.flavours"
        implementationClass = "de.gematik.ti.erp.app.plugins.buildapp.BuildAppFlavoursPlugin"
    }
    plugins.create("teamsCommunication") {
        id = "de.gematik.ti.erp.teams"
        implementationClass = "de.gematik.ti.erp.app.plugins.teams.TeamsCommunicationPlugin"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.3")
    implementation("org.apache.httpcomponents.client5:httpclient5-fluent:5.3")
    implementation("com.opencsv:opencsv:5.5.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
}

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

package extensions

import org.gradle.api.artifacts.ConfigurationContainer
import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension
import org.owasp.dependencycheck.reporting.ReportGenerator

fun DependencyCheckExtension.checks(configurations: ConfigurationContainer) {
    analyzers.assemblyEnabled = false
    suppressionFile = "${project.rootDir}" + "/config/dependency-check/suppressions.xml"
    formats =
        listOf(
            ReportGenerator.Format.HTML,
            ReportGenerator.Format.XML
        )
    scanConfigurations = configurations.filter {
        it.name.startsWith("api") ||
            it.name.startsWith("implementation") ||
            it.name.startsWith("kapt")
    }.map { it.name }
    failBuildOnCVSS = Versions.CVSS.allowed
}

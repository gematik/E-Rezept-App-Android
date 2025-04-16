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

package de.gematik.ti.erp.app.plugins.dependencies

import de.gematik.ti.erp.app.ErpPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getPlugin
import java.util.Properties
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

class DependenciesPlugin : ErpPlugin {
    val overrideProperties = Properties()
    val gradleProperties = Properties()
    override fun apply(project: Project) {
        val ciPropertiesFile = project.rootProject.file("ci-overrides.properties")
        if (ciPropertiesFile.exists()) {
            overrideProperties.load(ciPropertiesFile.inputStream())
        }

        val gradlePropertiesFile = project.rootProject.file("gradle.properties")
        if (gradlePropertiesFile.exists()) {
            gradleProperties.load(gradlePropertiesFile.inputStream())
        }
    }
}

fun Project.overrides(): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, String>> {
    return PropertyDelegateProvider { _: Any?, _ ->
        ReadOnlyProperty<Any?, String> { _, property ->
            project.plugins.getPlugin(DependenciesPlugin::class)
                .overrideProperties
                .getProperty(property.name)
                ?: (project.properties[property.name] as? String)
                ?: run {
                    project.plugins.getPlugin(DependenciesPlugin::class)
                        .gradleProperties
                        .getProperty(property.name)
                        ?: (project.properties[property.name] as? String)
                        ?: ""
                }
        }
    }
}

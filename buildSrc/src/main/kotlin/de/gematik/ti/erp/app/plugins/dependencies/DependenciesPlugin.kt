/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.plugins.dependencies

import de.gematik.ti.erp.app.ErpPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getPlugin
import java.util.Properties
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

private val CI_ONLY_KEYS = setOf(
    "NEXUS_URL",
    "NEXUS_USERNAME",
    "NEXUS_PASSWORD",
    "GITLAB_PRIVATE_TOKEN",
    "GITLAB_PROJECT_API_URL",
    "TEAMS_RELEASE_WEBHOOK_URL",
    "TEAMS_NIGHTLY_WEBHOOK_URL",
    "TEAMS_MR_WEBHOOK_URL",
    "CHANGELOGS_PROJECT_ID",
    "LOKALISE_PROJECT_ID"
)

class DependenciesPlugin : ErpPlugin {
    val overrideProperties = Properties()
    val apiKeysProperties = Properties()
    val gradleProperties = Properties()
    override fun apply(project: Project) {
        // Load app-runtime keys from properties files that are safe to expose via BuildKonfig.
        // - secrets.properties: runtime values already used by the app
        // - apikeys.properties: ERP API keys for BuildConfig/BuildKonfig
        val secretsFile = project.rootProject.file("ci/local/secrets.properties")
        if (secretsFile.exists()) {
            overrideProperties.load(secretsFile.inputStream())
        }

        val apiKeysFile = project.rootProject.file("ci/local/apikeys.properties")
        if (apiKeysFile.exists()) {
            apiKeysProperties.load(apiKeysFile.inputStream())
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
            if (property.name in CI_ONLY_KEYS) {
                // Hard fail-safe: CI-only keys are never exposed through overrides/build config wiring.
                return@ReadOnlyProperty ""
            }
            val dependencyPlugin = project.plugins.getPlugin(DependenciesPlugin::class)
            dependencyPlugin.overrideProperties.getProperty(property.name)
                ?: dependencyPlugin.apiKeysProperties.getProperty(property.name)
                ?: dependencyPlugin.gradleProperties.getProperty(property.name)
                ?: (project.properties[property.name] as? String)
                ?: ""
        }
    }
}

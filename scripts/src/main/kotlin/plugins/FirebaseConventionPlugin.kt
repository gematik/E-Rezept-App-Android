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

@file:Suppress("unused")

package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class FirebaseConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val buildKonfigFlavor = project.findProperty(BUILDKONFIG_FLAVOR_PROPERTY) as? String
        if (buildKonfigFlavor?.startsWith(HUAWEI_FLAVOR_PREFIX) == true) {
            project.logger.lifecycle(
                "Skipping $GOOGLE_SERVICES_PLUGIN_ID for Huawei flavor '$buildKonfigFlavor'."
            )
            return
        }

        val googleServicesFile = project.file(GOOGLE_SERVICES_FILE)

        if (googleServicesFile.isFile) {
            project.pluginManager.apply(GOOGLE_SERVICES_PLUGIN_ID)
        } else {
            project.logger.warn(
                "$GOOGLE_SERVICES_FILE not found. Add a valid Firebase config at " +
                    "${project.rootProject.relativePath(googleServicesFile)} to enable FCM."
            )
        }
    }

    private companion object {
        const val BUILDKONFIG_FLAVOR_PROPERTY = "buildkonfig.flavor"
        const val GOOGLE_SERVICES_FILE = "google-services.json"
        const val GOOGLE_SERVICES_PLUGIN_ID = "com.google.gms.google-services"
        const val HUAWEI_FLAVOR_PREFIX = "huawei"
    }
}

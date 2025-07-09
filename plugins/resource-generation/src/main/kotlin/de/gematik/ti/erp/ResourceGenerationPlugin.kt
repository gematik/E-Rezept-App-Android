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

package de.gematik.ti.erp

import de.gematik.ti.erp.networkSecurityConfigGen.AndroidNetworkConfigGeneratorTask
import de.gematik.ti.erp.stringResGen.AndroidStringResourceGeneratorTask
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class ResourceGenerationPlugin : Plugin<Project> {
    private val groupName = "Desktop Code Generation"

    override fun apply(project: Project) {
        project.tasks.create("stringResGen", AndroidStringResourceGeneratorTask::class.java).apply {
            description = "Takes android string resource files as input and maps it to native kotlin."
            group = groupName
        }
        project.tasks.create("netConfGen", AndroidNetworkConfigGeneratorTask::class.java).apply {
            description = "Takes an android network configuration and generates an okhttp friendly cert pinning config."
            group = groupName
        }
    }
}

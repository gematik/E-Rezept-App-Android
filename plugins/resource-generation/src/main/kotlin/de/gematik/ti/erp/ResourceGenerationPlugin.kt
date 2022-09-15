/*
 * Copyright (c) 2022 gematik GmbH
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

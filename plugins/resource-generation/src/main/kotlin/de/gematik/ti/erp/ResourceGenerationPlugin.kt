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

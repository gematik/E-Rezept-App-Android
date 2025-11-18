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

package de.gematik.ti.erp.app.plugins.teams

import de.gematik.ti.erp.app.ErpPlugin
import de.gematik.ti.erp.app.plugins.teams.JenkinsInformation.AppName
import de.gematik.ti.erp.app.plugins.teams.JenkinsInformation.Branch
import de.gematik.ti.erp.app.plugins.teams.JenkinsInformation.BuildNr
import de.gematik.ti.erp.app.plugins.teams.JenkinsInformation.Code
import de.gematik.ti.erp.app.plugins.teams.JenkinsInformation.Job
import de.gematik.ti.erp.app.plugins.teams.JenkinsInformation.Name
import de.gematik.ti.erp.app.plugins.teams.JenkinsInformation.Owner
import de.gematik.ti.erp.app.plugins.teams.JenkinsInformation.Status
import de.gematik.ti.erp.app.plugins.teams.JenkinsInformation.WebHookUrl
import de.gematik.ti.erp.app.plugins.teams.TeamsProjectInformation.Companion.toPayLoad
import de.gematik.ti.erp.app.utils.LAST_MESSAGE_STRING
import de.gematik.ti.erp.app.utils.PAYLOAD
import de.gematik.ti.erp.app.utils.TaskNames
import de.gematik.ti.erp.app.utils.detectPropertyOrNull
import de.gematik.ti.erp.app.utils.detectPropertyOrThrow
import de.gematik.ti.erp.app.utils.execute
import de.gematik.ti.erp.app.utils.lastCommit
import de.gematik.ti.erp.app.utils.versionCode
import de.gematik.ti.erp.app.utils.versionName
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.extra
import java.io.ByteArrayOutputStream

class TeamsCommunicationPlugin : ErpPlugin {
    override fun apply(project: Project) {
        /*
         * Example usage of this plugin:
         * ./gradlew sendTeamsNotification
         *      -Purl=https://teams.url.com/apitoken
         *      -Pname="version name"
         *      -Pcode="version code"
         *      -Pbranch="branch name"
         *      -Pstatus="branch status"
         *      -PbuildNr=100
         *      -Pjob="Job name"
         *      -PappOwner="App center owner"
         *      -PappName="App center app name"
         */
        project.tasks.register(TaskNames.sendTeamsNotification) {
            runDependencyTasks()
            doLast {
                try {
                    val webhookUrl = project.detectPropertyOrThrow(WebHookUrl())

                    val payload = TeamsProjectInformation(
                        versionName = project.versionName() ?: project.detectPropertyOrThrow(Name()),
                        versionCode = project.versionCode()?.toString() ?: project.detectPropertyOrThrow(Code()),
                        branchName = project.detectPropertyOrThrow(Branch()),
                        buildStatus = project.detectPropertyOrThrow(Status()),
                        buildNumber = project.detectPropertyOrThrow(BuildNr()),
                        buildJob = project.detectPropertyOrNull(Job()),
                        appCenterAppName = project.detectPropertyOrThrow(AppName()),
                        appCenterOwner = project.detectPropertyOrThrow(Owner())
                    ).toPayLoad(
                        summary = "Hey there! You have a new build for ${project.versionName()}, check it out if this is required for testing",
                        lastCommitMessage = project.lastCommit() ?: "Cannot get last commit message"
                    )

                    println("Executing teams channel call")

                    project.exec {
                        commandLine("curl", "-H", "Content-Type: application/json", "-d", payload, webhookUrl)
                        standardOutput = System.out
                        println("Executed command: ${commandLine.execute()}")
                        project.extensions.extraProperties[PAYLOAD] = payload
                    }
                } catch (e: Throwable) {
                    println(e.stackTraceToString())
                    throw GradleException(e.localizedMessage)
                }
            }
        }

        project.tasks.register(TaskNames.lastMessage) {
            val output = ByteArrayOutputStream().use {
                project.exec {
                    commandLine = "git log -1 --pretty=%B".execute()
                    standardOutput = it
                }
                it.toString()
            }
            println("Last commit message = $output")
            project.extra[LAST_MESSAGE_STRING] = output
        }
    }
}

private fun Task.runDependencyTasks() {
    dependsOn(TaskNames.versionApp)
    dependsOn(TaskNames.lastMessage)
}

enum class JenkinsInformation(
    private val stringValue: String
) {
    WebHookUrl("url"),
    Name("name"),
    Code("code"),
    Branch("branch"),
    Status("status"),
    BuildNr("buildNr"),
    Job("job"),
    Owner("appOwner"),
    AppName("appName")
    ;

    operator fun invoke() = stringValue
}

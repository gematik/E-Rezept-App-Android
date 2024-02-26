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

package de.gematik.ti.erp.app.plugins.buildapp

import de.gematik.ti.erp.app.ErpPlugin
import de.gematik.ti.erp.app.utils.SHELL_RES_FOLDER
import de.gematik.ti.erp.app.utils.TaskNames
import de.gematik.ti.erp.app.utils.VERSION_CODE_STRING
import de.gematik.ti.erp.app.utils.VERSION_ERROR
import de.gematik.ti.erp.app.utils.VERSION_NAME_EXCEPTION
import de.gematik.ti.erp.app.utils.VERSION_NAME_STRING
import de.gematik.ti.erp.app.utils.VERSION_PATTERN_EXCEPTION
import de.gematik.ti.erp.app.utils.OS_NAME
import de.gematik.ti.erp.app.utils.WINDOWS
import de.gematik.ti.erp.app.utils.doesNotHaveVersionCodeName
import de.gematik.ti.erp.app.utils.execute
import de.gematik.ti.erp.app.utils.isInvalidVersioningPattern
import de.gematik.ti.erp.app.utils.isVersionNameEmpty
import de.gematik.ti.erp.app.utils.splitForVersionParts
import de.gematik.ti.erp.app.utils.versionCode
import de.gematik.ti.erp.app.utils.versionName
import org.gradle.api.GradleScriptException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.extra
import java.io.ByteArrayOutputStream


@Suppress("unused")
class VersionAppPlugin : ErpPlugin {

    override fun apply(project: Project) {
        project.tasks.create(TaskNames.versionApp) {
            val executableFile = "get_release_versions.sh"
            val isWindows = System.getProperty(OS_NAME).lowercase().contains(WINDOWS)

            val outputText by lazy {
                if (isWindows) {
                    "version_code:1:version_name:1.1.1-RC1-windows"
                } else {
                    ByteArrayOutputStream().use { outputStream ->
                        project.exec {
                            commandLine = "bash ${project.projectDir}/$SHELL_RES_FOLDER/$executableFile".execute()
                            standardOutput = outputStream
                        }
                        outputStream.toString()
                    }
                }
            }
            doLast {
                // sanity checks for version pattern
                val parts = outputText.splitForVersionParts()
                if (parts.doesNotHaveVersionCodeName()) {
                    throw GradleScriptException(
                        VERSION_ERROR, Exception(
                            "$VERSION_PATTERN_EXCEPTION $outputText"
                        )
                    )
                }
                if (parts.versionName().isInvalidVersioningPattern()) {
                    throw GradleScriptException(
                        VERSION_ERROR, Exception(
                            "$VERSION_PATTERN_EXCEPTION ${parts.versionName()}"
                        )
                    )
                }
                if (parts.isVersionNameEmpty()) {
                    throw GradleScriptException(VERSION_ERROR, Exception(VERSION_NAME_EXCEPTION))
                }

                project.extra[VERSION_CODE_STRING] = parts.versionCode()
                project.extra[VERSION_NAME_STRING] = parts.versionName()
                println("Versioning from given branch and tag done. $outputText")
            }
        }

        project.tasks.register(TaskNames.printVersionCode) {
            runDependencyTasks()
            doLast {
                println(project.versionCode())
            }
        }

        project.tasks.register(TaskNames.printVersionName) {
            runDependencyTasks()
            doLast {
                println(project.versionName())
            }
        }
    }

    private fun Task.runDependencyTasks() {
        dependsOn(TaskNames.versionApp)
    }
}

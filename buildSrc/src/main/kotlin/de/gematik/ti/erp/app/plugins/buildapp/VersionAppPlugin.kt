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

package de.gematik.ti.erp.app.plugins.buildapp

import de.gematik.ti.erp.app.ErpPlugin
import de.gematik.ti.erp.app.utils.GRADLE_PROPERTIES_FILE
import de.gematik.ti.erp.app.utils.OS_NAME
import de.gematik.ti.erp.app.utils.SHELL_RES_FOLDER
import de.gematik.ti.erp.app.utils.TaskNames
import de.gematik.ti.erp.app.utils.VERSION_CODE_STRING
import de.gematik.ti.erp.app.utils.VERSION_ERROR
import de.gematik.ti.erp.app.utils.VERSION_NAME_EXCEPTION
import de.gematik.ti.erp.app.utils.VERSION_NAME_STRING
import de.gematik.ti.erp.app.utils.VERSION_PATTERN_EXCEPTION
import de.gematik.ti.erp.app.utils.WINDOWS
import de.gematik.ti.erp.app.utils.doesNotHaveVersionCodeName
import de.gematik.ti.erp.app.utils.execute
import de.gematik.ti.erp.app.utils.isInvalidVersioningPattern
import de.gematik.ti.erp.app.utils.isVersionNameEmpty
import de.gematik.ti.erp.app.utils.letNotNull
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
                // If existing gradle.properties file contains a value higher than the one from git, it will be used
                val parts = outputText.splitForVersionParts()
                val existingVersionName = project.readVersionNameFromProperties()
                val existingVersionCode = project.readVersionCodeFromProperties()

                if (parts.isVersionNameEmpty()) {
                    throw GradleScriptException(VERSION_ERROR, Exception(VERSION_NAME_EXCEPTION))
                }

                val gitVersionName = parts.versionName()

                val semVerComparisonResult = compareSemver(
                    gradleVersion = existingVersionName ?: "0.0.0",
                    gitVersion = gitVersionName
                )

                when (semVerComparisonResult) {
                    SemVer.GRADLE, SemVer.SAME -> {
                        letNotNull(existingVersionName, (existingVersionCode)) { versionName, versionCode ->
                            println("Updating version name from gradle.properties as $versionName")

                            // even on gradle update use the greater version code
                            val gradleVersionCode = versionCode.toInt()
                            val gitVersionCode = parts.versionCode()
                            val newVersionCode = maxOf(gradleVersionCode, gitVersionCode)

                            println("Updating version code as $newVersionCode")

                            project.extra[VERSION_CODE_STRING] = newVersionCode
                            project.extra[VERSION_NAME_STRING] = versionName
                        } ?: throw GradleScriptException(
                            VERSION_ERROR,
                            Exception(
                                "Version code or name not found in gradle.properties"
                            )
                        )
                    }

                    SemVer.GIT -> {
                        if (parts.doesNotHaveVersionCodeName()) {
                            throw GradleScriptException(
                                VERSION_ERROR,
                                Exception(
                                    "$VERSION_PATTERN_EXCEPTION $outputText"
                                )
                            )
                        }
                        if (parts.versionName().isInvalidVersioningPattern()) {
                            throw GradleScriptException(
                                VERSION_ERROR,
                                Exception(
                                    "$VERSION_PATTERN_EXCEPTION ${parts.versionName()}"
                                )
                            )
                        }
                        print("Updating values from git $outputText")
                        project.extra[VERSION_CODE_STRING] = parts.versionCode()
                        project.extra[VERSION_NAME_STRING] = parts.versionName()
                    }
                }
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

    private fun Project.gradlePropertiesFile() = file(GRADLE_PROPERTIES_FILE)

    private fun Project.readVersionNameFromProperties() =
        gradlePropertiesFile().useLines {
            it.firstOrNull { value -> value.startsWith(VERSION_NAME_LABEL) }
        }?.split("=")?.get(1)?.trim()

    private fun Project.readVersionCodeFromProperties() =
        gradlePropertiesFile().useLines {
            it.firstOrNull { value -> value.startsWith(VERSION_CODE_LABEL) }
        }?.split("=")?.get(1)?.trim()

    private fun compareSemver(gradleVersion: String, gitVersion: String): SemVer {
        val regex = Regex("^(\\d+)\\.(\\d+)\\.(\\d+)(?:-([0-9A-Za-z.-]+))?$")
        val gradleVersionMatch = regex.matchEntire(gradleVersion)
        val gitVersionMatch = regex.matchEntire(gitVersion)

        if (gradleVersionMatch != null && gitVersionMatch != null) {
            val (majorGradle, minorGradle, patchGradle, preReleaseGradle) = gradleVersionMatch.destructured
            val (majorGit, minorGit, patchGit, preReleaseGit) = gitVersionMatch.destructured

            if (majorGradle.toInt() != majorGit.toInt()) return if (majorGradle.toInt() > majorGit.toInt()) SemVer.GRADLE else SemVer.GIT
            if (minorGradle.toInt() != minorGit.toInt()) return if (minorGradle.toInt() > minorGit.toInt()) SemVer.GRADLE else SemVer.GIT
            if (patchGradle.toInt() != patchGit.toInt()) return if (patchGradle.toInt() > patchGit.toInt()) SemVer.GRADLE else SemVer.GIT

            // Compare pre-release versions if they exist
            if (preReleaseGradle.isNotEmpty() || preReleaseGit.isNotEmpty()) {
                val prePartsGradle = preReleaseGradle.split(".")
                val prePartsGit = preReleaseGit.split(".")
                val preComparison = comparePreRelease(prePartsGradle, prePartsGit)
                return when {
                    preComparison > 0 -> SemVer.GRADLE
                    preComparison < 0 -> SemVer.GIT
                    else -> SemVer.SAME
                }
            }

            return SemVer.SAME
        } else {
            println("Invalid version on git $gitVersion")
            return SemVer.GRADLE
        }
    }

    @Suppress("MagicNumber")
    private fun comparePreRelease(preGradle: List<String>, preGit: List<String>): Int {
        val maxLength = maxOf(preGradle.size, preGit.size)
        for (i in 0 until maxLength) {
            val valGradle = preGradle.getOrElse(i) { "" }
            val valGit = preGit.getOrElse(i) { "" }
            val numGradle = valGradle.toIntOrNull()
            val numGit = valGit.toIntOrNull()

            when {
                numGradle != null && numGit != null -> { // Both are numbers
                    if (numGradle != numGit) return numGradle.compareTo(numGit)
                }

                numGradle != null -> return -1 // Numbers have lower precedence
                numGit != null -> return 1
                else -> { // Both are strings
                    val comp = valGradle.compareTo(valGit)
                    if (comp != 0) return comp
                }
            }
        }

        return preGradle.size.compareTo(preGit.size) // Fewer elements means lower precedence
    }

    private fun maxOf(a: Int, b: Int): Int {
        return if (a > b) a else b
    }

    enum class SemVer {
        GRADLE, GIT, SAME
    }

    companion object {
        private const val VERSION_NAME_LABEL = "VERSION_NAME="
        private const val VERSION_CODE_LABEL = "VERSION_CODE="
    }
}

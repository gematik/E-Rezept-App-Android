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

package de.gematik.ti.erp.app.tasks

import de.gematik.ti.erp.app.plugins.buildapp.BuildAppFlavoursPlugin
import de.gematik.ti.erp.app.utils.BUILD_EXCEPTION
import de.gematik.ti.erp.app.utils.TaskNames
import de.gematik.ti.erp.app.utils.extractRCVersion
import de.gematik.ti.erp.app.utils.extractVersion
import de.gematik.ti.erp.app.utils.letNotNull
import de.gematik.ti.erp.app.utils.versionCode
import de.gematik.ti.erp.app.utils.versionName
import org.gradle.api.GradleScriptException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer

private const val GRADLE_APP_IDENTIFIER = "./gradlew :app:android:"
private const val GRADLE_MOCK_APP_IDENTIFIER = "./gradlew :app:android-mock:"
private const val GIT_HASH = "git-hash"
private const val FORCED_VERSION_CODE = "v-code"
private const val FORCED_VERSION_NAME = "v-name"

/**
 * Registers a task to build the Play Store bundle.
 * This task calculates the version code and name, then executes the build script.
 */
internal fun TaskContainer.buildPlayStoreBundle() {
    register(TaskNames.buildPlayStoreBundle) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.PlayStoreBundle
        doLast {
            val (versionCode, versionName) = project.calculateVersionCodeName(isRC = false)
            project.exec {
                commandLine(
                    "bash",
                    "-c",
                    buildScript(
                        versionCode = versionCode,
                        versionName = versionName,
                        buildCondition = buildCondition
                    )
                )
                standardOutput = System.out
                errorOutput = System.out
            }
        }
    }
}

/**
 * Registers a task to build the Play Store APK.
 * This task calculates the version code and name, appends the git hash if available, then executes the build script.
 */
internal fun TaskContainer.buildPlayStoreApp() {
    register(TaskNames.buildPlayStoreApp) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.PlayStoreApk
        doLast {
            val (versionCode, versionName) = project.calculateVersionCodeName(isRC = true)
            val gitHash = project.getGitHash()
            project.exec {
                commandLine(
                    "bash",
                    "-c",
                    buildScript(
                        versionCode = versionCode,
                        versionName = if (gitHash.isNullOrEmpty()) versionName else "$versionName-$gitHash",
                        buildCondition = buildCondition
                    )
                )
                standardOutput = System.out
                errorOutput = System.out
            }
        }
    }
}

/**
 * Registers a task to build the App Gallery bundle.
 * This task calculates the version code and name, then executes the build script.
 */
internal fun TaskContainer.buildAppGalleryBundle() {
    register(TaskNames.buildAppGalleryBundle) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.AppGalleryBundle
        doLast {
            val (versionCode, versionName) = project.calculateVersionCodeName(isRC = false)
            project.exec {
                commandLine(
                    "bash",
                    "-c",
                    buildScript(
                        versionCode = versionCode,
                        versionName = versionName,
                        buildCondition = buildCondition
                    )
                )
                standardOutput = System.out
                errorOutput = System.out
            }
        }
    }
}

/**
 * Registers a task to build the App Gallery APK.
 * This task calculates the version code and name, then executes the build script.
 */
internal fun TaskContainer.buildAppGalleryApp() {
    register(TaskNames.buildAppGalleryApp) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.AppGalleryApk
        doLast {
            val (versionCode, versionName) = project.calculateVersionCodeName(isRC = false)
            project.exec {
                commandLine(
                    "bash",
                    "-c",
                    buildScript(
                        versionCode = versionCode,
                        versionName = versionName,
                        buildCondition = buildCondition
                    )
                )
                standardOutput = System.out
                errorOutput = System.out
            }
        }
    }
}

/**
 * Registers a task to build the TU release APK.
 * This task calculates the version code and name, appends the git hash if available, then executes the build script.
 */
internal fun TaskContainer.buildTuReleaseApp() {
    register(TaskNames.buildTuReleaseApp) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.GoogleTuApk
        doLast {
            val (versionCode, versionName) = project.calculateVersionCodeName(isRC = true)
            val gitHash = project.getGitHash()
            project.exec {
                commandLine(
                    "bash",
                    "-c",
                    buildScript(
                        versionCode = versionCode,
                        versionName = if (gitHash.isNullOrEmpty()) versionName else "$versionName-$gitHash",
                        buildCondition = buildCondition
                    )
                )
                standardOutput = System.out
                errorOutput = System.out
            }
        }
    }
}

/**
 * Registers a task to build the Konny APK.
 * This task calculates the version code and name, appends the git hash if available, then executes the build script.
 */
internal fun TaskContainer.buildKonnyApp() {
    register(TaskNames.buildKonnyApp) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.KonnyApk
        doLast {
            val (versionCode, versionName) = project.calculateVersionCodeName(isRC = true)
            val gitHash = project.getGitHash()
            project.exec {
                commandLine(
                    "bash",
                    "-c",
                    buildScript(
                        versionCode = versionCode,
                        versionName = if (gitHash.isNullOrEmpty()) versionName else "$versionName-$gitHash",
                        buildCondition = buildCondition
                    )
                )
                standardOutput = System.out
                errorOutput = System.out
            }
        }
    }
}

/**
 * Registers a task to build the TU debug APK.
 * This task calculates the version code and name, appends the git hash if available, then executes the build script.
 */
internal fun TaskContainer.buildTuDebugApp() {
    register(TaskNames.buildTuDebugApp) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.DebugTuApk
        doLast {
            val (versionCode, versionName) = project.calculateVersionCodeName(isRC = true)
            val gitHash = project.getGitHash()
            project.exec {
                commandLine(
                    "bash",
                    "-c",
                    buildScript(
                        versionCode = versionCode,
                        versionName = if (gitHash.isNullOrEmpty()) versionName else "$versionName-$gitHash",
                        buildCondition = buildCondition
                    )
                )
                standardOutput = System.out
                errorOutput = System.out
            }
        }
    }
}

/**
 * Registers a task to build the mock APK.
 * This task calculates the version code and name, appends the git hash if available, then executes the build script.
 */
internal fun TaskContainer.buildMockApp() {
    register(TaskNames.buildMockApp) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.MockApk
        doLast {
            val (versionCode, versionName) = project.calculateVersionCodeName(isRC = true)
            val gitHash = project.getGitHash()
            project.exec {
                commandLine(
                    "bash",
                    "-c",
                    buildScript(
                        appIdentifier = GRADLE_MOCK_APP_IDENTIFIER,
                        versionCode = versionCode,
                        versionName = if (gitHash.isNullOrEmpty()) versionName else "$versionName-$gitHash",
                        buildCondition = buildCondition
                    )
                )
                standardOutput = System.out
                errorOutput = System.out
            }
        }
    }
}

private fun Project.calculateVersionCodeName(isRC: Boolean = false): Pair<Int, String> =
    try {
        letNotNull(getUserGivenVersionCode(), getUserGivenVersionName()) { versionCode, versionName ->
            val codeToGo = versionCode.toInt()
            val nameToGo = if (isRC) versionName.extractRCVersion() else versionName.extractVersion()
            print("Using given code and name $codeToGo $nameToGo")
            if (nameToGo == null || codeToGo == 0) {
                throw GradleScriptException("Error calculating version code name", Exception(BUILD_EXCEPTION))
            }
            codeToGo to nameToGo
        } ?: run {
            val items = project.getVersionCodeName(isRC)
            print("Using git code and name ${items.first} ${items.second}")
            items
        }
    } catch (e: Exception) {
        throw GradleScriptException("Error calculating version code name", e)
    }

private fun Project.getVersionCodeName(isReleaseCandidate: Boolean = false): Pair<Int, String> {
    val versionName = if (isReleaseCandidate) versionName()?.extractRCVersion() else versionName()?.extractVersion()
    val versionCode = versionCode()
    if (versionName == null || versionCode == null) {
        throw GradleScriptException("BuildTasks getVersionCodeName issue", Exception(BUILD_EXCEPTION))
    }
    return versionCode to versionName
}

// external values from jenkins / commandline
private fun Project.getGitHash(): String? = findProperty(GIT_HASH) as? String
private fun Project.getUserGivenVersionName(): String? = findProperty(FORCED_VERSION_NAME) as? String
private fun Project.getUserGivenVersionCode(): String? = findProperty(FORCED_VERSION_CODE) as? String

private fun Task.runDependencyTasks() {
    dependsOn(TaskNames.versionApp)
    dependsOn(TaskNames.updateGradleProperties)
}

private fun buildScript(
    appIdentifier: String = GRADLE_APP_IDENTIFIER,
    versionCode: Int,
    versionName: String,
    buildCondition: BuildAppFlavoursPlugin.BuildCondition
) =
    "$appIdentifier${buildCondition.assembleTask} " +
        "-PVERSION_CODE=$versionCode " +
        "-PVERSION_NAME=$versionName " +
        "-Pbuildkonfig.flavor=${buildCondition.buildFlavour} --no-daemon"

// ./gradlew bundleGooglePuExternalRelease -PVERSION_CODE=1 -PVERSION_NAME=1.0.0 -Pbuildkonfig.flavor=googlePuExternal

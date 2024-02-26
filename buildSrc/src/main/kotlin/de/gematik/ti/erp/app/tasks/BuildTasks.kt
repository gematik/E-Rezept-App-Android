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

package de.gematik.ti.erp.app.tasks

import de.gematik.ti.erp.app.utils.BUILD_EXCEPTION
import de.gematik.ti.erp.app.plugins.buildapp.BuildAppFlavoursPlugin
import de.gematik.ti.erp.app.utils.GRADLE_ERROR
import de.gematik.ti.erp.app.utils.TaskNames
import de.gematik.ti.erp.app.utils.execute
import de.gematik.ti.erp.app.utils.extractRCVersion
import de.gematik.ti.erp.app.utils.extractVersion
import de.gematik.ti.erp.app.utils.versionCode
import de.gematik.ti.erp.app.utils.versionName
import org.gradle.api.GradleScriptException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer

private const val GRADLE_APP_IDENTIFIER = "./gradlew :app:android:"
private const val GRADLE_MOCK_APP_IDENTIFIER = "./gradlew :app:android-mock:"
private const val GIT_HASH = "git-hash"

internal fun TaskContainer.registerBuildPlayStoreBundle() {
    register(TaskNames.buildPlayStoreBundle) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.PlayStoreBundle
        doLast {
            val (versionCode, versionName) = project.getVersionCodeName()
            val gitHash = project.getGitHash()
            project.exec {
                commandLine =
                    buildScript(
                        versionCode = versionCode,
                        versionName = if (gitHash.isNullOrEmpty()) versionName else "$versionName-$gitHash",
                        buildCondition = buildCondition
                    ).execute()
                standardOutput = System.out
            }
        }
        finalizedBy(TaskNames.copyPlayStoreBundle)
    }
}

internal fun TaskContainer.registerBuildPlayStoreApp() {
    register(TaskNames.buildPlayStoreApp) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.PlayStoreApk
        doLast {
            val (versionCode, versionName) = project.getVersionCodeName()
            val gitHash = project.getGitHash()
            project.exec {
                commandLine =
                    buildScript(
                        versionCode = versionCode,
                        versionName = if (gitHash.isNullOrEmpty()) versionName else "$versionName-$gitHash",
                        buildCondition = buildCondition
                    ).execute()
                standardOutput = System.out
            }
        }
    }
}

internal fun TaskContainer.registerBuildAppGalleryBundle() {
    register(TaskNames.buildAppGalleryBundle) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.AppGalleryBundle
        doLast {
            val (versionCode, versionName) = project.getVersionCodeName()
            val gitHash = project.getGitHash()
            project.exec {
                commandLine =
                    buildScript(
                        versionCode = versionCode,
                        versionName = if (gitHash.isNullOrEmpty()) versionName else "$versionName-$gitHash",
                        buildCondition = buildCondition
                    ).execute()
                standardOutput = System.out
            }
        }
        finalizedBy(TaskNames.copyAppGalleryBundle)
    }
}

internal fun TaskContainer.registerBuildGoogleTuApp() {
    register(TaskNames.buildGoogleTuApp) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.GoogleTuApk
        doLast {
            val (versionCode, versionName) = project.getVersionCodeName()
            val gitHash = project.getGitHash()
            project.exec {
                commandLine =
                    buildScript(
                        versionCode = versionCode,
                        versionName = if (gitHash.isNullOrEmpty()) versionName else "$versionName-$gitHash",
                        buildCondition = buildCondition
                    ).execute()
                standardOutput = System.out
            }
        }
        finalizedBy(TaskNames.copyGoogleTuApp)
    }
}

internal fun TaskContainer.registerBuildKonnyApp() {
    register(TaskNames.buildKonnyApp) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.KonnyApk
        doLast {
            val (versionCode, versionName) = project.getVersionCodeName(isReleaseCandidate = true)
            val gitHash = project.getGitHash()
            project.exec {
                commandLine =
                    buildScript(
                        versionCode = versionCode,
                        versionName = if (gitHash.isNullOrEmpty()) versionName else "$versionName-$gitHash",
                        buildCondition = buildCondition
                    ).execute()
                standardOutput = System.out
            }
        }
        finalizedBy(TaskNames.copyKonnyApp)
    }
}

internal fun TaskContainer.registerBuildDebugApp() {
    register(TaskNames.buildDebugApp) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.DebugApk
        doLast {
            val (versionCode, versionName) = project.getVersionCodeName(isReleaseCandidate = true)
            val gitHash = project.getGitHash()
            project.exec {
                commandLine = buildScript(
                    versionCode = versionCode,
                    versionName = if (gitHash.isNullOrEmpty()) versionName else "$versionName-$gitHash",
                    buildCondition = buildCondition
                ).execute()
                standardOutput = System.out
            }
        }
        finalizedBy(TaskNames.copyDebugApp)
    }
}

internal fun TaskContainer.registerBuildMockApp() {
    register(TaskNames.buildMockApp) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.MockApk
        doLast {
            val (versionCode, versionName) = project.getVersionCodeName(isReleaseCandidate = true)
            val gitHash = project.getGitHash()
            project.exec {
                commandLine =
                    buildScript(
                        appIdentifier = GRADLE_MOCK_APP_IDENTIFIER,
                        versionCode = versionCode,
                        versionName = if (gitHash.isNullOrEmpty()) versionName else "$versionName-$gitHash",
                        buildCondition = buildCondition
                    ).execute()
                standardOutput = System.out
            }
        }
        finalizedBy(TaskNames.copyMockApp)
    }
}

internal fun TaskContainer.registerBuildMinifiedApp() {
    register(TaskNames.buildMinifiedApp) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.MinifiedDebugApk
        doLast {
            val (versionCode, versionName) = project.getVersionCodeName(isReleaseCandidate = true)
            val gitHash = project.getGitHash()
            project.exec {
                commandLine =
                    buildScript(
                        versionCode = versionCode,
                        versionName = if (gitHash.isNullOrEmpty()) versionName else "$versionName-$gitHash",
                        buildCondition = buildCondition
                    ).execute()
                standardOutput = System.out
            }
        }
        finalizedBy(TaskNames.copyMinifiedApp)
    }
}

internal fun TaskContainer.registerBuildMinifiedKonnyApp() {
    register(TaskNames.buildMinifiedKonnyApp) {
        runDependencyTasks()
        val buildCondition = BuildAppFlavoursPlugin.BuildCondition.MinifiedKonnyApk
        doLast {
            val (versionCode, versionName) = project.getVersionCodeName(isReleaseCandidate = true)
            val gitHash = project.getGitHash()
            project.exec {
                commandLine =
                    buildScript(
                        versionCode = versionCode,
                        versionName = if (gitHash.isNullOrEmpty()) versionName else "$versionName-$gitHash",
                        buildCondition = buildCondition
                    ).execute()
                standardOutput = System.out
            }
        }
    }
}

private fun Project.getVersionCodeName(isReleaseCandidate: Boolean = false): Pair<Int, String> {
    val versionName = if (isReleaseCandidate) versionName()?.extractRCVersion() else versionName()?.extractVersion()
    val versionCode = versionCode()
    if (versionName == null || versionCode == null) {
        throw GradleScriptException(GRADLE_ERROR, Exception(BUILD_EXCEPTION))
    }
    println("versionCode = $versionCode")
    println("versionName = $versionName")
    return versionCode to versionName
}

private fun Project.getGitHash(): String? = findProperty(GIT_HASH) as? String

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
    "bash $appIdentifier${buildCondition.assembleTask} " +
            "-PVERSION_CODE=$versionCode " +
            "-PVERSION_NAME=$versionName " +
            "-Pbuildkonfig.flavor=${buildCondition.buildFlavour}"

// ./gradlew bundleGooglePuExternalRelease -PVERSION_CODE=1 -PVERSION_NAME=1.0.0 -Pbuildkonfig.flavor=googlePuExternal

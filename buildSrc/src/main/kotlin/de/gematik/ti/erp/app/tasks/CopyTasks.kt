/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.tasks

import de.gematik.ti.erp.app.plugins.buildapp.BuildAppFlavoursPlugin
import de.gematik.ti.erp.app.utils.APP_MOCK_PROJECT_NAME
import de.gematik.ti.erp.app.utils.APP_PROJECT_NAME
import de.gematik.ti.erp.app.utils.GOOGLE_TU_EXTERNAL_MAPPING_PATH
import de.gematik.ti.erp.app.utils.HUAWEI_STORE_BUNDLE_FILE
import de.gematik.ti.erp.app.utils.HUAWEI_STORE_BUNDLE_PATH
import de.gematik.ti.erp.app.utils.HUAWEI_STORE_MAPPING_PATH
import de.gematik.ti.erp.app.utils.KONNY_APP_APK_FILE
import de.gematik.ti.erp.app.utils.KONNY_APP_APK_PATH
import de.gematik.ti.erp.app.utils.MAPPING_FILE
import de.gematik.ti.erp.app.utils.MINIFIED_APP_APK_FILE
import de.gematik.ti.erp.app.utils.MINIFIED_APP_APK_PATH
import de.gematik.ti.erp.app.utils.MINIFIED_APP_MAPPING_PATH
import de.gematik.ti.erp.app.utils.MOCK_APP_APK_FILE
import de.gematik.ti.erp.app.utils.MOCK_APP_APK_PATH
import de.gematik.ti.erp.app.utils.OUTPUT_DIRECTORY
import de.gematik.ti.erp.app.utils.PLAY_STORE_BUNDLE_FILE
import de.gematik.ti.erp.app.utils.PLAY_STORE_BUNDLE_PATH
import de.gematik.ti.erp.app.utils.PLAY_STORE_MAPPING_PATH
import de.gematik.ti.erp.app.utils.TU_EXTERNAL_APP_APK_FILE
import de.gematik.ti.erp.app.utils.TU_EXTERNAL_APP_APK_FILE_UNSIGNED
import de.gematik.ti.erp.app.utils.TU_EXTERNAL_APP_APK_PATH
import de.gematik.ti.erp.app.utils.TU_INTERNAL_APP_APK_FILE
import de.gematik.ti.erp.app.utils.TU_INTERNAL_APP_APK_PATH
import de.gematik.ti.erp.app.utils.TaskNames
import org.gradle.api.GradleScriptException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

internal fun TaskContainer.copyPlayStoreBundle() {
    register(TaskNames.copyPlayStoreBundle) {
        project.makeAppOutputDirectoryIfNotExists()
        project.subprojects.find { it.name == APP_PROJECT_NAME }?.let { androidProject ->
            println("in ${androidProject.name} sub-project")
            val sourceDir = androidProject.buildDir
            doLast {
                val inputFile = sourceDir.resolve("$PLAY_STORE_BUNDLE_PATH/$PLAY_STORE_BUNDLE_FILE")
                if (inputFile.exists()) {
                    project copyFileFrom inputFile
                } else {
                    throw GradleScriptException("AAB not found", Exception("copyPlayStoreBundle failed"))
                }
                val inputMappingFile = sourceDir.resolve("$PLAY_STORE_MAPPING_PATH/$MAPPING_FILE")
                inputMappingFile moveMappingFileAndRenameTo BuildAppFlavoursPlugin.MappingFileName.PlayStore.fileName()
            }
        } ?: run {
            throw GradleScriptException("Project missing", Exception("Expected project not found"))
        }
    }
}

internal fun TaskContainer.copyAppGalleryBundle() {
    register(TaskNames.copyAppGalleryBundle) {
        project.makeAppOutputDirectoryIfNotExists()
        project.subprojects.find { it.name == APP_PROJECT_NAME }?.let { androidProject ->
            println("in ${androidProject.name} sub-project")
            val sourceDir = androidProject.buildDir
            doLast {
                val inputFile = sourceDir.resolve("$HUAWEI_STORE_BUNDLE_PATH/$HUAWEI_STORE_BUNDLE_FILE")
                if (inputFile.exists()) {
                    project copyFileFrom inputFile
                } else {
                    throw GradleScriptException("AAB not found", Exception("copyAppGalleryBundle failed"))
                }
                val inputMappingFile = sourceDir.resolve("$HUAWEI_STORE_MAPPING_PATH/$MAPPING_FILE")
                inputMappingFile moveMappingFileAndRenameTo BuildAppFlavoursPlugin.MappingFileName.AppGallery.fileName()
            }
        } ?: run {
            throw GradleScriptException("Project missing", Exception("Expected project not found"))
        }
    }
}

internal fun TaskContainer.copyKonnyApp() {
    register(TaskNames.copyKonnyApp) {
        project.makeAppOutputDirectoryIfNotExists()
        project.subprojects.find { it.name == APP_PROJECT_NAME }?.let { androidProject ->
            println("in ${androidProject.name} sub-project")
            val sourceDir = androidProject.buildDir
            doLast {
                val inputFile = sourceDir.resolve("$KONNY_APP_APK_PATH/$KONNY_APP_APK_FILE")
                if (inputFile.exists()) {
                    project copyFileFrom inputFile
                } else {
                    throw GradleScriptException("APK not found", Exception("copyKonnyApp failed"))
                }
            }
        } ?: run {
            throw GradleScriptException("Project missing", Exception("Expected project not found"))
        }
    }
}

internal fun TaskContainer.copyGoogleTuApp() {
    register(TaskNames.copyGoogleTuApp) {
        project.makeAppOutputDirectoryIfNotExists()
        project.subprojects.find { it.name == APP_PROJECT_NAME }?.let { androidProject ->
            println("in ${androidProject.name} sub-project")
            val sourceDir = androidProject.buildDir
            doLast {
                // Delete existing files in the output directory
                project.outputFile().listFiles()?.forEach { it.delete() }

                val inputFile = sourceDir.resolve("$TU_EXTERNAL_APP_APK_PATH/$TU_EXTERNAL_APP_APK_FILE")
                if (inputFile.exists()) {
                    project copyFileFrom inputFile
                } else {
                    val unsignedInputFile =
                        sourceDir.resolve("$TU_EXTERNAL_APP_APK_PATH/$TU_EXTERNAL_APP_APK_FILE_UNSIGNED")
                    project copyFileFrom unsignedInputFile
                    print("Tu app is unsigned, copying unsigned apk")
                }
                val inputMappingFile = sourceDir.resolve("$GOOGLE_TU_EXTERNAL_MAPPING_PATH/$MAPPING_FILE")
                inputMappingFile moveMappingFileAndRenameTo BuildAppFlavoursPlugin.MappingFileName.TuExternal.fileName()
            }
        } ?: run {
            throw GradleScriptException("Project missing", Exception("Expected project not found"))
        }
    }
}

internal fun TaskContainer.copyDebugApp() {
    register(TaskNames.copyDebugApp) {
        project.makeAppOutputDirectoryIfNotExists()
        project.subprojects.find { it.name == APP_PROJECT_NAME }?.let { androidProject ->
            println("in ${androidProject.name} sub-project")
            val sourceDir = androidProject.layout.buildDirectory.get().asFile
            doLast {
                val inputFile =
                    sourceDir.resolve("$TU_INTERNAL_APP_APK_PATH/$TU_INTERNAL_APP_APK_FILE")
                project copyFileFrom inputFile
            }
        } ?: run {
            throw GradleScriptException("Project missing", Exception("Expected project not found"))
        }
    }
}

internal fun TaskContainer.copyMockApp() {
    register(TaskNames.copyMockApp) {
        project.makeAppOutputDirectoryIfNotExists()
        project.subprojects.find { it.name == APP_MOCK_PROJECT_NAME }?.let { androidProject ->
            println("in ${androidProject.name} sub-project")
            val sourceDir = androidProject.buildDir
            doLast {
                val inputFile = sourceDir.resolve("$MOCK_APP_APK_PATH/$MOCK_APP_APK_FILE")
                project copyFileFrom inputFile
            }
        } ?: run {
            throw GradleScriptException("Project missing", Exception("Expected project not found"))
        }
    }
}

internal fun TaskContainer.copyMinifiedApp() {
    register(TaskNames.copyMinifiedApp) {
        project.makeAppOutputDirectoryIfNotExists()
        project.subprojects.find { it.name == APP_PROJECT_NAME }?.let { androidProject ->
            println("in ${androidProject.name} sub-project")
            val sourceDir = androidProject.buildDir
            doLast {
                val inputFile = sourceDir.resolve("$MINIFIED_APP_APK_PATH/$MINIFIED_APP_APK_FILE")
                project copyFileFrom inputFile
                val inputMappingFile = sourceDir.resolve("$MINIFIED_APP_MAPPING_PATH/$MAPPING_FILE")
                inputMappingFile moveMappingFileAndRenameTo BuildAppFlavoursPlugin.MappingFileName.MinifiedApp.fileName()
            }
        } ?: run {
            throw GradleScriptException("Project missing", Exception("Expected project not found"))
        }
    }
}

private fun Project.makeAppOutputDirectoryIfNotExists() {
    if (!rootProject.file(OUTPUT_DIRECTORY).exists()) {
        rootProject.file(OUTPUT_DIRECTORY).mkdirs()
    }
}

private fun Project.outputFile() = rootProject.file(OUTPUT_DIRECTORY)

private infix fun Project.copyFileFrom(inputFile: File?) {
    if (inputFile != null) {
        copy {
            println("copying from $inputFile to ${project.outputFile()}")
            from(inputFile)
            into(project.outputFile())
        }
    } else {
        println("copy failed")
    }
}

private infix fun (File?).moveMappingFileAndRenameTo(
    name: String
) {
    if (this != null) {
        Files.move(Paths.get(this.path), Paths.get("$OUTPUT_DIRECTORY/$name.txt"))
    } else {
        println("move failed")
    }
}

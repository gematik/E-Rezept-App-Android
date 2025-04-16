/*
 * Copyright 2025, gematik GmbH
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

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import de.gematik.ti.erp.app.tasks.ApoFzdApiKeysGradle.PHARMACY_API_KEY
import de.gematik.ti.erp.app.tasks.ApoFzdApiKeysGradle.PHARMACY_API_KEY_TEST
import de.gematik.ti.erp.app.utils.API_TOKEN
import de.gematik.ti.erp.app.utils.CsvDataFunctions
import de.gematik.ti.erp.app.utils.TaskNames
import de.gematik.ti.erp.app.utils.UnescapedProperties
import de.gematik.ti.erp.app.utils.buildForScenario
import de.gematik.ti.erp.app.utils.execute
import de.gematik.ti.erp.app.utils.extractMajorVersion
import de.gematik.ti.erp.app.utils.getToken
import de.gematik.ti.erp.app.utils.sanitizeForGitLab
import de.gematik.ti.erp.app.utils.versionName
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.FileWriter
import java.io.StringReader

/**
 *Task to update the API keys in the ci-overrides.properties file:
 * * [./gradlew updateApoFzdApiKeys -Ptoken=token] (get the token from someone in the team)
 */
internal fun TaskContainer.updateApoFzdApiKeyTask(project: Project) {
    register(TaskNames.updateApoFzdApiKeys) {
        runDependencyTasks()
        val properties = UnescapedProperties()
        doLast {
            val majorVersion = project.versionName()?.extractMajorVersion()?.sanitizeForGitLab() ?: ""
            println("major-version $majorVersion")
            val token = project.getToken()
            if (token.isNullOrEmpty()) {
                throw GradleException(
                    """
                    Missing token, cannot update API keys.
                    Task needs to be runs as ./gradlew ${TaskNames.updateApoFzdApiKeys} -P$API_TOKEN=token 
                    where the {token} is the api-token to the restricted repo
                    """
                        .trimMargin()
                )
            }
            val ruCsvData by lazy {
                project.getCsvDataAsString(token, ApoFzdApiKeyEnvironment.RU)
            }
            val puCsvData by lazy {
                project.getCsvDataAsString(token, ApoFzdApiKeyEnvironment.PU)
            }
            val ruKey = getApovzdKey(ruCsvData, majorVersion)
            val puKey = getApovzdKey(puCsvData, majorVersion)

            val ciOverridesPropertiesFile = project.file("ci-overrides.properties")
            properties.load(ciOverridesPropertiesFile.reader())

            if (ruKey.key.isNotEmpty()) {
                properties.setProperty(PHARMACY_API_KEY_TEST.name, ruKey.key)
            } else {
                throw GradleException("Could not find the ApoFzd API keys RU for the major version $majorVersion")
            }

            if (puKey.key.isNotEmpty()) {
                properties.setProperty(PHARMACY_API_KEY.name, puKey.key)
            } else {
                throw GradleException("Could not find the ApoFzd API keys PU for the major version $majorVersion")
            }

            if (ruKey.key.isNotEmpty() && puKey.key.isNotEmpty()) {
                println("apofzd api keys updated")
            }

            val writer = BufferedWriter(FileWriter(ciOverridesPropertiesFile))
            val time = getCurrentTimeFormatted()
            properties.storeNoEscape(writer, "Last changed on $time")
            writer.close()
        }
    }
}

private fun Project.getCsvDataAsString(
    token: String,
    environment: ApoFzdApiKeyEnvironment
): String {
    return ByteArrayOutputStream().use { outputStream ->
        this.exec {
            commandLine("curl", "-H", "PRIVATE-TOKEN: $token", apoFzdApiKey(environment.name))
            commandLine.execute()
            standardOutput = outputStream
        }
        outputStream.toString()
    }
}

private fun getApovzdKey(
    csvData: String,
    majorVersion: String
): ApovzdKey {
    var key = ApovzdKey()
    val parser = CSVParserBuilder().buildForScenario()
    val reader = CSVReaderBuilder(StringReader(csvData))
        .withCSVParser(parser)
        .build()
    reader.use { csvReader ->
        csvReader.forEach { record ->
            CsvDataFunctions.findKey(
                record = record,
                majorVersion = majorVersion,
                key = ApoFzdApiKeyPlatform.Android.name
            ) { apiKey ->
                key = ApovzdKey(apiKey)
            }
        }
    }
    return key
}

private fun apoFzdApiKey(environment: String) =
    "https://gitlab.prod.ccs.gematik.solutions/api/v4/projects/965/repository/files/$environment.csv/raw?ref=main"

private fun Task.runDependencyTasks() {
    dependsOn(TaskNames.versionApp)
}

enum class ApoFzdApiKeyPlatform {
    Android
}

enum class ApoFzdApiKeyEnvironment {
    PU, RU
}

enum class ApoFzdApiKeysGradle {
    PHARMACY_API_KEY_TEST,
    PHARMACY_API_KEY
}

@JvmInline
value class ApovzdKey(val key: String = "")

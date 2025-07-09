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

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import de.gematik.ti.erp.app.tasks.FdErpApiKeysGradle.ERP_API_KEY_GOOGLE_PU
import de.gematik.ti.erp.app.tasks.FdErpApiKeysGradle.ERP_API_KEY_GOOGLE_RU
import de.gematik.ti.erp.app.tasks.FdErpApiKeysGradle.ERP_API_KEY_GOOGLE_TU
import de.gematik.ti.erp.app.tasks.FdErpApiKeysGradle.ERP_API_KEY_HUAWEI_PU
import de.gematik.ti.erp.app.tasks.FdErpApiKeysGradle.ERP_API_KEY_HUAWEI_RU
import de.gematik.ti.erp.app.tasks.FdErpApiKeysGradle.ERP_API_KEY_HUAWEI_TU
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 *Task to update the API keys in the ci-overrides.properties file:
 * * [./gradlew updateFdApiKeys -Ptoken=token] (get the token from someone in the team)
 */
internal fun TaskContainer.updateFdApiKeysTask(project: Project) {
    register(TaskNames.updateFdApiKeys) {
        runDependencyTasks()
        val properties = UnescapedProperties()
        doLast {
            val majorVersion = project.versionName()?.extractMajorVersion()?.sanitizeForGitLab() ?: ""
            println("major-version $majorVersion")
            val token = project.getToken()
            if (!token.isNullOrEmpty()) {
                val ruCsvData by lazy {
                    project.getCsvDataAsString(token, FdApiKeyEnvironment.RU)
                }
                val puCsvData by lazy {
                    project.getCsvDataAsString(token, FdApiKeyEnvironment.PU)
                }
                val tuCsvData by lazy {
                    project.getCsvDataAsString(token, FdApiKeyEnvironment.TU)
                }
                val ruKey = getAndroidHuaweiVersion(ruCsvData, majorVersion)
                val puKey = getAndroidHuaweiVersion(puCsvData, majorVersion)
                val tuKey = getAndroidHuaweiVersion(tuCsvData, majorVersion)

                val ciOverridesPropertiesFile = project.file("ci-overrides.properties")
                properties.load(ciOverridesPropertiesFile.reader())

                if (ruKey.isNotEmpty()) {
                    properties.setProperty(ERP_API_KEY_GOOGLE_RU.name, ruKey.android)
                    properties.setProperty(ERP_API_KEY_HUAWEI_RU.name, ruKey.huawei)
                } else {
                    throw GradleException("Missing API keys for RU")
                }

                if (tuKey.isNotEmpty()) {
                    properties.setProperty(ERP_API_KEY_GOOGLE_TU.name, tuKey.android)
                    properties.setProperty(ERP_API_KEY_HUAWEI_TU.name, tuKey.huawei)
                } else {
                    throw GradleException("Missing API keys for TU")
                }

                if (puKey.isNotEmpty()) {
                    properties.setProperty(ERP_API_KEY_GOOGLE_PU.name, puKey.android)
                    properties.setProperty(ERP_API_KEY_HUAWEI_PU.name, puKey.huawei)
                } else {
                    throw GradleException("Missing API keys for PU")
                }

                if (ruKey.isNotEmpty() && tuKey.isNotEmpty() && puKey.isNotEmpty()) {
                    println("erp api keys updated.")
                }

                val writer = BufferedWriter(FileWriter(ciOverridesPropertiesFile))
                val time = getCurrentTimeFormatted()
                properties.storeNoEscape(writer, "Last changed on $time")
                writer.close()
            } else {
                throw GradleException(
                    """
                    Missing token, cannot update API keys.
                    Task needs to be runs as ./gradlew ${TaskNames.updateFdApiKeys} -P$API_TOKEN=token 
                    where the {token} is the api-token to the restricted repo
                    """
                        .trimMargin()
                )
            }
        }
    }
}

private fun Project.getCsvDataAsString(
    token: String,
    environment: FdApiKeyEnvironment
): String {
    return ByteArrayOutputStream().use { outputStream ->
        this.exec {
            commandLine("curl", "-H", "PRIVATE-TOKEN: $token", fdApiKeyUrl(environment.name))
            commandLine.execute()
            standardOutput = outputStream
        }
        outputStream.toString()
    }
}

@Suppress("NestedBlockDepth")
private fun getAndroidHuaweiVersion(
    csvData: String,
    majorVersion: String
): FdEnvironmentApiKey {
    var key = FdEnvironmentApiKey()
    val parser = CSVParserBuilder().buildForScenario()
    val reader = CSVReaderBuilder(StringReader(csvData))
        .withCSVParser(parser)
        .build()
    reader.use { csvReader ->
        csvReader.forEach { record ->
            CsvDataFunctions.findKey(
                record = record,
                majorVersion = majorVersion,
                key = FdApiKeyPlatform.Android.name
            ) {
                key = key.copy(android = it)
            }
            CsvDataFunctions.findKey(
                record = record,
                majorVersion = majorVersion,
                key = FdApiKeyPlatform.Huawei.name
            ) {
                key = key.copy(huawei = it)
            }
        }
    }
    return key
}

private fun fdApiKeyUrl(environment: String) =
    "https://gitlab.prod.ccs.gematik.solutions/api/v4/projects/961/repository/files/$environment.csv/raw?ref=main"

private fun Task.runDependencyTasks() {
    dependsOn(TaskNames.versionApp)
}

enum class FdApiKeyEnvironment {
    PU, TU, RU
}

enum class FdApiKeyPlatform {
    Android, Huawei
}

enum class FdErpApiKeysGradle {
    ERP_API_KEY_GOOGLE_RU,
    ERP_API_KEY_HUAWEI_RU,
    ERP_API_KEY_GOOGLE_TU,
    ERP_API_KEY_HUAWEI_TU,
    ERP_API_KEY_GOOGLE_PU,
    ERP_API_KEY_HUAWEI_PU
}

data class FdEnvironmentApiKey(
    val android: String = "",
    val huawei: String = ""
) {
    fun isNotEmpty() = android.isNotEmpty() && huawei.isNotEmpty()
}

fun getCurrentTimeFormatted(): String {
    val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.GERMANY)
    return dateFormat.format(Date())
}

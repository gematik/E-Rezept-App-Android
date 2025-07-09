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

import de.gematik.ti.erp.app.utils.TaskNames
import de.gematik.ti.erp.app.utils.loadCiOverridesProperties
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.apache.hc.client5.http.fluent.Request
import org.apache.hc.core5.http.io.entity.StringEntity
import org.gradle.api.GradleScriptException
import org.gradle.api.tasks.TaskContainer
import java.io.File
import java.util.zip.ZipInputStream

fun TaskContainer.downloadLokaliseStrings() {
    register(TaskNames.downloadLokaliseStrings) {
        group = "lokalise"
        description = "Downloads Android strings.xml files from Lokalise and extracts them into the res/ folder"

        val lokaliseApiKey = project.findProperty("lokaliseApiKey") as? String
            ?: throw GradleScriptException(
                "Missing parameter",
                IllegalArgumentException("Pass -PlokaliseApiKey=...")
            )

        val lokaliseProjectId = project.findProperty("lokaliseProjectId") as? String
            ?: project.loadCiOverridesProperties().getProperty("LOKALISE_PROJECT_ID")
            ?: throw GradleScriptException(
                "Missing 'lokaliseProjectId'",
                IllegalArgumentException("Set -PlokaliseProjectId=... or define LOKALISE_PROJECT_ID in ci-overrides.properties")
            )

        val outputResDir = project.rootProject.file("app-core/src/main/res")

        doLast {
            println("🔄 Downloading Lokalise translations...")

            val payloadJson = buildJsonObject {
                putJsonArray("language_mapping") {
                    addJsonObject {
                        put("original_language_iso", "en_GB")
                        put("custom_language_iso", "en")
                    }
                    addJsonObject {
                        put("original_language_iso", "tr_TR")
                        put("custom_language_iso", "tr")
                    }
                }
                put("format", "xml")
                put("bundle_structure", "values-%LANG_ISO%/strings.%FORMAT%")
                put("add_newline_eof", true)
                put("export_sort", "first_added")
                put("export_empty_as", "skip")
                put("replace_breaks", true)
                put("indentation", "4sp")
            }

            val responseBody = Request.post("https://api.lokalise.com/api2/projects/$lokaliseProjectId/files/download")
                .addHeader("X-Api-Token", lokaliseApiKey)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .body(StringEntity(Json.encodeToString(payloadJson)))
                .execute()
                .returnContent()
                .asString()

            val bundleUrl = Json.parseToJsonElement(responseBody).jsonObject["bundle_url"]?.jsonPrimitive?.content
                ?: error("❌ Failed to get bundle_url from Lokalise response.")

            println("📦 Downloading zip from: $bundleUrl")

            val zipBytes = Request.get(bundleUrl).execute().returnContent().asBytes()
            val zipStream = ZipInputStream(zipBytes.inputStream())

            var entry = zipStream.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".xml")) {
                    val outFile = File(outputResDir, entry.name)
                    outFile.parentFile.mkdirs()
                    outFile.outputStream().use { zipStream.copyTo(it) }
                    println("✅ Extracted: ${outFile.relativeTo(project.rootProject.projectDir)}")
                }
                entry = zipStream.nextEntry
            }

            println("🎉 Lokalise download complete.")
        }
    }
}

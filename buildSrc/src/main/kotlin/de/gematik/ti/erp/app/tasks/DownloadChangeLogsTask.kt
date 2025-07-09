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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.apache.hc.client5.http.fluent.Request
import org.apache.hc.core5.http.io.entity.StringEntity
import org.gradle.api.GradleScriptException
import org.gradle.api.tasks.TaskContainer
import java.io.File
import java.util.zip.ZipInputStream

private val json = Json { prettyPrint = true }

fun TaskContainer.downloadChangeLogs() {
    register(TaskNames.downloadChangeLogs) {
        group = "lokalise"
        description = "Downloads internal_messages.json files from Lokalise and extracts them into the assets/ folder"

        val apiKey = project.findProperty("lokaliseApiKey") as? String
            ?: throw GradleScriptException("Missing 'lokaliseApiKey'", IllegalArgumentException("Use -PlokaliseApiKey=..."))

        val projectId = project.findProperty("changeLogsProjectId") as? String
            ?: project.loadCiOverridesProperties().getProperty("CHANGELOGS_PROJECT_ID")
            ?: throw GradleScriptException(
                "Missing 'changeLogsProjectId'",
                IllegalArgumentException("Set -PlokaliseProjectId=... or define CHANGELOGS_PROJECT_ID in ci-overrides.properties")
            )

        val outputDir = project.rootProject.file("app/features/src/main/assets")

        doLast {
            try {
                println("📦 Requesting Lokalise JSON export...")

                val payload = buildJsonObject {
                    put("format", "json")
                    put("original_filenames", false)
                    put("bundle_structure", "%LANG_ISO%.lproj/internal_messages.%FORMAT%")
                    put("all_platforms", true)
                    put("replace_breaks", false)
                }

                val responseJson = Request.post("https://api.lokalise.com/api2/projects/$projectId/files/download")
                    .addHeader("x-api-token", apiKey)
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .body(StringEntity(Json.encodeToString(JsonObject.serializer(), payload)))
                    .execute()
                    .returnContent()
                    .asString()

                val bundleUrl = Json.parseToJsonElement(responseJson)
                    .jsonObject["bundle_url"]
                    ?.jsonPrimitive?.content
                    ?: error("❌ Failed to get bundle_url from Lokalise response")

                println("⬇️ Downloading zip from: $bundleUrl")

                val zipBytes = Request.get(bundleUrl)
                    .execute()
                    .returnContent()
                    .asBytes()

                val zipStream = ZipInputStream(zipBytes.inputStream())

                var entry = zipStream.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.endsWith(".json")) {
                        // Fix folder name: en_IL.lproj → en.lproj
                        val fixedEntryName = entry.name.replace(Regex("""^([a-z]{2})_[A-Z]{2}\.lproj/""")) {
                            "${it.groupValues[1]}.lproj/"
                        }

                        val outputFile = File(outputDir, fixedEntryName) // preserve folder structure!
                        outputFile.parentFile.mkdirs()

                        // Save the file
                        outputFile.outputStream().use { zipStream.copyTo(it) }
                        println("✅ Extracted: ${outputFile.relativeTo(project.rootProject.projectDir)}")

                        // Read and pretty print the changelog JSON array
                        val content = outputFile.readText()
                        val jsonArray = json.decodeFromString(JsonArray.serializer(), content)

                        println("📄 Changelog for ${outputFile.name}:\n")
                        jsonArray
                            .sortedByDescending { it.jsonObject["timestamp"]?.jsonPrimitive?.content }
                            .forEach { item ->
                                val version = item.jsonObject["version"]?.jsonPrimitive?.content ?: "unknown"
                                val text = item.jsonObject["text"]?.jsonPrimitive?.content ?: ""
                                println("🔸 Version $version\n$text\n")
                            }
                    }
                    entry = zipStream.nextEntry
                }

                zipStream.close()
                println("🎉 Lokalise JSON assets download complete.")
            } catch (e: Exception) {
                throw GradleScriptException("Error on change logs download", e)
            }
        }
    }
}

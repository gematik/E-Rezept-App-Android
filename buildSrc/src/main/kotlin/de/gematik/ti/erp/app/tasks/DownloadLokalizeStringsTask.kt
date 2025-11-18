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
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.apache.hc.client5.http.HttpResponseException
import org.apache.hc.client5.http.fluent.Request
import org.apache.hc.core5.http.io.entity.StringEntity
import org.gradle.api.GradleScriptException
import org.gradle.api.tasks.TaskContainer
import java.io.File
import java.util.zip.ZipInputStream

fun TaskContainer.downloadLokaliseStrings() {
    register(TaskNames.downloadLokaliseStrings) {
        group = "lokalise"
        description = "Downloads Android strings.xml files from Lokalise (async) and extracts them into res/"

        val lokaliseApiKey = project.findProperty("lokaliseApiKey") as? String
            ?: throw GradleScriptException("Missing parameter", IllegalArgumentException("Pass -PlokaliseApiKey=..."))

        val lokaliseProjectId = project.findProperty("lokaliseProjectId") as? String
            ?: project.loadCiOverridesProperties().getProperty("LOKALISE_PROJECT_ID")
            ?: throw GradleScriptException(
                "Missing 'lokaliseProjectId'",
                IllegalArgumentException("Set -PlokaliseProjectId=... or define LOKALISE_PROJECT_ID in ci-overrides.properties")
            )

        val outputResDir = project.rootProject.file("core/src/main/res")

        // ----- helpers -----

        fun startAsyncDownload(originalIso: String, customIso: String): String {
            val languageMapping = buildJsonArray {
                add(
                    buildJsonObject {
                        put("original_language_iso", originalIso)
                        put("custom_language_iso", customIso)
                    }
                )
            }
            val languages = buildJsonArray {
                add(originalIso)
            }

            val payload = buildJsonObject {
                put("format", "xml")
                put("bundle_structure", "values-%LANG_ISO%/strings.xml")
                put("indentation", "4sp")
                put("export_empty_as", "skip")
                put("replace_breaks", true)
                put("original_filenames", false)
                putJsonArray("filter_platforms") { add("android") }
                putJsonArray("filter_data") { add("translated") }
                putJsonArray("language_mapping") { languageMapping.forEach { add(it) } }
                putJsonArray("languages") { languages.forEach { add(it) } }
                // slim output
                put("include_comments", false)
                put("include_description", false)
                put("include_tags", false)
                put("include_project_ids", false)
            }

            val resp = Request.post("https://api.lokalise.com/api2/projects/$lokaliseProjectId/files/async-download")
                .addHeader("X-Api-Token", lokaliseApiKey)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .body(StringEntity(Json.encodeToString(payload)))
                .execute()
                .returnContent()
                .asString()

            println("✅ Async export started: $resp")

            val processId = Json.parseToJsonElement(resp).jsonObject["process_id"]
                ?.jsonPrimitive?.content
                ?: error("❌ Failed to start async export for $originalIso")

            return processId
        }

        fun pollProcess(
            processId: String,
            timeoutSeconds: Int = 600, // up to 10 min for very large projects
            initialIntervalMs: Long = 1000L, // start at 1s
            maxIntervalMs: Long = 8000L // cap at 8s
        ): String {
            var interval = initialIntervalMs
            val start = System.currentTimeMillis()

            while (true) {
                val resp = Request.get("https://api.lokalise.com/api2/projects/$lokaliseProjectId/processes/$processId")
                    .addHeader("X-Api-Token", lokaliseApiKey)
                    .addHeader("Accept", "application/json")
                    .execute()
                    .returnContent()
                    .asString()

                println("⏳ Poll response: $resp")

                val root = Json.parseToJsonElement(resp).jsonObject
                val process = root["process"]?.jsonObject
                    ?: error("❌ Missing 'process' in response: $resp")

                val status = process["status"]?.jsonPrimitive?.contentOrNull

                // details can be null while queued/in_progress — handle it safely
                val detailsElem = process["details"]
                val details: JsonObject? = when (detailsElem) {
                    null, JsonNull -> null
                    else -> detailsElem.jsonObject
                }

                // URL might be under either key; prefer download_url if present
                val downloadUrl = details?.get("download_url")?.jsonPrimitive?.contentOrNull
                    ?: details?.get("bundle_url")?.jsonPrimitive?.contentOrNull

                when (status) {
                    "finished" -> {
                        if (!downloadUrl.isNullOrBlank()) {
                            return downloadUrl
                        }
                        // finished but URL not ready — short grace retry
                        repeat(3) {
                            Thread.sleep(500)
                            val again = Request.get("https://api.lokalise.com/api2/projects/$lokaliseProjectId/processes/$processId")
                                .addHeader("X-Api-Token", lokaliseApiKey)
                                .addHeader("Accept", "application/json")
                                .execute()
                                .returnContent()
                                .asString()
                            val againObj = Json.parseToJsonElement(again).jsonObject
                            val againDetailsElem = againObj["process"]?.jsonObject?.get("details")
                            val againDetails = when (againDetailsElem) {
                                null, JsonNull -> null
                                else -> againDetailsElem.jsonObject
                            }
                            val againUrl = againDetails?.get("download_url")?.jsonPrimitive?.contentOrNull
                                ?: againDetails?.get("bundle_url")?.jsonPrimitive?.contentOrNull
                            if (!againUrl.isNullOrBlank()) return againUrl
                        }
                        error("❌ Process finished but no download URL available yet.")
                    }

                    "queued", "in_progress", "running" -> {
                        val elapsed = (System.currentTimeMillis() - start) / 1000
                        if (elapsed > timeoutSeconds) {
                            error("❌ Async export timed out after ${timeoutSeconds}s (status=$status)")
                        }
                        Thread.sleep(interval)
                        interval = (interval * 2).coerceAtMost(maxIntervalMs)
                    }

                    "failed", "cancelled", "skipped" -> {
                        error("❌ Async export ended with status=$status: $resp")
                    }

                    else -> {
                        val elapsed = (System.currentTimeMillis() - start) / 1000
                        if (elapsed > timeoutSeconds) {
                            error("❌ Async export timed out (unknown status). Response: $resp")
                        }
                        Thread.sleep(interval)
                        interval = (interval * 2).coerceAtMost(maxIntervalMs)
                    }
                }
            }
        }

        fun downloadAndExtractZip(bundleUrl: String) {
            val zipBytes = Request.get(bundleUrl).execute().returnContent().asBytes()
            ZipInputStream(zipBytes.inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.endsWith(".xml")) {
                        val file = File(outputResDir, entry.name)
                        file.parentFile.mkdirs()
                        file.outputStream().use { zip.copyTo(it) }
                        println("✅ Extracted ${file.relativeTo(project.rootProject.projectDir)}")
                    }
                    entry = zip.nextEntry
                }
            }
        }

        doLast {
            println("🔄 Lokalise async export (per language) → $outputResDir")

            // Per-language async export (keeps bundles small & avoids 413)
            val languages = listOf(
                "en_GB" to "en",
                "tr_TR" to "tr"
            )

            languages.forEachIndexed { i, (originalIso, customIso) ->
                println("🌐 ${i + 1}/${languages.size}: $originalIso → $customIso")
                try {
                    val processId = startAsyncDownload(originalIso, customIso)
                    println("⏳ Waiting for download with ID $processId …")
                    val bundleUrl = pollProcess(processId)
                    println("📦 Downloading from $bundleUrl")
                    downloadAndExtractZip(bundleUrl)
                    println("✅ $originalIso done.")
                } catch (e: HttpResponseException) {
                    throw GradleScriptException("Lokalise async export failed for $originalIso: ${e.message}", e)
                }
            }

            println("🎉 Lokalise download complete (async).")
        }
    }
}

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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.apache.hc.client5.http.fluent.Request
import org.apache.hc.core5.http.io.entity.StringEntity
import org.gradle.api.GradleScriptException
import org.gradle.api.tasks.TaskContainer
import java.io.File
import java.util.Base64

@Suppress("TopLevelPropertyNaming")
private const val applicationJson = "application/json"

fun TaskContainer.uploadLokaliseStrings() {
    register(TaskNames.uploadLokaliseStrings) {
        group = "lokalise"
        description = "Uploads a strings.xml file to Lokalise and creates a snapshot before upload."

        val apiKey = project.findProperty("lokaliseApiKey") as? String
            ?: throw GradleScriptException("Missing 'lokaliseApiKey'", IllegalArgumentException("Use -PlokaliseApiKey=..."))

        val lokaliseProjectId = project.findProperty("lokaliseProjectId") as? String
            ?: project.loadCiOverridesProperties().getProperty("LOKALISE_PROJECT_ID")
            ?: throw GradleScriptException(
                "Missing 'lokaliseProjectId'",
                IllegalArgumentException("Set -PlokaliseProjectId=... or define LOKALISE_PROJECT_ID in ci-overrides.properties")
            )

        val sourceFilePath = project.findProperty("sourcePath") as? String
            ?: "core/src/main/res/values/strings.xml"

        val sourceFile = File(sourceFilePath)

        doLast {
            if (!sourceFile.exists()) {
                throw GradleScriptException("File not found", Exception("Expected file: ${sourceFile.absolutePath}"))
            }

            println("📸 Creating snapshot before upload...")

            val snapshotPayload = buildJsonObject {
                put("title", "Before Upload Snapshot")
            }

            val snapshotResponse = Request.post("https://api.lokalise.com/api2/projects/$lokaliseProjectId/snapshots")
                .addHeader("X-Api-Token", apiKey)
                .addHeader("Content-Type", applicationJson)
                .addHeader("Accept", applicationJson)
                .body(StringEntity(Json.encodeToString(JsonObject.serializer(), snapshotPayload)))
                .execute()
                .returnContent()
                .asString()

            println("✅ Snapshot created: $snapshotResponse")

            println("📤 Uploading strings.xml to Lokalise...")

            val base64Strings = Base64.getEncoder().encodeToString(sourceFile.readBytes())

            val uploadPayload = buildJsonObject {
                put("data", base64Strings)
                put("lang_iso", "de")
                put("filename", "strings.xml")
                put("slashn_to_linebreak", true)
                put("cleanup_mode", true)
                put("use_automations", true)
            }

            val uploadResponse = Request.post("https://api.lokalise.com/api2/projects/$lokaliseProjectId/files/upload")
                .addHeader("X-Api-Token", apiKey)
                .addHeader("Content-Type", applicationJson)
                .addHeader("Accept", applicationJson)
                .body(StringEntity(Json.encodeToString(JsonObject.serializer(), uploadPayload)))
                .execute()
                .returnContent()
                .asString()

            println("✅ Upload response: $uploadResponse")
            println("🎉 Upload complete.")
        }
    }
}

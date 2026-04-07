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

@file:Suppress("unused")

package de.gematik.ti.erp.app.plugins.teams

import de.gematik.ti.erp.app.ErpPlugin
import de.gematik.ti.erp.app.utils.TaskNames
import de.gematik.ti.erp.app.utils.detectPropertyOrNull
import de.gematik.ti.erp.app.utils.detectPropertyOrThrow
import de.gematik.ti.erp.app.utils.loadCiOverridesProperties
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Gradle plugin that registers the [TaskNames.sendMRTeamsNotification] task.
 *
 * Reads [TEAMS_MR_WEBHOOK_URL], [GITLAB_PROJECT_API_URL], and [GITLAB_PRIVATE_TOKEN] from
 * `ci-overrides.properties`. Fetches MR metadata and commits from the GitLab API, then posts a
 * reviewer-friendly Teams card showing:
 *   - MR title, status, labels, milestone, assignees
 *   - Commit list
 *   - AppCenter download link + git hash
 *
 * Usage from Jenkins (Multibranch pipeline):
 * ```
 * ./gradlew sendMRTeamsNotification \
 *     -PmrBranch=MR-2315 \
 *     -Pstatus=SUCCESS \
 *     -PbuildNr=42 \
 *     -PgitHash=abc1234 \
 *     -PfirebaseUrl=https://install.appcenter.ms/... \
 *     -Pjob="eRp-Android-App-Multibranch/MR-2315"
 * ```
 */
class MRNotificationPlugin : ErpPlugin {

    override fun apply(project: Project) {
        project.tasks.register(TaskNames.sendMRTeamsNotification) {
            doLast {
                val ciProps = project.loadCiOverridesProperties()

                val webhookUrl = ciProps.getProperty("TEAMS_MR_WEBHOOK_URL")
                    ?: throw GradleException("TEAMS_MR_WEBHOOK_URL not found in ci-overrides.properties")
                val gitlabApiUrl = ciProps.getProperty("GITLAB_PROJECT_API_URL")
                    ?: throw GradleException("GITLAB_PROJECT_API_URL not found in ci-overrides.properties")
                val gitlabToken = ciProps.getProperty("GITLAB_PRIVATE_TOKEN")
                    ?: throw GradleException("GITLAB_PRIVATE_TOKEN not found in ci-overrides.properties")

                val mrBranch = project.detectPropertyOrThrow("mrBranch") // e.g. "MR-2315"
                val buildStatus = project.detectPropertyOrThrow("status")
                val buildNumber = project.detectPropertyOrNull("buildNr") ?: "N/A"
                val gitHash = project.detectPropertyOrNull("gitHash")?.trim() ?: "N/A"
                val firebaseUrl = (project.detectPropertyOrNull("firebaseUrl")?.trim() ?: "")
                    .substringBefore("?utm_source")
                val jobName = project.detectPropertyOrNull("job") ?: "N/A"

                // "MR-2315" or "MR-2315-some-extra" → "2315"
                val mrNumber = mrBranch.removePrefix("MR-").split("-").first()

                val mrDetails = try {
                    fetchMRDetails(gitlabApiUrl, gitlabToken, mrNumber)
                } catch (e: Exception) {
                    println("Warning: Could not fetch MR details: ${e.message}")
                    null
                }

                val commits = try {
                    fetchMRCommits(gitlabApiUrl, gitlabToken, mrNumber)
                } catch (e: Exception) {
                    println("Warning: Could not fetch MR commits: ${e.message}")
                    emptyList()
                }

                val payload = buildMRTeamsPayload(
                    mrNumber = mrNumber,
                    mrBranch = mrBranch,
                    buildStatus = buildStatus,
                    buildNumber = buildNumber,
                    gitHash = gitHash,
                    firebaseUrl = firebaseUrl,
                    jobName = jobName,
                    mrDetails = mrDetails,
                    commits = commits
                )

                try {
                    postToTeams(webhookUrl, payload)
                    println("Teams MR notification sent for $mrBranch ($buildStatus)")
                } catch (e: Exception) {
                    // Never fail the build because of a notification error
                    println("Warning: Could not send Teams MR notification: ${e.message}")
                }
            }
        }
    }
}

// --- Domain model ------------------------------------------------------------

internal data class MRDetails(
    val title: String,
    val webUrl: String,
    val labels: List<String>,
    val milestone: String?,
    val assignees: List<String>
)

internal data class MRCommit(
    val shortId: String,
    val title: String
)

// --- GitLab API --------------------------------------------------------------

/**
 * Fetches title, web URL, labels, milestone, and assignees for a single MR.
 * Uses `GET {apiUrl}/merge_requests/{iid}`.
 */
internal fun fetchMRDetails(apiUrl: String, token: String, mrNumber: String): MRDetails {
    val connection = (URL("$apiUrl/merge_requests/$mrNumber").openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        setRequestProperty("PRIVATE-TOKEN", token)
        setRequestProperty("Accept", "application/json")
        connectTimeout = 30_000
        readTimeout = 30_000
    }
    check(connection.responseCode == HttpURLConnection.HTTP_OK) {
        "GitLab MR API returned HTTP ${connection.responseCode}"
    }
    val obj = Json.parseToJsonElement(connection.inputStream.bufferedReader().readText()).jsonObject
    return MRDetails(
        title = obj["title"]?.jsonPrimitive?.content ?: "",
        webUrl = obj["web_url"]?.jsonPrimitive?.content ?: "",
        labels = obj["labels"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
        milestone = obj["milestone"]?.jsonObject?.get("title")?.jsonPrimitive?.content,
        assignees = obj["assignees"]?.jsonArray?.mapNotNull {
            it.jsonObject["name"]?.jsonPrimitive?.content
        } ?: emptyList()
    )
}

/**
 * Fetches the commit list for a MR (most recent first, capped at 20).
 * Uses `GET {apiUrl}/merge_requests/{iid}/commits`.
 */
internal fun fetchMRCommits(apiUrl: String, token: String, mrNumber: String): List<MRCommit> {
    val connection = (
        URL("$apiUrl/merge_requests/$mrNumber/commits?per_page=20")
            .openConnection() as HttpURLConnection
        ).apply {
        requestMethod = "GET"
        setRequestProperty("PRIVATE-TOKEN", token)
        setRequestProperty("Accept", "application/json")
        connectTimeout = 30_000
        readTimeout = 30_000
    }
    if (connection.responseCode != HttpURLConnection.HTTP_OK) return emptyList()
    return Json.parseToJsonElement(connection.inputStream.bufferedReader().readText()).jsonArray
        .map { element ->
            val obj = element.jsonObject
            MRCommit(
                shortId = obj["short_id"]?.jsonPrimitive?.content ?: "",
                title = obj["title"]?.jsonPrimitive?.content ?: ""
            )
        }
}

// --- Teams payload builder ---------------------------------------------------

private fun mrFact(name: String, value: String) = buildJsonObject {
    put("name", JsonPrimitive(name))
    put("value", JsonPrimitive(value))
}

/**
 * Builds a reviewer-friendly Teams MessageCard for an MR build result.
 */
internal fun buildMRTeamsPayload(
    mrNumber: String,
    mrBranch: String,
    buildStatus: String,
    buildNumber: String,
    gitHash: String,
    firebaseUrl: String,
    jobName: String,
    mrDetails: MRDetails?,
    commits: List<MRCommit>
): String {
    val isSuccess = buildStatus.equals("SUCCESS", ignoreCase = true)
    val themeColor = if (isSuccess) "00C851" else "FF4444"
    val statusIcon = if (isSuccess) "✅" else "❌"
    val statusLabel = if (isSuccess) "PASSED" else "FAILED"
    val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

    val title = mrDetails?.title ?: mrBranch
    val mrLink = mrDetails?.webUrl
        ?: "https://gitlab.prod.ccs.gematik.solutions/e-rezept/fdv/apps/core/erp-app-android/-/merge_requests/$mrNumber"

    val sections = mutableListOf<JsonObject>()

    // --- Section 1: MR summary -----------------------------------------------
    sections += buildJsonObject {
        put("activityTitle", JsonPrimitive("$statusIcon MR-$mrNumber: $title — $statusLabel"))
        put("activitySubtitle", JsonPrimitive("🕐 $time  |  Build #$buildNumber  |  git: $gitHash"))
        put(
            "facts",
            buildJsonArray {
                add(mrFact("🔗 MR Link:", mrLink))
                if (mrDetails != null) {
                    val labelText = if (mrDetails.labels.isNotEmpty()) mrDetails.labels.joinToString(", ") else "—"
                    add(mrFact("🏷️ Labels:", labelText))
                    add(mrFact("📌 Milestone:", mrDetails.milestone ?: "—"))
                    val assigneeText = if (mrDetails.assignees.isNotEmpty()) mrDetails.assignees.joinToString(", ") else "—"
                    add(mrFact("👤 Assignee:", assigneeText))
                }
            }
        )
        put("markdown", JsonPrimitive(true))
    }

    // --- Section 2: Commits --------------------------------------------------
    if (commits.isNotEmpty()) {
        val commitLines = commits.joinToString("\n") { "- `${it.shortId}` ${it.title}" }
        sections += buildJsonObject {
            put("activityTitle", JsonPrimitive("📝 Commits (${commits.size})"))
            put("text", JsonPrimitive(commitLines))
            put("markdown", JsonPrimitive(true))
        }
    }

    // --- Section 3: Download link + QR code -------------------------------------
    if (firebaseUrl.isNotBlank()) {
        val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=$firebaseUrl"
        sections += buildJsonObject {
            put("activityTitle", JsonPrimitive("🔥 Download APK (Firebase)"))
            put("text", JsonPrimitive("[$firebaseUrl]($firebaseUrl)\n\n![]($qrUrl)"))
            put("markdown", JsonPrimitive(true))
        }
    }

    val card = buildJsonObject {
        put("@type", JsonPrimitive("MessageCard"))
        put("@context", JsonPrimitive("http://schema.org/extensions"))
        put("themeColor", JsonPrimitive(themeColor))
        put("summary", JsonPrimitive("$statusIcon MR-$mrNumber $statusLabel — $title"))
        put("sections", JsonArray(sections))
    }

    return card.toString()
}

// --- HTTP send ---------------------------------------------------------------

private fun postToTeams(webhookUrl: String, payload: String) {
    val bytes = payload.toByteArray(Charsets.UTF_8)
    val connection = (URL(webhookUrl).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        setRequestProperty("Content-Type", "application/json; charset=utf-8")
        setRequestProperty("Content-Length", bytes.size.toString())
        doOutput = true
        connectTimeout = 30_000
        readTimeout = 30_000
    }
    connection.outputStream.use { it.write(bytes) }
    val responseCode = connection.responseCode
    if (responseCode !in 200..299) {
        throw GradleException("Teams webhook returned HTTP $responseCode")
    }
}

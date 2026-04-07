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
import kotlinx.serialization.json.put
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Gradle plugin that registers the [TaskNames.sendReleaseNotification] task.
 *
 * Credentials ([TEAMS_RELEASE_WEBHOOK_URL], [GITLAB_PROJECT_API_URL], [GITLAB_PRIVATE_TOKEN])
 * are read from `ci-overrides.properties` at the project root.
 *
 * On SUCCESS the task:
 *  1. Derives the milestone version from the supplied version name (e.g. "1.37.1" -> "1.37.0").
 *  2. Checks whether that milestone exists in GitLab; falls back to the next minor milestone if not.
 *  3. Fetches all non-closed MRs for the milestone and categorises them.
 *  4. Posts a green Teams card with the categorised MR list (and a warning when the milestone was missing).
 *
 * On FAILURE a red Teams card is posted with build info only.
 *
 * Usage from Jenkins:
 * ```
 * ./gradlew sendReleaseNotification \
 *     -PversionName=1.37.1 \
 *     -Pstatus=SUCCESS \
 *     -PbuildNr=42 \
 *     -Pjob="eRp-Android-Release"
 * ```
 */
class ReleaseNotificationPlugin : ErpPlugin {

    override fun apply(project: Project) {
        project.tasks.register(TaskNames.sendReleaseNotification) {
            doLast {
                val ciProps = project.loadCiOverridesProperties()

                val webhookUrl = ciProps.getProperty("TEAMS_RELEASE_WEBHOOK_URL")
                    ?: throw GradleException("TEAMS_RELEASE_WEBHOOK_URL not found in ci-overrides.properties")
                val gitlabApiUrl = ciProps.getProperty("GITLAB_PROJECT_API_URL")
                    ?: throw GradleException("GITLAB_PROJECT_API_URL not found in ci-overrides.properties")
                val gitlabToken = ciProps.getProperty("GITLAB_PRIVATE_TOKEN")
                    ?: throw GradleException("GITLAB_PRIVATE_TOKEN not found in ci-overrides.properties")

                val versionName = project.detectPropertyOrThrow("versionName")
                val buildStatus = project.detectPropertyOrThrow("status")
                val buildNumber = project.detectPropertyOrNull("buildNr") ?: "N/A"
                val jobName = project.detectPropertyOrNull("job") ?: "N/A"

                val milestoneVersion = toMilestoneVersion(versionName)

                // Only fetch MRs on success; failure cards just carry build info
                val gitLabInfo: ResolvedGitLabInfo? = when {
                    buildStatus.equals("SUCCESS", ignoreCase = true) -> {
                        try {
                            resolveGitLabMRs(gitlabApiUrl, gitlabToken, milestoneVersion)
                        } catch (e: Exception) {
                            println("Warning: Could not fetch GitLab MRs: ${e.message}")
                            null
                        }
                    }
                    else -> null
                }

                val payload = buildReleaseTeamsPayload(
                    versionName = versionName,
                    milestoneVersion = milestoneVersion,
                    buildStatus = buildStatus,
                    buildNumber = buildNumber,
                    jobName = jobName,
                    gitLabInfo = gitLabInfo
                )

                try {
                    sendToTeams(webhookUrl, payload)
                    println("Teams release notification sent for $milestoneVersion ($buildStatus)")
                } catch (e: Exception) {
                    // Never fail the build because of a notification error
                    println("Warning: Could not send Teams notification: ${e.message}")
                }
            }
        }
    }
}

// --- Version helpers ----------------------------------------------------------

/**
 * Normalises a build version name to the GitLab milestone label.
 * Patch is always set to 0 because milestones are created per minor release only.
 *
 *   "1.37.0" -> "1.37.0"
 *   "1.37.1" -> "1.37.0"
 */
internal fun toMilestoneVersion(versionName: String): String {
    val parts = versionName.trim().split(".")
    return if (parts.size >= 2) "${parts[0]}.${parts[1]}.0" else versionName
}

/** Increments the minor segment: "1.37.0" -> "1.38.0". */
internal fun nextMinorVersion(version: String): String {
    val parts = version.split(".")
    return if (parts.size >= 2) "${parts[0]}.${(parts[1].toIntOrNull() ?: 0) + 1}.0" else version
}

// --- Domain model -------------------------------------------------------------

internal enum class MRCategory { FEATURE, ENHANCEMENT, BUG_FIX }

/**
 * A single GitLab merge request that is either *merged* (shown in a category) or *open/unmerged*
 * (shown in a dedicated warning section). Closed MRs are discarded before this point.
 */
internal data class MergeRequest(
    val originalTitle: String,
    val cleanedTitle: String,
    val labels: List<String>,
    val sourceBranch: String,
    /** true when state == "merged", false when state == "opened". */
    val isMerged: Boolean,
    val category: MRCategory
)

// --- GitLab API ---------------------------------------------------------------

internal sealed class GitLabFetchResult {
    data class Found(val mergeRequests: List<MergeRequest>) : GitLabFetchResult()

    /** The milestone title does not exist in the project. */
    object MilestoneNotFound : GitLabFetchResult()
    data class FetchError(val message: String) : GitLabFetchResult()
}

/**
 * Returns true when a milestone with [version] as its title exists in the GitLab project.
 * Uses GET {apiUrl}/milestones?title={version} -- an empty array means it is absent.
 */
internal fun checkMilestoneExists(apiUrl: String, token: String, version: String): Boolean {
    val encoded = URLEncoder.encode(version, "UTF-8")
    val connection = (URL("$apiUrl/milestones?title=$encoded").openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        setRequestProperty("PRIVATE-TOKEN", token)
        setRequestProperty("Accept", "application/json")
        connectTimeout = 30_000
        readTimeout = 30_000
    }
    if (connection.responseCode != HttpURLConnection.HTTP_OK) return false
    return Json.parseToJsonElement(connection.inputStream.bufferedReader().readText()).jsonArray.isNotEmpty()
}

/**
 * Fetches all non-closed MRs tagged with [version] as their milestone.
 * Closed MRs (state == "closed") are silently discarded.
 */
internal fun fetchMRsForMilestone(apiUrl: String, token: String, version: String): List<MergeRequest> {
    val encoded = URLEncoder.encode(version, "UTF-8")
    val connection = (
        URL("$apiUrl/merge_requests?milestone=$encoded&per_page=100&include_labels=true")
            .openConnection() as HttpURLConnection
        ).apply {
        requestMethod = "GET"
        setRequestProperty("PRIVATE-TOKEN", token)
        setRequestProperty("Accept", "application/json")
        connectTimeout = 30_000
        readTimeout = 30_000
    }
    if (connection.responseCode != HttpURLConnection.HTTP_OK) return emptyList()
    return parseMRsResponse(connection.inputStream.bufferedReader().readText())
}

internal fun queryGitLabMilestone(apiUrl: String, token: String, version: String): GitLabFetchResult {
    return try {
        if (!checkMilestoneExists(apiUrl, token, version)) {
            println("GitLab: milestone '$version' does not exist")
            GitLabFetchResult.MilestoneNotFound
        } else {
            GitLabFetchResult.Found(fetchMRsForMilestone(apiUrl, token, version))
        }
    } catch (e: Exception) {
        GitLabFetchResult.FetchError(e.message ?: "Unknown error")
    }
}

// --- Resolution with fallback -------------------------------------------------

internal data class ResolvedGitLabInfo(
    val mergeRequests: List<MergeRequest>,
    /** The milestone that was actually queried (may differ from the originally requested one). */
    val resolvedVersion: String,
    /** true when the originally requested milestone was absent and a fallback was used. */
    val originalVersionMissing: Boolean,
    /** true when even the next-minor fallback was not found. */
    val fallbackAlsoMissing: Boolean = false
)

/**
 * Tries [version] first; if the milestone does not exist in GitLab, retries with the next minor
 * version and sets [ResolvedGitLabInfo.originalVersionMissing] so the Teams card can warn the team.
 */
internal fun resolveGitLabMRs(apiUrl: String, token: String, version: String): ResolvedGitLabInfo {
    return when (val primary = queryGitLabMilestone(apiUrl, token, version)) {
        is GitLabFetchResult.Found -> ResolvedGitLabInfo(
            mergeRequests = primary.mergeRequests,
            resolvedVersion = version,
            originalVersionMissing = false
        )
        is GitLabFetchResult.MilestoneNotFound -> {
            val next = nextMinorVersion(version)
            println("GitLab: '$version' not found, trying fallback '$next'")
            when (val fallback = queryGitLabMilestone(apiUrl, token, next)) {
                is GitLabFetchResult.Found -> ResolvedGitLabInfo(
                    mergeRequests = fallback.mergeRequests,
                    resolvedVersion = next,
                    originalVersionMissing = true
                )
                is GitLabFetchResult.MilestoneNotFound -> ResolvedGitLabInfo(
                    mergeRequests = emptyList(),
                    resolvedVersion = next,
                    originalVersionMissing = true,
                    fallbackAlsoMissing = true
                )
                is GitLabFetchResult.FetchError -> ResolvedGitLabInfo(
                    mergeRequests = emptyList(),
                    resolvedVersion = version,
                    originalVersionMissing = true,
                    fallbackAlsoMissing = true
                )
            }
        }
        is GitLabFetchResult.FetchError -> {
            println("Warning: GitLab fetch error -- ${primary.message}")
            ResolvedGitLabInfo(mergeRequests = emptyList(), resolvedVersion = version, originalVersionMissing = false)
        }
    }
}

// --- MR parsing & categorisation ---------------------------------------------

/**
 * Parses the GitLab MR list JSON.
 * MRs whose state is "closed" are silently discarded -- they were abandoned without
 * merging and are not relevant to a release notification.
 */
internal fun parseMRsResponse(json: String): List<MergeRequest> {
    return Json.parseToJsonElement(json).jsonArray.mapNotNull { element ->
        val obj = element.jsonObject
        val state = obj["state"]?.jsonPrimitive?.content ?: "opened"

        // Discard closed MRs entirely -- they were abandoned and should not appear in any section
        if (state == "closed") return@mapNotNull null

        val originalTitle = obj["title"]?.jsonPrimitive?.content ?: ""
        val labels = obj["labels"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
        val sourceBranch = obj["source_branch"]?.jsonPrimitive?.content ?: "N/A"
        val isMerged = state == "merged"

        MergeRequest(
            originalTitle = originalTitle,
            cleanedTitle = cleanTitle(originalTitle),
            labels = labels,
            sourceBranch = sourceBranch,
            isMerged = isMerged,
            category = categorise(labels)
        )
    }
}

private fun cleanTitle(original: String): String {
    var t = original.replace(Regex("""\bERA[-_.]?\d+_""", RegexOption.IGNORE_CASE), "")
    t = t.replace(Regex("""\bERA[-_.]?\d+\b\s*[:_]?""", RegexOption.IGNORE_CASE), "")
    return t.trim().replace("_", " ")
}

private fun categorise(labels: List<String>): MRCategory {
    val lower = labels.map { it.lowercase() }
    return when {
        lower.any { it == "bugfix" || it == "crash-fix" } -> MRCategory.BUG_FIX
        lower.any {
            it == "screenshot::test" ||
                it == "enhancement::jenkins" ||
                it == "enchancement::jenkins" // preserve historical typo
        } -> MRCategory.ENHANCEMENT
        else -> MRCategory.FEATURE
    }
}

// --- Teams payload builder ----------------------------------------------------

private fun fact(name: String, value: String) = buildJsonObject {
    put("name", JsonPrimitive(name))
    put("value", JsonPrimitive(value))
}

private fun textSection(text: String): JsonObject = buildJsonObject {
    put("text", JsonPrimitive(text))
    put("markdown", JsonPrimitive(true))
}

private fun formatMR(mr: MergeRequest, useOriginalTitle: Boolean = false): String {
    val title = if (useOriginalTitle) mr.originalTitle else mr.cleanedTitle
    return "- $title"
}

/**
 * Builds the Teams MessageCard payload.
 * - green card on SUCCESS with categorised MR sections
 * - red card on FAILURE with build info only
 */
internal fun buildReleaseTeamsPayload(
    versionName: String,
    milestoneVersion: String,
    buildStatus: String,
    buildNumber: String,
    jobName: String,
    gitLabInfo: ResolvedGitLabInfo?
): String {
    val isSuccess = buildStatus.equals("SUCCESS", ignoreCase = true)
    val themeColor = if (isSuccess) "00C851" else "FF4444"
    val statusIcon = if (isSuccess) "✅" else "❌"
    val statusLabel = if (isSuccess) "succeeded" else "FAILED"
    val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

    val sections = mutableListOf<JsonObject>()

    // Section 1: Build info
    sections += buildJsonObject {
        put("activityTitle", JsonPrimitive("$statusIcon Release $milestoneVersion $statusLabel"))
        put("activitySubtitle", JsonPrimitive("🕐 $time"))
        put(
            "facts",
            buildJsonArray {
                add(fact("Version:", versionName))
                add(fact("Milestone:", milestoneVersion))
                add(fact("Status:", buildStatus))
                add(fact("Job:", jobName))
                add(fact("Build #:", buildNumber))
            }
        )
        put("markdown", JsonPrimitive(true))
    }

    // Sections 2+: MR lists (success only)
    if (isSuccess && gitLabInfo != null) {
        // Warn when the originally requested milestone is absent
        if (gitLabInfo.originalVersionMissing) {
            val warningText = if (gitLabInfo.fallbackAlsoMissing) {
                "⚠️ **Milestone $milestoneVersion does not exist in GitLab** — please create it and " +
                    "assign the relevant MRs.\n\n" +
                    "Fallback milestone **${gitLabInfo.resolvedVersion}** was also not found."
            } else {
                "⚠️ **Milestone $milestoneVersion does not exist in GitLab** — please create it and " +
                    "assign the relevant MRs.\n\n" +
                    "Showing MRs from milestone **${gitLabInfo.resolvedVersion}** as fallback."
            }
            sections += textSection(warningText)
        }

        val mrs = gitLabInfo.mergeRequests
        if (mrs.isEmpty() && !gitLabInfo.fallbackAlsoMissing) {
            sections += textSection("📋 No merge requests found for milestone **${gitLabInfo.resolvedVersion}**.")
        } else if (mrs.isNotEmpty()) {
            val displayVersion = gitLabInfo.resolvedVersion
            val merged = mrs.filter { it.isMerged }
            val unmerged = mrs.filter { !it.isMerged }

            // Enhancements are folded into Features — only Bugs / Features / Unmerged are shown
            val features = merged.filter { it.category == MRCategory.FEATURE || it.category == MRCategory.ENHANCEMENT }
            val bugFixes = merged.filter { it.category == MRCategory.BUG_FIX }

            val sb = StringBuilder()
            sb.appendLine("**📋 Merge Requests — milestone $displayVersion**")

            if (features.isNotEmpty()) {
                sb.appendLine()
                sb.appendLine("**✨ Features (${features.size})**")
                features.forEach { sb.appendLine(formatMR(it)) }
            }
            if (bugFixes.isNotEmpty()) {
                sb.appendLine()
                sb.appendLine("**🐞 Bugs (${bugFixes.size})**")
                bugFixes.forEach { sb.appendLine(formatMR(it)) }
            }
            if (unmerged.isNotEmpty()) {
                sb.appendLine()
                sb.appendLine("**⚠️ Not yet merged (${unmerged.size})**")
                unmerged.forEach { sb.appendLine("- ❗ ${formatMR(it, useOriginalTitle = true).removePrefix("- ")}") }
            }

            sections += textSection(sb.toString().trim())
        }
    }

    val card = buildJsonObject {
        put("@type", JsonPrimitive("MessageCard"))
        put("@context", JsonPrimitive("http://schema.org/extensions"))
        put("themeColor", JsonPrimitive(themeColor))
        put("summary", JsonPrimitive("$statusIcon Release $milestoneVersion $statusLabel"))
        put("sections", JsonArray(sections))
    }

    return card.toString()
}

// --- HTTP send ----------------------------------------------------------------

private fun sendToTeams(webhookUrl: String, payload: String) {
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

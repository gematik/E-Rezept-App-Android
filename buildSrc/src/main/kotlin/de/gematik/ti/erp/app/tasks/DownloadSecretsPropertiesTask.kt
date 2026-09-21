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

import de.gematik.ti.erp.app.utils.API_TOKEN
import de.gematik.ti.erp.app.utils.TaskNames
import de.gematik.ti.erp.app.utils.execute
import de.gematik.ti.erp.app.utils.getToken
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import java.io.ByteArrayOutputStream

/**
 * Downloads (or refreshes) the `secrets.properties` file from the shared GitLab repository.
 *
 * Usage:
 *   ./gradlew downloadSecretsProperties -Ptoken=<gitlab-personal-access-token>
 *
 * - When `secrets.properties` is **present** the build uses its real values.
 * - When it is **absent** every key falls back to an empty string (placeholder), so
 *   the project still compiles; features that require real keys will simply not work at
 *   runtime until the file is populated.
 */
internal fun TaskContainer.downloadSecretsProperties(project: Project) {
    register(TaskNames.downloadSecretsProperties) {
        group = "secrets"
        description = """
            Downloads secrets.properties from the shared GitLab repository and writes it
            to the project root.  Pass the GitLab personal-access token as:
              ./gradlew ${TaskNames.downloadSecretsProperties} -P$API_TOKEN=<token>
        """.trimIndent()

        doLast {
            val token = project.getToken()
                ?: throw GradleException(
                    """
                    Missing token – run the task as:
                      ./gradlew ${TaskNames.downloadSecretsProperties} -P$API_TOKEN=<token>
                    where <token> is your GitLab personal-access token with read_repository scope.
                    """.trimIndent()
                )

            // Resolve SECRETS_RAW_URL: env var (Jenkins) → -P param → ci/local/ci-overrides.properties
            val ciOverridesProps = java.util.Properties().apply {
                val f = project.rootProject.file("ci/local/ci-overrides.properties")
                if (f.exists()) f.reader().use { load(it) }
            }
            val secretsRawUrl = System.getenv("SECRETS_RAW_URL")
                ?: (project.findProperty("SECRETS_RAW_URL") as? String)
                ?: ciOverridesProps.getProperty("SECRETS_RAW_URL")
                ?: throw GradleException(
                    "SECRETS_RAW_URL not found. Set it as a Jenkins env var, " +
                        "-PSECRETS_RAW_URL=<url>, or in ci/local/ci-overrides.properties."
                )

            println("⬇️  Downloading secrets.properties from GitLab…")

            val rawContent = project.downloadRawFileFromGitLab(token, secretsRawUrl)

            if (rawContent.contains("<html", ignoreCase = true) || rawContent.contains("<body", ignoreCase = true)) {
                throw GradleException(
                    "❌ Downloaded HTML instead of secrets.properties. " +
                        "Your token is likely missing access to the shared-data repo."
                )
            }

            // Validate it looks like a properties file (has at least one KEY=VALUE line)
            val hasAtLeastOneEntry = rawContent.lines().any { line ->
                val trimmed = line.trim()
                trimmed.isNotEmpty() && !trimmed.startsWith("#") && propertiesLineRegex.matches(trimmed)
            }
            if (!hasAtLeastOneEntry) {
                throw GradleException(
                    "❌ Downloaded content does not look like a valid .properties file:\n$rawContent"
                )
            }

            val secretsFile = project.rootProject.file("ci/local/secrets.properties")

            // Extract MAPS_API_KEY if present and write to local.properties
            val mapsApiKeyLine = rawContent.lines().find { it.trim().startsWith("MAPS_API_KEY=") }
            if (mapsApiKeyLine != null) {
                val mapsApiKeyValue = mapsApiKeyLine.substringAfter("=").trim()
                val localPropertiesFile = project.rootProject.file("local.properties")

                // Read existing local.properties or create new content
                val existingContent = if (localPropertiesFile.exists()) {
                    localPropertiesFile.readText().split("\n").filter {
                        !it.trim().startsWith("MAPS_API_KEY=")
                    }
                } else {
                    emptyList()
                }

                // Append MAPS_API_KEY
                val updatedContent = (existingContent + "MAPS_API_KEY=$mapsApiKeyValue\n").joinToString("\n")
                localPropertiesFile.writeText(updatedContent)
                println("✅ MAPS_API_KEY written to ${localPropertiesFile.absolutePath}")

                // Remove MAPS_API_KEY from secrets.properties
                val secretsContent = rawContent.lines()
                    .filter { !it.trim().startsWith("MAPS_API_KEY=") }
                    .joinToString("\n")
                secretsFile.writeText(secretsContent)
                println("✅ secrets.properties written to ${secretsFile.absolutePath} (MAPS_API_KEY extracted)")
            } else {
                secretsFile.writeText(rawContent)
                println("✅ secrets.properties written to ${secretsFile.absolutePath}")
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Private helpers
// ---------------------------------------------------------------------------

private val propertiesLineRegex = Regex("""^[A-Za-z0-9_.-]+\s*=.*$""")

private fun Project.downloadRawFileFromGitLab(token: String, url: String): String =
    ByteArrayOutputStream().use { outputStream ->
        exec {
            commandLine(
                "curl",
                "--silent",
                "--show-error",
                "--fail",
                "--header",
                "PRIVATE-TOKEN: $token",
                url
            )
            commandLine.execute()
            standardOutput = outputStream
        }
        outputStream.toString(Charsets.UTF_8.name())
    }

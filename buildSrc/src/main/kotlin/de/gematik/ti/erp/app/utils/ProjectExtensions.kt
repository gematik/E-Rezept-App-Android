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

package de.gematik.ti.erp.app.utils

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import java.util.Properties

internal fun Project.versionCode() = extra[VERSION_CODE_STRING] as? Int

internal fun Project.versionName() = extra[VERSION_NAME_STRING] as? String

internal fun Project.lastCommit() = extra[LAST_MESSAGE_STRING] as? String

internal fun Project.getToken(): String? = findProperty(API_TOKEN) as? String

internal fun Project.detectPropertyOrNull(name: String) = findProperty(name) as? String

internal fun Project.detectPropertyOrThrow(name: String): String {
    val property = findProperty(name) as? String
    return property ?: throw GradleException("Missing argument $name")
}

/**
 * CI-only keys that must NEVER be injected into BuildConfig.
 * They are only used inside Gradle tasks (Nexus repo auth, GitLab API calls, Teams webhooks).
 * On CI these come from Jenkins credentials passed as environment variables.
 * Locally, developers can keep them in a gitignored ci-overrides.properties.
 */
private val CI_ONLY_KEYS = setOf(
    "NEXUS_URL",
    "NEXUS_USERNAME",
    "NEXUS_PASSWORD",
    "GITLAB_PRIVATE_TOKEN",
    "GITLAB_PROJECT_API_URL",
    "TEAMS_RELEASE_WEBHOOK_URL",
    "TEAMS_NIGHTLY_WEBHOOK_URL",
    "TEAMS_MR_WEBHOOK_URL"
)

/**
 * Resolves one of the provided keys with priority: env var -> ci/local/ci-overrides.properties.
 * This is intended for CI/task-only keys where multiple aliases may exist across Jenkins jobs.
 */
internal fun Project.resolveFromEnvOrCiOverrides(vararg keys: String): String? {
    keys.forEach { key ->
        val value = System.getenv(key)
        if (!value.isNullOrBlank()) {
            return value
        }
    }

    keys.forEach { key ->
        val value = project.detectPropertyOrNull(key)
        if (!value.isNullOrBlank()) {
            return value
        }
    }

    val ciOverrides = Properties().apply {
        val file = rootProject.file("ci/local/ci-overrides.properties")
        if (file.exists()) {
            file.reader().use { load(it) }
        }
    }

    keys.forEach { key ->
        val value = ciOverrides.getProperty(key)
        if (!value.isNullOrBlank()) {
            return value
        }
    }
    return null
}

/**
 * Loads ci-overrides.properties from the project root.
 * This file must be listed in .gitignore and is only used as a local-dev fallback.
 * On CI, all values are injected via Jenkins credentials (env vars / -P params).
 */
internal fun Project.loadCiOverridesProperties(): Properties {
    val props = Properties()
    val file = rootProject.file("ci/local/ci-overrides.properties")
    if (file.exists()) {
        file.reader().use { props.load(it) }
    }
    return props
}

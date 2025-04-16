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

internal fun Project.loadCiOverridesProperties(): Properties {
    val props = Properties()
    val file = rootProject.file("ci-overrides.properties")
    if (file.exists()) {
        file.reader().use { props.load(it) }
    }
    return props
}

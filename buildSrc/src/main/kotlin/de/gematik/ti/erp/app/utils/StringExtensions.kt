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

internal fun List<String>.versionCode() = this[VERSION_CODE_PART].trim().toInt()
internal fun List<String>.versionName() = this[VERSION_NAME_PART].trim()
internal fun List<String>.isVersionNameEmpty() = this.versionName().isEmpty()
internal fun List<String>.hasVersionCodeName() = this.size == VERSION_PARTS
internal fun List<String>.doesNotHaveVersionCodeName() = !hasVersionCodeName()

internal fun (MutableList<String>?).execute() = this?.joinToString(" ")

internal fun String.splitForVersionParts() = this.split(":")

// The script expects the string split in the end to execute the gradle task
internal fun String.execute() = split(" ")

internal fun String.isValidVersionCode(): Boolean {
    // Checks if the version follows 1.10.2-RC1, the R is removed in the shell script
    val pattern = Regex("\\d+\\.\\d+\\.\\d+-RC\\d+(-[a-zA-Z0-9]+)?$")
    return pattern.matches(this)
}

internal fun String.isInvalidVersioningPattern() = !isValidVersionCode()

internal fun String.extractVersion(): String? {
    // Make R1.19.1-RC2 to 1.19.1
    val regex = Regex("\\d+\\.\\d+\\.\\d+")
    val matchResult = regex.find(this)
    return matchResult?.value
}

internal fun String.extractMajorVersion(): String? {
    // Make R1.19.1-RC2 to 1.19
    val regex = Regex("\\d+\\.\\d+")
    val matchResult = regex.find(this)
    return matchResult?.value
}

internal fun String.extractRCVersion(): String {
    // Make R1.19.1-RC2 to 1.19.1-RC2
    // Make 1.19.1 to 1.19.1-RC1
    val regex = Regex("^R(.+)$") // Remove leading "R"
    val version = regex.replace(this, "$1")

    return if (!version.contains("-RC")) {
        "$version-RC1" // if there is no RC given we assume its the first one
    } else {
        version
    }
}

internal fun String.sanitizeForGitLab() = "$this.0"

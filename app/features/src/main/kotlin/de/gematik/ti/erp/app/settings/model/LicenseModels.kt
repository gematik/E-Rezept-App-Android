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

package de.gematik.ti.erp.app.settings.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Immutable
@Serializable
data class LicenseEntry(
    val project: String,
    val description: String? = null,
    val version: String? = null,
    val developers: List<String>,
    val url: String? = null,
    val year: String? = null,
    val licenses: List<License>,
    val dependency: String
)

@Immutable
@Serializable
data class License(
    val license: String,
    @SerialName("license_url")
    val licenseUrl: String
)

fun parseLicenses(json: String): List<LicenseEntry> {
    val list: List<LicenseEntry> = Json.decodeFromString(json)
    return list.sortedBy { it.dependency }
}

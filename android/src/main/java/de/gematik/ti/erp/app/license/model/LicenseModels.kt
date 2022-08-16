/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.license.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
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

fun parseLicenses(json: String): List<LicenseEntry> =
    Json.decodeFromString(json)

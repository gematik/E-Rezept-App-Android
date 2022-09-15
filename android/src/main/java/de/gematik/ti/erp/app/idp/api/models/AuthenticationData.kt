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

package de.gematik.ti.erp.app.idp.api.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Device type. `gemF_Biometrie 4.1.2.2`
 */
@JsonClass(generateAdapter = true)
data class DeviceType(
    @Json(name = "device_type_data_version") val version: String = "1.0",
    @Json(name = "manufacturer") val manufacturer: String,
    @Json(name = "product") val productName: String,
    @Json(name = "model") val model: String,
    @Json(name = "os") val operatingSystem: String,
    @Json(name = "os_version") val operatingSystemVersion: String,
)

/**
 * Device information. `gemF_Biometrie 4.1.2.3`
 */
@JsonClass(generateAdapter = true)
data class DeviceInformation(
    @Json(name = "device_information_data_version") val version: String = "1.0",
    @Json(name = "name") val name: String, // android device name set by user
    @Json(name = "device_type") val deviceType: DeviceType,
)

/**
 * Pairing data. `gemF_Biometrie 4.1.2.4`
 */
@JsonClass(generateAdapter = true)
data class PairingData(
    @Json(name = "pairing_data_version") val version: String = "1.0",

    @Json(name = "se_subject_public_key_info") val subjectPublicKeyInfoOfSecureElement: String,
    @Json(name = "key_identifier") val keyAliasOfSecureElement: String, // alias of the keystore entry
    @Json(name = "product") val productName: String,

    @Json(name = "serialnumber") val serialNumberOfHealthCard: String,
    @Json(name = "issuer") val issuerOfHealthCard: String,
    @Json(name = "not_after") val validityUntilOfHealthCard: Long,

    @Json(name = "auth_cert_subject_public_key_info") val subjectPublicKeyInfoOfHealthCard: String,
)

/**
 * Registration data. `gemF_Biometrie 4.1.2.6`
 */
@JsonClass(generateAdapter = true)
data class RegistrationData(
    @Json(name = "registration_data_version") val version: String = "1.0",
    @Json(name = "signed_pairing_data") val signedPairingData: String,
    @Json(name = "auth_cert") val healthCardCertificate: String,
    @Json(name = "device_information") val deviceInformation: DeviceInformation,
)

/**
 * Authentication data. `gemF_Biometrie 4.1.2.8`
 */
@JsonClass(generateAdapter = true)
data class AuthenticationData(
    @Json(name = "authentication_data_version") val version: String = "1.0",
    @Json(name = "challenge_token") val challenge: String,
    @Json(name = "auth_cert") val healthCardCertificate: String,
    @Json(name = "key_identifier") val keyAliasOfSecureElement: String, // alias of the keystore entry
    @Json(name = "device_information") val deviceInformation: DeviceInformation,
    @Json(name = "amr") val authenticationMethod: List<String>,
)

/**
 * Pairing entry. `gemF_Biometrie 4.1.2.11`
 */
@JsonClass(generateAdapter = true)
data class PairingResponseEntry(
    @Json(name = "pairing_entry_data_version") val version: String = "1.0",
    @Json(name = "name") val name: String, // android device name set by user
    @Json(name = "creation_time") val authCert: Long,
    @Json(name = "signed_pairing_data") val signedPairingData: String,
)

/**
 * Pairing entries. `gemF_Biometrie 4.1.2.12`
 */
@JsonClass(generateAdapter = true)
data class PairingResponseEntries(
    @Json(name = "pairing_entries") val entries: List<PairingResponseEntry>,
)

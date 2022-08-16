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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Device type. `gemF_Biometrie 4.1.2.2`
 */
@Serializable
data class DeviceType(
    @SerialName("device_type_data_version") val version: String = "1.0",
    @SerialName("manufacturer") val manufacturer: String,
    @SerialName("product") val productName: String,
    @SerialName("model") val model: String,
    @SerialName("os") val operatingSystem: String,
    @SerialName("os_version") val operatingSystemVersion: String
)

/**
 * Device information. `gemF_Biometrie 4.1.2.3`
 */
@Serializable
data class DeviceInformation(
    @SerialName("device_information_data_version") val version: String = "1.0",
    @SerialName("name") val name: String, // android device name set by user
    @SerialName("device_type") val deviceType: DeviceType
)

/**
 * Pairing data. `gemF_Biometrie 4.1.2.4`
 */
@Serializable
data class PairingData(
    @SerialName("pairing_data_version") val version: String = "1.0",

    @SerialName("se_subject_public_key_info") val subjectPublicKeyInfoOfSecureElement: String,
    @SerialName("key_identifier") val keyAliasOfSecureElement: String, // alias of the keystore entry
    @SerialName("product") val productName: String,

    @SerialName("serialnumber") val serialNumberOfHealthCard: String,
    @SerialName("issuer") val issuerOfHealthCard: String,
    @SerialName("not_after") val validityUntilOfHealthCard: Long,

    @SerialName("auth_cert_subject_public_key_info") val subjectPublicKeyInfoOfHealthCard: String
)

/**
 * Registration data. `gemF_Biometrie 4.1.2.6`
 */
@Serializable
data class RegistrationData(
    @SerialName("registration_data_version") val version: String = "1.0",
    @SerialName("signed_pairing_data") val signedPairingData: String,
    @SerialName("auth_cert") val healthCardCertificate: String,
    @SerialName("device_information") val deviceInformation: DeviceInformation
)

/**
 * Authentication data. `gemF_Biometrie 4.1.2.8`
 */
@Serializable
data class AuthenticationData(
    @SerialName("authentication_data_version") val version: String = "1.0",
    @SerialName("challenge_token") val challenge: String,
    @SerialName("auth_cert") val healthCardCertificate: String,
    @SerialName("key_identifier") val keyAliasOfSecureElement: String, // alias of the keystore entry
    @SerialName("device_information") val deviceInformation: DeviceInformation,
    @SerialName("amr") val authenticationMethod: List<String>
)

/**
 * Pairing entry. `gemF_Biometrie 4.1.2.11`
 */
@Serializable
data class PairingResponseEntry(
    @SerialName("pairing_entry_data_version") val version: String = "1.0",
    @SerialName("name") val name: String, // android device name set by user
    @SerialName("creation_time") val creationTime: Long,
    @SerialName("signed_pairing_data") val signedPairingData: String
)

/**
 * Pairing entries. `gemF_Biometrie 4.1.2.12`
 */
@Serializable
data class PairingResponseEntries(
    @SerialName("pairing_entries") val entries: List<PairingResponseEntry>
)

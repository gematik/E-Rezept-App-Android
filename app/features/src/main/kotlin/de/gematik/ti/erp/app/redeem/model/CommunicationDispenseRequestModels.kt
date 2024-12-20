/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.redeem.model
import kotlinx.serialization.Serializable

internal const val COMMUNICATION_PROFILE_1_2 = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DispReq|1.2"
internal const val ORDER_ID_IDENTIFIER = "https://gematik.de/fhir/NamingSystem/OrderID"
internal const val RECIPIENT_IDENTIFIER = "https://gematik.de/fhir/sid/telematik-id"

/**
 * The serialized object that is sent as a json to the Fachdienst for the pharmacy to process that an order was placed
 * for a prescription/s and it needs to be processed
 */
@Serializable
internal data class Communication(
    val resourceType: String = "Communication",
    val meta: Meta,
    val identifier: List<Identifier>,
    val status: String = "unknown",
    val basedOn: List<Reference>,
    val recipient: List<Recipient>,
    val payload: List<Payload>
)

@Serializable
internal data class Meta(
    val profile: List<String>
)

@Serializable
internal data class Identifier(
    val system: String,
    val value: String
)

@Serializable
internal data class Reference(
    val reference: String
)

@Serializable
internal data class Recipient(
    val identifier: RecipientIdentifier
)

@Serializable
internal data class RecipientIdentifier(
    val system: String,
    val value: String
)

@Serializable
internal data class Payload(
    val contentString: String
)

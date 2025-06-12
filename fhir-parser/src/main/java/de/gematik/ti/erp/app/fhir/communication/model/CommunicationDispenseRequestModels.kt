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

package de.gematik.ti.erp.app.fhir.communication.model

import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import kotlinx.serialization.Serializable

/**
 * The serialized object that is sent as a json to the Fachdienst for the pharmacy to process that an order was placed
 * for a prescription/s and it needs to be processed
 */
@Serializable
internal data class CommunicationDispenseRequest(
    val resourceType: String = "Communication",
    val meta: FhirMeta,
    val identifier: List<FhirIdentifier>,
    val status: String = "unknown",
    val extension: List<CommunicationValueCodingExtension>? = null,
    val basedOn: List<CommunicationReference>,
    val recipient: List<CommunicationRecipient>,
    val payload: List<PayloadForCommunication>
)

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
import kotlinx.serialization.Serializable

@Serializable
internal data class CommunicationValueCodingExtension(val url: String, val valueCoding: CommunicationValueCoding)

@Serializable
internal data class CommunicationValueCoding(val system: String, val code: String, val display: String)

@Serializable
internal data class CommunicationReference(val reference: String)

@Serializable
internal data class CommunicationRecipient(val identifier: FhirIdentifier)

@Serializable
internal data class PayloadForCommunication(val contentString: String)

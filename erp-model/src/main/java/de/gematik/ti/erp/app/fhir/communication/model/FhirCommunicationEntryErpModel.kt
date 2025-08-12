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

package de.gematik.ti.erp.app.fhir.communication.model

import de.gematik.ti.erp.app.fhir.communication.model.support.CommunicationParticipantErpModel
import de.gematik.ti.erp.app.fhir.communication.model.support.DispenseCommunicationPayloadContentErpModel
import de.gematik.ti.erp.app.fhir.communication.model.support.DispensePrescriptionTypeErpModel
import de.gematik.ti.erp.app.fhir.communication.model.support.ReplyCommunicationPayloadContentErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import kotlinx.serialization.Serializable

// common communication properties
@Serializable
sealed class FhirCommunicationEntryErpModel {
    abstract val id: String
    abstract val profile: String
    abstract val taskId: String?
    abstract val sender: CommunicationParticipantErpModel?
    abstract val recipient: CommunicationParticipantErpModel?
    abstract val orderId: String?
    abstract val sent: FhirTemporal.Instant?
    abstract val isDiga: Boolean
}

// Reply specific communication model
@Serializable
data class FhirReplyCommunicationEntryErpModel(
    override val id: String,
    override val profile: String,
    override val taskId: String?,
    override val sender: CommunicationParticipantErpModel?,
    override val recipient: CommunicationParticipantErpModel?,
    override val orderId: String? = null,
    override val sent: FhirTemporal.Instant?,
    val received: FhirTemporal.Instant?,
    val payload: ReplyCommunicationPayloadContentErpModel,
    override val isDiga: Boolean = false
) : FhirCommunicationEntryErpModel()

// Dispense specific communication model
@Serializable
data class FhirDispenseCommunicationEntryErpModel(
    override val id: String,
    override val profile: String,
    override val taskId: String?,
    override val sender: CommunicationParticipantErpModel?,
    override val recipient: CommunicationParticipantErpModel?,
    override val orderId: String? = null,
    override val sent: FhirTemporal.Instant?,
    val payload: DispenseCommunicationPayloadContentErpModel,
    val prescriptionType: DispensePrescriptionTypeErpModel? = null,
    override val isDiga: Boolean = false
) : FhirCommunicationEntryErpModel()

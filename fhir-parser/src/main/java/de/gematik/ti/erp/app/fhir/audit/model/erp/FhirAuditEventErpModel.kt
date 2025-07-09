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

package de.gematik.ti.erp.app.fhir.audit.model.erp

import de.gematik.ti.erp.app.fhir.audit.model.original.FhirAgent
import de.gematik.ti.erp.app.fhir.audit.model.original.FhirAuditEventEntity
import de.gematik.ti.erp.app.fhir.audit.model.original.FhirAuditEventModel
import de.gematik.ti.erp.app.fhir.constant.audit.FhirAuditEventsConstants.AUDIT_EVENT_TELEMATIK_ID_IDENTIFIER
import de.gematik.ti.erp.app.fhir.constant.audit.FhirAuditEventsConstants.AUDIT_EVENT_VERSION_1_1_KVNR_IDENTIFIER
import de.gematik.ti.erp.app.fhir.constant.audit.FhirAuditEventsConstants.AUDIT_EVENT_VERSION_1_1_PRESCRIPTION_IDENTIFIER
import de.gematik.ti.erp.app.fhir.constant.audit.FhirAuditEventsConstants.AUDIT_EVENT_VERSION_1_2_KVNR_IDENTIFIER
import de.gematik.ti.erp.app.fhir.constant.audit.FhirAuditEventsConstants.AUDIT_EVENT_VERSION_1_2_PRESCRIPTION_IDENTIFIER
import de.gematik.ti.erp.app.fhir.constant.audit.FhirAuditEventsConstants.sanitizeAsAuditEventDescription
import de.gematik.ti.erp.app.fhir.error.fhirError
import de.gematik.ti.erp.app.utils.FhirTemporal
import de.gematik.ti.erp.app.utils.ParserUtil.asFhirInstant
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class FhirAuditEventErpModel(
    val uuid: String = UUID.randomUUID().toString(),
    val id: String,
    val taskId: String?,
    val description: String,
    val telematikId: String?,
    val kvnrNumber: String?,
    val timestamp: FhirTemporal.Instant
) {
    companion object {

        private fun List<FhirAuditEventEntity>.getWhatValue(systemUrl: String) =
            firstOrNull { it.what?.identifier?.system == systemUrl }
                ?.what?.identifier?.value

        private fun List<FhirAgent>.getWhoValue(systemUrl: String) =
            firstOrNull { it.who?.identifier?.system == systemUrl }
                ?.who?.identifier?.value

        private fun List<FhirAuditEventEntity>.taskId() =
            getWhatValue(AUDIT_EVENT_VERSION_1_1_PRESCRIPTION_IDENTIFIER) ?: getWhatValue(AUDIT_EVENT_VERSION_1_2_PRESCRIPTION_IDENTIFIER)

        private fun List<FhirAgent>.telematikId() = getWhoValue(AUDIT_EVENT_TELEMATIK_ID_IDENTIFIER)

        private fun List<FhirAgent>.kvnr() = getWhoValue(AUDIT_EVENT_VERSION_1_1_KVNR_IDENTIFIER) ?: getWhoValue(AUDIT_EVENT_VERSION_1_2_KVNR_IDENTIFIER)

        internal fun FhirAuditEventModel.toErpModel() = FhirAuditEventErpModel(
            id = id ?: "",
            taskId = entity.taskId(),
            description = text?.div?.sanitizeAsAuditEventDescription() ?: "",
            telematikId = agent.telematikId(),
            kvnrNumber = agent.kvnr(),
            timestamp = recorded?.asFhirInstant() ?: fhirError("Missing recorded timestamp")
        )
    }
}

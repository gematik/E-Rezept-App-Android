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

import de.gematik.ti.erp.app.fhir.audit.model.FhirAuditEventErpModel
import de.gematik.ti.erp.app.fhir.audit.model.original.FhirAgent
import de.gematik.ti.erp.app.fhir.audit.model.original.FhirAuditEventEntity
import de.gematik.ti.erp.app.fhir.audit.model.original.FhirAuditEventModel
import de.gematik.ti.erp.app.fhir.constant.audit.FhirAuditEventsConstants
import de.gematik.ti.erp.app.fhir.constant.audit.FhirAuditEventsConstants.sanitizeAsAuditEventDescription
import de.gematik.ti.erp.app.fhir.error.fhirError
import de.gematik.ti.erp.app.utils.ParserUtil.asFhirInstant

private fun List<FhirAuditEventEntity>.getWhatValue(systemUrl: String) =
    firstOrNull { it.what?.identifier?.system == systemUrl }
        ?.what?.identifier?.value

private fun List<FhirAgent>.getWhoValue(systemUrl: String) =
    firstOrNull { it.who?.identifier?.system == systemUrl }
        ?.who?.identifier?.value

@Suppress("ktlint:standard:max-line-length", "MaxLineLength")
private fun List<FhirAuditEventEntity>.taskId() =
    getWhatValue(FhirAuditEventsConstants.PrescriptionIdentifiers.VERSION_1_1) ?: getWhatValue(FhirAuditEventsConstants.PrescriptionIdentifiers.VERSION_1_2)

private fun List<FhirAgent>.telematikId() = getWhoValue(FhirAuditEventsConstants.TELEMATIK_ID)

@Suppress("ktlint:standard:max-line-length", "MaxLineLength")
private fun List<FhirAgent>.kvnr() =
    getWhoValue(FhirAuditEventsConstants.PatientIdentifiers.VERSION_1_1) ?: getWhoValue(FhirAuditEventsConstants.PatientIdentifiers.VERSION_1_2)

@Suppress("ktlint:standard:max-line-length", "MaxLineLength")
internal fun FhirAuditEventModel.toErpModel() = FhirAuditEventErpModel(
    id = id ?: "",
    taskId = entity.taskId(),
    description = text?.div?.sanitizeAsAuditEventDescription() ?: "",
    telematikId = agent.telematikId(),
    kvnrNumber = agent.kvnr(),
    timestamp = recorded?.asFhirInstant() ?: fhirError("Missing recorded timestamp")
)

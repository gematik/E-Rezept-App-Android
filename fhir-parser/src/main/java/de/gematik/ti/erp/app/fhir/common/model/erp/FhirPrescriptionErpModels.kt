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

package de.gematik.ti.erp.app.fhir.common.model.erp

import de.gematik.ti.erp.app.fhir.common.model.erp.support.FhirTaskEntryDataErpModel
import de.gematik.ti.erp.app.fhir.model.TaskStatus
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirCoverageErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvMedicationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvMedicationRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvPatientErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvPractitionerErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskMetaDataPayloadErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskOrganizationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FirTaskKbvPayloadErpModel
import de.gematik.ti.erp.app.utils.FhirTemporal
import kotlinx.serialization.Serializable

// Interface for all Task-related internal models
@Serializable
sealed interface FhirTaskErpModel : FhirErpModel

@Serializable
data class FhirTaskEntryParserResultErpModel(
    val bundleTotal: Int,
    val taskEntries: List<FhirTaskEntryDataErpModel>,
    val firstPageUrl: String? = null,
    val previousPageUrl: String? = null,
    val selfPageUrl: String? = null,
    val nextPageUrl: String? = null
) : FhirTaskErpModel

// Represents the FHIR Task + KBV bundle wrapper
@Serializable
data class FhirTaskPayloadErpModel(
    val taskBundle: FhirTaskMetaDataPayloadErpModel,
    val kbvBundle: FirTaskKbvPayloadErpModel
) : FhirTaskErpModel

// Represents detailed task metadata
@Serializable
data class FhirTaskMetaDataErpModel(
    val taskId: String,
    val accessCode: String,
    val lastModified: FhirTemporal.Instant,
    val expiresOn: FhirTemporal.LocalDate? = null,
    val acceptUntil: FhirTemporal.LocalDate? = null,
    val authoredOn: FhirTemporal.Instant,
    val status: TaskStatus, // todo, using the TaskStatus from the fhir model, need to move it
    val lastMedicationDispense: FhirTemporal.Instant? = null
) : FhirTaskErpModel

@Serializable
data class FhirTaskDataErpModel(
    val pvsId: String?,
    val medicationRequest: FhirTaskKbvMedicationRequestErpModel?, // not present for device request
    val medication: FhirTaskKbvMedicationErpModel?, // not present for device request
    val patient: FhirTaskKbvPatientErpModel?,
    val practitioner: FhirTaskKbvPractitionerErpModel?,
    val organization: FhirTaskOrganizationErpModel?,
    val coverage: FhirCoverageErpModel?,
    val deviceRequest: FhirTaskKbvDeviceRequestErpModel? // present only for device request
) : FhirTaskErpModel {
    fun getMissingProperties(): List<String> {
        return listOfNotNull(
            if (pvsId == null) "pvsId" else null,
            if (medication == null) "medication" else null,
            if (patient == null) "patient" else null,
            if (practitioner == null) "practitioner" else null,
            if (organization == null) "organization" else null,
            if (coverage == null) "coverage" else null,
            if (medicationRequest == null && deviceRequest == null) "request" else null
        )
    }
}

/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.prescription.usecase.model

import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.fhir.InsuranceCompanyDetail
import de.gematik.ti.erp.app.fhir.MedicationDetail
import de.gematik.ti.erp.app.fhir.MedicationRequestDetail
import de.gematik.ti.erp.app.fhir.OrganizationDetail
import de.gematik.ti.erp.app.fhir.PatientDetail
import de.gematik.ti.erp.app.fhir.PractitionerDetail
import java.time.LocalDate
import java.time.LocalDateTime

object PrescriptionUseCaseData {
    @Stable
    data class Prescription(
        val taskId: String,
        val name: String?,
        val organization: String,
        val authoredOn: LocalDateTime,
        val expiresOn: LocalDate,
        val acceptUntil: LocalDate,
        val redeemedOn: LocalDate?
    )

    @Stable
    data class MedicationDispense(
        val medication: MedicationDetail,
        val dosageInstruction: String?
    )

    @Stable
    data class PrescriptionDetails(
        val prescription: Prescription,
        val patient: PatientDetail,
        val practitioner: PractitionerDetail,
        val medication: MedicationDetail,
        val medicationDispenses: List<MedicationDispense>,
        val insurance: InsuranceCompanyDetail,
        val organization: OrganizationDetail,
        val medicationRequest: MedicationRequestDetail
    ) {
        val isDispensed = medicationDispenses.isNotEmpty()

        // TODO account for multiple dispenses
        val isSubstituted =
            isDispensed && medication.uniqueIdentifier != medicationDispenses.first().medication.uniqueIdentifier

        // how to apply/consume the medication
        fun dosageInstruction(): String? =
            if (isSubstituted) {
                // TODO see above
                medicationDispenses.first().dosageInstruction
            } else {
                medicationRequest.dosageInstruction
            }

        // what kind of dosage; like pills, creme, ...
        fun medicationType(): String? =
            if (isSubstituted) {
                // TODO see above
                medicationDispenses.first().medication.dosageCode
            } else {
                medication.dosageCode
            }

        // PZN
        fun uniqueIdentifier(): String? =
            if (isSubstituted) {
                // TODO see above
                medicationDispenses.first().medication.uniqueIdentifier
            } else {
                medication.uniqueIdentifier
            }
    }

    @Stable
    data class PrescriptionAudit(
        val text: String?,
        val timestamp: LocalDateTime
    )
}

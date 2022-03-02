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

package de.gematik.ti.erp.app.prescription.usecase

import de.gematik.ti.erp.app.fhir.extractInsurance
import de.gematik.ti.erp.app.fhir.extractMedication
import de.gematik.ti.erp.app.fhir.extractMedicationRequest
import de.gematik.ti.erp.app.fhir.extractOrganization
import de.gematik.ti.erp.app.fhir.extractPatient
import de.gematik.ti.erp.app.fhir.extractPractitioner
import de.gematik.ti.erp.app.prescription.repository.model.SimpleMedicationDispense
import de.gematik.ti.erp.app.prescription.repository.model.SimpleTask
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import java.time.LocalDate

class PrescriptionMapper {
    fun mapSimpleTask(task: SimpleTask, redeemedOn: LocalDate?) =
        PrescriptionUseCaseData.Prescription(
            taskId = task.taskId,
            name = task.medicationText,
            expiresOn = task.expiresOn,
            acceptUntil = task.acceptUntil,
            organization = task.organization,
            authoredOn = task.authoredOn,
            redeemedOn = redeemedOn,
        )

    fun mapSimpleTaskDetailed(task: SimpleTask, dispenses: List<SimpleMedicationDispense>) =
        PrescriptionUseCaseData.PrescriptionDetails(
            prescription = mapSimpleTask(task, dispenses.firstOrNull()?.whenHandedOver?.toLocalDate()),
            patient = requireNotNull(task.rawKBVBundle.extractPatient()),
            practitioner = requireNotNull(task.rawKBVBundle.extractPractitioner()),
            medication = requireNotNull(task.rawKBVBundle.extractMedication()),
            medicationDispenses = dispenses.map { mapSimpleMedicationDispense(it) },
            insurance = requireNotNull(task.rawKBVBundle.extractInsurance()),
            organization = requireNotNull(task.rawKBVBundle.extractOrganization()),
            medicationRequest = requireNotNull(task.rawKBVBundle.extractMedicationRequest()),
        )

    fun mapSimpleMedicationDispense(dispense: SimpleMedicationDispense) =
        PrescriptionUseCaseData.MedicationDispense(
            medication = dispense.medicationDetail,
            dosageInstruction = dispense.dosageInstruction
        )
}

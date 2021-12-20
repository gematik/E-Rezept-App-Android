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
            accessCode = task.accessCode,
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

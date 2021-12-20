package de.gematik.ti.erp.app.prescription.repository.model

import de.gematik.ti.erp.app.fhir.MedicationDetail
import java.time.LocalDateTime

data class SimpleMedicationDispense(
    val id: String,
    val taskId: String,
    val patientIdentifier: String, // KVNR
    val wasSubstituted: Boolean,
    val dosageInstruction: String?,
    val performer: String, // Telematik-ID
    val whenHandedOver: LocalDateTime,
    val medicationDetail: MedicationDetail
)

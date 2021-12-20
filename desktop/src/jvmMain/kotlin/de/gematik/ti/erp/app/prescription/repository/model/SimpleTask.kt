package de.gematik.ti.erp.app.prescription.repository.model

import org.hl7.fhir.r4.model.Bundle as FhirBundle
import java.time.LocalDate
import java.time.LocalDateTime

data class SimpleTask(
    val taskId: String,
    val accessCode: String,
    val lastModified: LocalDateTime? = null,

    val organization: String, // an organization can contain multiple authors
    val medicationText: String,
    val expiresOn: LocalDate,
    val acceptUntil: LocalDate,
    val authoredOn: LocalDateTime,

    // synced only
    val status: String? = null,

    val rawKBVBundle: FhirBundle
)

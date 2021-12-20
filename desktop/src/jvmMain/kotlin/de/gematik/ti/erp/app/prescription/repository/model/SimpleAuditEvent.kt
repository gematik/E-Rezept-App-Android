package de.gematik.ti.erp.app.prescription.repository.model

import java.time.LocalDateTime

data class SimpleAuditEvent(
    val id: String,
    val locale: String,
    val text: String?,
    val timestamp: LocalDateTime,
    val taskId: String
)

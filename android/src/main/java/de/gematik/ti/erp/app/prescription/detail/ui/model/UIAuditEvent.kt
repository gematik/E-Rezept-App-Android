package de.gematik.ti.erp.app.prescription.detail.ui.model

import java.time.LocalDateTime

data class UIAuditEvent(
    val id: String,
    val locale: String,
    val text: String?,
    val timestamp: LocalDateTime,
    val taskId: String
)

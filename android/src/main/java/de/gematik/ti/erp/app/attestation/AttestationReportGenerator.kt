package de.gematik.ti.erp.app.attestation

interface AttestationReportGenerator {
    suspend fun convertToReport(jwt: String, nonce: ByteArray): Attestation.Report
}

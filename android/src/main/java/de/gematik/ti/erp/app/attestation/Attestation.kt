package de.gematik.ti.erp.app.attestation

interface Attestation {

    suspend fun attest(request: Request): Result

    interface Request {
        val nonce: ByteArray
    }

    interface Result

    interface Report
}

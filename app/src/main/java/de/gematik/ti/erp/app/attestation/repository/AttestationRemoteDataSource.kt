package de.gematik.ti.erp.app.attestation.repository

import de.gematik.ti.erp.app.attestation.Attestation
import javax.inject.Inject

class AttestationRemoteDataSource @Inject constructor(
    private val safetynetAttestation: Attestation
) {
    suspend fun fetchAttestationReport(request: Attestation.Request) =
        safetynetAttestation.attest(request)
}

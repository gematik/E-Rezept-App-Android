package de.gematik.ti.erp.app.attestation.repository

import de.gematik.ti.erp.app.attestation.Attestation
import de.gematik.ti.erp.app.db.entities.SafetynetAttestationEntity
import javax.inject.Inject

class SafetynetAttestationRepository @Inject constructor(
    private val localDataSource: AttestationLocalDataSource,
    private val remoteDataSource: AttestationRemoteDataSource
) {
    suspend fun fetchAttestationReportRemote(request: Attestation.Request) =
        remoteDataSource.fetchAttestationReport(request)

    suspend fun persistAttestationReport(attestationEntity: SafetynetAttestationEntity) {
        localDataSource.persistReport(attestationEntity)
    }

    fun fetchAttestationsLocal() =
        localDataSource.fetchAttestations()
}

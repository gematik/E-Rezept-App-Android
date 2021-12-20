package de.gematik.ti.erp.app.attestation.repository

import de.gematik.ti.erp.app.db.AppDatabase
import de.gematik.ti.erp.app.db.entities.SafetynetAttestationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AttestationLocalDataSource @Inject constructor(
    private val db: AppDatabase
) {
    suspend fun persistReport(attestationEntity: SafetynetAttestationEntity) {
        db.attestationDao().insertAttestation(attestationEntity)
    }

    fun fetchAttestations(): Flow<List<SafetynetAttestationEntity>> {
        return db.attestationDao().getAllAttestations()
    }
}

package de.gematik.ti.erp.app.attestation.usecase

import com.google.android.gms.common.api.ApiException
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.attestation.Attestation
import de.gematik.ti.erp.app.attestation.AttestationReportGenerator
import de.gematik.ti.erp.app.attestation.SafetynetAttestationRequirements
import de.gematik.ti.erp.app.attestation.SafetynetReport
import de.gematik.ti.erp.app.attestation.SafetynetResult
import de.gematik.ti.erp.app.attestation.repository.SafetynetAttestationRepository
import de.gematik.ti.erp.app.db.entities.SafetynetAttestationEntity
import de.gematik.ti.erp.app.secureRandomInstance
import de.gematik.ti.erp.app.vau.toLowerCaseHex
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class SafetynetUseCase @Inject constructor(
    private val repository: SafetynetAttestationRepository,
    private val attestationReportGenerator: AttestationReportGenerator,
    private val dispatcher: DispatchProvider
) {

    fun runSafetynetAttestation() =
        repository.fetchAttestationsLocal().map {
            withContext(dispatcher.io()) {
                if (it.isEmpty()) {
                    fetchSafetynetResultRemoteAndPersist()
                    true
                } else {
                    val attestationEntity = it[0]
                    val safetynetReport =
                        attestationReportGenerator.convertToReport(
                            attestationEntity.jws,
                            attestationEntity.ourNonce
                        ) as SafetynetReport
                    if (shouldFetchReportRemote(safetynetReport.timestampMS)) {
                        fetchSafetynetResultRemoteAndPersist()
                    }
                    safetynetReport.attestationCheckOK(SafetynetAttestationRequirements())
                    true
                }
            }
        }.catch { exception ->
            Timber.d("exception: ${exception.printStackTrace()}")
            if (exception is ApiException) {
                emit(true)
            } else {
                emit(false)
            }
        }

    private suspend fun fetchSafetynetResultRemoteAndPersist() {
        val salt = provideSalt()
        val nonce = generateNonce(salt, nonceData())
        val request = safetynetRequest(nonce)
        val safetynetResult =
            repository.fetchAttestationReportRemote(request) as SafetynetResult
        repository.persistAttestationReport(
            mapToAttestationEntity(
                safetynetResult,
                nonce
            )
        )
    }

    private fun safetynetRequest(nonce: ByteArray) = object : Attestation.Request {
        override val nonce: ByteArray = nonce
    }

    private fun nonceData() = ("GmtkEPrescriptionApp: " + System.currentTimeMillis()).toByteArray()

    private fun shouldFetchReportRemote(timestamp: Long): Boolean {
        val lastFetched: LocalDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
        val validUntil = lastFetched.plusHours(12)
        return LocalDateTime.now().isAfter(validUntil)
    }

    private fun mapToAttestationEntity(result: SafetynetResult, ourNonce: ByteArray) =
        SafetynetAttestationEntity(
            id = 0,
            jws = result.jws,
            ourNonce = ourNonce
        )

    private fun provideSalt() = ByteArray(32).apply {
        secureRandomInstance().nextBytes(this)
    }

    private fun generateNonce(salt: ByteArray, word: ByteArray): ByteArray {
        val combined = word + salt
        return MessageDigest.getInstance("SHA-256").digest(combined).toLowerCaseHex()
    }
}

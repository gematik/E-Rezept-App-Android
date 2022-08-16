/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.attestation.usecase

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.attestation.Attestation
import de.gematik.ti.erp.app.attestation.AttestationException
import de.gematik.ti.erp.app.attestation.AttestationReportGenerator
import de.gematik.ti.erp.app.attestation.SafetynetAttestationRequirements
import de.gematik.ti.erp.app.attestation.SafetynetReport
import de.gematik.ti.erp.app.attestation.SafetynetResult
import de.gematik.ti.erp.app.attestation.model.AttestationData
import de.gematik.ti.erp.app.attestation.repository.SafetynetAttestationRepository
import de.gematik.ti.erp.app.secureRandomInstance
import de.gematik.ti.erp.app.vau.toLowerCaseHex
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import io.github.aakira.napier.Napier
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class SafetynetUseCase(
    private val repository: SafetynetAttestationRepository,
    private val attestationReportGenerator: AttestationReportGenerator,
    private val dispatchers: DispatchProvider
) {

    fun runSafetynetAttestation() =
        repository.fetchAttestationsLocal().map {
            withContext(dispatchers.IO) {
                if (it == null) {
                    fetchSafetynetResultRemoteAndPersist()
                    true
                } else {
                    val attestationEntity = it
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
            Napier.d("exception: ${exception.message}")
            emit(exception !is AttestationException)
        }

    private suspend fun fetchSafetynetResultRemoteAndPersist() {
        val salt = provideSalt()
        val nonce = generateNonce(salt, nonceData())
        val request = safetynetRequest(nonce)
        val safetynetResult =
            repository.fetchAttestationReportRemote(request) as SafetynetResult
        repository.persistAttestationReport(
            AttestationData.SafetynetAttestation(
                jws = safetynetResult.jws,
                ourNonce = nonce
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

    private fun provideSalt() = ByteArray(32).apply {
        secureRandomInstance().nextBytes(this)
    }

    private fun generateNonce(salt: ByteArray, word: ByteArray): ByteArray {
        val combined = word + salt
        return MessageDigest.getInstance("SHA-256").digest(combined).toLowerCaseHex()
    }
}

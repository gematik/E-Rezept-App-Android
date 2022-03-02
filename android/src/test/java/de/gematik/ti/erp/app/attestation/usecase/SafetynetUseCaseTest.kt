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

import de.gematik.ti.erp.app.attestation.AttestationException
import de.gematik.ti.erp.app.attestation.AttestationReportGenerator
import de.gematik.ti.erp.app.attestation.SafetynetReport
import de.gematik.ti.erp.app.attestation.repository.SafetynetAttestationRepository
import de.gematik.ti.erp.app.messages.listOfAttestationEntities
import de.gematik.ti.erp.app.messages.safetynetResult
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SafetynetUseCaseTest {

    private lateinit var useCase: SafetynetUseCase
    private lateinit var repo: SafetynetAttestationRepository
    private lateinit var reportGenerator: AttestationReportGenerator
    private lateinit var attestationReport: SafetynetReport

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Before
    fun setup() {
        val now = System.currentTimeMillis()
        repo = mockk()
        reportGenerator = mockk()
        attestationReport = mockk()
        every { attestationReport.timestampMS } returns now
        useCase = SafetynetUseCase(repo, reportGenerator, coroutineRule.testDispatchProvider)
    }

    @Test
    fun `test running safetynet attestation - throws AttestationException`() =
        runTest {
            every { repo.fetchAttestationsLocal() } returns flowOf(listOfAttestationEntities())
            coEvery { reportGenerator.convertToReport(any(), any()) } returns attestationReport
            every { attestationReport.attestationCheckOK(any()) } throws AttestationException(
                AttestationException.AttestationExceptionType.ATTESTATION_FAILED,
                message = "fail"
            )
            val result = useCase.runSafetynetAttestation().first()
            assertFalse(result)
        }

    @Test
    fun `test running safetynet attestation - throws Exception when creating report`() =
        runTest {
            every { repo.fetchAttestationsLocal() } returns flow { emit(listOfAttestationEntities()) }
            coEvery { repo.fetchAttestationReportRemote(any()) } returns safetynetResult()

            coEvery {
                reportGenerator.convertToReport(
                    any(),
                    any()
                )
            } throws AttestationException(
                AttestationException.AttestationExceptionType.ATTESTATION_FAILED,
                "generating report failed"
            )
            every { attestationReport.attestationCheckOK(any()) }
            val result = useCase.runSafetynetAttestation().first()
            assertFalse(result)
        }

    @Test
    fun `test running safetynet attestation - throws Exception when fetching safetynet from remote`() {
        runTest {
            every { repo.fetchAttestationsLocal() } returns flow { emit(listOfAttestationEntities()) }
            coEvery { repo.fetchAttestationReportRemote(any()) } throws Exception("failed fetching safetynet")

            coEvery { reportGenerator.convertToReport(any(), any()) } returns attestationReport
            every { attestationReport.attestationCheckOK(any()) } returns Unit
            val result = useCase.runSafetynetAttestation().first()
            assertTrue(result)
        }
    }

    @Test
    fun `test running safetynet attestation - passes`() =
        runTest {
            every { repo.fetchAttestationsLocal() } returns flow { emit(listOfAttestationEntities()) }
            coEvery { reportGenerator.convertToReport(any(), any()) } returns attestationReport
            every { attestationReport.attestationCheckOK(any()) } returns Unit
            val result = useCase.runSafetynetAttestation().first()
            assertTrue(result)
        }
}

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
import kotlinx.coroutines.test.runBlockingTest
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
    fun `test running safetynet attestation - throws AttestationException`() {
        coroutineRule.testDispatcher.runBlockingTest {
            every { repo.fetchAttestationsLocal() } returns flowOf(listOfAttestationEntities())
            coEvery { reportGenerator.convertToReport(any(), any()) } returns attestationReport
            every { attestationReport.attestationCheckOK(any()) } throws AttestationException(
                AttestationException.AttestationExceptionType.ATTESTATION_FAILED,
                message = "fail"
            )
            val result = useCase.runSafetynetAttestation().first()
            assertFalse(result)
        }
    }

    @Test
    fun `test running safetynet attestation - throws Exception when creating report`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repo.fetchAttestationsLocal() } returns flow { emit(listOfAttestationEntities()) }
            coEvery { repo.fetchAttestationReportRemote(any()) } returns safetynetResult()

            coEvery {
                reportGenerator.convertToReport(
                    any(),
                    any()
                )
            } throws Exception("generating report failed")
            every { attestationReport.attestationCheckOK(any()) }
            val result = useCase.runSafetynetAttestation().first()
            assertFalse(result)
        }

    @Test
    fun `test running safetynet attestation - throws Exception when fetching safetynet from remote`() {
        coroutineRule.testDispatcher.runBlockingTest {
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
        coroutineRule.testDispatcher.runBlockingTest {
            every { repo.fetchAttestationsLocal() } returns flow { emit(listOfAttestationEntities()) }
            coEvery { reportGenerator.convertToReport(any(), any()) } returns attestationReport
            every { attestationReport.attestationCheckOK(any()) } returns Unit
            val result = useCase.runSafetynetAttestation().first()
            assertTrue(result)
        }
}

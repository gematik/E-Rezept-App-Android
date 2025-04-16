/*
 * Copyright 2025, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.idp.usecase

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.vau.usecase.TruststoreUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.hours

class IdpBasicUseCaseTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @MockK
    private lateinit var idpRepository: IdpRepository

    @MockK
    private lateinit var truststoreUseCase: TruststoreUseCase

    private lateinit var useCase: IdpBasicUseCase

    private val now = Clock.System.now()
    private val idpConfigNow = IdpData.IdpConfiguration(
        authorizationEndpoint = "",
        ssoEndpoint = "",
        tokenEndpoint = "",
        pairingEndpoint = "",
        authenticationEndpoint = "",
        pukIdpEncEndpoint = "",
        pukIdpSigEndpoint = "",
        certificate = mockk(),
        expirationTimestamp = now + 24.hours,
        issueTimestamp = now,
        externalAuthorizationIDsEndpoint = "",
        federationAuthorizationIDsEndpoint = "",
        thirdPartyAuthorizationEndpoint = "",
        federationAuthorizationEndpoint = ""
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        useCase = spyk(
            IdpBasicUseCase(
                repository = idpRepository,
                truststoreUseCase = truststoreUseCase
            )
        )

        coEvery { truststoreUseCase.checkIdpCertificate(any(), any()) } coAnswers {}
    }

    @Requirement(
        "A_20512#3",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Testing the implementation.",
        codeLines = 6
    )
    @Test
    fun `checkIdpConfigurationValidity - valid document`() = runTest {
        useCase.checkIdpConfigurationValidity(
            idpConfigNow,
            now
        )
    }

    @Requirement(
        "A_20512#4",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Testing the implementation.",
        codeLines = 8
    )
    @Test(expected = Exception::class)
    fun `checkIdpConfigurationValidity - document expired - throws exception`() = runTest {
        useCase.checkIdpConfigurationValidity(
            idpConfigNow,
            now + 25.hours // account for clock skew
        )
    }

    @Test(expected = Exception::class)
    fun `checkIdpConfigurationValidity - document expires too late - throws exception`() = runTest {
        useCase.checkIdpConfigurationValidity(
            idpConfigNow.copy(
                expirationTimestamp = now + 25.hours
            ),
            now
        )
    }

    @Test
    fun `generateCodeVerifier - must be min 43 and max 128 characters of length`() {
        assertTrue(useCase.generateCodeVerifier().length in 43..128)
    }

    @Test(expected = Exception::class)
    fun `initializeConfigurationAndKeys - invalid idp config causes exception`() = runTest {
        coEvery { idpRepository.loadUncheckedIdpConfiguration() } returns idpConfigNow
        coEvery { idpRepository.invalidateConfig() } coAnswers {}
        coEvery { useCase.checkIdpConfigurationValidity(any(), any()) } coAnswers { error("") }

        try {
            useCase.initializeConfigurationAndKeys()
        } finally {
            coVerifyOrder {
                idpRepository.loadUncheckedIdpConfiguration()
                useCase.checkIdpConfigurationValidity(any(), any())
                idpRepository.invalidateConfig()
                idpRepository.loadUncheckedIdpConfiguration()
                useCase.checkIdpConfigurationValidity(any(), any())
                idpRepository.invalidateConfig()
            }
            coVerify(exactly = 2) { idpRepository.loadUncheckedIdpConfiguration() }
            coVerify(exactly = 2) { idpRepository.invalidateConfig() }
            coVerify(exactly = 2) { useCase.checkIdpConfigurationValidity(any(), any()) }
        }
    }

    @Test
    fun `initializeConfigurationAndKeys - invalid local idp config - reload idp config`() = runTest {
        val expected = Exception()

        coEvery { idpRepository.loadUncheckedIdpConfiguration() } returns idpConfigNow
        coEvery { idpRepository.invalidateConfig() } coAnswers {}
        coEvery { idpRepository.fetchIdpPukEnc(any()) } coAnswers { throw expected }
        coEvery { idpRepository.fetchIdpPukSig(any()) } coAnswers { throw expected }
        coEvery { useCase.checkIdpConfigurationValidity(any(), any()) } coAnswers { error("") } coAndThen { }

        try {
            useCase.initializeConfigurationAndKeys()
        } catch (e: Exception) {
            assertTrue(e == expected)
        } finally {
            coVerifyOrder {
                idpRepository.loadUncheckedIdpConfiguration()
                useCase.checkIdpConfigurationValidity(any(), any())
                idpRepository.invalidateConfig()
                idpRepository.loadUncheckedIdpConfiguration()
                useCase.checkIdpConfigurationValidity(any(), any())
            }
            coVerify(exactly = 2) { idpRepository.loadUncheckedIdpConfiguration() }
            coVerify(exactly = 1) { idpRepository.invalidateConfig() }
            coVerify(exactly = 2) { useCase.checkIdpConfigurationValidity(any(), any()) }
        }
    }

    @Test
    fun `initializeConfigurationAndKeys - valid idp config`() = runTest {
        val expected = Exception()

        coEvery { idpRepository.loadUncheckedIdpConfiguration() } returns idpConfigNow
        coEvery { idpRepository.invalidateConfig() } coAnswers {}
        coEvery { idpRepository.fetchIdpPukEnc(any()) } coAnswers { throw expected }
        coEvery { idpRepository.fetchIdpPukSig(any()) } coAnswers { throw expected }
        coEvery { useCase.checkIdpConfigurationValidity(any(), any()) } coAnswers { }

        try {
            useCase.initializeConfigurationAndKeys()
        } catch (e: Exception) {
            assertTrue(e == expected)
        } finally {
            coVerifyOrder {
                idpRepository.loadUncheckedIdpConfiguration()
                useCase.checkIdpConfigurationValidity(any(), any())
            }
            coVerify(exactly = 1) { idpRepository.loadUncheckedIdpConfiguration() }
            coVerify(exactly = 0) { idpRepository.invalidateConfig() }
            coVerify(exactly = 1) { useCase.checkIdpConfigurationValidity(any(), any()) }
        }
    }
}

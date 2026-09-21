/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.consent.usecases

import de.gematik.ti.erp.app.consent.repository.ConsentLocalDataSource
import de.gematik.ti.erp.app.consent.repository.ConsentRemoteDataSource
import de.gematik.ti.erp.app.consent.repository.ConsentRepository
import de.gematik.ti.erp.app.consent.repository.DefaultConsentRepository
import de.gematik.ti.erp.app.consent.usecase.GrantConsentUseCase
import de.gematik.ti.erp.app.fhir.consent.FhirConsentParser
import de.gematik.ti.erp.app.fhir.consent.model.ConsentCategory
import de.gematik.ti.erp.app.fhir.constant.consent.ConsentConstants
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.debug.repository.ConsentVersionRepository
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class GrantConsentUseCaseTest {

    private val dispatcher = StandardTestDispatcher()

    private val remoteDataSource = mockk<ConsentRemoteDataSource>()

    private val localDataSource = mockk<ConsentLocalDataSource>()

    @MockK(relaxed = true)
    private lateinit var fhirConsentParser: FhirConsentParser

    private lateinit var repository: ConsentRepository

    private val consentVersionRepository: ConsentVersionRepository = mockk()

    private lateinit var useCase: GrantConsentUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { remoteDataSource.grantConsent(any(), any()) } returns Result.success(Unit)
        coEvery { localDataSource.getInsuranceId(any()) } returns "123"
        coEvery { consentVersionRepository.getConsentVersion() } returns ConsentConstants.ErpCharge.DEFAULT

        repository = DefaultConsentRepository(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            parsers = fhirConsentParser
        )

        useCase = GrantConsentUseCase(
            repository = repository,
            consentVersionRepository = consentVersionRepository,
            dispatcher = dispatcher
        )
    }

    @Test
    fun `on consent granted successfully for a profile`() {
        runTest(dispatcher) {
            val result = useCase.invoke(profile, ConsentCategory.PKVCONSENT)
            assertTrue(result.isSuccess)
            coVerify(exactly = 1) {
                repository.grantConsent(profileId, any())
            }
        }
    }

    @Test
    fun `on consent granted failed on granting consent for a profile`() {
        coEvery { remoteDataSource.grantConsent(any(), any()) } returns Result.failure(Throwable("server error"))
        runTest(dispatcher) {
            val result = useCase.invoke(profile, ConsentCategory.PKVCONSENT)
            assertTrue(result.isFailure)
            coVerify(exactly = 1) {
                repository.grantConsent(profileId, any())
            }
        }
    }

    companion object {
        private const val profileId = "7fo98w-43tgv-23w"

        val profile = ProfileErpModel(
            id = "7fo98w-43tgv-23w",
            name = "Test",
            profileImageData = ProfileImageDataErpModel(
                color = ProfileColorNames.PINK,
                image = null,
                avatar = Avatar.PersonalizedImage
            ),
            insuranceData = ProfileInsuranceDataErpModel(
                insurantName = "Erna P",
                insuranceIdentifier = "AOK",
                insuranceName = null,
                insuranceType = InsuranceType.GKV,
                organizationIdentifier = null
            ),
            lastAuthenticated = null,
            userAuthentication = UserAuthenticationErpModel.NotInitialized,
            lastAuditEventSynced = null,
            lastTaskSynced = null,
            active = true,
            isNewlyCreated = false,
            isConsentDrawerShown = true
        )
    }
}

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

import de.gematik.ti.erp.app.consent.model.ConsentState
import de.gematik.ti.erp.app.consent.repository.ConsentLocalDataSource
import de.gematik.ti.erp.app.consent.repository.ConsentRemoteDataSource
import de.gematik.ti.erp.app.consent.repository.ConsentRepository
import de.gematik.ti.erp.app.consent.repository.DefaultConsentRepository
import de.gematik.ti.erp.app.consent.usecase.RevokeConsentUseCase
import de.gematik.ti.erp.app.fhir.consent.FhirConsentParser
import de.gematik.ti.erp.app.fhir.consent.model.ConsentCategory
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class RevokeConsentUseCaseTest {

    private val dispatcher = StandardTestDispatcher()

    private val remoteDataSource = mockk<ConsentRemoteDataSource>(relaxed = true)

    private val localDataSource = mockk<ConsentLocalDataSource>(relaxed = true)

    @MockK(relaxed = true)
    private lateinit var fhirConsentParser: FhirConsentParser

    private lateinit var repository: ConsentRepository

    private lateinit var useCase: RevokeConsentUseCase

    private val testProfileId = "123"

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        repository = DefaultConsentRepository(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            parsers = fhirConsentParser
        )
        useCase = RevokeConsentUseCase(repository, dispatcher)
    }

    @Test
    fun `on consent revoked successfully for a profile`() {
        coEvery {
            remoteDataSource.deleteConsent(any(), any())
        } returns Result.success(Unit)

        runTest(dispatcher) {
            val result = useCase.invoke(testProfileId).first()

            assertEquals(ConsentState.ValidState.Revoked, result)
            coVerify(exactly = 1) {
                remoteDataSource.deleteConsent(testProfileId, ConsentCategory.PKVCONSENT.code)
            }
        }
    }

    @Test
    fun `on consent revoked failed for a profile`() {
        coEvery {
            remoteDataSource.deleteConsent(any(), any())
        } returns Result.failure<Unit>(Throwable("error"))

        runTest(dispatcher) {
            val result = useCase.invoke(testProfileId).first()

            assert(result is ConsentState.ConsentErrorState)
            coVerify(exactly = 1) {
                remoteDataSource.deleteConsent(testProfileId, ConsentCategory.PKVCONSENT.code)
            }
        }
    }
}

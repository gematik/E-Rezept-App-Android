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

import de.gematik.ti.erp.app.consent.model.ConsentContext
import de.gematik.ti.erp.app.consent.model.ConsentState
import de.gematik.ti.erp.app.consent.repository.ConsentLocalDataSource
import de.gematik.ti.erp.app.consent.repository.ConsentRemoteDataSource
import de.gematik.ti.erp.app.consent.repository.ConsentRepository
import de.gematik.ti.erp.app.consent.repository.DefaultConsentRepository
import de.gematik.ti.erp.app.consent.usecase.GrantConsentUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GrantConsentUseCaseTest {

    private val dispatcher = StandardTestDispatcher()

    private val remoteDataSource = mockk<ConsentRemoteDataSource>()

    private val localDataSource = mockk<ConsentLocalDataSource>()

    private lateinit var repository: ConsentRepository

    private lateinit var useCase: GrantConsentUseCase

    @Before
    fun setup() {
        coEvery { remoteDataSource.grantConsent(any(), any()) } returns Result.success(Unit)
        coEvery { localDataSource.getInsuranceId(any()) } returns "123"

        repository = DefaultConsentRepository(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource
        )

        useCase = GrantConsentUseCase(
            repository = repository,
            dispatcher = dispatcher
        )
    }

    @Test
    fun `on consent granted successfully for a profile`() {
        runTest(dispatcher) {
            val result = useCase.invoke(profileId).first()
            assertEquals(ConsentState.ValidState.Granted(ConsentContext.GrantConsent), result)
            coVerify(exactly = 1) {
                repository.grantConsent(profileId, any())
            }
        }
    }

    @Test
    fun `on consent granted failed on granting consent for a profile`() {
        coEvery { remoteDataSource.grantConsent(any(), any()) } returns Result.failure(Throwable("server error"))
        runTest(dispatcher) {
            val result = useCase.invoke(profileId).first()
            assertEquals(ConsentState.ConsentErrorState.Unknown, result)
            coVerify(exactly = 1) {
                repository.grantConsent(profileId, any())
            }
        }
    }

    companion object {
        private const val profileId = "7fo98w-43tgv-23w"
    }
}

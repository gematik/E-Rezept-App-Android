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

package de.gematik.ti.erp.app.consent.usecases

import de.gematik.ti.erp.app.consent.repository.ConsentLocalDataSource
import de.gematik.ti.erp.app.consent.repository.ConsentRemoteDataSource
import de.gematik.ti.erp.app.consent.repository.ConsentRepository
import de.gematik.ti.erp.app.consent.repository.DefaultConsentRepository
import de.gematik.ti.erp.app.consent.usecase.SaveGrantConsentDrawerShownUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SaveGrantConsentDrawerShownUseCaseTest {

    private lateinit var saveGrantConsentDrawerShownUseCase: SaveGrantConsentDrawerShownUseCase

    @MockK(relaxed = true)
    private lateinit var localDataSource: ConsentLocalDataSource

    @MockK
    private lateinit var remoteDataSource: ConsentRemoteDataSource

    private lateinit var consentRepository: ConsentRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { localDataSource.saveGiveConsentDrawerShown("123") } returns Unit
        consentRepository = DefaultConsentRepository(remoteDataSource, localDataSource)
        saveGrantConsentDrawerShownUseCase = SaveGrantConsentDrawerShownUseCase(consentRepository)
    }

    @Test
    fun `save consent drawer shown for profileId`() = runTest {
        saveGrantConsentDrawerShownUseCase("123")
        coVerify(exactly = 1) {
            consentRepository.saveGrantConsentDrawerShown("123")
        }
        coVerify(exactly = 0) {
            consentRepository.saveGrantConsentDrawerShown("0")
        }
    }
}

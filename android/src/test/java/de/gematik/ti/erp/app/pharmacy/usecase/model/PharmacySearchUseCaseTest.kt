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

package de.gematik.ti.erp.app.pharmacy.usecase.model

import com.squareup.moshi.Moshi
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.repository.RemoteRedeemOption
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import de.gematik.ti.erp.app.utils.testTasks
import de.gematik.ti.erp.app.utils.testUIPrescription
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PharmacySearchUseCaseTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var useCase: PharmacySearchUseCase
    private lateinit var repository: PrescriptionRepository
    private lateinit var moshi: Moshi
    private lateinit var profilesUseCase: ProfilesUseCase

    @Before
    fun setUp() {
        repository =
            PrescriptionRepository(coroutineRule.testDispatchProvider, mockk(), mockk(), mockk())
        moshi = Moshi.Builder().build()
        profilesUseCase = mockk()
        useCase = PharmacySearchUseCase(
            mockk(),
            repository,
            mockk(relaxed = true),
            mockk(),
            moshi,
            coroutineRule.testDispatchProvider,
            profilesUseCase
        )
        coEvery {
            repository.redeemPrescription(
                any(),
                any()
            )
        } answers { Result.Success("".toResponseBody()) }
        coEvery { repository.loadTasksForTaskId(any()) } answers { flow { testTasks() } }
        coEvery { profilesUseCase.activeProfileName() } answers { flowOf("tester") }
    }

    @Test
    fun `tests redeemPrescription`() =
        coroutineRule.testDispatcher.runBlockingTest {
            val redeemOption = RemoteRedeemOption.Local
            val uiPrescriptionOrder = testUIPrescription()
            val telematicsId = "foo"
            val result = useCase.redeemPrescription(redeemOption, uiPrescriptionOrder, telematicsId)
            assertTrue(result is Result.Success)
        }
}
